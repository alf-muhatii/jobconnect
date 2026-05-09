package com.example.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.app.model.Message
import com.example.app.viewmodel.ChatViewModel
import com.example.app.viewmodel.QualifiedCandidateViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel, 
    qualViewModel: QualifiedCandidateViewModel,
    receiverId: String, 
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val jobClasses by qualViewModel.jobClasses.collectAsState()
    val currentUserId = viewModel.getUserId()
    var text by remember { mutableStateOf("") }
    
    var showClassSelection by remember { mutableStateOf(false) }
    var showCreateClassDialog by remember { mutableStateOf(false) }
    var newClassTitle by remember { mutableStateOf("") }

    LaunchedEffect(receiverId) {
        viewModel.loadMessages(receiverId)
        qualViewModel.loadJobClasses()
    }

    if (showClassSelection) {
        AlertDialog(
            onDismissRequest = { showClassSelection = false },
            title = { Text("Select Job Class") },
            text = {
                Column {
                    jobClasses.forEach { jobClass ->
                        ListItem(
                            headlineContent = { Text(jobClass.title) },
                            modifier = Modifier.clickable {
                                qualViewModel.addCandidate(jobClass.id, receiverId)
                                showClassSelection = false
                            }
                        )
                        HorizontalDivider()
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showClassSelection = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCreateClassDialog) {
        AlertDialog(
            onDismissRequest = { showCreateClassDialog = false },
            title = { Text("No Job Classes Found") },
            text = {
                Column {
                    Text("You need at least one job class to add a qualified candidate. Create one now?")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newClassTitle,
                        onValueChange = { newClassTitle = it },
                        label = { Text("Job Class Title") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newClassTitle.isNotBlank()) {
                        qualViewModel.createJobClass(newClassTitle)
                        showCreateClassDialog = false
                        // Ideally we'd wait for creation and then add, but let's keep it simple
                    }
                }) {
                    Text("Create & Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateClassDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            when {
                                jobClasses.isEmpty() -> showCreateClassDialog = true
                                jobClasses.size == 1 -> qualViewModel.addCandidate(jobClasses[0].id, receiverId)
                                else -> showClassSelection = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Qualified", style = MaterialTheme.typography.labelLarge)
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                IconButton(onClick = {
                    viewModel.sendMessage(receiverId, text)
                    text = ""
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            reverseLayout = false
        ) {
            items(messages) { message ->
                MessageBubble(message = message, isCurrentUser = message.senderId == currentUserId)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(8.dp),
                color = if (isCurrentUser) Color.White else Color.Black
            )
        }
        Text(
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
            style = MaterialTheme.typography.bodySmall
        )
    }
}
