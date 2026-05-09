package com.example.app.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.app.model.JobPost
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JobCard(
    job: JobPost,
    isSaved: Boolean = false,
    onSaveClick: () -> Unit = {},
    showShare: Boolean = true
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = job.authorProfilePicture.ifEmpty { "https://cdn-icons-png.flaticon.com/512/149/149071.png" },
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = job.authorName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(job.timestamp)),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Save Button
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Job",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share Button
                if (showShare) {
                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Check out this job: ${job.title}")
                            putExtra(Intent.EXTRA_TEXT, "Job Title: ${job.title}\n\nDescription: ${job.description}\n\nPosted by: ${job.authorName}\n\nCheck it out on KaziKenya!")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Job via"))
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Job")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = job.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = job.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
