package com.example.app.model

data class User(
    val id: String = "",
    val name: String = "",
    val searchName: String = "", // Lowercase name for searching
    val email: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList()
)
