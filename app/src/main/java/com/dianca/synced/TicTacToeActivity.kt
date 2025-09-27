package com.dianca.synced

import android.os.Bundle
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dianca.synced.R
import com.dianca.synced.ScoreManager
import com.google.firebase.auth.FirebaseAuth

class TicTacToeActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var gridLayout: GridLayout
    private var board = Array(9) { "" }
    private var currentPlayer = "X"
    private var gameActive = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tic_tac_toe)

        statusText = findViewById(R.id.txtStatus)
        gridLayout = findViewById(R.id.gridLayout)

        for (i in 0 until gridLayout.childCount) {
            val button = gridLayout.getChildAt(i) as Button
            button.setOnClickListener { onCellClicked(i, button) }
        }
    }

    private fun onCellClicked(index: Int, button: Button) {
        if (board[index].isNotEmpty() || !gameActive) return

        board[index] = currentPlayer
        button.text = currentPlayer

        if (checkWinner()) {
            statusText.text = "$currentPlayer Wins!"
            saveScore( if (currentPlayer == "X") 10 else 5 )
            gameActive = false
        } else if (board.all { it.isNotEmpty() }) {
            statusText.text = "Draw!"
            saveScore(3)
            gameActive = false
        } else {
            currentPlayer = if (currentPlayer == "X") "O" else "X"
            statusText.text = "Player $currentPlayer's Turn"
        }
    }

    private fun checkWinner(): Boolean {
        val winningPositions = arrayOf(
            intArrayOf(0,1,2), intArrayOf(3,4,5), intArrayOf(6,7,8), // rows
            intArrayOf(0,3,6), intArrayOf(1,4,7), intArrayOf(2,5,8), // cols
            intArrayOf(0,4,8), intArrayOf(2,4,6) // diagonals
        )
        for (pos in winningPositions) {
            if (board[pos[0]] == currentPlayer &&
                board[pos[1]] == currentPlayer &&
                board[pos[2]] == currentPlayer) {
                return true
            }
        }
        return false
    }

    private fun saveScore(points: Int) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
        ScoreManager.saveScore(userId, "friendId123", "TicTacToe", points)
    }
}
