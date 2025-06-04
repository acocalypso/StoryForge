package de.astronarren.storyforge.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileWriter
import java.io.OutputStream

/**
 * Modern file export helper that handles Android version differences
 * without requiring MANAGE_EXTERNAL_STORAGE permission
 */
object ModernFileExportHelper {
    
    /**
     * Save content to a file using version-appropriate methods
     */
    fun saveFile(
        context: Context,
        fileName: String,
        content: ByteArray,
        mimeType: String
    ): Result<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ (API 29+) - Use MediaStore
                saveViaMediaStore(context, fileName, content, mimeType)
            } else {
                // Android 9- - Legacy method (requires WRITE_EXTERNAL_STORAGE permission)
                saveViaLegacy(context, fileName, content)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Modern method for Android 10+ using MediaStore
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveViaMediaStore(
        context: Context,
        fileName: String,
        content: ByteArray,
        mimeType: String
    ): Result<String> {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/StoryForge")
        }

        return try {
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    stream.write(content)
                }
                Result.success("Documents/StoryForge/$fileName")
            } ?: Result.failure(Exception("Failed to create file URI"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Legacy method for Android 9 and below
     */
    private fun saveViaLegacy(
        context: Context,
        fileName: String,
        content: ByteArray
    ): Result<String> {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "StoryForge"
        )
        
        if (!dir.exists()) {
            dir.mkdirs()
        }
        
        val file = File(dir, fileName)
        return try {
            file.writeBytes(content)
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Helper method for text content
     */
    fun saveTextFile(
        context: Context,
        fileName: String,
        content: String
    ): Result<String> {
        return saveFile(context, fileName, content.toByteArray(), "text/plain")
    }
}
