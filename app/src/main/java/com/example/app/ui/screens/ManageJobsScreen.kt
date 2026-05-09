package com.example.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.app.model.JobPost
import com.example.app.ui.components.EmptyState
import com.example.app.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageJobsScreen(viewModel: HomeViewModel, onBack: () -> Unit) {
    val jobs by viewModel.userJobs.collectAsState()
    val selectedJobIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        viewModel.loadUserJobs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage My Jobs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedJobIds.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.deleteSelectedJobs(selectedJobIds.toList())
                            selectedJobIds.clear()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (jobs.isEmpty()) {
                EmptyState("You haven't posted any jobs yet.")
            } else {
                Text(
                    text = "Select jobs to delete",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(jobs) { job ->
                        val isSelected = selectedJobIds.contains(job.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    if (isSelected) selectedJobIds.remove(job.id)
                                    else selectedJobIds.add(job.id)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) 
                                               else MaterialTheme.colorScheme.surface
                            ),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.error) else null
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (isSelected) selectedJobIds.remove(job.id)
                                            else selectedJobIds.add(job.id)
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = job.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(job.timestamp)),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = job.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}
