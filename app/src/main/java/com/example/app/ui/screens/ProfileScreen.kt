package com.example.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app.ui.components.ProfileImageWithBadge
import com.example.app.ui.components.VerifiedLabel
import com.example.app.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    userId: String? = null, // If null, show current user's profile
    profileViewModel: ProfileViewModel,
    onEditProfileClick: () -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val isOwnProfile = userId == null
    val user by (if (isOwnProfile) profileViewModel.userProfile else profileViewModel.otherUser).collectAsState()
    
    var showNameDialog by remember { mutableStateOf(false) }
    var newName by remember(user) { mutableStateOf(user?.name ?: "") }

    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        profileViewModel.loadProfile(userId)
    }

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
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            // Header with Settings (Only on own profile)
            if (isOwnProfile) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 🔥 PROFILE CARD
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    ProfileImageWithBadge(
                        imageUrl = user?.profilePictureUrl,
                        isVerified = user?.isVerified ?: false,
                        size = 110.dp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = user?.name ?: "Loading...",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (isOwnProfile) {
                            IconButton(onClick = { showNameDialog = true }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    if (user?.isVerified == true) {
                        VerifiedLabel(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Text(
                        text = user?.bio ?: "No bio set.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🔥 STATS CARDS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Followers",
                    value = user?.followers?.size ?: 0,
                    onClick = { user?.let { onFollowersClick(it.id) } },
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Following",
                    value = user?.following?.size ?: 0,
                    onClick = { user?.let { onFollowingClick(it.id) } },
                    modifier = Modifier.weight(1f)
                )
            }

            if (isOwnProfile) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onEditProfileClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile")
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // 🔥 NAME DIALOG
    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    scope.launch {
                        profileViewModel.updateProfile(
                            newName,
                            user?.bio ?: "",
                            null
                        )
                        showNameDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
