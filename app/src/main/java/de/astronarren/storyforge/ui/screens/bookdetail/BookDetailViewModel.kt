package de.astronarren.storyforge.ui.screens.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.data.database.entities.Book
import de.astronarren.storyforge.data.model.ExportFormat
import de.astronarren.storyforge.data.model.ExportResult
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import de.astronarren.storyforge.data.service.ChapterExportService
import de.astronarren.storyforge.utils.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Export state for BookDetail screen
 */
data class ExportState(
    val isExporting: Boolean = false,
    val result: ExportResult? = null,
    val error: String? = null
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val repository: StoryForgeRepository,
    private val exportService: ChapterExportService,
    val permissionManager: PermissionManager
) : ViewModel() {    
    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()
    
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _exportState = MutableStateFlow(ExportState())
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()
      private val _currentBookId = MutableStateFlow<String?>(null)
    private val _chapterCount = MutableStateFlow(0)
    
    fun loadBook(bookId: String) {
        _currentBookId.value = bookId
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val book = repository.getBookById(bookId)
                _currentBook.value = book
                
                // Load chapter count
                val chapters = repository.getChaptersByBookId(bookId).first()
                _chapterCount.value = chapters.size
            } catch (throwable: Throwable) {
                // Handle error silently - book details screen can still function
                _currentBook.value = null
                _chapterCount.value = 0
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Get the chapter count for the current book
     */
    fun getChapterCount(): Int {
        return _chapterCount.value
    }    /**
     * Export the current book in the specified format
     */
    fun exportBook(format: ExportFormat) {
        val bookId = _currentBookId.value ?: return
        val book = _currentBook.value ?: return
        
        viewModelScope.launch {
            try {
                _exportState.value = _exportState.value.copy(isExporting = true, error = null, result = null)                // Get chapters for the book
                val chapters = repository.getChaptersByBookId(bookId).first()
                
                // Generate filename with book title, timestamp and proper file extension
                val fileName = "${book.title.replace("[^a-zA-Z0-9]".toRegex(), "_")}_${System.currentTimeMillis()}.${format.extension}"
                val result = exportService.exportBook(book, chapters, format, fileName)
                when (result) {
                    is ExportResult.Success -> {
                        _exportState.value = _exportState.value.copy(
                            isExporting = false,
                            result = result,
                            error = null
                        )
                    }
                    is ExportResult.Error -> {
                        _exportState.value = _exportState.value.copy(
                            isExporting = false,
                            result = null,
                            error = "Export failed: ${result.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _exportState.value = _exportState.value.copy(
                    isExporting = false,
                    result = null,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Clear export results
     */
    fun clearExportResult() {
        _exportState.value = _exportState.value.copy(result = null, error = null)
    }
}
