package com.dianca.synced

data class Score(
    var id: String? = null,
    var userId: String? = null,
    var friendId: String? = null,
    var game: String? = null,
    var points: Int = 0,
    var timestamp: String? = null
)

