package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app.ui.components.LoadingScreen
import com.example.app.viewmodel.QualifiedCandidateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendApprovalLetterScreen(
    jobClassId: String,
    viewModel: QualifiedCandidateViewModel,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val candidates by viewModel.candidates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var messageText by remember { mutableStateOf("") }

    // Load candidates again to be sure
    LaunchedEffect(jobClassId) {
        viewModel.loadCandidates(jobClassId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send Approval Letter") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Sending to ${candidates.size} candidates",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("Compose Message") },
                    placeholder = { Text("Enter interview invitation or approval letter details here...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    minLines = 10
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            viewModel.sendBatchMessages(messageText) {
                                onSuccess()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = messageText.isNotBlank() && candidates.isNotEmpty()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Send Message to All")
                }
            }
        }
    }
}
