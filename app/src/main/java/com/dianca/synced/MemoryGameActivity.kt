package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth


class MemoryGameActivity : AppCompatActivity() {

    private lateinit var txtScore: TextView
    private lateinit var txtScoreboard: TextView
    private lateinit var gridLayout: GridLayout
    private lateinit var btnBack: ImageButton
    private lateinit var tvNavTitle: TextView

    private var score = 0
    private var flippedCards = mutableListOf<Button>()
    private var cardValues = mutableListOf("🍎","🍎","🍌","🍌","🍇","🍇","🍒","🍒")

    private lateinit var friendId: String
    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_game)


        // Bottom Navigation
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.nav_home -> startActivity(Intent(this, TopMatchesActivity::class.java))
                R.id.nav_messages -> startActivity(Intent(this, SyncedFriendsActivity::class.java))
                R.id.nav_geo -> startActivity(Intent(this, GeolocationActivity::class.java))
                R.id.nav_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
                R.id.nav_help -> startActivity(Intent(this, HelpActivity::class.java))
            }
            true
        }

        txtScore = findViewById(R.id.txtScore)
        txtScoreboard = findViewById(R.id.txtScoreboard)
        gridLayout = findViewById(R.id.gridLayoutMemory)

        // Get IDs
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        friendId = intent.getStringExtra("friendId") ?: "unknown"

        // Shuffle cards
        cardValues.shuffle()

        // Add buttons to Grid
        for (i in cardValues.indices) {
            val button = Button(this).apply {
                text = "❓"
                textSize = 24f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 150
                    height = 150
                    marginStart = 8
                    topMargin = 8
                }
                setOnClickListener { flipCard(this, i) }
            }
            gridLayout.addView(button)
        }

        // Load competitive scores
        loadScores()
    }

    private fun flipCard(button: Button, index: Int) {
        if (button.text != "❓" || flippedCards.size == 2) return

        button.text = cardValues[index]
        flippedCards.add(button)

        if (flippedCards.size == 2) {
            val first = flippedCards[0]
            val second = flippedCards[1]

            button.postDelayed({
                if (first.text == second.text) {
                    score += 5
                    txtScore.text = "Score: $score"
                } else {
                    first.text = "❓"
                    second.text = "❓"
                }
                flippedCards.clear()

                if (isGameOver()) {
                    txtScore.text = "Game Over! Final Score: $score"
                    saveScore(score)
                }
            }, 800)
        }
    }

    private fun isGameOver(): Boolean {
        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            if (button.text == "❓") return false
        }
        return true
    }

    private fun saveScore(points: Int) {
        ScoreManager.saveScore(userId, friendId, "MemoryMatch", points)
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
