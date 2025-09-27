package com.dianca.synced.models

import com.dianca.synced.R

data class MatchModel(
    val uid: String,
    val name: String,
    val age: Int,
    val bio: String,
    val gender: String,
    val location: String,
    val avatarName: String,
    val percentage: Int = 0
)
