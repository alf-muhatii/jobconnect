package com.example.app.repository

import com.example.app.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class UserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    
    suspend fun saveUser(user: User) {
        db.collection("users").document(user.id).set(user).await()
    }

    suspend fun getUser(uid: String): User? {
        return db.collection("users").document(uid).get().await().toObject(User::class.java)
    }

    fun searchUsers(): Flow<List<User>> = callbackFlow {
        val subscription = db.collection("users")
            .addSnapshotListener { snapshot, _ ->
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun followUser(currentUserId: String, targetUserId: String) {
        try {
            val batch = db.batch()
            val currentUserRef = db.collection("users").document(currentUserId)
            val targetUserRef = db.collection("users").document(targetUserId)
            
            batch.update(currentUserRef, "following", FieldValue.arrayUnion(targetUserId))
            batch.update(targetUserRef, "followers", FieldValue.arrayUnion(currentUserId))
            batch.commit().await()
        } catch (e: Exception) {
            android.util.Log.e("UserRepo", "followUser failed: ${e.message}")
            throw e
        }
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String) {
        try {
            val batch = db.batch()
            val currentUserRef = db.collection("users").document(currentUserId)
            val targetUserRef = db.collection("users").document(targetUserId)
            
            batch.update(currentUserRef, "following", FieldValue.arrayRemove(targetUserId))
            batch.update(targetUserRef, "followers", FieldValue.arrayRemove(currentUserId))
            batch.commit().await()
        } catch (e: Exception) {
            android.util.Log.e("UserRepo", "unfollowUser failed: ${e.message}")
            throw e
        }
    }

    fun getUsersByIds(ids: List<String>): Flow<List<User>> = callbackFlow {
        if (ids.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        
        // Firestore whereIn limit is 10, so we might need to chunk if list is big
        // For now, let's keep it simple
        val subscription = db.collection("users").whereIn("id", ids)
            .addSnapshotListener { snapshot, _ ->
                val users = snapshot?.toObjects(User::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveJob(userId: String, jobId: String) {
        db.collection("users").document(userId)
            .update("savedJobs", FieldValue.arrayUnion(jobId))
            .await()
    }

    suspend fun unsaveJob(userId: String, jobId: String) {
        db.collection("users").document(userId)
            .update("savedJobs", FieldValue.arrayRemove(jobId))
            .await()
    }

    fun getFollowedUsers(userId: String): Flow<List<User>> = callbackFlow {
        // First get the following list of current user
        val subscription = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, _ ->
                val user = snapshot?.toObject(User::class.java)
                val followingIds = user?.following ?: emptyList()
                
                if (followingIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                db.collection("users").whereIn("id", followingIds)
                    .get()
                    .addOnSuccessListener { usersSnapshot ->
                        trySend(usersSnapshot.toObjects(User::class.java))
                    }
            }
        awaitClose { subscription.remove() }
    }
}
