package com.dianca.synced

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ScoreManager {

    fun saveScore(userId: String, friendId: String, game: String, points: Int) {
        val score = Score(userId = userId, friendId = friendId, game = game, points = points)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.postScore(score)
                if (response.isSuccessful) {
                    println("✅ Score saved to DB: ${response.body()}")
                } else {
                    val error = response.errorBody()?.string()
                    println("❌ Failed to save score: ${response.code()} - $error")
                    println("Sent JSON: $score")
                }
            } catch (e: Exception) {
                println("🔥 Exception while saving score: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun fetchFriendScores(friendId: String, onResult: (List<Score>) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getFriendScores(friendId)
                if (response.isSuccessful) {
                    val scores = response.body() ?: emptyList()
                    CoroutineScope(Dispatchers.Main).launch { onResult(scores) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
