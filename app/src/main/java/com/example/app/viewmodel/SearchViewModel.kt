package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.User
import com.example.app.repository.AuthRepository
import com.example.app.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SearchViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private var searchJob: Job? = null

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val uid = authRepo.getUserId() ?: return@launch
            _currentUser.value = userRepo.getUser(uid)
        }
    }

    fun searchUsers(query: String) {
        searchJob?.cancel()
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) 
            // Fetch ALL users and filter locally to ensure everyone appears regardless of case
            userRepo.searchUsers().collectLatest { allUsers ->
                val uid = authRepo.getUserId()
                _searchResults.value = allUsers.filter { 
                    it.id != uid && it.name.contains(query, ignoreCase = true)
                }
            }
        }
    }

    fun followUser(targetUserId: String) {
        viewModelScope.launch {
            try {
                val currentUserId = authRepo.getUserId() ?: return@launch
                val isFollowing = _currentUser.value?.following?.contains(targetUserId) ?: false
                
                if (isFollowing) {
                    userRepo.unfollowUser(currentUserId, targetUserId)
                } else {
                    userRepo.followUser(currentUserId, targetUserId)
                }
                loadCurrentUser() // Refresh local state
            } catch (e: Exception) {
                android.util.Log.e("SearchVM", "Follow/Unfollow failed: ${e.message}")
            }
        }
    }
}
