package com.dianca.synced.models

data class MatchModel(
    val uid: String,
    val name: String,
    val age: Int,
    val bio: String,
    val gender: String,
    val location: String,
    val avatarName: String,
    val percentage: Int,
    val hobbies: List<String> = emptyList(),
    val food: String = "",
    val movieGenre: String = "",
    val favoriteColor: String = "",
    val zodiacSign: String = ""  ,
    val intent: String = "Friendship"// <-- Added for zodiac compatibility
)
