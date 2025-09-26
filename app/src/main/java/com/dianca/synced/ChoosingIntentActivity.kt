package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dianca.synced.databinding.ActivityChoosingIntentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChoosingIntentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChoosingIntentBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var selectedIntent: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChoosingIntentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Back to Questionnaire
        binding.tvBack.setOnClickListener {
            finish()
        }

        // Select Friendship
        binding.cardFriendship.setOnClickListener {
            selectedIntent = "Friendship"
            highlightSelection("Friendship")
        }

        // Select Dating
        binding.cardDating.setOnClickListener {
            selectedIntent = "Dating"
            highlightSelection("Dating")
        }

        binding.btnContinue.setOnClickListener {
            if (selectedIntent == null) {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uid = auth.currentUser?.uid ?: return@setOnClickListener
            db.collection("users").document(uid)
                .update("intent", selectedIntent)
                .addOnSuccessListener {
                    Toast.makeText(this, "Intent saved!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, TopMatchesActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save intent", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun highlightSelection(selected: String) {
        if (selected == "Friendship") {
            binding.cardFriendship.setBackgroundResource(R.drawable.selected_border)
            binding.cardDating.setBackgroundResource(R.drawable.default_border)
        } else {
            binding.cardDating.setBackgroundResource(R.drawable.selected_border)
            binding.cardFriendship.setBackgroundResource(R.drawable.default_border)
        }
    }
}
