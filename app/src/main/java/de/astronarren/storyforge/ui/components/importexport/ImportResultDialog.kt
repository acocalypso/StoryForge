package de.astronarren.storyforge.ui.components.importexport

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.data.service.StoryForgeImportExportService.ImportResult
import de.astronarren.storyforge.data.service.StoryForgeImportExportService.ImportMode

/**
 * Dialog showing import results after successful import
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportResultDialog(
    importResult: ImportResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Card {
            when (importResult) {
                is ImportResult.Success -> {
                    SuccessContent(importResult, onDismiss)
                }
                is ImportResult.Error -> {
                    ErrorContent(importResult, onDismiss)
                }
            }
        }
    }
}

@Composable
private fun SuccessContent(
    result: ImportResult.Success,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success header
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Import Successful!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Your StoryForge data has been imported successfully.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        HorizontalDivider()
        
        // Import statistics
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Import Summary",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                if (result.importedBooks > 0) {
                    ImportStatRow(
                        icon = Icons.Default.MenuBook,
                        label = "Books imported",
                        count = result.importedBooks
                    )
                }
                
                if (result.importedChapters > 0) {
                    ImportStatRow(
                        icon = Icons.Default.Article,
                        label = "Chapters imported",
                        count = result.importedChapters
                    )
                }
                
                if (result.importedCharacters > 0) {
                    ImportStatRow(
                        icon = Icons.Default.Person,
                        label = "Characters imported",
                        count = result.importedCharacters
                    )
                }
                
                if (result.importedScenes > 0) {
                    ImportStatRow(
                        icon = Icons.Default.Movie,
                        label = "Scenes imported",
                        count = result.importedScenes
                    )
                }
                
                if (result.importedTimelineEvents > 0) {
                    ImportStatRow(
                        icon = Icons.Default.Schedule,
                        label = "Timeline events imported",
                        count = result.importedTimelineEvents
                    )
                }
                
                if (result.skippedBooks > 0) {
                    ImportStatRow(
                        icon = Icons.Default.SkipNext,
                        label = "Books skipped (already exist)",
                        count = result.skippedBooks,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Import mode info
        Card {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (result.importMode) {
                        ImportMode.MERGE -> Icons.Default.MergeType
                        ImportMode.SKIP_EXISTING -> Icons.Default.SkipNext
                        ImportMode.REPLACE -> Icons.Default.SwapHoriz
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Column {
                    Text(
                        text = "Import Mode: ${
                            when (result.importMode) {
                                ImportMode.MERGE -> "Merge"
                                ImportMode.SKIP_EXISTING -> "Skip Existing"
                                ImportMode.REPLACE -> "Replace"
                                else -> "Unknown"
                            }
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = when (result.importMode) {
                            ImportMode.MERGE -> "Imported data merged with existing"
                            ImportMode.SKIP_EXISTING -> "Existing books were preserved"
                            ImportMode.REPLACE -> "Existing books were replaced"
                            else -> "Import completed"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Action button
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun ErrorContent(
    result: ImportResult.Error,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Error header
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = "Import Failed",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        
        Text(
            text = result.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Action button
        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("OK")
        }
    }
}

@Composable
private fun ImportStatRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = tint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}
