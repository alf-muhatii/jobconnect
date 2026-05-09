package com.example.app.repository

import com.example.app.model.Conversation
import com.example.app.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose

class ChatRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun getConversations(userId: String): Flow<List<Conversation>> = callbackFlow {
        val subscription = db.collection("conversations")
            .whereArrayContains("participants", userId)
            .addSnapshotListener { snapshot, _ ->
                val convs = snapshot?.toObjects(Conversation::class.java) ?: emptyList()
                // Sort in Kotlin to avoid Index requirements
                trySend(convs.sortedByDescending { it.timestamp })
            }
        awaitClose { subscription.remove() }
    }

    suspend fun sendMessage(senderId: String, receiverId: String, text: String) {
        try {
            val participants = listOf(senderId, receiverId).sorted()
            val convId = participants.joinToString("_")
            
            val message = Message(
                id = db.collection("messages").document().id,
                senderId = senderId,
                receiverId = receiverId,
                text = text,
                timestamp = System.currentTimeMillis()
            )

            val batch = db.batch()
            val msgRef = db.collection("conversations").document(convId).collection("messages").document(message.id)
            val convRef = db.collection("conversations").document(convId)

            batch.set(msgRef, message)
            batch.set(convRef, Conversation(id = convId, participants = participants, lastMessage = text, timestamp = message.timestamp))
            batch.commit().await()
        } catch (e: Exception) {
            android.util.Log.e("ChatRepo", "sendMessage failed: ${e.message}")
            throw e
        }
    }

    fun getMessages(senderId: String, receiverId: String): Flow<List<Message>> = callbackFlow {
        val participants = listOf(senderId, receiverId).sorted()
        val convId = participants.joinToString("_")

        val subscription = db.collection("conversations").document(convId)
            .collection("messages")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ChatRepo", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }
                val msgs = snapshot?.toObjects(Message::class.java) ?: emptyList()
                // Sort in Kotlin to avoid Index requirements which often cause disappearing messages
                trySend(msgs.sortedBy { it.timestamp })
            }
        awaitClose { subscription.remove() }
    }
}
