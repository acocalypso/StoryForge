package de.astronarren.storyforge.ui.components.permissions

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*
import de.astronarren.storyforge.utils.PermissionManager

/**
 * Comprehensive permission handler for storage permissions with fallback options
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionHandler(
    permissionManager: PermissionManager,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onUseAppStorage: () -> Unit,
    content: @Composable (requestPermission: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    // Get required permissions based on Android version
    val requiredPermissions = permissionManager.getRequiredStoragePermissions()
    
    // State for dialogs
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showDeniedDialog by remember { mutableStateOf(false) }
    
    if (requiredPermissions.isEmpty()) {
        // No permissions needed for this Android version
        LaunchedEffect(Unit) {
            onPermissionGranted()
        }
        content { /* No action needed */ }
    } else {
        // Handle multiple permissions
        val permissionStates = rememberMultiplePermissionsState(
            permissions = requiredPermissions.toList()
        )
        
        // Check current permission status
        LaunchedEffect(permissionStates.allPermissionsGranted) {
            if (permissionStates.allPermissionsGranted) {
                onPermissionGranted()
            }
        }
        
        // Content with permission request trigger
        content {
            when {
                permissionStates.allPermissionsGranted -> {
                    // Permissions already granted
                    onPermissionGranted()
                }
                permissionStates.shouldShowRationale -> {
                    // Show rationale dialog
                    showPermissionDialog = true
                }
                permissionStates.permissions.any { !it.status.isGranted } -> {
                    // Check if permanently denied
                    val permanentlyDenied = activity?.let { act ->
                        permissionManager.shouldShowStoragePermissionRationale(act).not()
                    } ?: false
                    
                    if (permanentlyDenied && permissionStates.permissions.any { it.status is PermissionStatus.Denied }) {
                        showDeniedDialog = true
                    } else {
                        showPermissionDialog = true
                    }
                }
                else -> {
                    // Request permissions
                    permissionStates.launchMultiplePermissionRequest()
                }
            }
        }
        
        // Permission request dialog
        if (showPermissionDialog) {
            PermissionRequestDialog(
                onRequestPermission = {
                    showPermissionDialog = false
                    permissionStates.launchMultiplePermissionRequest()
                },
                onUseAppStorage = {
                    showPermissionDialog = false
                    onUseAppStorage()
                },
                onDismiss = {
                    showPermissionDialog = false
                    onPermissionDenied()
                }
            )
        }
        
        // Permission denied dialog
        if (showDeniedDialog) {
            PermissionDeniedDialog(
                onOpenSettings = {
                    showDeniedDialog = false
                    // Open app settings
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                },
                onUseAppStorage = {
                    showDeniedDialog = false
                    onUseAppStorage()
                },
                onDismiss = {
                    showDeniedDialog = false
                    onPermissionDenied()
                }
            )
        }
    }
}

/**
 * Simple permission checker composable that shows status indicators
 */
@Composable
fun PermissionStatusIndicator(
    permissionManager: PermissionManager,
    showText: Boolean = true,
    showDetails: Boolean = false
) {    val hasPermissions = permissionManager.hasStoragePermission()
    val requiredPermissions = permissionManager.getRequiredStoragePermissions()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {if (requiredPermissions.isEmpty()) {
            // No permissions needed
            if (showText) {
                Text(
                    text = "✓ Storage access available",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            val statusText = permissionManager.getPermissionStatusMessage()
            val statusColor = if (hasPermissions) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
            
            if (showText) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
        
        if (showDetails) {
            Text(
                text = permissionManager.getPermissionDescription(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
