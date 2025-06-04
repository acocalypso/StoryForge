package de.astronarren.storyforge.ui.screens.chapterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.data.database.entities.Chapter
import de.astronarren.storyforge.data.model.ExportFormat
import de.astronarren.storyforge.data.model.ExportResult
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import de.astronarren.storyforge.data.service.ChapterExportService
import de.astronarren.storyforge.utils.PermissionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChapterListViewModel @Inject constructor(
    private val repository: StoryForgeRepository,
    private val exportService: ChapterExportService,
    val permissionManager: PermissionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChapterListUiState())
    val uiState: StateFlow<ChapterListUiState> = _uiState.asStateFlow()
    
    fun loadChapters(bookId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                repository.getChaptersByBookId(bookId).collect { chapters ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            chapters = chapters.sortedBy { chapter -> chapter.order }
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
    }
    
    fun addNewChapter(bookId: String) {
        viewModelScope.launch {
            try {
                val maxOrder = repository.getMaxChapterOrder(bookId) ?: 0
                val newChapter = Chapter(
                    id = UUID.randomUUID().toString(),
                    bookId = bookId,
                    title = "New Chapter",
                    description = "",
                    content = "",
                    order = maxOrder + 1,
                    wordCount = 0,
                    targetWordCount = null,
                    notes = "",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isCompleted = false,
                    color = null
                )
                
                repository.insertChapter(newChapter)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to create chapter: ${e.message}")
                }
            }
        }
    }
    
    fun deleteChapter(chapter: Chapter) {
        viewModelScope.launch {
            try {
                repository.deleteChapter(chapter)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Failed to delete chapter: ${e.message}")
                }
            }
        }
    }
      fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Exports all chapters of a book in the specified format
     */
    fun exportBook(bookId: String, format: ExportFormat) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isExporting = true, error = null) }
                
                val book = repository.getBookById(bookId)
                if (book == null) {
                    _uiState.update { 
                        it.copy(
                            isExporting = false,
                            error = "Book not found"                        )
                    }
                    return@launch
                }
                  val chapters = repository.getChaptersByBookId(bookId).first()
                // Generate filename with book title, timestamp and proper file extension
                val fileName = "${book.title.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${System.currentTimeMillis()}.${format.extension}"
                val result = exportService.exportBook(book, chapters, format, fileName)
                
                when (result) {
                    is ExportResult.Success -> {
                        _uiState.update { 
                            it.copy(
                                isExporting = false,
                                error = null,
                                exportResult = result
                            )
                        }
                    }
                    is ExportResult.Error -> {
                        _uiState.update { 
                            it.copy(
                                isExporting = false,
                                error = "Export failed: ${result.message}",
                                exportResult = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isExporting = false,
                        error = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Clear export results
     */
    fun clearExportResult() {
        _uiState.update { it.copy(exportResult = null, error = null) }
    }
}

data class ChapterListUiState(
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val error: String? = null,
    val chapters: List<Chapter> = emptyList(),
    val exportResult: ExportResult? = null
)
