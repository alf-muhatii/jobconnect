package com.example.app.model

data class JobPost(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorProfilePicture: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isAuthorVerified: Boolean = false
)
