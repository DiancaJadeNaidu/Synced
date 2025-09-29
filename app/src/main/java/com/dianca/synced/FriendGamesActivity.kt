package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FriendGamesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_games)

        // Get the friend name from intent
        val friendName = intent.getStringExtra("friendName") ?: "Friend"

        // Update the title text
        val tvFriendTitle = findViewById<TextView>(R.id.tvFriendTitle)
        tvFriendTitle.text = "Play Games with $friendName"

        // Setup game buttons
        val btnMemoryGame = findViewById<LinearLayout>(R.id.btnMemoryGame)
        val btnTrivia = findViewById<LinearLayout>(R.id.btnTrivia)
        val btnNumberGuess = findViewById<LinearLayout>(R.id.btnNumberGuess)
        val btnTicTacToe = findViewById<LinearLayout>(R.id.btnTicTacToe)

        btnMemoryGame.setOnClickListener {
            val intent = Intent(this, MemoryGameActivity::class.java)
            intent.putExtra("friendId", intent.getStringExtra("friendId"))
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }

        btnTrivia.setOnClickListener {
            val intent = Intent(this, TriviaActivity::class.java)
            intent.putExtra("friendId", intent.getStringExtra("friendId"))
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }

        btnNumberGuess.setOnClickListener {
            val intent = Intent(this, NumberGuessActivity::class.java)
            intent.putExtra("friendId", intent.getStringExtra("friendId"))
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }

        btnTicTacToe.setOnClickListener {
            val intent = Intent(this, TicTacToeActivity::class.java)
            intent.putExtra("friendId", intent.getStringExtra("friendId"))
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }
    }
    }
