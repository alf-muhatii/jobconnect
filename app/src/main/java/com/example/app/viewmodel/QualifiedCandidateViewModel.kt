package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.JobClass
import com.example.app.model.User
import com.example.app.repository.AuthRepository
import com.example.app.repository.ChatRepository
import com.example.app.repository.JobClassRepository
import com.example.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class QualifiedCandidateViewModel(
    private val jobClassRepo: JobClassRepository = JobClassRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val chatRepo: ChatRepository = ChatRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _jobClasses = MutableStateFlow<List<JobClass>>(emptyList())
    val jobClasses: StateFlow<List<JobClass>> = _jobClasses

    private val _candidates = MutableStateFlow<List<User>>(emptyList())
    val candidates: StateFlow<List<User>> = _candidates

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadJobClasses() {
        val userId = authRepo.getUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            jobClassRepo.getJobClasses(userId).collectLatest {
                _jobClasses.value = it
                _isLoading.value = false
            }
        }
    }

    fun createJobClass(title: String) {
        val userId = authRepo.getUserId() ?: return
        viewModelScope.launch {
            try {
                jobClassRepo.createJobClass(title, userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun loadCandidates(jobClassId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            jobClassRepo.getCandidates(jobClassId).collectLatest { ids ->
                userRepo.getUsersByIds(ids).collectLatest { users ->
                    _candidates.value = users
                    _isLoading.value = false
                }
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
                val uid = authRepo.getUserId()
                _searchResults.value = allUsers.filter {
                    it.id != uid && (it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true))
                }
            }
        }
    }

    fun addCandidate(jobClassId: String, userId: String) {
        viewModelScope.launch {
            try {
                jobClassRepo.addCandidateToJobClass(jobClassId, userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeCandidate(jobClassId: String, userId: String) {
        viewModelScope.launch {
            try {
                jobClassRepo.removeCandidateFromJobClass(jobClassId, userId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteJobClass(jobClassId: String) {
        viewModelScope.launch {
            try {
                jobClassRepo.deleteJobClass(jobClassId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun sendBatchMessages(message: String, onComplete: () -> Unit) {
        val senderId = authRepo.getUserId() ?: return
        val receiverIds = _candidates.value.map { it.id }
        
        viewModelScope.launch {
            _isLoading.value = true
            try {
                receiverIds.forEach { receiverId ->
                    chatRepo.sendMessage(senderId, receiverId, message)
                }
                onComplete()
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
