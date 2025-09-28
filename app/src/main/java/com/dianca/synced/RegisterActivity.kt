package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dianca.synced.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private var selectedAvatarResId: Int? = null
    private var selectedAvatarView: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupAvatarSelection()
        setupRegisterButton()
        setupLoginHint()
    }

    private fun setupAvatarSelection() {
        val avatarViews = listOf(
            binding.avatar1, binding.avatar2, binding.avatar3, binding.avatar4,
            binding.avatar5, binding.avatar6, binding.avatar7, binding.avatar8
        )
        for (avatar in avatarViews) avatar.setOnClickListener { selectAvatar(avatar) }
    }

    private fun selectAvatar(avatar: ImageView) {
        selectedAvatarView?.background = null
        avatar.setBackgroundResource(R.drawable.selected_avatar_border)
        selectedAvatarView = avatar
        selectedAvatarResId = when (avatar.id) {
            R.id.avatar1 -> R.drawable.ic_avatar1
            R.id.avatar2 -> R.drawable.ic_avatar2
            R.id.avatar3 -> R.drawable.ic_avatar3
            R.id.avatar4 -> R.drawable.ic_avatar4
            R.id.avatar5 -> R.drawable.ic_avatar5
            R.id.avatar6 -> R.drawable.ic_avatar6
            R.id.avatar7 -> R.drawable.ic_avatar7
            R.id.avatar8 -> R.drawable.ic_avatar8
            else -> R.drawable.default_avatar_foreground
        }
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val name = binding.etName.text.toString().trim()
            val whatsapp = binding.etWhatsApp.text.toString().trim()
            val instagram = binding.etInstagram.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedAvatarResId == null) {
                Toast.makeText(this, "Please select an avatar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cleanWhatsapp = sanitizeWhatsapp(whatsapp)
            if (cleanWhatsapp.length !in 9..15) {
                Toast.makeText(this, "Please enter a valid WhatsApp number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val userMap = hashMapOf(
                            "name" to name,
                            "avatarId" to selectedAvatarResId,
                            "whatsapp" to cleanWhatsapp,
                            "instagram" to instagram
                        )
                        FirebaseFirestore.getInstance().collection("users")
                            .document(uid).set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                navigateAfterRegister()
                            }.addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to save user info: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    // User-friendly sanitizer: handles +country code or local numbers
    private fun sanitizeWhatsapp(input: String): String {
        var number = input.replace("[^\\d+]".toRegex(), "") // remove spaces, dashes, parentheses
        if (number.startsWith("+")) number = number.drop(1) // remove '+' for wa.me
        else if (number.startsWith("0")) number = "27${number.drop(1)}" // assume South Africa if starts with 0
        return number
    }

    private fun setupLoginHint() {
        binding.tvLoginHint.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

        private fun navigateAfterRegister() {
            val uid = auth.currentUser?.uid ?: return
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val hasDetails = snapshot.getString("birthday")?.isNotEmpty() == true &&
                            snapshot.getString("gender")?.isNotEmpty() == true

                    if (hasDetails) {
                        startActivity(Intent(this, TopMatchesActivity::class.java))
                    } else {
                        startActivity(Intent(this, QuestionnaireActivity::class.java))
                    }
                    finish()
                } else {
                    startActivity(Intent(this, QuestionnaireActivity::class.java))
                    finish()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error checking user details", Toast.LENGTH_SHORT).show()
            }
        }
}
