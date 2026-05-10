package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.app.viewmodel.AuthViewModel
import com.example.app.viewmodel.ProfileViewModel
import com.example.app.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel,
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPostJobClick: () -> Unit,
    onManageJobsClick: () -> Unit,
    onQualifiedCandidateClick: () -> Unit,
    onSavedJobsClick: () -> Unit,
    onVerifyAccountsClick: () -> Unit,
    onLogoutSuccess: () -> Unit
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val user by profileViewModel.userProfile.collectAsState()
    
    val isAlf = user?.email == "muhatialf@gmail.com"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Dark Mode Toggle
            SettingsToggleItem(
                title = "Dark Mode",
                icon = Icons.Default.DarkMode,
                checked = isDarkMode,
                onCheckedChange = { themeViewModel.setDarkMode(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Action Buttons
            SettingsActionItem(
                title = "Edit Profile",
                icon = Icons.Default.Edit,
                onClick = onEditProfileClick
            )

            // Verification Section
            if (isAlf) {
                SettingsActionItem(
                    title = "Verify Accounts",
                    icon = Icons.Default.Verified,
                    onClick = onVerifyAccountsClick
                )
            } else if (user?.isVerified == false) {
                SettingsActionItem(
                    title = if (user?.verificationRequested == true) "Verification Pending..." else "Apply for Verification",
                    icon = Icons.Default.Verified,
                    onClick = { if (user?.verificationRequested == false) profileViewModel.applyForVerification() }
                )
            }

            SettingsActionItem(
                title = "Post a New Job",
                icon = Icons.Default.Work,
                onClick = onPostJobClick
            )

            SettingsActionItem(
                title = "Manage My Jobs",
                icon = Icons.Default.WorkOutline,
                onClick = onManageJobsClick
            )

            SettingsActionItem(
                title = "Qualified Candidates",
                icon = Icons.Default.Star,
                onClick = onQualifiedCandidateClick
            )

            SettingsActionItem(
                title = "Saved Jobs",
                icon = Icons.Default.Bookmark,
                onClick = onSavedJobsClick
            )

            Spacer(modifier = Modifier.height(24.dp))

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
}

@Composable
fun SettingsActionItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}
