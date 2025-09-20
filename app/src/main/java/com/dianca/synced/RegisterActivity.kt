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

    // Selected avatar resource ID
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
            binding.avatar1,
            binding.avatar2,
            binding.avatar3,
            binding.avatar4,
            binding.avatar5,
            binding.avatar6,
            binding.avatar7,
            binding.avatar8
        )

        for (avatar in avatarViews) {
            avatar.setOnClickListener { selectAvatar(avatar) }
        }
    }

    private fun selectAvatar(avatar: ImageView) {
        // Remove highlight from previous avatar
        selectedAvatarView?.background = null

        // Highlight the selected avatar
        avatar.setBackgroundResource(R.drawable.selected_avatar_border)

        selectedAvatarView = avatar

        // Save selected avatar resource ID
        selectedAvatarResId = avatar.id
    }

    private fun setupRegisterButton() {
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()
            val name = binding.etName.text.toString().trim()

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

            // Firebase registration
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Save extra user info to Firestore
                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                        val userMap = hashMapOf(
                            "name" to name,
                            "avatarId" to selectedAvatarResId
                        )
                        val db = FirebaseFirestore.getInstance()
                        db.collection("users").document(uid)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Registration successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateToHome()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Failed to save user info: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun setupLoginHint() {
        binding.tvLoginHint.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
