package com.dianca.synced

data class SyncRequest(
    var id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val status: String = "pending",
    var senderName: String = "",
    var senderAge: Int = 0,
    var senderImage: String = ""
)
