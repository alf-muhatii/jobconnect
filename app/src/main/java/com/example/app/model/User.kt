package com.example.app.model

import com.google.firebase.firestore.PropertyName

data class User(
    val id: String = "",
    val name: String = "",
    val searchName: String = "", // Lowercase name for searching
    val email: String = "",
    val bio: String = "",
    val profilePictureUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val savedJobs: List<String> = emptyList(),
    @get:PropertyName("isVerified") @set:PropertyName("isVerified") var isVerified: Boolean = false,
    @get:PropertyName("verificationRequested") @set:PropertyName("verificationRequested") var verificationRequested: Boolean = false
)
