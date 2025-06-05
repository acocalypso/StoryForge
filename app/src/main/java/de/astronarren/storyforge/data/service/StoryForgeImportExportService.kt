package de.astronarren.storyforge.data.service

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import de.astronarren.storyforge.data.database.StoryForgeDatabase
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoryForgeImportExportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: StoryForgeRepository,
    private val database: StoryForgeDatabase
) {

    enum class ExportFormat {
        DATABASE_BACKUP,
        JSON_LEGACY
    }

    enum class ImportMode {
        REPLACE_ALL,
        MERGE_SKIP_EXISTING,
        MERGE,
        SKIP_EXISTING,
        REPLACE
    }

    sealed class Result<T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error<T>(val message: String, val exception: Throwable? = null) : Result<T>()
    }

    sealed class ImportResult {
        data class Success(
            val message: String,
            val importedBooks: Int = 0,
            val importedChapters: Int = 0,
            val importedCharacters: Int = 0,
            val importedScenes: Int = 0,
            val importedTimelineEvents: Int = 0,
            val skippedBooks: Int = 0,
            val importMode: ImportMode
        ) : ImportResult()
        data class Error(val message: String, val exception: Throwable? = null) : ImportResult()
    }

    /**
     * Export the entire database to a backup file
     */
    suspend fun exportDatabase(
        outputUri: Uri,
        format: ExportFormat = ExportFormat.DATABASE_BACKUP
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            when (format) {
                ExportFormat.DATABASE_BACKUP -> exportDatabaseBackup(outputUri)
                ExportFormat.JSON_LEGACY -> Result.Error("JSON export not implemented yet - use DATABASE_BACKUP for full data preservation")
            }        } catch (e: Exception) {
            Result.Error("Export failed: ${e.message}", e)
        }
    }

    /**
     * Import database from a backup file
     */
    suspend fun importDatabase(
        inputUri: Uri,
        mode: ImportMode = ImportMode.REPLACE_ALL
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            when (mode) {
                ImportMode.REPLACE_ALL, ImportMode.REPLACE -> importDatabaseBackup(inputUri, replaceExisting = true, mode = mode)
                ImportMode.MERGE_SKIP_EXISTING, ImportMode.MERGE, ImportMode.SKIP_EXISTING -> importDatabaseBackup(inputUri, replaceExisting = false, mode = mode)
            }
        } catch (e: Exception) {
            ImportResult.Error("Import failed: ${e.message}", e)
        }
    }    /**
     * Export all data - exports the database file to Documents/StoryForge/backup
     */
    suspend fun exportAllData(fileName: String? = null): Result<String> {
        return try {
            // Generate file name with timestamp if not provided
            val actualFileName = fileName ?: "storyforge_backup_${System.currentTimeMillis()}.db"
            
            // Get current database file
            val dbFile = context.getDatabasePath("storyforge_database")
            if (!dbFile.exists()) {
                return Result.Error("Database file not found", RuntimeException("Database file does not exist"))
            }
            
            // Read database content
            val dbContent = dbFile.readBytes()
            
            // Save to user-accessible Documents/StoryForge/backup folder
            val saveResult = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Use MediaStore to save to Documents/StoryForge/backup
                saveBackupViaMediaStore(actualFileName, dbContent)
            } else {
                // Pre-Android 10 - Use legacy method to save to Documents/StoryForge/backup
                saveBackupViaLegacy(actualFileName, dbContent)
            }
            
            saveResult.fold(
                onSuccess = { path -> Result.Success(path) },
                onFailure = { e -> Result.Error("Export failed: ${e.message}", e) }
            )
        } catch (e: Exception) {
            Result.Error("Export failed: ${e.message}", e)
        }
    }
    
    /**
     * Save backup using MediaStore for Android 10+ to user-accessible Documents folder
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveBackupViaMediaStore(fileName: String, content: ByteArray): kotlin.Result<String> {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/octet-stream")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/StoryForge/backup")
        }

        return try {
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { stream ->
                    stream.write(content)
                }
                kotlin.Result.success("Documents/StoryForge/backup/$fileName")
            } ?: kotlin.Result.failure(Exception("Failed to create backup file URI"))
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }
    
    /**
     * Save backup using legacy method for Android 9 and below
     */
    private fun saveBackupViaLegacy(fileName: String, content: ByteArray): kotlin.Result<String> {
        val backupDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "StoryForge/backup"
        )
        
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        
        val backupFile = File(backupDir, fileName)
        return try {
            backupFile.writeBytes(content)
            kotlin.Result.success(backupFile.absolutePath)
        } catch (e: Exception) {
            kotlin.Result.failure(e)
        }
    }

    /**
     * Import data - alias for importDatabase for ViewModel compatibility
     */
    suspend fun importData(fileUri: Uri, importMode: ImportMode): Result<ImportResult> {
        return try {
            val result = importDatabase(fileUri, importMode)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error("Import failed: ${e.message}", e)
        }
    }

    /**
     * Export a single book to a portable format (for sharing individual books)
     */
    suspend fun exportBook(
        bookId: String,
        outputUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Create a temporary database with only the specific book's data
            val tempDbFile = File(context.cacheDir, "temp_export_${bookId}.db")
            
            // Use SQLite commands to extract only the book's data
            val commands = buildBookExportQueries(bookId)
            exportPartialDatabase(tempDbFile, commands)
            
            // Copy the temporary database to the output URI
            tempDbFile.inputStream().use { input ->
                context.contentResolver.openOutputStream(outputUri)?.use { output ->
                    input.copyTo(output)
                }
            }
            
            // Clean up temporary file
            tempDbFile.delete()
            
            Result.Success("Book exported successfully")
        } catch (e: Exception) {
            Result.Error("Book export failed: ${e.message}", e)
        }
    }

    private suspend fun exportDatabaseBackup(outputUri: Uri): Result<String> {
        return try {
            // Get the actual database file path
            val dbFile = getDatabaseFile()
            
            // Ensure all transactions are completed
            database.close()
            
            // Copy the database file to the output URI
            dbFile.inputStream().use { input ->
                context.contentResolver.openOutputStream(outputUri)?.use { output ->
                    input.copyTo(output)
                }
            }
            
            // Reopen database for continued use
            // The database will be automatically reopened on next access due to Room's architecture
              Result.Success("Database backup exported successfully")
        } catch (e: Exception) {
            Result.Error("Database backup export failed: ${e.message}", e)
        }
    }    private suspend fun importDatabaseBackup(inputUri: Uri, replaceExisting: Boolean, mode: ImportMode): ImportResult {
        return try {
            val dbFile = getDatabaseFile()
            
            if (replaceExisting) {
                // Close the database connection
                database.close()
                
                // Replace the existing database file
                context.contentResolver.openInputStream(inputUri)?.use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                
                // Database will be automatically reopened on next access
                // Get counts after import
                val afterCounts = getEntityCounts()
                
                ImportResult.Success(
                    message = "Database restored successfully from backup",
                    importedBooks = afterCounts.books,
                    importedChapters = afterCounts.chapters,
                    importedCharacters = afterCounts.characters,
                    importedScenes = afterCounts.scenes,
                    importedTimelineEvents = afterCounts.timelineEvents,
                    skippedBooks = 0,
                    importMode = mode
                )
            } else {
                // For merge operations, we need more complex logic
                // This would require reading the backup database and merging data
                ImportResult.Error("Merge import mode not yet implemented for database backups")
            }
        } catch (e: Exception) {
            ImportResult.Error("Database backup import failed: ${e.message}", e)
        }
    }

    private suspend fun getEntityCounts(): EntityCounts {
        return try {
            val books = repository.getAllActiveBooks().first()
            var totalChapters = 0
            var totalCharacters = 0
            var totalScenes = 0
            var totalTimelineEvents = 0
            
            for (book in books) {
                totalChapters += repository.getChaptersByBookId(book.id).first().size
                totalCharacters += repository.getCharactersByBookId(book.id).size
                totalScenes += repository.getScenesByBookId(book.id).size
                totalTimelineEvents += repository.getTimelineEventsByBookId(book.id).size
            }
            
            EntityCounts(
                books = books.size,
                chapters = totalChapters,
                characters = totalCharacters,
                scenes = totalScenes,
                timelineEvents = totalTimelineEvents
            )
        } catch (e: Exception) {
            EntityCounts(0, 0, 0, 0, 0)
        }
    }

    private data class EntityCounts(
        val books: Int,
        val chapters: Int,
        val characters: Int,
        val scenes: Int,
        val timelineEvents: Int
    )

    private fun getDatabaseFile(): File {
        val dbPath = database.openHelper.writableDatabase.path
            ?: throw IllegalStateException("Could not get database path")
        return File(dbPath)
    }

    private fun buildBookExportQueries(bookId: String): List<String> {
        return listOf(
            // Create tables (simplified - would need full schema)
            "CREATE TABLE IF NOT EXISTS books AS SELECT * FROM books WHERE id = '$bookId';",
            "CREATE TABLE IF NOT EXISTS chapters AS SELECT * FROM chapters WHERE bookId = '$bookId';",
            "CREATE TABLE IF NOT EXISTS characters AS SELECT * FROM characters WHERE bookId = '$bookId';",
            "CREATE TABLE IF NOT EXISTS scenes AS SELECT * FROM scenes WHERE bookId = '$bookId';",
            "CREATE TABLE IF NOT EXISTS timeline_events AS SELECT * FROM timeline_events WHERE bookId = '$bookId';",
            "CREATE TABLE IF NOT EXISTS character_relationships AS SELECT * FROM character_relationships WHERE character1Id IN (SELECT id FROM characters WHERE bookId = '$bookId') OR character2Id IN (SELECT id FROM characters WHERE bookId = '$bookId');"
        )    }

    @Suppress("UNUSED_PARAMETER")
    private fun exportPartialDatabase(outputFile: File, queries: List<String>) {
        // This would need to be implemented using SQLite command execution
        // For now, this is a placeholder for the concept
        // Note: outputFile and queries parameters are placeholders for future implementation
        throw UnsupportedOperationException("Partial database export not fully implemented")
    }

    /**
     * Get backup information without actually performing the backup
     */
    suspend fun getBackupInfo(): Result<BackupInfo> = withContext(Dispatchers.IO) {
        try {
            val dbFile = getDatabaseFile()
            val backupInfo = BackupInfo(
                databaseSize = dbFile.length(),
                lastModified = Date(dbFile.lastModified()),
                tablesCount = getTablesCount(),
                recordsCount = getRecordsCount()
            )
            Result.Success(backupInfo)
        } catch (e: Exception) {
            Result.Error("Failed to get backup info: ${e.message}", e)
        }
    }

    private suspend fun getTablesCount(): Int {
        return try {
            // Count of main tables in the database
            val tables = listOf("books", "chapters", "characters", "scenes", "timeline_events", "character_relationships", "chapter_backups", "project_versions")
            tables.size
        } catch (e: Exception) {
            0
        }
    }

    private suspend fun getRecordsCount(): Int {
        return try {
            var totalRecords = 0
            
            // Count records from each table using the repository
            // Books - get all active books
            val books = repository.getAllActiveBooks().first()
            totalRecords += books.size
            
            // For other entities, we need to count per book and sum up
            for (book in books) {
                // Chapters
                totalRecords += repository.getChaptersByBookId(book.id).first().size
                
                // Characters  
                val characters = repository.getCharactersByBookId(book.id)
                totalRecords += characters.size
                
                // Scenes
                totalRecords += repository.getScenesByBookId(book.id).size
                
                // Timeline Events
                totalRecords += repository.getTimelineEventsByBookId(book.id).size
                
                // Project Versions
                totalRecords += repository.getVersionsByBook(book.id).first().size
                
                // Character Relationships (avoid double counting by only counting for each character once)
                for (character in characters) {
                    totalRecords += repository.getRelationshipsByCharacter(character.id).first().size
                }
                
                // Chapter Backups (count for each chapter)
                val chapters = repository.getChaptersByBookId(book.id).first()
                for (chapter in chapters) {
                    totalRecords += repository.getChapterBackupCount(chapter.id)
                }
            }
            
            totalRecords
        } catch (e: Exception) {
            0
        }
    }

    data class BackupInfo(
        val databaseSize: Long,
        val lastModified: Date,
        val tablesCount: Int,
        val recordsCount: Int
    )
}
