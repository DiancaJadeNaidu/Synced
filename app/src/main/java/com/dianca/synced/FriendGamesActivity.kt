package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FriendGamesActivity : AppCompatActivity() {

    private var friendId: String? = null
    private var friendName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_games)

        friendId = intent.getStringExtra("friendId")
        friendName = intent.getStringExtra("friendName")

        findViewById<TextView>(R.id.tvFriendTitle).text = "Play Games with $friendName"

        findViewById<Button>(R.id.btnHangman).setOnClickListener {
            val intent = Intent(this, HangmanActivity::class.java)
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnTrivia).setOnClickListener {
            val intent = Intent(this, TriviaActivity::class.java)
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnEmojiMatch).setOnClickListener {
            val intent = Intent(this, EmojiMatchActivity::class.java)
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnRapidTap).setOnClickListener {
            val intent = Intent(this, RapidTapActivity::class.java)
            intent.putExtra("friendName", friendName)
            startActivity(intent)
        }
    }
}
