package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.components.UserCard
import com.example.app.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel, onMessageClick: (String) -> Unit) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    
    var selectedUser by remember { mutableStateOf<com.example.app.model.User?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { 
                query = it
                viewModel.searchUsers(it)
            },
            label = { Text("Search users by name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (searchResults.isEmpty() && query.isNotEmpty()) {
            Text(text = "No users found matching \"$query\"", modifier = Modifier.padding(16.dp))
        }
        LazyColumn {
            items(searchResults) { user ->
                if (user.id != currentUser?.id) {
                    val isFollowing = currentUser?.following?.contains(user.id) ?: false
                    UserCard(
                        user = user,
                        isFollowing = isFollowing,
                        onFollowClick = { viewModel.followUser(user.id) },
                        onClick = {
                            selectedUser = user
                            showDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showDialog && selectedUser != null) {
        val isFollowing = currentUser?.following?.contains(selectedUser!!.id) ?: false
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(selectedUser!!.name) },
            text = { Text(if (isFollowing) "What would you like to do?" else "Follow first to message") },
            confirmButton = {
                Button(
                    enabled = isFollowing,
                    onClick = {
                        showDialog = false
                        onMessageClick(selectedUser!!.id)
                    }
                ) {
                    Text("Message")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.followUser(selectedUser!!.id)
                }) {
                    Text(if (isFollowing) "Unfollow" else "Follow")
                }
            }
        )
    }
}
