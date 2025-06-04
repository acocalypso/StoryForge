package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.astronarren.storyforge.ui.components.haptic.rememberHapticFeedback
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType
import java.io.File

@Composable
fun BookCoverImage(
    coverImagePath: String?,
    title: String,
    modifier: Modifier = Modifier,
    isEditable: Boolean = false,
    onImageClick: (() -> Unit)? = null,
    size: BookCoverSize = BookCoverSize.MEDIUM
) {
    val context = LocalContext.current
    val haptics = rememberHapticFeedback()
    
    Box(
        modifier = modifier
            .size(
                width = size.width,
                height = size.height
            )
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = isEditable && onImageClick != null) {
                haptics.performHapticFeedback(HapticFeedbackType.LightTap)
                onImageClick?.invoke()
            }
    ) {
        if (coverImagePath != null && File(coverImagePath).exists()) {
            // Display the actual book cover
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(coverImagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = "Cover of $title",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Display placeholder with book icon
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {                    Icon(
                        imageVector = if (isEditable) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = if (isEditable) "Add cover image" else "Book",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(size.iconSize)
                    )
                    
                    if (size != BookCoverSize.SMALL) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEditable) "Add Cover" else title.take(20),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
        
        // Add a subtle overlay for editable covers
        if (isEditable && onImageClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

enum class BookCoverSize(
    val width: androidx.compose.ui.unit.Dp,
    val height: androidx.compose.ui.unit.Dp,
    val iconSize: androidx.compose.ui.unit.Dp
) {
    SMALL(60.dp, 80.dp, 20.dp),
    MEDIUM(80.dp, 110.dp, 28.dp),
    LARGE(120.dp, 160.dp, 40.dp)
}

