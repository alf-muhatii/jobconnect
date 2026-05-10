package com.example.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.components.EmptyState
import com.example.app.ui.components.ProfileImageWithBadge
import com.example.app.viewmodel.ProfileViewModel

@Composable
fun UserListScreen(
    title: String,
    viewModel: ProfileViewModel,
    onUserClick: (String) -> Unit
) {
    val users by viewModel.userList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        if (users.isEmpty()) {
            EmptyState("No users found.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp) // Space for floating bar
            ) {
                items(users) { user ->
                    ListItem(
                        headlineContent = { Text(user.name) },
                        supportingContent = { Text(user.bio, maxLines = 1) },
                        leadingContent = {
                            ProfileImageWithBadge(
                                imageUrl = user.profilePictureUrl,
                                isVerified = user.isVerified,
                                size = 50.dp
                            )
                        },
                        modifier = Modifier.clickable { onUserClick(user.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
