package com.dianca.synced

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.dianca.synced.R
import com.dianca.synced.ScoreManager
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

class NumberGuessActivity : AppCompatActivity() {

    private lateinit var edtGuess: EditText
    private lateinit var btnGuess: Button
    private lateinit var txtFeedback: TextView
    private lateinit var txtAttempts: TextView

    private var targetNumber = 0
    private var attempts = 0
    private var maxAttempts = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_guess)

        edtGuess = findViewById(R.id.edtGuess)
        btnGuess = findViewById(R.id.btnGuess)
        txtFeedback = findViewById(R.id.txtFeedback)
        txtAttempts = findViewById(R.id.txtAttempts)

        resetGame()

        btnGuess.setOnClickListener {
            val guess = edtGuess.text.toString().toIntOrNull()
            if (guess == null) {
                Toast.makeText(this, "Enter a valid number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            attempts++
            txtAttempts.text = "Attempts: $attempts/$maxAttempts"

            when {
                guess == targetNumber -> {
                    val points = (maxAttempts - attempts + 1) * 10
                    txtFeedback.text = "🎉 Correct! You scored $points points."
                    saveScore(points)
                    btnGuess.isEnabled = false
                }
                attempts >= maxAttempts -> {
                    txtFeedback.text = "❌ Game over! Number was $targetNumber."
                    saveScore(0)
                    btnGuess.isEnabled = false
                }
                guess < targetNumber -> txtFeedback.text = "Higher ↑"
                guess > targetNumber -> txtFeedback.text = "Lower ↓"
            }
        }
    }

    private fun resetGame() {
        targetNumber = Random.nextInt(1, 101) // random number 1-100
        attempts = 0
        txtAttempts.text = "Attempts: 0/$maxAttempts"
        txtFeedback.text = "Guess a number between 1 and 100"
        btnGuess.isEnabled = true
    }

    private fun saveScore(points: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        ScoreManager.saveScore(userId, "friendId123", "NumberGuess", points)
    }
}
