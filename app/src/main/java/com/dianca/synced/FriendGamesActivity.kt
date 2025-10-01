package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class FriendGamesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_games)

        // Get friend details
        val friendId = intent.getStringExtra("friendId") ?: ""
        val friendName = intent.getStringExtra("friendName") ?: "Friend"

        // Title text (below toolbar)
        val tvFriendTitle = findViewById<TextView>(R.id.tvFriendTitle)
        tvFriendTitle.text = "Play Games with $friendName"

        // Game buttons
        val btnMemoryGame = findViewById<LinearLayout>(R.id.btnMemoryGame)
        val btnTrivia = findViewById<LinearLayout>(R.id.btnTrivia)
        val btnNumberGuess = findViewById<LinearLayout>(R.id.btnNumberGuess)
        val btnTicTacToe = findViewById<LinearLayout>(R.id.btnTicTacToe)

        btnMemoryGame.setOnClickListener {
            val i = Intent(this, MemoryGameActivity::class.java)
            i.putExtra("friendId", friendId)
            i.putExtra("friendName", friendName)
            startActivity(i)
        }

        btnTrivia.setOnClickListener {
            val i = Intent(this, TriviaActivity::class.java)
            i.putExtra("friendId", friendId)
            i.putExtra("friendName", friendName)
            startActivity(i)
        }

        btnNumberGuess.setOnClickListener {
            val i = Intent(this, NumberGuessActivity::class.java)
            i.putExtra("friendId", friendId)
            i.putExtra("friendName", friendName)
            startActivity(i)
        }

        btnTicTacToe.setOnClickListener {
            val i = Intent(this, TicTacToeActivity::class.java)
            i.putExtra("friendId", friendId)
            i.putExtra("friendName", friendName)
            startActivity(i)
        }

        // ✅ Bottom Nav setup
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, TopMatchesActivity::class.java))
                    true
                }
                R.id.nav_messages -> {
                    startActivity(Intent(this, SyncedFriendsActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                R.id.nav_help -> {
                    startActivity(Intent(this, HelpActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
