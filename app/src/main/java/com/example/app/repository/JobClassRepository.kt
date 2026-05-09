package com.example.app.repository

import com.example.app.model.JobClass
import com.example.app.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JobClassRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun createJobClass(title: String, userId: String) {
        val ref = db.collection("job_classes").document()
        val jobClass = JobClass(
            id = ref.id,
            title = title,
            createdAt = System.currentTimeMillis(),
            createdBy = userId
        )
        ref.set(jobClass).await()
    }

    fun getJobClasses(userId: String): Flow<List<JobClass>> = callbackFlow {
        val subscription = db.collection("job_classes")
            .whereEqualTo("createdBy", userId)
            .addSnapshotListener { snapshot, _ ->
                val classes = snapshot?.toObjects(JobClass::class.java) ?: emptyList()
                trySend(classes.sortedByDescending { it.createdAt })
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addCandidateToJobClass(jobClassId: String, userId: String) {
        db.collection("job_classes").document(jobClassId)
            .collection("candidates").document(userId)
            .set(mapOf("userId" to userId, "addedAt" to System.currentTimeMillis()))
            .await()
    }

    fun getCandidates(jobClassId: String): Flow<List<String>> = callbackFlow {
        val subscription = db.collection("job_classes").document(jobClassId)
            .collection("candidates")
            .addSnapshotListener { snapshot, _ ->
                val ids = snapshot?.documents?.map { it.id } ?: emptyList()
                trySend(ids)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun removeCandidateFromJobClass(jobClassId: String, userId: String) {
        db.collection("job_classes").document(jobClassId)
            .collection("candidates").document(userId)
            .delete().await()
    }

    suspend fun deleteJobClass(jobClassId: String) {
        val classRef = db.collection("job_classes").document(jobClassId)
        
        // Note: In a production app, you should also delete the 'candidates' subcollection.
        // Firestore doesn't delete subcollections automatically when a parent doc is deleted.
        // For simplicity, we delete the main class document.
        classRef.delete().await()
    }
}
