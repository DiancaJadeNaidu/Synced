package com.dianca.synced

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FriendGamesActivity : AppCompatActivity() {

    private var friendId: String? = null
    private var friendName: String? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_games)

        friendId = intent.getStringExtra("friendId")
        friendName = intent.getStringExtra("friendName")

        findViewById<TextView>(R.id.tvFriendName).text = "Play Games with $friendName"

        findViewById<Button>(R.id.btnMemoryGame).setOnClickListener {
            val intent = Intent(this, MemoryGameActivity::class.java)
            intent.putExtra("friendId", friendId)
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnTrivia).setOnClickListener {
            val intent = Intent(this, TriviaActivity::class.java)
            intent.putExtra("friendId", friendId)
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnNumberGuess).setOnClickListener {
            val intent = Intent(this, NumberGuessActivity::class.java)
            intent.putExtra("friendId", friendId)
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnTicTacToe).setOnClickListener {
            val intent = Intent(this, TicTacToeActivity::class.java)
            intent.putExtra("friendId", friendId)
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }
    }
}
