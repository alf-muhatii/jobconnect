package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.components.EmptyState
import com.example.app.ui.components.JobCard
import com.example.app.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel, 
    onPostJobClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onNavigateToSaved: () -> Unit
) {
    val jobs by viewModel.jobs.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showSavedPopup by remember { mutableStateOf(false) }

    val filteredJobs = remember(jobs, searchQuery) {
        if (searchQuery.isEmpty()) jobs
        else jobs.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(Unit) {
        viewModel.loadJobs()
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 3.dp,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp, start = 8.dp),
                        placeholder = { Text("Search jobs by title...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onPostJobClick) {
                Icon(Icons.Default.Add, contentDescription = "Post Job")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (filteredJobs.isEmpty() && !isRefreshing) {
                    val message = if (searchQuery.isEmpty()) "No jobs posted yet." 
                                 else "No jobs found matching \"$searchQuery\""
                    EmptyState(message)
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredJobs) { job ->
                            val isSaved = currentUser?.savedJobs?.contains(job.id) ?: false
                            JobCard(
                                job = job,
                                isSaved = isSaved,
                                onSaveClick = {
                                    if (isSaved) {
                                        viewModel.unsaveJob(job.id)
                                    } else {
                                        viewModel.saveJob(job.id)
                                        showSavedPopup = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Floating Card Popup
            if (showSavedPopup) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
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
