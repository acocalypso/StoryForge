package de.astronarren.storyforge.ui.components.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.astronarren.storyforge.data.model.ExportFormat
import de.astronarren.storyforge.ui.components.permissions.PermissionHandler
import de.astronarren.storyforge.ui.components.permissions.PermissionStatusIndicator
import de.astronarren.storyforge.utils.PermissionManager

/**
 * Dialog for exporting a single chapter in various formats
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterExportDialog(
    chapterTitle: String,
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit,
    isExporting: Boolean = false,
    permissionManager: PermissionManager? = null,
    modifier: Modifier = Modifier
) {
    var selectedFormat by remember { mutableStateOf(ExportFormat.TXT) }
    var showPermissionFlow by remember { mutableStateOf(false) }
    var permissionGranted by remember { mutableStateOf(false) }
    var useAppStorage by remember { mutableStateOf(false) }
    
    // Check initial permission status
    LaunchedEffect(permissionManager) {
        permissionManager?.let { pm ->
            permissionGranted = pm.hasExportPermissions()
        }
    }
    
    if (showPermissionFlow && permissionManager != null) {
        PermissionHandler(
            permissionManager = permissionManager,
            onPermissionGranted = {
                permissionGranted = true
                showPermissionFlow = false
                onExport(selectedFormat)
            },
            onPermissionDenied = {
                showPermissionFlow = false
            },
            onUseAppStorage = {
                useAppStorage = true
                showPermissionFlow = false
                onExport(selectedFormat)
            }
        ) { requestPermission ->
            LaunchedEffect(Unit) {
                requestPermission()
            }
        }
        return
    }
    
    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        modifier = modifier
    ) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Export Chapter",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                // Chapter info
                Text(
                    text = "Export \"$chapterTitle\" as:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                  // Permission status
                permissionManager?.let { pm ->
                    PermissionStatusIndicator(
                        permissionManager = pm,
                        showText = true,
                        showDetails = true
                    )
                }
                
                // Format selection
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {                    ExportFormatOption(
                        format = ExportFormat.TXT,
                        icon = Icons.Default.TextFields,
                        title = "Plain Text (.txt)",
                        description = "Simple text format, universally compatible",
                        selected = selectedFormat == ExportFormat.TXT,
                        onSelect = { selectedFormat = ExportFormat.TXT },
                        enabled = !isExporting
                    )
                    
                    // Note: DOCX and PDF formats removed due to Android compatibility issues
                }
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isExporting
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                      Button(
                        onClick = { 
                            if (permissionManager != null && !permissionManager.hasExportPermissions()) {
                                showPermissionFlow = true
                            } else {
                                onExport(selectedFormat)
                            }
                        },
                        enabled = !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exporting...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportFormatOption(
    format: ExportFormat,
    icon: ImageVector,
    title: String,
    description: String,
    selected: Boolean,
    onSelect: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
                enabled = enabled
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (enabled) {
                if (selected) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) {
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                }
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled
        )
    }
}
