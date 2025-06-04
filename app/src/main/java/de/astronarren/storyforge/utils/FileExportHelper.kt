package de.astronarren.storyforge.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles file export operations with proper permission handling
 * and modern Android storage access patterns
 */
@Singleton
class FileExportHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {
      /**
     * Get the appropriate export directory based on Android version and permissions
     */
    fun getExportDirectory(bookTitle: String? = null): File {
        val baseDir = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10+ - Use app-specific external files directory
                // This doesn't require permissions and files are accessible via Documents folder
                File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "StoryForge")
            }
            permissionManager.hasStoragePermission() -> {
                // Pre-Android 10 with permissions - Use public Downloads directory
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                File(downloadsDir, "StoryForge")
            }
            else -> {
                // No permissions - Use app-specific internal storage
                File(context.filesDir, "exports")
            }
        }
        
        // Create book-specific subdirectory if book title is provided
        val finalDir = if (bookTitle != null) {
            // Sanitize book title for filename
            val sanitizedTitle = bookTitle.replace(Regex("[/\\\\:*?\"<>|]"), "_")
            File(baseDir, sanitizedTitle)
        } else {
            baseDir
        }
        
        finalDir.apply { mkdirs() }
        return finalDir
    }    /**
     * Save content to a file with proper error handling using modern storage APIs
     */
    fun saveTextFile(fileName: String, content: String, bookTitle: String? = null): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(fileName, content, bookTitle, "text/plain")
        } else {
            saveLegacyTextFile(fileName, content, bookTitle)
        }
    }
    
    /**
     * Save binary content to a file (for DOCX, PDF files)
     */
    fun saveBinaryFile(fileName: String, bookTitle: String? = null, writeToFile: (File) -> Unit): File {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStoreWithCallback(fileName, bookTitle, getMimeType(fileName), writeToFile)
        } else {
            saveLegacyBinaryFile(fileName, bookTitle, writeToFile)
        }
    }
    
    /**
     * Save file using MediaStore (Android 10+)
     */
    private fun saveViaMediaStore(fileName: String, content: String, bookTitle: String?, mimeType: String): File {
        val resolver = context.contentResolver
        val folderPath = if (bookTitle != null) {
            "Documents/StoryForge/${bookTitle.replace(Regex("[/\\\\:*?\"<>|]"), "_")}"
        } else {
            "Documents/StoryForge"
        }
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath)
        }
        
        try {
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                ?: throw RuntimeException("Failed to create MediaStore entry for $fileName")
            
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray())
            } ?: throw RuntimeException("Failed to open output stream for $fileName")
              // Return a placeholder file for compatibility (the actual file is managed by MediaStore)
            return File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        } catch (e: Exception) {
            throw RuntimeException("Failed to save file via MediaStore: ${e.message}", e)
        }
    }
    
    /**
     * Save file using MediaStore with callback for binary content (Android 10+)
     */
    private fun saveViaMediaStoreWithCallback(
        fileName: String, 
        bookTitle: String?, 
        mimeType: String, 
        writeToFile: (File) -> Unit
    ): File {
        val resolver = context.contentResolver
        val folderPath = if (bookTitle != null) {
            "Documents/StoryForge/${bookTitle.replace(Regex("[/\\\\:*?\"<>|]"), "_")}"
        } else {
            "Documents/StoryForge"
        }
        
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath)
        }
        
        try {
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                ?: throw RuntimeException("Failed to create MediaStore entry for $fileName")
            
            // Create a temporary file for the callback
            val tempFile = File(context.cacheDir, fileName)
            writeToFile(tempFile)
            
            // Copy the temporary file content to MediaStore
            resolver.openOutputStream(uri)?.use { outputStream ->
                tempFile.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw RuntimeException("Failed to open output stream for $fileName")
            
            // Clean up temporary file
            tempFile.delete()
              // Return a placeholder file for compatibility
            return File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        } catch (e: Exception) {
            throw RuntimeException("Failed to save file via MediaStore: ${e.message}", e)
        }
    }
    
    /**
     * Legacy text file saving (Android 9 and below)
     */
    private fun saveLegacyTextFile(fileName: String, content: String, bookTitle: String?): File {
        val exportDir = getExportDirectory(bookTitle)
        val file = File(exportDir, fileName)
        
        try {
            FileOutputStream(file).use { output ->
                output.write(content.toByteArray())
            }
        } catch (e: Exception) {
            throw RuntimeException("Failed to save file: ${e.message}", e)
        }
        
        return file
    }
    
    /**
     * Legacy binary file saving (Android 9 and below)
     */
    private fun saveLegacyBinaryFile(fileName: String, bookTitle: String?, writeToFile: (File) -> Unit): File {
        val exportDir = getExportDirectory(bookTitle)
        val file = File(exportDir, fileName)
        
        try {
            writeToFile(file)
        } catch (e: Exception) {
            throw RuntimeException("Failed to save binary file: ${e.message}", e)
        }
        
        return file
    }
    
    /**
     * Get a shareable URI for the exported file
     */
    fun getShareableUri(file: File): Uri? {
        return try {
            if (file.exists()) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Create a sharing intent for the exported file
     */
    fun createShareIntent(file: File, mimeType: String): Intent? {
        val uri = getShareableUri(file) ?: return null
        
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    /**
     * Get appropriate MIME type for file extension
     */
    fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast(".", "").lowercase()) {
            "txt" -> "text/plain"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "pdf" -> "application/pdf"
            "json" -> "application/json"
            "csv" -> "text/csv"
            else -> "application/octet-stream"
        }
    }
      /**
     * Check if the export directory is accessible and writable
     */
    fun isExportDirectoryAccessible(bookTitle: String? = null): Boolean {
        return try {
            val exportDir = getExportDirectory(bookTitle)
            exportDir.exists() || exportDir.mkdirs()
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get a description of where files will be saved
     */
    fun getExportLocationDescription(bookTitle: String? = null): String {
        val bookPath = if (bookTitle != null) "/$bookTitle" else ""
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                "Files will be saved to: Documents/StoryForge$bookPath"
            }
            permissionManager.hasStoragePermission() -> {
                "Files will be saved to: Downloads/StoryForge$bookPath"
            }
            else -> {
                "Files will be saved to app internal storage (limited access)"
            }
        }
    }
}
