package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
fun ManageJobsScreen(viewModel: HomeViewModel, onBack: () -> Unit) {
    val userJobs by viewModel.userJobs.collectAsState()
    val selectedJobs = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        viewModel.loadUserJobs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage My Jobs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (selectedJobs.isNotEmpty()) {
                        IconButton(onClick = {
                            viewModel.deleteSelectedJobs(selectedJobs.toList())
                            selectedJobs.clear()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { padding ->
        if (userJobs.isEmpty()) {
            EmptyState("You haven't posted any jobs yet.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = 120.dp, // Space for floating bar
                    start = 8.dp,
                    end = 8.dp
                )
            ) {
                items(userJobs) { job ->
                    val isSelected = selectedJobs.contains(job.id)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                if (it) selectedJobs.add(job.id) else selectedJobs.remove(job.id)
                            }
                        )
                        JobCard(
                            job = job,
                            showShare = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
