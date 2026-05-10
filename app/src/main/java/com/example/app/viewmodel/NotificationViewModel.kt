package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.Notification
import com.example.app.model.NotificationType
import com.example.app.model.User
import com.example.app.repository.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val jobRepo: JobRepository = JobRepository(),
    private val chatRepo: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadNotifications() {
        val currentUserId = authRepo.getUserId() ?: return
        
        viewModelScope.launch {
            _isLoading.value = true
            
            combine(
                chatRepo.getConversations(currentUserId),
                userRepo.searchUsers(),
                jobRepo.getJobPosts(),
                userRepo.getUserFlow(currentUserId)
            ) { conversations, allUsers, allJobs, currentUser ->
                val followingIds = currentUser?.following ?: emptyList()
                val followerIds = currentUser?.followers ?: emptyList()
                val dismissedIds = currentUser?.dismissedNotifications ?: emptyList()
                
                val combinedList = mutableListOf<Notification>()
                
                // Messages
                conversations.forEach { conv ->
                    val partnerId = conv.participants.find { it != currentUserId }
                    val partner = allUsers.find { it.id == partnerId }
                    if (partner != null && conv.lastMessage.isNotEmpty()) {
                        combinedList.add(
                            Notification(
                                id = conv.id,
                                type = NotificationType.MESSAGE,
                                fromUserId = partner.id,
                                fromUserName = partner.name,
                                fromUserProfilePic = partner.profilePictureUrl,
                                isFromUserVerified = partner.isVerified,
                                content = "Sent you a message: ${conv.lastMessage}",
                                timestamp = conv.timestamp
                            )
                        )
                    }
                }
                
                // Jobs
                allJobs.filter { it.authorId in followingIds }.forEach { job ->
                    combinedList.add(
                        Notification(
                            id = job.id,
                            type = NotificationType.JOB_POST,
                            fromUserId = job.authorId,
                            fromUserName = job.authorName,
                            fromUserProfilePic = job.authorProfilePicture,
                            isFromUserVerified = job.isAuthorVerified,
                            content = "Posted a new job: ${job.title}",
                            timestamp = job.timestamp
                        )
                    )
                }

                // Followers
                followerIds.forEach { fid ->
                    val follower = allUsers.find { it.id == fid }
                    if (follower != null) {
                        combinedList.add(
                            Notification(
                                id = "follow_$fid",
                                type = NotificationType.NEW_FOLLOWER,
                                fromUserId = follower.id,
                                fromUserName = follower.name,
                                fromUserProfilePic = follower.profilePictureUrl,
                                isFromUserVerified = follower.isVerified,
                                content = "Started following you",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
                
                // Filter out dismissed notifications
                combinedList
                    .filter { it.id !in dismissedIds }
                    .sortedByDescending { it.timestamp }
            }.collectLatest {
                _notifications.value = it
                _isLoading.value = false
            }
        }
    }

    fun clearNotifications() {
        val currentUserId = authRepo.getUserId() ?: return
        val currentIds = _notifications.value.map { it.id }
        if (currentIds.isEmpty()) return

        viewModelScope.launch {
            try {
                userRepo.dismissNotifications(currentUserId, currentIds)
            } catch (e: Exception) {
                android.util.Log.e("NotificationVM", "Clear failed: ${e.message}")
            }
        }
    }
}
