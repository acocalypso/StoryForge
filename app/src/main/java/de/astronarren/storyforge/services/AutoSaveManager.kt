package de.astronarren.storyforge.services

import de.astronarren.storyforge.data.database.entities.Chapter
import de.astronarren.storyforge.data.database.entities.ChapterBackup
import de.astronarren.storyforge.data.database.entities.BackupType
import de.astronarren.storyforge.data.models.AutoSaveConfig
import de.astronarren.storyforge.data.models.AutoSaveState
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import de.astronarren.storyforge.ui.components.richtext.RichTextDocument
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for managing automatic saving and backup functionality
 */
@Singleton
class AutoSaveManager @Inject constructor(
    private val repository: StoryForgeRepository
) {
    private val _autoSaveState = MutableStateFlow(AutoSaveState.IDLE)
    val autoSaveState: StateFlow<AutoSaveState> = _autoSaveState.asStateFlow()
    
    private val _config = MutableStateFlow(AutoSaveConfig())
    val config: StateFlow<AutoSaveConfig> = _config.asStateFlow()
    
    private var currentSaveJob: Job? = null
    private var lastAutoBackupTime = 0L
    
    /**
     * Updates autosave configuration
     */
    fun updateConfig(newConfig: AutoSaveConfig) {
        _config.value = newConfig
    }
    
    /**
     * Schedules an autosave with debouncing
     */
    fun scheduleAutoSave(
        scope: CoroutineScope,
        chapter: Chapter,
        richTextDocument: RichTextDocument,
        debounceMs: Long = _config.value.contentDebounceMs,
        onSaveComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (!_config.value.isEnabled) return
        
        // Cancel any existing save job
        currentSaveJob?.cancel()
        
        // Set state to pending
        _autoSaveState.value = AutoSaveState.PENDING
          // Schedule new save job with debouncing
        currentSaveJob = scope.launch {
            try {
                delay(debounceMs)
                
                _autoSaveState.value = AutoSaveState.SAVING
                
                // Save the chapter
                val contentJson = Json.encodeToString(richTextDocument)
                val updatedChapter = chapter.copy(
                    content = contentJson,
                    wordCount = richTextDocument.wordCount,
                    updatedAt = System.currentTimeMillis()
                )
                
                repository.updateChapter(updatedChapter)
                
                // Check if we need to create an automatic backup
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastAutoBackupTime >= _config.value.backupIntervalMs) {
                    createAutoBackup(chapter.id, updatedChapter, richTextDocument)
                    lastAutoBackupTime = currentTime
                }
                
                _autoSaveState.value = AutoSaveState.SAVED
                onSaveComplete(true, null)
                
                // Reset to idle after showing "saved" state briefly
                delay(2000)
                if (_autoSaveState.value == AutoSaveState.SAVED) {
                    _autoSaveState.value = AutoSaveState.IDLE
                }
                  } catch (e: kotlinx.coroutines.CancellationException) {
                // Coroutine was cancelled - this is normal during debouncing
                // Don't show error to user, just reset to idle state
                _autoSaveState.value = AutoSaveState.IDLE
                // Important: Don't call onSaveComplete, as this is not an error
                throw e // Re-throw to properly handle cancellation
            } catch (e: Exception) {
                _autoSaveState.value = AutoSaveState.ERROR
                onSaveComplete(false, e.message)
                
                // Reset to idle after showing error state briefly
                delay(3000)
                if (_autoSaveState.value == AutoSaveState.ERROR) {
                    _autoSaveState.value = AutoSaveState.IDLE
                }
            }
        }
    }
    
    /**
     * Forces an immediate save without debouncing
     */
    suspend fun forceSave(
        chapter: Chapter,
        richTextDocument: RichTextDocument
    ): Result<Chapter> {
        return try {
            _autoSaveState.value = AutoSaveState.SAVING
            
            val contentJson = Json.encodeToString(richTextDocument)
            val updatedChapter = chapter.copy(
                content = contentJson,
                wordCount = richTextDocument.wordCount,
                updatedAt = System.currentTimeMillis()
            )
            
            repository.updateChapter(updatedChapter)
            
            _autoSaveState.value = AutoSaveState.SAVED
            
            // Reset to idle after brief delay
            delay(1000)
            if (_autoSaveState.value == AutoSaveState.SAVED) {
                _autoSaveState.value = AutoSaveState.IDLE
            }
            
            Result.success(updatedChapter)
        } catch (e: Exception) {
            _autoSaveState.value = AutoSaveState.ERROR
            Result.failure(e)
        }
    }
    
    /**
     * Creates a manual backup with optional description
     */
    suspend fun createManualBackup(
        chapterId: String,
        chapter: Chapter,
        richTextDocument: RichTextDocument,
        description: String? = null
    ): Result<ChapterBackup> {
        return try {
            val backup = ChapterBackup(
                id = UUID.randomUUID().toString(),
                chapterId = chapterId,
                backupType = BackupType.MANUAL,
                title = chapter.title,
                content = Json.encodeToString(richTextDocument),
                wordCount = richTextDocument.wordCount,
                createdAt = System.currentTimeMillis(),
                description = description
            )
            
            repository.insertChapterBackup(backup)
            
            // Clean up old backups if we exceed the limit
            cleanupOldBackups(chapterId)
            
            Result.success(backup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Creates an automatic backup
     */
    private suspend fun createAutoBackup(
        chapterId: String,
        chapter: Chapter,
        richTextDocument: RichTextDocument
    ) {
        try {
            val backup = ChapterBackup(
                id = UUID.randomUUID().toString(),
                chapterId = chapterId,
                backupType = BackupType.AUTO,
                title = chapter.title,
                content = Json.encodeToString(richTextDocument),
                wordCount = richTextDocument.wordCount,
                createdAt = System.currentTimeMillis(),
                description = "Automatic backup"
            )
            
            repository.insertChapterBackup(backup)
            cleanupOldBackups(chapterId)
        } catch (e: Exception) {
            // Log error but don't fail the main save operation
            println("Failed to create auto backup: ${e.message}")
        }
    }
    
    /**
     * Restores chapter content from a backup
     */
    suspend fun restoreFromBackup(
        chapter: Chapter,
        backup: ChapterBackup
    ): Result<Pair<Chapter, RichTextDocument>> {
        return try {
            // Parse the backed up content
            val richTextDocument = Json.decodeFromString<RichTextDocument>(backup.content)
            
            // Create updated chapter with backup content
            val restoredChapter = chapter.copy(
                title = backup.title,
                content = backup.content,
                wordCount = backup.wordCount,
                updatedAt = System.currentTimeMillis()
            )
            
            // Save the restored chapter
            repository.updateChapter(restoredChapter)
            
            Result.success(Pair(restoredChapter, richTextDocument))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets list of backups for a chapter
     */
    suspend fun getBackups(chapterId: String): List<ChapterBackup> {
        return try {
            repository.getChapterBackups(chapterId)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Deletes a specific backup
     */
    suspend fun deleteBackup(backup: ChapterBackup): Result<Unit> {
        return try {
            repository.deleteChapterBackup(backup)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Cleans up old backups to maintain the configured limit
     */
    private suspend fun cleanupOldBackups(chapterId: String) {
        try {
            repository.deleteOldChapterBackups(chapterId, _config.value.maxBackups)
        } catch (e: Exception) {
            // Log but don't fail
            println("Failed to cleanup old backups: ${e.message}")
        }
    }
    
    /**
     * Cancels any pending autosave
     */
    fun cancelPendingSave() {
        currentSaveJob?.cancel()
        _autoSaveState.value = AutoSaveState.IDLE
    }
    
    /**
     * Gets current autosave status for display
     */
    fun getStatusText(state: AutoSaveState): String {
        return when (state) {
            AutoSaveState.IDLE -> ""
            AutoSaveState.PENDING -> "Changes pending..."
            AutoSaveState.SAVING -> "Saving..."
            AutoSaveState.SAVED -> "Saved"
            AutoSaveState.ERROR -> "Save failed"
        }
    }
}
