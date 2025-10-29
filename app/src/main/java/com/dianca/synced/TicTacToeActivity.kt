package com.dianca.synced

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TicTacToeActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var yourScoreText: TextView
    private lateinit var friendScoreText: TextView
    private lateinit var gridLayout: GridLayout
    private lateinit var btnRestart: Button

    private lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNav: BottomNavigationView

    private var board = Array(9) { "" }
    private var currentPlayer = "X"
    private var gameActive = true
    private var yourScore = 0
    private var friendScore = 0

    private lateinit var userId: String
    private lateinit var friendId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tic_tac_toe)

        // Bind views
        statusText = findViewById(R.id.txtStatus)
        yourScoreText = findViewById(R.id.txtYourScore)
        friendScoreText = findViewById(R.id.txtFriendScore)
        gridLayout = findViewById(R.id.gridLayout)
        btnRestart = findViewById(R.id.btnRestart)

        bottomNav = findViewById(R.id.bottomNav)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        friendId = intent.getStringExtra("friendId") ?: "friend123"



        // Setup bottom navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }

                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Setup grid buttons
        for (i in 0 until 9) {
            val button = Button(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 100
                    height = 100
                    rowSpec = GridLayout.spec(i / 3, 1f)
                    columnSpec = GridLayout.spec(i % 3, 1f)
                    setMargins(4, 4, 4, 4)
                }
                textSize = 24f
            }
            button.setOnClickListener { onCellClicked(i, button) }
            gridLayout.addView(button)
        }

        btnRestart.setOnClickListener { resetGame() }

        fetchScores()
    }

    private fun onCellClicked(index: Int, button: Button) {
        if (board[index].isNotEmpty() || !gameActive) return

        board[index] = currentPlayer
        button.text = currentPlayer

        when {
            checkWinner() -> {
                val points = if (currentPlayer == "X") 10 else 5
                statusText.text = "$currentPlayer Wins!"
                gameActive = false
                saveScore(points)
            }
            board.all { it.isNotEmpty() } -> {
                statusText.text = "Draw!"
                gameActive = false
                saveScore(3)
            }
            else -> {
                currentPlayer = if (currentPlayer == "X") "O" else "X"
                statusText.text = "Player $currentPlayer's Turn"
            }
        }
    }

    private fun checkWinner(): Boolean {
        val wins = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )
        return wins.any { line ->
            board[line[0]] == currentPlayer &&
                    board[line[1]] == currentPlayer &&
                    board[line[2]] == currentPlayer
        }
    }

    private fun saveScore(points: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            ScoreManager.saveScore(userId, friendId, "TicTacToe", points)
            fetchScores()
        }
    }

    private fun fetchScores() {
        ScoreManager.fetchFriendScores(friendId) { scores ->
            yourScore = scores.filter { it.userId == userId }.sumOf { it.points }
            friendScore = scores.filter { it.userId == friendId }.sumOf { it.points }
            runOnUiThread {
                yourScoreText.text = "Your Score: $yourScore"
                friendScoreText.text = "Friend Score: $friendScore"
            }
        }
    }

    private fun resetGame() {
        board = Array(9) { "" }
        currentPlayer = "X"
        gameActive = true
        statusText.text = "Player X's Turn"

        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.text = ""
        }
    }
}
