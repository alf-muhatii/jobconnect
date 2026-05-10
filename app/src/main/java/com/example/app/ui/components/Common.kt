package com.example.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun ProfileImageWithBadge(
    imageUrl: String?,
    isVerified: Boolean,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.size(size)) {
        AsyncImage(
            model = if (imageUrl.isNullOrEmpty())
                "https://cdn-icons-png.flaticon.com/512/149/149071.png"
            else imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        if (isVerified) {
            val badgeSize = size * 0.28f
            Surface(
                modifier = Modifier
                    .size(badgeSize)
                    .align(Alignment.BottomEnd),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF1DA1F2),
                        modifier = Modifier.fillMaxSize(0.9f)
                    )
                }
            }
        }
    }
}

@Composable
fun VerifiedLabel(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color(0xFF1DA1F2).copy(alpha = 0.1f), MaterialTheme.shapes.small)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF1DA1F2),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "This account is verified",
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF1DA1F2),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun KaziKenyaLogo(modifier: Modifier = Modifier, size: Int = 80) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Work,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size((size * 0.6).dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "KaziKenya",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}
