package com.example.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app.viewmodel.ProfileViewModel
import com.example.app.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    authViewModel: AuthViewModel,
    onEditProfileClick: () -> Unit,
    onLogoutSuccess: () -> Unit,
    onPostJobClick: () -> Unit,
    onFollowersClick: (String) -> Unit,
    onFollowingClick: (String) -> Unit,
    onManageJobsClick: () -> Unit,
    onQualifiedCandidateClick: () -> Unit
) {
    val user by profileViewModel.userProfile.collectAsState()
    var showNameDialog by remember { mutableStateOf(false) }
    var newName by remember(user) { mutableStateOf(user?.name ?: "") }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        profileViewModel.loadProfile()
    }

    if (showNameDialog) {
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Edit Account Name") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    coroutineScope.launch {
                        profileViewModel.updateProfile(newName, user?.bio ?: "", null)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = if (user?.profilePictureUrl.isNullOrEmpty()) 
                "https://cdn-icons-png.flaticon.com/512/149/149071.png" 
            else user?.profilePictureUrl,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = user?.name ?: "Loading...", 
                style = MaterialTheme.typography.headlineMedium
            )
            IconButton(onClick = { showNameDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Name",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Account Bio
        Text(
            text = user?.bio ?: "No bio set.", 
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Followers and Following Counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { user?.let { onFollowersClick(it.id) } }
            ) {
                Text(text = "${user?.followers?.size ?: 0}", style = MaterialTheme.typography.titleLarge)
                Text(text = "Followers", style = MaterialTheme.typography.bodySmall)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { user?.let { onFollowingClick(it.id) } }
            ) {
                Text(text = "${user?.following?.size ?: 0}", style = MaterialTheme.typography.titleLarge)
                Text(text = "Following", style = MaterialTheme.typography.bodySmall)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        
        // Primary Action: Post a Job
        Button(
            onClick = onPostJobClick, 
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Post a New Job")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Manage Jobs Action
        OutlinedButton(
            onClick = onManageJobsClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Manage My Jobs")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Qualified Candidate Action
        OutlinedButton(
            onClick = onQualifiedCandidateClick,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("Qualified Candidate")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Secondary Action: Edit Profile
        OutlinedButton(
            onClick = onEditProfileClick, 
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Edit Profile")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Logout Button
        Button(
            onClick = {
                authViewModel.logout {
                    onLogoutSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(Icons.Default.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout Account")
        }
    }
}
