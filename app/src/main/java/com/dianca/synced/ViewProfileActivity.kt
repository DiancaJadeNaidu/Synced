package com.dianca.synced

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dianca.synced.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ViewProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var txtNameAge: TextView
    private lateinit var txtGenderLocation: TextView
    private lateinit var txtBio: TextView
    private lateinit var btnSendMessage: Button
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var db: FirebaseFirestore
    private var userId: String? = null

    private lateinit var localDb: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)

        // Initialize local and cloud databases
        localDb = AppDatabase.getInstance(this)
        db = FirebaseFirestore.getInstance()
        userId = intent.getStringExtra("uid")

        // Bottom navigation
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TopMatchesActivity::class.java))
                R.id.nav_geo -> startActivity(Intent(this, GeoLocationActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }
            true
        }

        // Profile UI
        imgProfile = findViewById(R.id.imgProfile)
        txtNameAge = findViewById(R.id.txtNameAge)
        txtGenderLocation = findViewById(R.id.txtGenderLocation)
        txtBio = findViewById(R.id.txtBio)
        btnSendMessage = findViewById(R.id.btnSendMessage)

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Load user from local DB first, then Firestore
        loadUserProfile(userId!!)
        btnSendMessage.setOnClickListener { sendMessageRequest() }
    }

    private fun calculateAge(birthdayStr: String): Int {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            val birthDate = sdf.parse(birthdayStr) ?: return 0
            val today = Calendar.getInstance()
            val birthCal = Calendar.getInstance().apply { time = birthDate }
            var age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) age--
            age
        } catch (e: Exception) {
            0
        }
    }

    private fun loadUserProfile(uid: String) {
        val userDao = localDb.userDao()

        lifecycleScope.launch {
            // Load cached data first
            val cachedUser = userDao.getUser(uid)
            if (cachedUser != null) displayUser(cachedUser)

            // Fetch latest data if online
            if (isOnline()) {
                db.collection("users").document(uid).get().addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val name = doc.getString("name") ?: "Unknown"
                        val birthday = doc.getString("birthday") ?: ""
                        val age = calculateAge(birthday)
                        val gender = doc.getString("gender") ?: "N/A"
                        val continent = doc.getString("continent") ?: ""
                        val country = doc.getString("country") ?: ""
                        val location = if (continent.isNotEmpty() && country.isNotEmpty()) "$country, $continent" else "N/A"

                        val hobbies = doc.get("hobbies") as? List<String> ?: emptyList()
                        val food = doc.getString("food") ?: ""
                        val movieGenre = doc.getString("movieGenre") ?: ""
                        val color = doc.getString("favoriteColor") ?: ""
                        val petPref = doc.getString("petPreference") ?: ""
                        val outgoingLevel = doc.getLong("outgoingLevel")?.toInt() ?: 0
                        val avatarId = doc.getLong("avatarId")?.toInt() ?: R.drawable.default_avatar_foreground

                        val bioList = mutableListOf<String>()
                        if (food.isNotEmpty()) bioList.add("Loves $food")
                        if (movieGenre.isNotEmpty()) bioList.add("Enjoys $movieGenre movies")
                        if (color.isNotEmpty()) bioList.add("Likes $color color")
                        if (hobbies.isNotEmpty()) bioList.add("Hobbies: ${hobbies.joinToString(", ")}")
                        if (petPref.isNotEmpty()) bioList.add("Pet preference: $petPref")
                        if (outgoingLevel > 0) bioList.add("Outgoing level: $outgoingLevel/10")

                        val bio = bioList.joinToString(" • ")

                        val userEntity = UserEntity(uid, name, age, gender, location, bio, avatarId)

                        lifecycleScope.launch { userDao.insertUser(userEntity) }
                        displayUser(userEntity)
                    }
                }
            } else {
                Toast.makeText(this@ViewProfileActivity, "Offline mode: showing cached profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUser(user: UserEntity) {
        txtNameAge.text = "${user.name}, ${user.age}"
        txtGenderLocation.text = "${user.gender} - ${user.location}"
        txtBio.text = user.bio
        imgProfile.setImageResource(user.avatarId)
    }

    private fun sendMessageRequest() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val receiverId = intent.getStringExtra("uid") ?: return
        val requestId = "${currentUser.uid}_$receiverId"
        val requestDao = localDb.requestDao()

        val request = RequestEntity(
            id = requestId,
            senderId = currentUser.uid,
            receiverId = receiverId,
            status = "pending",
            timestamp = System.currentTimeMillis(),
            synced = false
        )

        lifecycleScope.launch {
            requestDao.insertRequest(request)
            Toast.makeText(this@ViewProfileActivity, "Message queued 📩 (will sync when online)", Toast.LENGTH_SHORT).show()
            if (isOnline()) syncRequests()
        }
    }

    private fun syncRequests() {
        val requestDao = localDb.requestDao()
        lifecycleScope.launch {
            val unsynced = requestDao.getPendingRequests()
            for (req in unsynced) {
                val data = hashMapOf(
                    "id" to req.id,
                    "senderId" to req.senderId,
                    "receiverId" to req.receiverId,
                    "status" to req.status,
                    "timestamp" to com.google.firebase.Timestamp.now()
                )
                db.collection("requests").document(req.id).set(data)
                    .addOnSuccessListener {
                        lifecycleScope.launch {
                            requestDao.updateRequest(req.copy(synced = true))
                        }
                    }
            }
        }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
