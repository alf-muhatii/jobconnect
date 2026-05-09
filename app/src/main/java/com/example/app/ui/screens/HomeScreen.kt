package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.components.EmptyState
import com.example.app.ui.components.JobCard
import com.example.app.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel, onPostJobClick: () -> Unit) {
    val jobs by viewModel.jobs.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

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
                // By not using statusBarsPadding here, it stays at the absolute top
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onPostJobClick) {
                Icon(Icons.Default.Add, contentDescription = "Post Job")
            }
        }
    ) { padding ->
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
                        JobCard(job = job)
                    }
                }
            }
        }
    }
}
