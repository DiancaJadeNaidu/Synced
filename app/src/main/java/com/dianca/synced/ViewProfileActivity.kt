package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_profile)

        // Initialize BottomNavigationView
        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TopMatchesActivity::class.java))
                R.id.nav_messages -> startActivity(Intent(this, SyncedFriendsActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }
            true
        }

        // Initialize profile views
        imgProfile = findViewById(R.id.imgProfile)
        txtNameAge = findViewById(R.id.txtNameAge)
        txtGenderLocation = findViewById(R.id.txtGenderLocation)
        txtBio = findViewById(R.id.txtBio)
        btnSendMessage = findViewById(R.id.btnSendMessage)

        // Firestore
        db = FirebaseFirestore.getInstance()
        userId = intent.getStringExtra("uid")

        if (userId == null) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (!doc.exists()) return@addOnSuccessListener

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

            txtNameAge.text = "$name, $age"
            txtGenderLocation.text = "$gender - $location"

            val bioList = mutableListOf<String>()
            if (food.isNotEmpty()) bioList.add("Loves $food")
            if (movieGenre.isNotEmpty()) bioList.add("Enjoys $movieGenre movies")
            if (color.isNotEmpty()) bioList.add("Likes $color color")
            if (hobbies.isNotEmpty()) bioList.add("Hobbies: ${hobbies.joinToString(", ")}")
            if (petPref.isNotEmpty()) bioList.add("Pet preference: $petPref")
            if (outgoingLevel > 0) bioList.add("Outgoing level: $outgoingLevel/10")

            txtBio.text = bioList.joinToString(" • ")

            imgProfile.setImageResource(avatarId)
        }
    }

    private fun sendMessageRequest() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val receiverId = intent.getStringExtra("uid") ?: return
        val requestId = "${currentUser.uid}_$receiverId"

        val request = hashMapOf(
            "id" to requestId,
            "senderId" to currentUser.uid,
            "receiverId" to receiverId,
            "status" to "pending",
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("requests").document(requestId).set(request)
            .addOnSuccessListener {
                Toast.makeText(this, "Message request sent ✅", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
