package com.dianca.synced

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dianca.synced.R
import com.dianca.synced.ScoreManager
import com.google.firebase.auth.FirebaseAuth

class MemoryGameActivity : AppCompatActivity() {

    private lateinit var txtScore: TextView
    private lateinit var gridLayout: GridLayout
    private var score = 0
    private var flippedCards = mutableListOf<Button>()
    private var cardValues = mutableListOf("🍎", "🍎", "🍌", "🍌", "🍇", "🍇", "🍒", "🍒")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memory_game)

        txtScore = findViewById(R.id.txtScore)
        gridLayout = findViewById(R.id.gridLayoutMemory)

        // Shuffle cards
        cardValues.shuffle()

        // Add buttons for each card
        for (i in cardValues.indices) {
            val button = Button(this)
            button.text = "❓"
            button.textSize = 24f
            button.layoutParams = GridLayout.LayoutParams().apply {
                width = 150
                height = 150
                marginStart = 8
                topMargin = 8
            }

            button.setOnClickListener { flipCard(button, i) }
            gridLayout.addView(button)
        }
    }

    private fun flipCard(button: Button, index: Int) {
        if (button.text != "❓" || flippedCards.size == 2) return

        button.text = cardValues[index]
        flippedCards.add(button)

        if (flippedCards.size == 2) {
            val first = flippedCards[0]
            val second = flippedCards[1]

            // Delay check
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
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        ScoreManager.saveScore(userId, "friendId123", "MemoryMatch", points)
    }
}
