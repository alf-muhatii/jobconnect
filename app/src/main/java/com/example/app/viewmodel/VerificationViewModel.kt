package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.User
import com.example.app.repository.AuthRepository
import com.example.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VerificationViewModel(
    private val userRepo: UserRepository = UserRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _pendingUsers = MutableStateFlow<List<User>>(emptyList())
    val pendingUsers: StateFlow<List<User>> = _pendingUsers

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadPendingRequests() {
        viewModelScope.launch {
            _isLoading.value = true
            userRepo.getPendingVerifications().collectLatest {
                _pendingUsers.value = it
                _isLoading.value = false
            }
        }
    }

    fun searchUsers(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            userRepo.searchUsers().collectLatest { allUsers ->
                _searchResults.value = allUsers.filter { 
                    it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
                }
            }
        }
    }

    fun verifyUser(userId: String, verify: Boolean) {
        viewModelScope.launch {
            userRepo.verifyUser(userId, verify)
        }
    }
}
