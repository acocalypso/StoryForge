package de.astronarren.storyforge.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/**
 * Utility class for sharing files using Android's share intent system.
 * Handles proper file URI creation and sharing with other applications.
 */
object ShareUtil {
    
    /**
     * Shares a file using Android's share intent.
     * Creates a proper FileProvider URI for secure file sharing.
     *
     * @param context The context to use for starting the share intent
     * @param filePath The absolute path to the file to share
     * @param mimeType The MIME type of the file (e.g., "text/plain", "application/pdf")
     * @param shareTitle Optional title for the share chooser dialog
     */
    fun shareFile(
        context: Context,
        filePath: String,
        mimeType: String,
        shareTitle: String = "Share exported file"
    ) {
        try {
            val file = File(filePath)
            
            if (!file.exists()) {
                // File doesn't exist, show error
                return
            }
              // Create a content URI using FileProvider for secure sharing
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // Create share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, file.nameWithoutExtension)
                putExtra(Intent.EXTRA_TEXT, "Shared from StoryForge: ${file.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Start share chooser
            val chooserIntent = Intent.createChooser(shareIntent, shareTitle)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            
        } catch (e: Exception) {
            // Handle sharing error silently or show toast if needed
            e.printStackTrace()
        }
    }
    
    /**
     * Gets the appropriate MIME type for a file based on its extension.
     *
     * @param filePath The path to the file
     * @return The MIME type string
     */
    fun getMimeType(filePath: String): String {
        return when (File(filePath).extension.lowercase()) {
            "txt" -> "text/plain"
            "pdf" -> "application/pdf"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "json" -> "application/json"
            "csv" -> "text/csv"
            "storyforge" -> "application/octet-stream"
            else -> "application/octet-stream"
        }
    }
    
    /**
     * Creates a share intent for a StoryForge export file with appropriate metadata.
     *
     * @param context The context to use for starting the share intent
     * @param filePath The absolute path to the exported file
     * @param exportType Description of what was exported (e.g., "3 chapters", "Complete book")
     */
    fun shareStoryForgeExport(
        context: Context,
        filePath: String,
        exportType: String
    ) {
        val file = File(filePath)
        val mimeType = getMimeType(filePath)
        val shareTitle = "Share StoryForge Export"
          try {
            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "StoryForge Export: ${file.nameWithoutExtension}")
                putExtra(
                    Intent.EXTRA_TEXT, 
                    "I've exported $exportType from StoryForge.\n\nFile: ${file.name}\nCreated with StoryForge - A comprehensive creative writing companion for Android."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, shareTitle)
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
