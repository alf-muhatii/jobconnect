package com.example.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.model.User
import com.example.app.repository.AuthRepository
import com.example.app.repository.StorageRepository
import com.example.app.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val authRepo: AuthRepository,
    private val storageRepo: StorageRepository
) : ViewModel() {

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating

    private val _userList = MutableStateFlow<List<User>>(emptyList())
    val userList: StateFlow<List<User>> = _userList

    fun clear() {
        _userProfile.value = null
        _userList.value = emptyList()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                val uid = authRepo.getUserId() ?: return@launch
                val user = userRepo.getUser(uid)
                _userProfile.value = user
            } catch (e: Exception) {
                android.util.Log.e("ProfileVM", "Error loading profile: ${e.message}")
            }
        }
    }

    fun loadFollowers(userId: String) {
        viewModelScope.launch {
            val user = userRepo.getUser(userId)
            val followerIds = user?.followers ?: emptyList()
            userRepo.getUsersByIds(followerIds).collectLatest {
                _userList.value = it
            }
        }
    }

    fun loadFollowing(userId: String) {
        viewModelScope.launch {
            val user = userRepo.getUser(userId)
            val followingIds = user?.following ?: emptyList()
            userRepo.getUsersByIds(followingIds).collectLatest {
                _userList.value = it
            }
        }
    }

    fun applyForVerification() {
        val userId = authRepo.getUserId() ?: return
        viewModelScope.launch {
            try {
                userRepo.applyForVerification(userId)
                loadProfile() // Refresh local state
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    suspend fun updateProfile(name: String, bio: String, imageUri: Uri?): Boolean {
        _isUpdating.value = true
        try {
            val uid = authRepo.getUserId() ?: return false
            val currentUser = _userProfile.value ?: return false
            
            var imageUrl = currentUser.profilePictureUrl
            if (imageUri != null) {
                imageUrl = storageRepo.uploadProfilePicture(imageUri)
            }

            val updatedUser = currentUser.copy(
                name = name,
                searchName = name.lowercase(),
                bio = bio,
                profilePictureUrl = imageUrl
            )
            
            // Update Auth profile
            val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                displayName = name
            }
            authRepo.getCurrentUser()?.updateProfile(profileUpdates)?.await()

            userRepo.saveUser(updatedUser)
            _userProfile.value = updatedUser
            return true
        } catch (e: Exception) {
            android.util.Log.e("ProfileVM", "Update failed: ${e.message}")
            return false
        } finally {
            _isUpdating.value = false
        }
    }
}
