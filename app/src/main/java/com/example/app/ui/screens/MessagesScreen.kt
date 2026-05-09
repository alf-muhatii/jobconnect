package com.example.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app.ui.components.EmptyState
import com.example.app.viewmodel.ChatViewModel

@Composable
fun MessagesScreen(viewModel: ChatViewModel, onChatClick: (String) -> Unit) {
    val chatPartners by viewModel.chatPartners.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadConversations()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Messages",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        if (chatPartners.isEmpty()) {
            EmptyState("No messages yet.")
        } else {
            LazyColumn {
                items(chatPartners) { user ->
                    ListItem(
                        headlineContent = { Text(user.name) },
                        supportingContent = { Text(user.bio, maxLines = 1) },
                        leadingContent = {
                            AsyncImage(
                                model = user.profilePictureUrl.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" },
                                contentDescription = null,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        },
                        modifier = Modifier.clickable { onChatClick(user.id) }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
