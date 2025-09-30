package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dianca.synced.ScoreManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlin.random.Random

class NumberGuessActivity : AppCompatActivity() {

    private lateinit var edtGuess: EditText
    private lateinit var btnGuess: Button
    private lateinit var txtFeedback: TextView
    private lateinit var txtAttempts: TextView
    private lateinit var txtScoreboard: TextView

    private var targetNumber = 0
    private var attempts = 0
    private val maxAttempts = 10

    private lateinit var userId: String
    private lateinit var friendId: String
    private var currentScore = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_number_guess)

        // Toolbar setup
        val toolbar: Toolbar = findViewById(R.id.toolbarNumberGuess)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TopMatchesActivity::class.java))
                R.id.nav_messages -> startActivity(Intent(this, SyncedFriendsActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }
            true
        }

        edtGuess = findViewById(R.id.edtGuess)
        btnGuess = findViewById(R.id.btnGuess)
        txtFeedback = findViewById(R.id.txtFeedback)
        txtAttempts = findViewById(R.id.txtAttempts)
        txtScoreboard = findViewById(R.id.txtScoreboard)

        // Get user/friend IDs
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        friendId = intent.getStringExtra("friendId") ?: "unknown"

        resetGame()
        loadScores()

        btnGuess.setOnClickListener {
            val guess = edtGuess.text.toString().toIntOrNull()
            if (guess == null) {
                Toast.makeText(this, "Enter a valid number!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            attempts++
            txtAttempts.text = "Attempts: $attempts/$maxAttempts"

            when {
                guess == targetNumber -> {
                    currentScore = (maxAttempts - attempts + 1) * 10
                    txtFeedback.text = "🎉 Correct! You scored $currentScore points!"
                    btnGuess.isEnabled = false
                    saveScore(currentScore)
                }
                attempts >= maxAttempts -> {
                    currentScore = 0
                    txtFeedback.text = "❌ Game Over! Number was $targetNumber."
                    btnGuess.isEnabled = false
                    saveScore(currentScore)
                }
                guess < targetNumber -> txtFeedback.text = "⬆ Higher!"
                guess > targetNumber -> txtFeedback.text = "⬇ Lower!"
            }
        }
    }

    private fun resetGame() {
        targetNumber = Random.nextInt(1, 101)
        attempts = 0
        currentScore = 0
        txtAttempts.text = "Attempts: 0/$maxAttempts"
        txtFeedback.text = "Guess a number between 1 and 100"
        btnGuess.isEnabled = true
    }

    private fun saveScore(points: Int) {
        ScoreManager.saveScore(userId, friendId, "NumberGuess", points)
        loadScores()
    }

    private fun loadScores() {
        ScoreManager.fetchFriendScores(friendId) { scores ->
            val myScore = scores.filter { it.userId == userId }.maxByOrNull { it.points }?.points ?: 0
            val friendScore = scores.filter { it.userId == friendId }.maxByOrNull { it.points }?.points ?: 0
            txtScoreboard.text = "You: $myScore  Friend: $friendScore"
        }
    }
}
