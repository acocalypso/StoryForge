package de.astronarren.storyforge.data.model

import de.astronarren.storyforge.data.database.entities.Book
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**
 * Export data models for book import/export functionality
 */
@Serializable
data class BookExportData(
    val version: String = "1.0",
    val exportedAt: Long = System.currentTimeMillis(),
    val books: List<SerializableBook>
)

@Serializable
data class SerializableBook(
    val id: String,
    val title: String,
    val description: String,
    val author: String,
    val genre: String,
    val targetWordCount: Int?,
    val wordCount: Int,
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val version: Int,
    val coverImagePath: String?
)

/**
 * Extension functions for converting between Book entities and serializable format
 */
fun Book.toSerializable(): SerializableBook {
    return SerializableBook(
        id = id,
        title = title,
        description = description,
        author = author,
        genre = genre,
        targetWordCount = targetWordCount,
        wordCount = wordCount,
        isFavorite = isFavorite,
        isArchived = !isActive, // Convert isActive to isArchived
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version,
        coverImagePath = coverImagePath
    )
}

fun SerializableBook.toBook(): Book {
    return Book(
        id = id,
        title = title,
        description = description,
        author = author,
        genre = genre,
        targetWordCount = targetWordCount,
        wordCount = wordCount,
        isFavorite = isFavorite,
        isActive = !isArchived, // Convert isArchived to isActive
        createdAt = createdAt,
        updatedAt = updatedAt,
        version = version,
        coverImagePath = coverImagePath
    )
}

/**
 * Export formats supported by the application
 */
enum class ExportFormat(val displayName: String, val extension: String, val mimeType: String) {
    TXT("Plain Text", "txt", "text/plain"),
    DOCX("Word Document", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    PDF("PDF Document", "pdf", "application/pdf")
}

/**
 * Import/Export result data classes
 */
sealed class ExportResult {
    data class Success(val filePath: String, val bookCount: Int) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

sealed class ImportResult {
    data class Success(val importedBooks: List<Book>, val skippedBooks: List<String>) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

/**
 * Export options for customizing the export process
 */
data class ExportOptions(
    val format: ExportFormat = ExportFormat.TXT,
    val includeArchived: Boolean = false,
    val includeFavoritesOnly: Boolean = false,
    val selectedGenres: Set<String> = emptySet(),
    val filename: String = generateDefaultFilename()
)

private fun generateDefaultFilename(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())
    return "storyforge_books_${dateFormat.format(Date())}"
}

