package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app.model.Notification
import com.example.app.model.NotificationType
import com.example.app.ui.components.EmptyState
import com.example.app.ui.components.LoadingScreen
import com.example.app.viewmodel.NotificationViewModel
import com.example.app.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationViewModel,
    homeViewModel: HomeViewModel,
    onBack: () -> Unit,
    onNotificationClick: (String, NotificationType) -> Unit,
    onNavigateToSaved: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by homeViewModel.currentUser.collectAsState()
    var showSavedPopup by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                LoadingScreen()
            } else if (notifications.isEmpty()) {
                EmptyState("No new notifications.")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notifications) { notification ->
                        val isSaved = currentUser?.savedJobs?.contains(notification.id) ?: false
                        NotificationCard(
                            notification = notification,
                            isSaved = isSaved,
                            onSaveClick = {
                                if (notification.type == NotificationType.JOB_POST) {
                                    if (isSaved) homeViewModel.unsaveJob(notification.id)
                                    else {
                                        homeViewModel.saveJob(notification.id)
                                        showSavedPopup = true
                                    }
                                }
                            },
                            onClick = {
                                onNotificationClick(notification.fromUserId, notification.type)
                            }
                        )
                    }
                }
            }

            if (showSavedPopup) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Job saved! Do you want to see your saved jobs?", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { showSavedPopup = false }) {
                                Text("No")
                            }
                            Button(onClick = {
                                showSavedPopup = false
                                onNavigateToSaved()
                            }) {
                                Text("Yes")
                            }
                        }
                    }
                }
                LaunchedEffect(showSavedPopup) {
                    if (showSavedPopup) {
                        kotlinx.coroutines.delay(5000)
                        showSavedPopup = false
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification, 
    isSaved: Boolean,
    onSaveClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = notification.fromUserProfilePic.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" },
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.fromUserName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = notification.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2
                )
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(notification.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (notification.type == NotificationType.JOB_POST) {
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Job",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
