package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
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
fun CharacterPortraitImage(
    portraitImagePath: String?,
    characterName: String,
    modifier: Modifier = Modifier,
    isEditable: Boolean = false,
    onImageClick: (() -> Unit)? = null,
    size: CharacterPortraitSize = CharacterPortraitSize.MEDIUM,
    shape: CharacterPortraitShape = CharacterPortraitShape.CIRCLE
) {
    val context = LocalContext.current
    val haptics = rememberHapticFeedback()
    
    val clipShape = when (shape) {
        CharacterPortraitShape.CIRCLE -> CircleShape
        CharacterPortraitShape.ROUNDED_RECTANGLE -> RoundedCornerShape(12.dp)
    }
    
    Box(
        modifier = modifier
            .size(size.size)
            .clip(clipShape)
            .clickable(enabled = isEditable && onImageClick != null) {
                haptics.performHapticFeedback(HapticFeedbackType.LightTap)
                onImageClick?.invoke()
            }    ) {
        if (portraitImagePath != null && File(portraitImagePath).exists()) {
            // Display the actual character portrait with memory caching
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(portraitImagePath)
                    .crossfade(true)
                    .memoryCacheKey("character_${characterName}_$portraitImagePath")
                    .diskCacheKey("character_${characterName}_$portraitImagePath")
                    .allowHardware(false) // Prevent hardware bitmap issues
                    .build(),
                contentDescription = "Portrait of $characterName",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Display placeholder with person icon
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = clipShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isEditable) Icons.Default.Add else Icons.Default.Person,
                        contentDescription = if (isEditable) "Add portrait image" else "Character",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(size.iconSize)
                    )
                    
                    if (size != CharacterPortraitSize.SMALL && isEditable) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add Photo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        
        // Add a subtle overlay for editable portraits
        if (isEditable && onImageClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.1f),
                        clipShape
                    )
            )
        }
    }
}

enum class CharacterPortraitSize(
    val size: androidx.compose.ui.unit.Dp,
    val iconSize: androidx.compose.ui.unit.Dp
) {
    SMALL(40.dp, 16.dp),
    MEDIUM(80.dp, 24.dp),
    LARGE(120.dp, 36.dp),
    EXTRA_LARGE(160.dp, 48.dp)
}

enum class CharacterPortraitShape {
    CIRCLE,
    ROUNDED_RECTANGLE
}

