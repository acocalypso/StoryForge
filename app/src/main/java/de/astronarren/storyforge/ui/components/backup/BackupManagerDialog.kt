package de.astronarren.storyforge.ui.components.backup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.astronarren.storyforge.data.database.entities.ChapterBackup
import de.astronarren.storyforge.data.database.entities.BackupType
import java.text.SimpleDateFormat
import java.util.*

/**
 * Dialog for managing chapter backups with restore/delete functionality
 */
@Composable
fun BackupManagerDialog(
    backups: List<ChapterBackup>,
    onCreateManualBackup: () -> Unit,
    onRestoreBackup: (ChapterBackup) -> Unit,
    onDeleteBackup: (ChapterBackup) -> Unit,
    onDismiss: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Backup Manager",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Create backup button
                Button(
                    onClick = { showCreateDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Create Manual Backup")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Backups list
                Text(
                    text = "Available Backups (${backups.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (backups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No backups available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(backups) { backup ->
                            BackupCard(
                                backup = backup,
                                onRestore = { onRestoreBackup(backup) },
                                onDelete = { onDeleteBackup(backup) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Create backup confirmation dialog
    if (showCreateDialog) {
        CreateBackupDialog(
            onConfirm = {
                onCreateManualBackup()
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false }
        )
    }
}

@Composable
private fun BackupCard(
    backup: ChapterBackup,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Backup type and label
                    Row(verticalAlignment = Alignment.CenterVertically) {                        Icon(
                            imageVector = when (backup.backupType) {
                                BackupType.AUTO -> Icons.Default.Schedule
                                BackupType.MANUAL -> Icons.Default.PersonPin
                                BackupType.PRE_EDIT -> Icons.Default.Edit
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when (backup.backupType) {
                                BackupType.AUTO -> MaterialTheme.colorScheme.primary
                                BackupType.MANUAL -> MaterialTheme.colorScheme.secondary
                                BackupType.PRE_EDIT -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = backup.description ?: when (backup.backupType) {
                                BackupType.AUTO -> "Auto Backup"
                                BackupType.MANUAL -> "Manual Backup"
                                BackupType.PRE_EDIT -> "Pre-Edit Backup"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Timestamp
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                    Text(
                        text = dateFormat.format(Date(backup.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Word count
                    Text(
                        text = "${backup.wordCount} words",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Action buttons
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = onRestore,
                        modifier = Modifier.size(width = 80.dp, height = 32.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        Text(
                            text = "Restore",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(width = 80.dp, height = 32.dp),
                        contentPadding = PaddingValues(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(
                            text = "Delete",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Backup") },
            text = { Text("Are you sure you want to delete this backup? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
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

@Composable
private fun CreateBackupDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudQueue,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Manual Backup")
            }
        },
        text = { 
            Text("This will create a backup of your current chapter content that you can restore later.") 
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Create Backup")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
