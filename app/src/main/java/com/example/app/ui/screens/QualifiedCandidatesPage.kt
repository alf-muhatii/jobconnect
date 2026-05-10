package com.example.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app.model.User
import com.example.app.ui.components.EmptyState
import com.example.app.ui.components.LoadingScreen
import com.example.app.ui.components.ProfileImageWithBadge
import com.example.app.viewmodel.QualifiedCandidateViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QualifiedCandidatesPage(
    jobClassId: String,
    jobClassTitle: String,
    viewModel: QualifiedCandidateViewModel,
    onBack: () -> Unit,
    onSendApprovalClick: (String) -> Unit
) {
    val candidates by viewModel.candidates.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForAdd by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(jobClassId) {
        viewModel.loadCandidates(jobClassId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(jobClassTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onSendApprovalClick(jobClassId) },
                icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
                text = { Text("Send Letter of Approval") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Add Qualified Candidate",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchUsers(it)
                },
                label = { Text("Search users...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Live Search Results
            if (searchQuery.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(searchResults) { user ->
                            ListItem(
                                headlineContent = { Text(user.name) },
                                supportingContent = { Text(user.email) },
                                modifier = Modifier.clickable {
                                    selectedUserForAdd = user
                                    searchQuery = "" // Clear search after selection
                                }
                            )
                        }
                    }
                }
            }
            
            // Selected User for adding
            selectedUserForAdd?.let { user ->
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Selected: ${user.name}", modifier = Modifier.weight(1f))
                    Button(onClick = {
                        viewModel.addCandidate(jobClassId, user.id)
                        selectedUserForAdd = null
                    }) {
                        Text("Add")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Qualified Candidates List",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (isLoading) {
                LoadingScreen()
            } else if (candidates.isEmpty()) {
                EmptyState("No candidates added to this class yet.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(candidates) { user ->
                        CandidateItem(
                            user = user,
                            onRemoveClick = { viewModel.removeCandidate(jobClassId, user.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) } // Space for FAB
                }
            }
        }
    }
}

@Composable
fun CandidateItem(user: User, onRemoveClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileImageWithBadge(
                imageUrl = user.profilePictureUrl,
                isVerified = user.isVerified,
                size = 50.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = user.email, style = MaterialTheme.typography.bodySmall)
                if (user.bio.isNotEmpty()) {
                    Text(text = user.bio, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }
            IconButton(onClick = onRemoveClick) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
