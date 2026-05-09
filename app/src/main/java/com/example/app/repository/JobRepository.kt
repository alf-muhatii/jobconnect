package com.example.app.repository

import com.example.app.model.JobPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class JobRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
    
    suspend fun postJob(job: JobPost) {
        val ref = db.collection("jobs").document()
        val jobWithId = job.copy(id = ref.id)
        ref.set(jobWithId).await()
    }

    fun getJobPosts(): Flow<List<JobPost>> = callbackFlow {
        // Removed .orderBy() to ensure posts show up without needing to create a manual Index in Firebase Console yet
        val subscription = db.collection("jobs")
            .addSnapshotListener { snapshot, _ ->
                val jobs = snapshot?.toObjects(JobPost::class.java) ?: emptyList()
                // Manual sort in Kotlin to save you from Firestore Indexing headache
                trySend(jobs.sortedByDescending { it.timestamp })
            }
        awaitClose { subscription.remove() }
    }

    fun getUserJobs(userId: String): Flow<List<JobPost>> = callbackFlow {
        val subscription = db.collection("jobs")
            .whereEqualTo("authorId", userId)
            .addSnapshotListener { snapshot, _ ->
                val jobs = snapshot?.toObjects(JobPost::class.java) ?: emptyList()
                trySend(jobs.sortedByDescending { it.timestamp })
            }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteJobs(jobIds: List<String>) {
        val batch = db.batch()
        jobIds.forEach { id ->
            batch.delete(db.collection("jobs").document(id))
        }
        batch.commit().await()
    }
}
