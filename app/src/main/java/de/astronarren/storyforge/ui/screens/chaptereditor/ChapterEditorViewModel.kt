package de.astronarren.storyforge.ui.screens.chaptereditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.data.database.entities.Chapter
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.data.database.entities.Scene
import de.astronarren.storyforge.data.database.entities.TimelineEvent
import de.astronarren.storyforge.data.database.entities.ChapterBackup
import de.astronarren.storyforge.data.models.AutoSaveState
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import de.astronarren.storyforge.services.AutoSaveManager
import de.astronarren.storyforge.ui.components.richtext.*
import de.astronarren.storyforge.utils.PermissionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ChapterEditorViewModel @Inject constructor(
    private val repository: StoryForgeRepository,
    private val autoSaveManager: AutoSaveManager,
    val permissionManager: PermissionManager
) : ViewModel() {
      private val _uiState = MutableStateFlow(ChapterEditorUiState())
    val uiState: StateFlow<ChapterEditorUiState> = _uiState.asStateFlow()
    
    // AutoSave state from AutoSaveManager
    val autoSaveState = autoSaveManager.autoSaveState
    
    // Backups state
    private val _backups = MutableStateFlow<List<ChapterBackup>>(emptyList())
    val backups: StateFlow<List<ChapterBackup>> = _backups.asStateFlow()
    
    private var originalChapter: Chapter? = null
    private var hasUnsavedChanges = false
    
    fun loadChapter(chapterId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val chapter = repository.getChapterById(chapterId)
                if (chapter != null) {
                    originalChapter = chapter
                    
                    // Parse rich text content
                    val richTextDocument = if (chapter.content.isNotBlank()) {
                        try {
                            Json.decodeFromString<RichTextDocument>(chapter.content)
                        } catch (e: Exception) {
                            // Fallback to plain text if parsing fails
                            RichTextDocument.fromPlainText(chapter.content)
                        }
                    } else {
                        RichTextDocument()
                    }
                      // Load related story elements
                    val characters = repository.getCharactersByBookId(chapter.bookId)
                    val scenes = repository.getScenesByBookId(chapter.bookId)
                    val timelineEvents = repository.getTimelineEventsByBookId(chapter.bookId)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapter = chapter,
                            richTextDocument = richTextDocument,
                            characters = characters,
                            scenes = scenes,
                            timelineEvents = timelineEvents
                        )
                    }
                    
                    // Load backups for this chapter
                    loadBackups(chapter.id)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Chapter not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }    fun updateContent(document: RichTextDocument) {
        _uiState.update { 
            it.copy(richTextDocument = document) 
        }
        hasUnsavedChanges = true
        
        // Use AutoSaveManager for debounced saving
        val currentChapter = _uiState.value.chapter
        if (currentChapter != null) {
            autoSaveManager.scheduleAutoSave(
                scope = viewModelScope,
                chapter = currentChapter,
                richTextDocument = document,
                onSaveComplete = { success, error ->
                    if (success) {
                        hasUnsavedChanges = false
                        // Update the chapter in UI state
                        _uiState.update { state ->
                            state.chapter?.let { chapter ->
                                val updatedChapter = chapter.copy(
                                    content = Json.encodeToString(document),
                                    wordCount = document.wordCount,
                                    updatedAt = System.currentTimeMillis()
                                )
                                state.copy(chapter = updatedChapter)
                            } ?: state
                        }
                    } else {
                        _uiState.update { it.copy(error = error ?: "Save failed") }
                    }
                }
            )
        }
    }
      fun updateChapterTitle(newTitle: String) {
        val currentChapter = _uiState.value.chapter ?: return
        val updatedChapter = currentChapter.copy(
            title = newTitle,
            updatedAt = System.currentTimeMillis()
        )
        
        _uiState.update {
            it.copy(chapter = updatedChapter)
        }
        hasUnsavedChanges = true
          // Save the title change immediately using AutoSaveManager
        viewModelScope.launch {
            try {
                repository.updateChapter(updatedChapter)
                hasUnsavedChanges = false
                originalChapter = updatedChapter
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to save title: ${e.message}") }
            }
        }
    }
    
    /**
     * Internal save method used for auto-save operations
     */
    private suspend fun saveChapterInternal() {
        val currentState = _uiState.value
        val chapter = currentState.chapter ?: return
        
        val result = autoSaveManager.forceSave(chapter, currentState.richTextDocument)
        result.onSuccess { updatedChapter ->
            hasUnsavedChanges = false
            originalChapter = updatedChapter
            _uiState.update { it.copy(chapter = updatedChapter) }
        }.onFailure { error ->
            _uiState.update { it.copy(error = "Failed to save: ${error.message}") }
        }
    }fun addStoryReference(reference: StoryElementReference) {
        val currentDocument = _uiState.value.richTextDocument
        val referenceText = "@${reference.type.toString().lowercase()}:${reference.displayName}"
        
        // Add reference text to the current content
        val updatedPlainText = if (currentDocument.plainText.isEmpty()) {
            referenceText
        } else {
            "${currentDocument.plainText} $referenceText"
        }
        
        // Create updated document with the new text and reference
        val updatedDocument = RichTextDocument.fromPlainText(updatedPlainText).copy(
            storyReferences = currentDocument.storyReferences + reference
        )
        
        _uiState.update {
            it.copy(richTextDocument = updatedDocument)
        }
        hasUnsavedChanges = true
    }
      fun saveChapter() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val chapter = currentState.chapter ?: return@launch
            
            val result = autoSaveManager.forceSave(chapter, currentState.richTextDocument)
            result.onSuccess { updatedChapter ->
                hasUnsavedChanges = false
                originalChapter = updatedChapter
                _uiState.update { it.copy(chapter = updatedChapter) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = "Failed to save: ${error.message}") }
            }
        }
    }
    
    /**
     * Creates a manual backup with optional description
     */
    fun createManualBackup(description: String? = null) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val chapter = currentState.chapter ?: return@launch
            
            val result = autoSaveManager.createManualBackup(
                chapterId = chapter.id,
                chapter = chapter,
                richTextDocument = currentState.richTextDocument,
                description = description
            )
            
            result.onSuccess {
                loadBackups(chapter.id)
                _uiState.update { it.copy(error = null) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = "Failed to create backup: ${error.message}") }
            }
        }
    }
    
    /**
     * Restores chapter content from a backup
     */
    fun restoreFromBackup(backup: ChapterBackup) {
        viewModelScope.launch {
            val currentChapter = _uiState.value.chapter ?: return@launch
            
            val result = autoSaveManager.restoreFromBackup(currentChapter, backup)
            result.onSuccess { (restoredChapter, richTextDocument) ->
                originalChapter = restoredChapter
                hasUnsavedChanges = false
                _uiState.update {
                    it.copy(
                        chapter = restoredChapter,
                        richTextDocument = richTextDocument,
                        error = null
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(error = "Failed to restore backup: ${error.message}") }
            }        }
    }

    /**
     * Loads available backups for the current chapter
     */
    fun loadBackups(chapterId: String) {
        viewModelScope.launch {
            val backupsList = autoSaveManager.getBackups(chapterId)
            _backups.update { backupsList }
        }
    }
    
    /**
     * Deletes a specific backup
     */
    fun deleteBackup(backup: ChapterBackup) {
        viewModelScope.launch {
            val result = autoSaveManager.deleteBackup(backup)
            result.onSuccess {
                val currentChapter = _uiState.value.chapter
                if (currentChapter != null) {
                    loadBackups(currentChapter.id)
                }
            }.onFailure { error ->
                _uiState.update { it.copy(error = "Failed to delete backup: ${error.message}") }
            }
        }
    }

    /**
     * Gets autosave status text for UI display
     */    fun getAutoSaveStatusText(state: AutoSaveState): String {
        return autoSaveManager.getStatusText(state)
    }
}

data class ChapterEditorUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val chapter: Chapter? = null,
    val richTextDocument: RichTextDocument = RichTextDocument(),
    val characters: List<Character> = emptyList(),
    val scenes: List<Scene> = emptyList(),
    val timelineEvents: List<TimelineEvent> = emptyList()
)
