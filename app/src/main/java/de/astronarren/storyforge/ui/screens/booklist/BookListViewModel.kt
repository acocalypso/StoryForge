package de.astronarren.storyforge.ui.screens.booklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.data.database.entities.Book
import de.astronarren.storyforge.data.model.AdvancedSearchCriteria
import de.astronarren.storyforge.data.model.BookAnalytics
import de.astronarren.storyforge.data.model.BookGenre
import de.astronarren.storyforge.data.model.ExportOptions
import de.astronarren.storyforge.data.model.ExportResult
import de.astronarren.storyforge.data.model.ImportResult
import de.astronarren.storyforge.data.model.SortBy
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import de.astronarren.storyforge.data.service.BookImportExportService
import de.astronarren.storyforge.data.service.StoryForgeImportExportService
import de.astronarren.storyforge.data.service.StoryForgeImportExportService.ImportMode
import de.astronarren.storyforge.data.service.StoryForgeImportExportService.Result
import de.astronarren.storyforge.data.service.StoryForgeImportExportService.ImportResult as ServiceImportResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    private val repository: StoryForgeRepository,
    private val importExportService: BookImportExportService,
    private val storyForgeImportExportService: de.astronarren.storyforge.data.service.StoryForgeImportExportService
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BookListUiState())
    val uiState: StateFlow<BookListUiState> = _uiState.asStateFlow()
    
    private val _searchCriteria = MutableStateFlow(AdvancedSearchCriteria())
    private val allBooks = MutableStateFlow<List<Book>>(emptyList())
    
    init {
        loadBooks()
        setupSearch()
    }
    
    private fun setupSearch() {
        viewModelScope.launch {
            kotlinx.coroutines.flow.combine(
                allBooks,
                _searchCriteria
            ) { books, criteria ->
                criteria.filterBooks(books)
            }.collect { filteredBooks ->
                _uiState.value = _uiState.value.copy(
                    books = filteredBooks,
                    searchCriteria = _searchCriteria.value,
                    isLoading = false
                )
            }
        }
    }    private fun loadBooks() {
        viewModelScope.launch {
            repository.getAllActiveBooks().collect { books ->
                allBooks.value = books
                // The filtering will be handled by setupSearch()
            }
        }
    }
    
    fun updateSearchCriteria(criteria: AdvancedSearchCriteria) {
        _searchCriteria.value = criteria
    }
    
    fun updateSearchQuery(query: String) {
        val currentCriteria = _searchCriteria.value
        _searchCriteria.value = currentCriteria.copy(searchQuery = query)
    }
    
    fun clearSearch() {
        val currentCriteria = _searchCriteria.value
        _searchCriteria.value = currentCriteria.copy(searchQuery = "")
    }
    
    fun filterByGenre(genre: BookGenre?) {
        val currentCriteria = _searchCriteria.value
        val newGenres = if (genre == null) {
            emptySet()
        } else {
            setOf(genre)
        }
        _searchCriteria.value = currentCriteria.copy(genres = newGenres)
    }
    
    fun toggleFavoritesFilter() {
        val currentCriteria = _searchCriteria.value
        _searchCriteria.value = currentCriteria.copy(favoritesOnly = !currentCriteria.favoritesOnly)
    }
    
    fun clearFilters() {
        _searchCriteria.value = AdvancedSearchCriteria()
    }
    
    fun getAvailableAuthors(): List<String> {
        return allBooks.value
            .map { it.author }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }fun createBook(
        title: String, 
        description: String = "", 
        author: String = "",
        genre: BookGenre = BookGenre.OTHER,
        targetWordCount: Int? = null,
        coverImagePath: String? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreatingBook = true, error = null)
                
                val newBook = Book(
                    title = title,
                    description = description,
                    author = author,
                    genre = genre.displayName,
                    targetWordCount = targetWordCount,
                    coverImagePath = coverImagePath
                )
                repository.insertBook(newBook)
                
                _uiState.value = _uiState.value.copy(
                    isCreatingBook = false,
                    showCreateSuccess = true
                )
                
                // Auto-hide success message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(showCreateSuccess = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingBook = false,
                    error = "Failed to create book: ${e.message}"
                )
            }
        }
    }
    
    fun deleteBook(book: Book) {
        viewModelScope.launch {
            try {
                repository.deleteBook(book)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete book: ${e.message}"
                )
            }
        }
    }
      fun archiveBook(bookId: String) {
        viewModelScope.launch {
            try {
                repository.archiveBook(bookId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to archive book: ${e.message}"
                )
            }
        }
    }
    
    fun duplicateBook(book: Book) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isCreatingBook = true, error = null)
                
                val duplicatedBook = book.copy(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "${book.title} (Copy)",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    version = 1,
                    wordCount = 0 // Reset word count for the copy
                )
                
                repository.insertBook(duplicatedBook)
                
                _uiState.value = _uiState.value.copy(
                    isCreatingBook = false,
                    showCreateSuccess = true
                )
                
                // Auto-hide success message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(showCreateSuccess = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreatingBook = false,
                    error = "Failed to duplicate book: ${e.message}"
                )
            }
        }    }
    
    fun toggleFavorite(book: Book) {
        viewModelScope.launch {
            try {
                val updatedBook = book.copy(
                    isFavorite = !book.isFavorite,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateBook(updatedBook)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update favorite: ${e.message}"
                )
            }
        }
    }
      fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun getAnalytics(): BookAnalytics {
        return BookAnalytics.fromBooks(allBooks.value)
    }
    
    // Import/Export Functions
    fun exportBooks(options: ExportOptions) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExporting = true, error = null)
                
                val result = importExportService.exportBooks(allBooks.value, options)
                
                when (result) {
                    is ExportResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            exportResult = result
                        )
                    }
                    is ExportResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }
    
    fun importBooks(uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isImporting = true, error = null)
                
                val result = importExportService.importBooks(uri)
                
                when (result) {                    is ImportResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isImporting = false,
                            importPreview = result.importedBooks
                        )
                    }
                    is ImportResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isImporting = false,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "Import failed: ${e.message}"
                )
            }
        }
    }
    
    fun confirmImport() {
        viewModelScope.launch {
            try {
                val booksToImport = _uiState.value.importPreview ?: return@launch
                
                _uiState.value = _uiState.value.copy(isImporting = true, error = null)
                
                var importedCount = 0
                booksToImport.forEach { book ->
                    try {
                        repository.insertBook(book)
                        importedCount++
                    } catch (e: Exception) {
                        // Log individual book import errors but continue
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importPreview = null,
                    showImportSuccess = true
                )
                
                // Auto-hide success message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(showImportSuccess = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    error = "Import confirmation failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearImportPreview() {
        _uiState.value = _uiState.value.copy(importPreview = null)
    }
      fun clearExportResult() {
        _uiState.value = _uiState.value.copy(exportResult = null)
    }    /**
     * Export all data to .storyforge format
     */
    fun exportAllData(fileName: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isExportingAll = true, error = null)
                
                val result = storyForgeImportExportService.exportAllData(fileName = fileName)                
                when (result) {
                    is StoryForgeImportExportService.Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isExportingAll = false,
                            comprehensiveExportResult = result.data
                        )
                    }
                    is StoryForgeImportExportService.Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isExportingAll = false,
                            error = "Export failed: ${result.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExportingAll = false,
                    error = "Export failed: ${e.message}"
                )
            }
        }
    }    /**
     * Import data from .storyforge format
     */
    fun importAllData(fileUri: android.net.Uri, importMode: StoryForgeImportExportService.ImportMode) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isImportingAll = true, error = null)
                
                val result = storyForgeImportExportService.importData(
                    fileUri = fileUri,
                    importMode = importMode
                )                
                when (result) {
                    is StoryForgeImportExportService.Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isImportingAll = false,
                            comprehensiveImportResult = result.data
                        )
                        // Refresh the book list
                        loadBooks()
                    }
                    is StoryForgeImportExportService.Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isImportingAll = false,
                            error = "Import failed: ${result.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isImportingAll = false,
                    error = "Import failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearComprehensiveExportResult() {
        _uiState.value = _uiState.value.copy(comprehensiveExportResult = null)
    }
    
    fun clearComprehensiveImportResult() {
        _uiState.value = _uiState.value.copy(comprehensiveImportResult = null)
    }
    
    fun getAvailableGenres(): List<String> {
        return allBooks.value
            .map { it.genre }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }
}

data class BookListUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val isCreatingBook: Boolean = false,
    val showCreateSuccess: Boolean = false,
    val searchCriteria: AdvancedSearchCriteria = AdvancedSearchCriteria(),
    val error: String? = null,
    // Import/Export states
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportResult: ExportResult.Success? = null,
    val importPreview: List<Book>? = null,
    val showImportSuccess: Boolean = false,
    // Comprehensive Import/Export states
    val isExportingAll: Boolean = false,
    val isImportingAll: Boolean = false,
    val comprehensiveExportResult: String? = null,
    val comprehensiveImportResult: ServiceImportResult? = null
) {
    // Backward compatibility properties
    val searchQuery: String get() = searchCriteria.searchQuery
    val selectedGenre: BookGenre? get() = searchCriteria.genres.firstOrNull()
    val showFavoritesOnly: Boolean get() = searchCriteria.favoritesOnly
}

