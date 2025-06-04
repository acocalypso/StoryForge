package de.astronarren.storyforge.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager for handling runtime permissions, especially storage permissions
 * for different Android API levels
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {    /**
     * Check if storage permissions are granted
     */    fun hasStoragePermission(): Boolean {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ - For saving to Downloads, we'll use app-specific external storage
                // which doesn't require permissions, or SAF for user-selected locations
                true
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10-12 - Scoped storage, check for legacy storage if needed
                val readPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                readPermission
            }
            else -> {
                // Android 9 and below - Traditional storage permissions
                val writePermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                val readPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
                writePermission && readPermission
            }
        }
    }/**
     * Get the list of permissions to request based on Android version
     */
    fun getRequiredStoragePermissions(): Array<String> {        val permissions = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                // Android 13+ - No storage permissions needed for app-specific directories
                emptyArray()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10-12 - Read external storage
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                // Android 9 and below - Read and write external storage
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )            }
        }
        return permissions
    }
      /**
     * Check if we have all required permissions for export functionality
     */    fun hasExportPermissions(): Boolean {
        val requiredPermissions = getRequiredStoragePermissions()
        if (requiredPermissions.isEmpty()) {
            return true
        }
        
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if we should show rationale for storage permissions
     */
    fun shouldShowStoragePermissionRationale(activity: androidx.activity.ComponentActivity): Boolean {
        val requiredPermissions = getRequiredStoragePermissions()
        return requiredPermissions.any { permission ->
            activity.shouldShowRequestPermissionRationale(permission)
        }
    }
      /**
     * Get a user-friendly description of what permissions are needed
     */
    fun getPermissionDescription(): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                "Files will be saved to app-specific folder in your Documents directory."
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                "Storage access needed to save files to your Downloads folder."
            }
            else -> {
                "Storage access needed to save files to your device storage."
            }
        }
    }
    
    /**
     * Get current permission status for display
     */
    fun getPermissionStatusMessage(): String {
        return if (hasExportPermissions()) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    "✓ Ready to export - Files will be saved to Documents/StoryForge"
                }
                else -> {
                    "✓ Storage access granted - Files will be saved to Downloads"
                }
            }
        } else {
            "⚠ Storage permission required for Downloads access"
        }
    }
}
