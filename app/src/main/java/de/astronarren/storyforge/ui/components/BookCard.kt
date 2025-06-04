package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.data.database.entities.Book
import de.astronarren.storyforge.ui.components.haptic.rememberHapticFeedback
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit = {},
    onDuplicate: () -> Unit = {},
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {    val haptics = rememberHapticFeedback()
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }
      Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                haptics.performHapticFeedback(HapticFeedbackType.LightTap)
                onClick() 
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BookCoverImage(
                        coverImagePath = book.coverImagePath,
                        title = book.title,
                        size = BookCoverSize.SMALL,
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        if (book.author.isNotBlank()) {
                            Text(
                                text = "by ${book.author}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }                }
                  
                Row {
                    IconButton(onClick = { 
                        haptics.performHapticFeedback(HapticFeedbackType.LightTap)
                        onToggleFavorite() 
                    }) {
                        Icon(
                            imageVector = if (book.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = if (book.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (book.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                      Box {
                        IconButton(onClick = { 
                            haptics.performHapticFeedback(HapticFeedbackType.LightTap)
                            showMenu = true 
                        }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        
                        DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {                        DropdownMenuItem(
                            text = { Text("Duplicate") },                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.MediumTap)
                                showDuplicateDialog = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            }                        )
                        
                        DropdownMenuItem(
                            text = { Text("Archive") },
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.MediumTap)
                                onArchive()
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Archive, contentDescription = null)
                            }
                        )
                        
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.MediumTap)
                                showDeleteDialog = true
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
                }
            }
            
            if (book.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Genre and Target Word Count Row
            if (book.genre.isNotBlank() || book.targetWordCount != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (book.genre.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Text(
                                text = book.genre,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    if (book.targetWordCount != null && book.targetWordCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            modifier = Modifier.wrapContentSize()
                        ) {
                            Text(
                                text = "Target: ${book.targetWordCount} words",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${book.wordCount} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Text(
                    text = "Updated ${dateFormat.format(Date(book.updatedAt))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Book") },
            text = { 
                Text("Are you sure you want to delete \"${book.title}\"? This action cannot be undone.") 
            },            confirmButton = {
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.Error)
                        onDelete()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.LightTap)
                    showDeleteDialog = false 
                }) {
                    Text("Cancel")
                }            }
        )
    }
    
    // Duplicate confirmation dialog
    if (showDuplicateDialog) {
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            title = { Text("Duplicate Book") },
            text = { 
                Text("Create a copy of \"${book.title}\"? The duplicate will have the same content but will be marked as a copy.") 
            },
            confirmButton = {
                Button(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.Success)
                        onDuplicate()
                        showDuplicateDialog = false
                    }
                ) {
                    Text("Duplicate")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    haptics.performHapticFeedback(HapticFeedbackType.LightTap)
                    showDuplicateDialog = false 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

