package com.dianca.synced

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "requests")
data class RequestEntity(
    @PrimaryKey val id: String,
    val senderId: String,
    val receiverId: String,
    val status: String,
    val timestamp: Long,
    val synced: Boolean = false
)
