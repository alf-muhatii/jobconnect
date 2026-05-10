package com.example.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app.ui.components.UserCard
import com.example.app.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onMessageClick: (String) -> Unit
) {

    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedUser by remember { mutableStateOf<com.example.app.model.User?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    val filtered = searchResults.filter {
        it.id != currentUser?.id
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {

                // 🔥 Header
                Text(
                    text = "Find People",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Search and connect with others",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔥 Search bar (cool floating style)
                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.searchUsers(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp)),
                    placeholder = { Text("Search users...") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 🔥 Results container
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
                        .padding(top = 8.dp)
                ) {

                    if (filtered.isEmpty() && query.isNotEmpty()) {

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found for \"$query\"",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    } else {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                top = 8.dp,
                                bottom = 120.dp // Space for floating bar
                            ),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            items(filtered) { user ->

                                val isFollowing =
                                    currentUser?.following?.contains(user.id) ?: false

                                UserCard(
                                    user = user,
                                    isFollowing = isFollowing,
                                    onFollowClick = {
                                        viewModel.followUser(user.id)
                                    },
                                    onClick = {
                                        selectedUser = user
                                        showDialog = true
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 🔥 Modern dialog
    if (showDialog && selectedUser != null) {

        val isFollowing =
            currentUser?.following?.contains(selectedUser!!.id) ?: false

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = selectedUser!!.name,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (isFollowing)
                        "Start a conversation?"
                    else
                        "Follow this user to message them"
                )
            },
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
                TextButton(
                    onClick = {
                        viewModel.followUser(selectedUser!!.id)
                    }
                ) {
                    Text(if (isFollowing) "Unfollow" else "Follow")
                }
            }
        )
    }
}
