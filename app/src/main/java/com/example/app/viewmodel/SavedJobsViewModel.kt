package com.example.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.JobPost
import com.example.app.repository.AuthRepository
import com.example.app.repository.JobRepository
import com.example.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SavedJobsViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository(),
    private val jobRepo: JobRepository = JobRepository()
) : ViewModel() {

    private val _savedJobs = MutableStateFlow<List<JobPost>>(emptyList())
    val savedJobs: StateFlow<List<JobPost>> = _savedJobs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadSavedJobs() {
        val userId = authRepo.getUserId() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            userRepo.searchUsers().collectLatest { allUsers ->
                val currentUser = allUsers.find { it.id == userId }
                val savedIds = currentUser?.savedJobs ?: emptyList()
                jobRepo.getJobsByIds(savedIds).collectLatest { jobs ->
                    _savedJobs.value = jobs
                    _isLoading.value = false
                }
            }
        }
    }

    fun unsaveJob(jobId: String) {
        val userId = authRepo.getUserId() ?: return
        viewModelScope.launch {
            try {
                userRepo.unsaveJob(userId, jobId)
                loadSavedJobs()
            } catch (e: Exception) {
                // handle error
            }
        }
    }
}
