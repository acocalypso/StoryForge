package de.astronarren.storyforge.data.service

import android.content.Context
import android.net.Uri
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import de.astronarren.storyforge.data.database.entities.Book
import de.astronarren.storyforge.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookImportExportService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Export books to a file in the specified format
     */
    suspend fun exportBooks(
        books: List<Book>,
        options: ExportOptions
    ): ExportResult = withContext(Dispatchers.IO) {
        try {
            // Filter books based on export options
            val filteredBooks = filterBooksForExport(books, options)
            
            if (filteredBooks.isEmpty()) {
                return@withContext ExportResult.Error("No books match the export criteria")
            }            // Generate file content based on format
            val content = when (options.format) {
                ExportFormat.TXT -> exportToTxt(filteredBooks)
                ExportFormat.DOCX -> throw UnsupportedOperationException("DOCX format not supported for book list export")
                ExportFormat.PDF -> throw UnsupportedOperationException("PDF format not supported for book list export")
            }
            
            // Save to file
            val fileName = "${options.filename}.${options.format.extension}"
            val file = saveToExternalStorage(fileName, content)
            
            ExportResult.Success(file.absolutePath, filteredBooks.size)
        } catch (e: Exception) {
            ExportResult.Error("Export failed: ${e.message}")
        }
    }
    
    /**
     * Import books from a file
     */
    suspend fun importBooks(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val content = readFileFromUri(uri)
            val importData = parseImportContent(content)
            
            val books = importData.books.map { it.toBook() }
            val skippedBooks = mutableListOf<String>()
            
            ImportResult.Success(books, skippedBooks)
        } catch (e: Exception) {
            ImportResult.Error("Import failed: ${e.message}")
        }
    }
    
    /**
     * Get available export formats
     */
    fun getAvailableExportFormats(): List<ExportFormat> {
        return ExportFormat.values().toList()
    }
    
    /**
     * Generate a shareable file URI for the exported file
     */
    fun getShareableUri(filePath: String): Uri? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }
      private fun filterBooksForExport(books: List<Book>, options: ExportOptions): List<Book> {
        return books.filter { book ->
            // Include archived books only if specified
            if (!options.includeArchived && !book.isActive) return@filter false
            
            // Include only favorites if specified
            if (options.includeFavoritesOnly && !book.isFavorite) return@filter false
            
            // Filter by selected genres
            if (options.selectedGenres.isNotEmpty() && book.genre !in options.selectedGenres) {
                return@filter false
            }
            
            true
        }    }
  
    
    private fun exportToTxt(books: List<Book>): String {
        val builder = StringBuilder()
        builder.appendLine("StoryForge Books Export")
        builder.appendLine("=".repeat(50))
        builder.appendLine("Exported: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
        builder.appendLine("Total Books: ${books.size}")
        builder.appendLine()
        
        books.forEach { book ->            builder.appendLine("Title: ${book.title}")
            builder.appendLine("Author: ${book.author}")
            builder.appendLine("Genre: ${book.genre}")
            if (book.targetWordCount != null) {
                builder.appendLine("Target Words: ${book.targetWordCount}")
            }
            builder.appendLine("Word Count: ${book.wordCount}")
            builder.appendLine("Favorite: ${if (book.isFavorite) "Yes" else "No"}")
            builder.appendLine("Archived: ${if (!book.isActive) "Yes" else "No"}")
            builder.appendLine("Created: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(book.createdAt)}")
            builder.appendLine("Updated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(book.updatedAt)}")
            
            if (book.description.isNotBlank()) {
                builder.appendLine()
                builder.appendLine("Description:")
                builder.appendLine(book.description)
            }
            
            builder.appendLine()
            builder.appendLine("-".repeat(30))
            builder.appendLine()
        }
        
        return builder.toString()
    }
    
    private fun saveToExternalStorage(fileName: String, content: String): File {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val storyForgeDir = File(downloadsDir, "StoryForge")
        
        if (!storyForgeDir.exists()) {
            storyForgeDir.mkdirs()
        }
        
        val file = File(storyForgeDir, fileName)
        file.writeText(content)
        
        return file
    }
    
    private fun readFileFromUri(uri: Uri): String {
        return context.contentResolver.openInputStream(uri)?.use { inputStream ->
            inputStream.bufferedReader().readText()
        } ?: throw IOException("Could not read file")
    }
      private fun parseImportContent(content: String): SimpleBookExportData {
        return try {
            json.decodeFromString<SimpleBookExportData>(content)
        } catch (e: Exception) {
            // Try to parse as legacy format or throw meaningful error
            throw IOException("Invalid import file format. Please ensure the file is a valid StoryForge export.")
        }
    }
}

