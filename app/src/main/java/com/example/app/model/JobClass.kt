package com.example.app.model

data class JobClass(
    val id: String = "",
    val title: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
)
