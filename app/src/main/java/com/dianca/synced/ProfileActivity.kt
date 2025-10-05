package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var txtNameAge: TextView
    private lateinit var txtIntent: TextView
    private lateinit var txtGender: TextView
    private lateinit var txtLocation: TextView
    private lateinit var txtHobbies: TextView
    private lateinit var btnEditProfile: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TopMatchesActivity::class.java))
                R.id.nav_messages -> startActivity(Intent(this, SyncedFriendsActivity::class.java))
                R.id.nav_profile -> {}
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }
            true
        }

        // Bind views
        imgProfile = findViewById(R.id.zodiacIcon)
        txtNameAge = findViewById(R.id.txtNameAge)
        txtIntent = findViewById(R.id.txtIntent)
        txtGender = findViewById(R.id.txtGender)
        txtLocation = findViewById(R.id.txtLocation)
        txtHobbies = findViewById(R.id.txtHobbies)
        btnEditProfile = findViewById(R.id.btnEditProfile)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadUserProfile()

        btnEditProfile.setOnClickListener {
            val options = arrayOf("Update Intent", "Update Profile Answers")
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Edit Profile")
            builder.setItems(options) { _, which ->
                when (which) {
                    0 -> showIntentUpdateDialog()
                    1 -> {
                        val intent = Intent(this, QuestionnaireActivity::class.java)
                        intent.putExtra("EXTRA_EDIT_MODE", true)
                        startActivity(intent)
                    }
                }
            }
            builder.show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val birthday = doc.getString("birthday") ?: ""
                    val age = calculateAge(birthday)
                    val intent = doc.getString("intent") ?: ""
                    val bio = doc.getString("bio") ?: ""
                    val gender = doc.getString("gender") ?: ""
                    val continent = doc.getString("continent") ?: ""
                    val country = doc.getString("country") ?: ""
                    val location = if (continent.isNotEmpty() && country.isNotEmpty()) "$country, $continent" else ""
                    val hobbies = (doc.get("hobbies") as? List<*>)?.joinToString(", ") ?: ""

                    val imageUrl = doc.getString("profileImageUrl")
                    val avatarId = doc.getLong("avatarId")?.toInt() ?: R.drawable.default_avatar_foreground

                    txtNameAge.text = if (name.isNotEmpty() && age > 0) "$name, $age" else name
                    txtIntent.text = if (intent.isNotEmpty()) "Looking for: $intent" else ""
                    txtGender.text = if (gender.isNotEmpty()) "Gender: $gender" else ""
                    txtLocation.text = if (location.isNotEmpty()) "Location: $location" else ""
                    txtHobbies.text = if (hobbies.isNotEmpty()) "Hobbies: $hobbies" else ""

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).into(imgProfile)
                    } else {
                        imgProfile.setImageResource(avatarId)
                    }

                } else {
                    Toast.makeText(this, "Please complete your profile", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, QuestionnaireActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
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

    private fun showIntentUpdateDialog() {
        val editText = EditText(this)
        editText.hint = "Enter your new intent"

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle("Update Intent")
            .setView(editText)
            .setPositiveButton("Save") { d, _ ->
                val newIntent = editText.text.toString().trim()
                if (newIntent.isNotEmpty()) updateIntent(newIntent)
                d.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateIntent(newIntent: String) {
        val uid = auth.currentUser?.uid ?: return

        // Normalize - first letter uppercase, rest lowercase
        val formattedIntent = newIntent.lowercase().replaceFirstChar { it.uppercase() }

        db.collection("users").document(uid)
            .update("intent", formattedIntent)
            .addOnSuccessListener {
                Toast.makeText(this, "Intent updated!", Toast.LENGTH_SHORT).show()
                txtIntent.text = "Looking for: $formattedIntent"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
