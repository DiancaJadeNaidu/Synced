package com.dianca.synced

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

        findViewById<Button>(R.id.btnTicTacToe).setOnClickListener {
            // TODO: open Tic Tac Toe screen
        }
        findViewById<Button>(R.id.btnTrivia).setOnClickListener {
            // TODO: open Trivia Quiz screen
        }
        findViewById<Button>(R.id.btnEmojiMatch).setOnClickListener {
            // TODO: open Emoji Match screen
        }
        findViewById<Button>(R.id.btnQuickDraw).setOnClickListener {
            // TODO: open Quick Draw screen
        }
    }
}
