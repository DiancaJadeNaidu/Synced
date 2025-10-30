package com.dianca.synced

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val age: Int,
    val gender: String,
    val location: String,
    var bio: String,
    val avatarId: Int
)
