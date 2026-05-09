package com.example.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.app.ui.components.EmptyState
import com.example.app.ui.components.JobCard
import com.example.app.ui.components.LoadingScreen
import com.example.app.viewmodel.SavedJobsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedJobsScreen(
    viewModel: SavedJobsViewModel,
    onBack: () -> Unit
) {
    val savedJobs by viewModel.savedJobs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSavedJobs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Jobs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingScreen()
        } else if (savedJobs.isEmpty()) {
            EmptyState("You haven't saved any jobs yet.")
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(savedJobs) { job ->
                    JobCard(
                        job = job,
                        isSaved = true,
                        onSaveClick = { viewModel.unsaveJob(job.id) },
                        showShare = true
                    )
                }
            }
        }
    }
}
