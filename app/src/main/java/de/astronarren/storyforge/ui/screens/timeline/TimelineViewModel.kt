package de.astronarren.storyforge.ui.screens.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.data.database.entities.EventType
import de.astronarren.storyforge.data.database.entities.TimelineEvent
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.data.database.entities.Scene
import de.astronarren.storyforge.data.database.entities.Book
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: StoryForgeRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private val _selectedBookId = MutableStateFlow("")
    
    // Current book for resolving book ID to title
    private val _currentBook = MutableStateFlow<Book?>(null)
    val currentBook: StateFlow<Book?> = _currentBook.asStateFlow()
    
    // Characters and scenes for the current book
    private val _characters = MutableStateFlow<List<Character>>(emptyList())
    val characters: StateFlow<List<Character>> = _characters.asStateFlow()
      private val _scenes = MutableStateFlow<List<Scene>>(emptyList())
    val scenes: StateFlow<List<Scene>> = _scenes.asStateFlow()
      
    fun initialize(bookId: String) {
        _selectedBookId.value = bookId
        loadCurrentBook(bookId)
        loadTimelineEvents(bookId)
        loadCharacters(bookId)
        loadScenes(bookId)
    }
    
    private fun loadTimelineEvents(bookId: String) {
        viewModelScope.launch {
            repository.getTimelineEventsByBook(bookId)
                .catch { throwable ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = "Failed to load timeline events: ${throwable.message}"
                        )
                    }
                }                .collect { events ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            timelineEvents = events,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
        }
    }
    
    private fun loadCharacters(bookId: String) {
        viewModelScope.launch {
            repository.getCharactersByBook(bookId)
                .catch { throwable ->
                    // Handle error silently for characters as it's not critical for timeline functionality
                }
                .collect { characters ->
                    _characters.value = characters
                }
        }
    }
      private fun loadScenes(bookId: String) {
        viewModelScope.launch {
            repository.getScenesByBook(bookId)
                .catch { throwable ->
                    // Handle error silently for scenes as it's not critical for timeline functionality
                }
                .collect { scenes ->
                    _scenes.value = scenes
                }
        }
    }
    
    private fun loadCurrentBook(bookId: String) {
        viewModelScope.launch {
            try {
                val book = repository.getBookById(bookId)
                _currentBook.value = book
            } catch (throwable: Throwable) {
                // Handle error silently for book as timeline can still function without book title
            }
        }
    }
        fun createTimelineEvent(
        title: String,
        description: String,
        date: String,
        time: String,
        eventType: EventType,
        charactersInvolved: List<String>,
        location: String,
        importance: Int,
        notes: String,
        tags: List<String>,
        color: String?,
        relatedScenes: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            try {
                // Get current events to determine the next order
                val currentEvents = _uiState.value.timelineEvents
                val maxOrder = currentEvents.maxOfOrNull { it.order } ?: 0
                  val newEvent = TimelineEvent(
                    bookId = _selectedBookId.value,
                    title = title,
                    description = description,
                    date = date,
                    time = time,
                    order = maxOrder + 1,
                    eventType = eventType,
                    charactersInvolved = charactersInvolved,
                    location = location,
                    relatedScenes = relatedScenes,
                    importance = importance,
                    notes = notes,
                    tags = tags,
                    color = color
                )
                repository.insertTimelineEvent(newEvent)
                clearCreateEventDialog()
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(errorMessage = "Failed to create event: ${e.message}")
                }
            }
        }
    }
    
    fun updateTimelineEvent(event: TimelineEvent) {
        viewModelScope.launch {
            try {
                repository.updateTimelineEvent(event.copy(updatedAt = System.currentTimeMillis()))
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(errorMessage = "Failed to update event: ${e.message}")
                }
            }
        }
    }
    
    fun deleteTimelineEvent(event: TimelineEvent) {
        viewModelScope.launch {
            try {
                repository.deleteTimelineEvent(event)
            } catch (e: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(errorMessage = "Failed to delete event: ${e.message}")
                }
            }
        }
    }
    
    fun reorderTimelineEvents(fromIndex: Int, toIndex: Int) {
        val events = _uiState.value.timelineEvents.toMutableList()
        if (fromIndex in events.indices && toIndex in events.indices) {
            val movedEvent = events.removeAt(fromIndex)
            events.add(toIndex, movedEvent)
            
            // Update order for all affected events
            viewModelScope.launch {
                try {
                    events.forEachIndexed { index, event ->
                        if (event.order != index + 1) {
                            repository.updateTimelineEvent(event.copy(order = index + 1))
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { currentState ->
                        currentState.copy(errorMessage = "Failed to reorder events: ${e.message}")
                    }
                }
            }
        }
    }
    
    fun showCreateEventDialog() {
        _uiState.update { currentState ->
            currentState.copy(isCreateDialogVisible = true)
        }
    }
    
    fun clearCreateEventDialog() {
        _uiState.update { currentState ->
            currentState.copy(isCreateDialogVisible = false)
        }
    }
    
    fun showEditEventDialog(event: TimelineEvent) {
        _uiState.update { currentState ->
            currentState.copy(
                isEditDialogVisible = true,
                selectedEvent = event
            )
        }
    }
    
    fun clearEditEventDialog() {
        _uiState.update { currentState ->
            currentState.copy(
                isEditDialogVisible = false,
                selectedEvent = null
            )
        }
    }
    
    fun setFilterEventType(eventType: EventType?) {
        _uiState.update { currentState ->
            currentState.copy(filterEventType = eventType)
        }
    }
    
    fun setFilterImportance(minImportance: Int?) {
        _uiState.update { currentState ->
            currentState.copy(filterMinImportance = minImportance)
        }
    }
    
    fun clearErrorMessage() {
        _uiState.update { currentState ->
            currentState.copy(errorMessage = null)
        }
    }
}

data class TimelineUiState(
    val timelineEvents: List<TimelineEvent> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isCreateDialogVisible: Boolean = false,
    val isEditDialogVisible: Boolean = false,
    val selectedEvent: TimelineEvent? = null,
    val filterEventType: EventType? = null,
    val filterMinImportance: Int? = null
)
