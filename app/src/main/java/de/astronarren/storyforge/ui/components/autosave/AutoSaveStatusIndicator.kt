package de.astronarren.storyforge.ui.components.autosave

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.data.models.AutoSaveState

/**
 * Component that displays the current autosave status
 */
@Composable
fun AutoSaveStatusIndicator(
    autoSaveState: AutoSaveState,
    statusText: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = autoSaveState != AutoSaveState.IDLE,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when (autoSaveState) {
                    AutoSaveState.SAVED -> MaterialTheme.colorScheme.primaryContainer
                    AutoSaveState.ERROR -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (autoSaveState) {
                    AutoSaveState.PENDING -> {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AutoSaveState.SAVING -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    AutoSaveState.SAVED -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    AutoSaveState.ERROR -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    AutoSaveState.IDLE -> {
                        // Hidden state, should not show
                    }
                }
                
                if (statusText.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = when (autoSaveState) {
                            AutoSaveState.SAVED -> MaterialTheme.colorScheme.primary
                            AutoSaveState.ERROR -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * Compact version for toolbar display
 */
@Composable
fun CompactAutoSaveIndicator(
    autoSaveState: AutoSaveState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = autoSaveState != AutoSaveState.IDLE,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        when (autoSaveState) {
            AutoSaveState.PENDING -> {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Changes pending",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AutoSaveState.SAVING -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            AutoSaveState.SAVED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Saved",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            AutoSaveState.ERROR -> {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Save failed",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            AutoSaveState.IDLE -> {
                // Hidden
            }
        }
    }
}
