package com.example.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.JobPost
import com.example.app.model.User
import com.example.app.repository.AuthRepository
import com.example.app.repository.JobRepository
import com.example.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeViewModel(
    private val jobRepo: JobRepository = JobRepository(),
    private val authRepo: AuthRepository = AuthRepository(),
    private val userRepo: UserRepository = UserRepository()
) : ViewModel() {

    private val _jobs = MutableStateFlow<List<JobPost>>(emptyList())
    val jobs: StateFlow<List<JobPost>> = _jobs

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _userJobs = MutableStateFlow<List<JobPost>>(emptyList())
    val userJobs: StateFlow<List<JobPost>> = _userJobs

    init {
        loadJobs()
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val userId = authRepo.getUserId() ?: return
        viewModelScope.launch {
            userRepo.searchUsers().collectLatest { allUsers ->
                _currentUser.value = allUsers.find { it.id == userId }
            }
        }
    }

    fun saveJob(jobId: String) {
        val userId = authRepo.getUserId() ?: return
        viewModelScope.launch {
            try {
                userRepo.saveJob(userId, jobId)
            } catch (e: Exception) {
                Log.e("HomeVM", "Failed to save job: ${e.message}")
            }
        }
    }

    fun unsaveJob(jobId: String) {
        val userId = authRepo.getUserId() ?: return
        viewModelScope.launch {
            try {
                userRepo.unsaveJob(userId, jobId)
            } catch (e: Exception) {
                Log.e("HomeVM", "Failed to unsave job: ${e.message}")
            }
        }
    }

    fun loadUserJobs() {
        viewModelScope.launch {
            val userId = authRepo.getUserId() ?: return@launch
            jobRepo.getUserJobs(userId).collectLatest {
                _userJobs.value = it
            }
        }
    }

    fun deleteSelectedJobs(jobIds: List<String>) {
        viewModelScope.launch {
            try {
                jobRepo.deleteJobs(jobIds)
            } catch (e: Exception) {
                Log.e("HomeVM", "Failed to delete jobs: ${e.message}")
            }
        }
    }

    fun loadJobs() {
        viewModelScope.launch {
            _isRefreshing.value = true
            jobRepo.getJobPosts().collectLatest {
                _jobs.value = it
                _isRefreshing.value = false
            }
        }
    }

    fun postJob(title: String, description: String) {
        viewModelScope.launch {
            val userId = authRepo.getUserId() ?: return@launch
            val user = userRepo.getUser(userId)
            
            // If user isn't in Firestore yet, use Auth display name as fallback
            val authorName = user?.name ?: authRepo.getCurrentUser()?.displayName ?: "User"
            val profilePic = user?.profilePictureUrl ?: ""

            val job = JobPost(
                authorId = userId,
                authorName = authorName,
                authorProfilePicture = profilePic,
                title = title,
                description = description,
                timestamp = System.currentTimeMillis()
            )
            
            try {
                jobRepo.postJob(job)
                Log.d("HomeVM", "Job posted successfully")
            } catch (e: Exception) {
                Log.e("HomeVM", "Failed to post job: ${e.message}")
            }
        }
    }
}
