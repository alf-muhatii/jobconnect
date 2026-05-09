package com.example.app.model

enum class NotificationType {
    MESSAGE, JOB_POST, NEW_FOLLOWER
}

data class Notification(
    val id: String = "",
    val type: NotificationType = NotificationType.MESSAGE,
    val fromUserId: String = "",
    val fromUserName: String = "",
    val fromUserProfilePic: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
