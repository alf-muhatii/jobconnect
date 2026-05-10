package com.example.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.app.ui.components.EmptyState
import com.example.app.ui.components.JobCard
import com.example.app.viewmodel.HomeViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPostJobClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onAuthorClick: (String) -> Unit
) {

    val jobs by viewModel.jobs.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val feedMode by viewModel.feedMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showSavedPopup by remember { mutableStateOf(false) }

    val filteredJobs = remember(jobs, searchQuery, feedMode, currentUser) {
        val baseList = if (feedMode == com.example.app.viewmodel.HomeFeedMode.PRO && currentUser != null) {
            jobs.filter { it.authorId in currentUser!!.following }
        } else {
            jobs
        }

        if (searchQuery.isEmpty()) {
            baseList
        } else {
            baseList.filter {
                it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadJobs()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {

        Scaffold(
            containerColor = Color.Transparent,

            topBar = {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            Text(
                                text = "Discover Jobs",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Find your next opportunity",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(onClick = onNotificationsClick) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search jobs...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                    )
                }
            },

            floatingActionButton = {
                FloatingActionButton(
                    onClick = onPostJobClick,
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = Color.White
                    )
                }
            }

        ) { padding ->

            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {

                if (filteredJobs.isEmpty() && !isRefreshing) {

                    EmptyState(
                        message = if (searchQuery.isEmpty()) {
                            "No jobs available yet."
                        } else {
                            "No jobs found for \"$searchQuery\""
                        }
                    )

                } else {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 8.dp,   // reduced for wider cards
                            end = 8.dp,     // reduced for wider cards
                            top = 8.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        items(filteredJobs) { job ->

                            val isSaved =
                                currentUser?.savedJobs?.contains(job.id) ?: false

                            Box(modifier = Modifier.fillMaxWidth()) {

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
                                    },
                                    onAuthorClick = onAuthorClick,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (isSaved) {
                                    Box(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                            )
                                            .padding(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = showSavedPopup,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(20.dp)
                ) {

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {

                        Column(modifier = Modifier.padding(20.dp)) {

                            Text(
                                text = "Job Saved 🎉",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text("Open your saved jobs?")

                            Spacer(modifier = Modifier.height(18.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {

                                TextButton(onClick = { showSavedPopup = false }) {
                                    Text("Later")
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = {
                                        showSavedPopup = false
                                        onNavigateToSaved()
                                    },
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Open")
                                }
                            }
                        }
                    }
                }

                LaunchedEffect(showSavedPopup) {
                    if (showSavedPopup) {
                        delay(4000)
                        showSavedPopup = false
                    }
                }
            }
        }
    }
}
