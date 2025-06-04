package de.astronarren.storyforge.ui.screens.characterlist

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.ui.components.haptic.rememberHapticFeedback
import de.astronarren.storyforge.ui.components.CharacterPortraitImage
import de.astronarren.storyforge.ui.components.CharacterPortraitSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterCard(
    character: Character,
    onCharacterClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleMainCharacter: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Stable references to prevent recomposition
    val haptic = rememberHapticFeedback()
    var showDeleteDialog by remember(character.id) { mutableStateOf(false) }
    
    // Stable computed values
    val starIcon = remember(character.isMainCharacter) {
        if (character.isMainCharacter) Icons.Filled.Star else Icons.Outlined.Star
    }
    
    val starContentDescription = remember(character.isMainCharacter) {
        if (character.isMainCharacter) "Remove from main characters" else "Add to main characters"
    }
    
    Card(
        onClick = {
            haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.LightTap)
            onCharacterClick()
        },
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (character.isMainCharacter) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with character name and main character indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Character portrait
                    CharacterPortraitImage(
                        portraitImagePath = character.portraitImagePath,
                        characterName = character.name,
                        size = CharacterPortraitSize.MEDIUM
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = character.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (character.occupation.isNotBlank()) {
                            Text(
                                text = character.occupation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                
                // Main character toggle
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.LightTap)
                        onToggleMainCharacter()
                    }
                ) {
                    Icon(
                        imageVector = starIcon,
                        contentDescription = starContentDescription,
                        tint = if (character.isMainCharacter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Character description
            if (character.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = character.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Character details chips
            if (character.age != null || character.personality.isNotBlank() || character.goals.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    character.age?.let { age ->
                        FilterChip(
                            onClick = { },
                            label = { Text("Age $age", style = MaterialTheme.typography.labelSmall) },
                            selected = false,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                    
                    if (character.personality.isNotBlank()) {
                        FilterChip(
                            onClick = { },
                            label = { 
                                Text(
                                    character.personality.take(15) + if (character.personality.length > 15) "..." else "",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = false,
                            modifier = Modifier.height(28.dp)
                        )
                    }
                }
            }
            
            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.LightTap)
                        onEditClick()
                    }
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.LightTap)
                        showDeleteDialog = true
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Character") },
            text = { 
                Text("Are you sure you want to delete \"${character.name}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.MediumTap)
                        showDeleteDialog = false
                        onDeleteClick()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

