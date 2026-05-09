package com.example.app.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {
    fun getCurrentUser() = auth.currentUser
    fun getUserId() = auth.currentUser?.uid
    
    suspend fun signUp(email: String, pass: String) = auth.createUserWithEmailAndPassword(email, pass).await()
    suspend fun login(email: String, pass: String) = auth.signInWithEmailAndPassword(email, pass).await()
    fun logout() = auth.signOut()
    fun isLoggedIn() = auth.currentUser != null
}
