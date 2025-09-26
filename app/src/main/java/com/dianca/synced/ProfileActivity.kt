package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var imgProfile: ImageView
    private lateinit var txtNameAge: TextView
    private lateinit var txtIntent: TextView
    private lateinit var txtBio: TextView
    private lateinit var txtGender: TextView
    private lateinit var txtLocation: TextView
    private lateinit var txtHobbies: TextView
    private lateinit var btnEditProfile: Button
    private lateinit var btnSettings: Button

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Bind views
        imgProfile = findViewById(R.id.imgProfile)
        txtNameAge = findViewById(R.id.txtNameAge)
        txtIntent = findViewById(R.id.txtIntent)
        txtBio = findViewById(R.id.txtBio)
        txtGender = findViewById(R.id.txtGender)
        txtLocation = findViewById(R.id.txtLocation)
        txtHobbies = findViewById(R.id.txtHobbies)
        btnEditProfile = findViewById(R.id.btnEditProfile)
        btnSettings = findViewById(R.id.btnSettings)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadUserProfile()

        btnEditProfile.setOnClickListener {
            // Open questionnaire or edit screen
            startActivity(Intent(this, QuestionnaireActivity::class.java))
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
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
                    val age = doc.getLong("age")?.toInt() ?: 0
                    val intent = doc.getString("intent") ?: ""
                    val bio = doc.getString("bio") ?: ""
                    val gender = doc.getString("gender") ?: ""
                    val location = doc.getString("location") ?: ""
                    val hobbies = (doc.get("hobbies") as? List<*>)?.joinToString(", ") ?: ""
                    val imageUrl = doc.getString("profileImageUrl")

                    // Fill UI
                    txtNameAge.text = if (name.isNotEmpty() && age > 0) "$name, $age" else name
                    txtIntent.text = if (intent.isNotEmpty()) "Looking for: $intent" else ""
                    txtBio.text = if (bio.isNotEmpty()) "Bio: $bio" else ""
                    txtGender.text = if (gender.isNotEmpty()) "Gender: $gender" else ""
                    txtLocation.text = if (location.isNotEmpty()) "Location: $location" else ""
                    txtHobbies.text = if (hobbies.isNotEmpty()) "Hobbies: $hobbies" else ""

                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imageUrl).into(imgProfile)
                    }
                } else {
                    // No profile yet → send to questionnaire
                    Toast.makeText(this, "Please complete your profile", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, QuestionnaireActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}