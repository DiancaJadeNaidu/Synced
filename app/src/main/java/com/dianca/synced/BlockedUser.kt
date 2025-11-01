package com.dianca.synced

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_users")
data class BlockedUser(
    @PrimaryKey val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)
