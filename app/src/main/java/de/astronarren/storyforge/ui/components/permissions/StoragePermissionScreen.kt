package de.astronarren.storyforge.ui.components.permissions

import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.utils.PermissionManager

@Composable
fun StoragePermissionScreen(
    permissionManager: PermissionManager,
    onPermissionResult: (Boolean) -> Unit,
    onSkip: () -> Unit
) {
    Log.d("StoragePermissionScreen", "StoragePermissionScreen composable started")
    
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    
    val requiredPermissions = permissionManager.getRequiredStoragePermissions()
    Log.d("StoragePermissionScreen", "Required permissions: ${requiredPermissions.contentToString()}")
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d("StoragePermissionScreen", "Permission result received: $permissions")
        val allGranted = permissions.all { it.value }
        Log.d("StoragePermissionScreen", "All permissions granted: $allGranted")
        permissionDenied = !allGranted
        
        if (allGranted) {
            onPermissionResult(true)
        } else {
            // Check if we should show rationale
            showRationale = permissionManager.shouldShowStoragePermissionRationale(
                context as androidx.activity.ComponentActivity
            )
            Log.d("StoragePermissionScreen", "Should show rationale: $showRationale")
        }
    }
    
    LaunchedEffect(Unit) {
        Log.d("StoragePermissionScreen", "LaunchedEffect triggered")
        if (permissionManager.hasExportPermissions()) {
            Log.d("StoragePermissionScreen", "Already has permissions, calling onPermissionResult(true)")
            onPermissionResult(true)
        } else if (requiredPermissions.isNotEmpty()) {
            Log.d("StoragePermissionScreen", "Launching permission request for: ${requiredPermissions.contentToString()}")
            permissionLauncher.launch(requiredPermissions)
        } else {
            // No permissions needed (Android 13+)
            Log.d("StoragePermissionScreen", "No permissions needed, calling onPermissionResult(true)")
            onPermissionResult(true)
        }
    }
    
    Log.d("StoragePermissionScreen", "permissionDenied: $permissionDenied")
    
    if (permissionDenied) {
        Log.d("StoragePermissionScreen", "Showing PermissionDeniedContent")
        PermissionDeniedContent(
            permissionManager = permissionManager,
            showRationale = showRationale,
            onRetry = {
                Log.d("StoragePermissionScreen", "Retry permission request")
                permissionDenied = false
                permissionLauncher.launch(requiredPermissions)
            },
            onSkip = onSkip
        )
    } else {
        Log.d("StoragePermissionScreen", "Showing PermissionExplanationContent")
        PermissionExplanationContent(
            permissionManager = permissionManager,
            onContinue = {
                Log.d("StoragePermissionScreen", "Continue button pressed")
                if (requiredPermissions.isNotEmpty()) {
                    Log.d("StoragePermissionScreen", "Launching permission request from continue")
                    permissionLauncher.launch(requiredPermissions)
                } else {
                    Log.d("StoragePermissionScreen", "No permissions needed from continue")
                    onPermissionResult(true)
                }
            },
            onSkip = onSkip
        )
    }
}

@Composable
private fun PermissionExplanationContent(
    permissionManager: PermissionManager,
    onContinue: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Folder,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "File Storage Setup",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = getStorageExplanation(),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Where will files be saved?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = permissionManager.getPermissionDescription(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (permissionManager.getRequiredStoragePermissions().isNotEmpty()) {
                    "Grant Storage Access"
                } else {
                    "Continue"
                }
            )
        }
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = onSkip,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip (Limited functionality)")
            }
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    permissionManager: PermissionManager,
    showRationale: Boolean,
    onRetry: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Storage Access Required",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (showRationale) {
                "StoryForge needs storage access to save your exported chapters and books to the Documents/StoryForge folder. Without this permission, files will only be saved to app-specific storage with limited access."
            } else {
                "Storage access was denied. To enable file exports to your Documents folder, please grant storage permission in your device settings or try again."
            },
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Again")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue Without Permission")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Note: You can grant permission later in the app settings.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun getStorageExplanation(): String {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            "StoryForge will save your exported books and chapters to a dedicated folder in your Documents directory. No special permissions are required on this Android version."
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            "StoryForge needs storage access to save your exported files to Documents/StoryForge. This ensures your files are easily accessible and can be shared with other apps."
        }
        else -> {
            "StoryForge needs storage access to save your exported books and chapters to your device's Downloads/StoryForge folder for easy access and sharing."
        }
    }
}
