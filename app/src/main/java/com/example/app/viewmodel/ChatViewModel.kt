package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.Conversation
import com.example.app.model.Message
import com.example.app.model.User
import com.example.app.repository.AuthRepository
import com.example.app.repository.ChatRepository
import com.example.app.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepo: ChatRepository = ChatRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages
    
    private val _followedUsers = MutableStateFlow<List<User>>(emptyList())
    val followedUsers: StateFlow<List<User>> = _followedUsers

    private val _chatPartners = MutableStateFlow<List<User>>(emptyList())
    val chatPartners: StateFlow<List<User>> = _chatPartners

    fun clear() {
        _conversations.value = emptyList()
        _messages.value = emptyList()
        _followedUsers.value = emptyList()
        _chatPartners.value = emptyList()
    }

    fun getUserId() = authRepo.getUserId() ?: ""

    fun loadFollowedUsers() {
        viewModelScope.launch {
            val uid = authRepo.getUserId() ?: return@launch
            userRepo.getFollowedUsers(uid).collectLatest {
                _followedUsers.value = it
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            val uid = authRepo.getUserId() ?: return@launch
            chatRepo.getConversations(uid).collectLatest { convs ->
                _conversations.value = convs
                
                // Fetch partner details in parallel for speed
                val partnerDeferred = convs.map { conv ->
                    val partnerId = conv.participants.firstOrNull { it != uid }
                    async {
                        if (partnerId != null) userRepo.getUser(partnerId) else null
                    }
                }
                
                val partners = partnerDeferred.awaitAll().filterNotNull()
                _chatPartners.value = partners
            }
        }
    }

    fun loadMessages(receiverId: String) {
        viewModelScope.launch {
            val senderId = authRepo.getUserId() ?: return@launch
            chatRepo.getMessages(senderId, receiverId).collectLatest {
                _messages.value = it
            }
        }
    }

    fun sendMessage(receiverId: String, text: String) {
        if (text.isEmpty()) return
        viewModelScope.launch {
            try {
                val senderId = authRepo.getUserId() ?: return@launch
                chatRepo.sendMessage(senderId, receiverId, text)
            } catch (e: Exception) {
                android.util.Log.e("ChatVM", "Send message failed: ${e.message}")
            }
        }
    }
}
