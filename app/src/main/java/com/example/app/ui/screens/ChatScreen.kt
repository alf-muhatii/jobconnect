package com.example.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    // Dialogs
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

    // 🔥 Background
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

        Scaffold(
            containerColor = Color.Transparent,

            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Chat",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    actions = {
                        AssistChip(
                            onClick = {
                                when {
                                    jobClasses.isEmpty() -> showCreateClassDialog = true
                                    jobClasses.size == 1 ->
                                        qualViewModel.addCandidate(jobClasses[0].id, receiverId)
                                    else -> showClassSelection = true
                                }
                            },
                            label = { Text("Qualified") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                    }
                )
            },

            bottomBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Message...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    IconButton(
                        onClick = {
                            if (text.isNotBlank()) {
                                viewModel.sendMessage(receiverId, text)
                                text = ""
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

        ) { padding ->

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 10.dp),
                contentPadding = PaddingValues(vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(messages) { message ->
                    MessageBubble(
                        message = message,
                        isCurrentUser = message.senderId == currentUserId
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {

    val bubbleColor =
        if (isCurrentUser)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surface

    val textColor =
        if (isCurrentUser) Color.White
        else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment =
        if (isCurrentUser) Alignment.End else Alignment.Start
    ) {

        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isCurrentUser) 18.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 18.dp
            ),
            color = bubbleColor,
            tonalElevation = 2.dp
        ) {

            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(message.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
