package de.astronarren.storyforge.ui.screens.scenelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.data.database.entities.Scene
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SceneListViewModel @Inject constructor(
    private val repository: StoryForgeRepository
) : ViewModel() {

    data class SceneListUiState(
        val scenes: List<Scene> = emptyList(),
        val filteredScenes: List<Scene> = emptyList(),
        val searchQuery: String = "",
        val selectedLocation: String = "",
        val selectedTimeOfDay: String = "",
        val selectedMood: String = "",
        val sortBy: SortOption = SortOption.ORDER,
        val isLoading: Boolean = false,
        val error: String? = null,
        val showDialog: Boolean = false,
        val editingScene: Scene? = null,
        val uniqueLocations: List<String> = emptyList(),
        val uniqueTimesOfDay: List<String> = emptyList(),
        val uniqueMoods: List<String> = emptyList()
    )

    enum class SortOption {
        ORDER, TITLE, LOCATION, TIME_OF_DAY, MOOD, CREATED_DATE, WORD_COUNT
    }

    private val _uiState = MutableStateFlow(SceneListUiState())
    val uiState: StateFlow<SceneListUiState> = _uiState.asStateFlow()

    private var currentBookId: String = ""

    fun initialize(bookId: String) {
        currentBookId = bookId
        loadScenes()
    }

    private fun loadScenes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                repository.getScenesByBook(currentBookId).collect { scenes ->
                    _uiState.update { currentState ->
                        val uniqueLocations = scenes.map { it.location }.filter { it.isNotBlank() }.distinct().sorted()
                        val uniqueTimesOfDay = scenes.map { it.timeOfDay }.filter { it.isNotBlank() }.distinct().sorted()
                        val uniqueMoods = scenes.map { it.mood }.filter { it.isNotBlank() }.distinct().sorted()
                        
                        currentState.copy(
                            scenes = scenes,
                            isLoading = false,
                            uniqueLocations = uniqueLocations,
                            uniqueTimesOfDay = uniqueTimesOfDay,
                            uniqueMoods = uniqueMoods
                        )
                    }
                    applyFiltersAndSort()
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = "Failed to load scenes: ${e.message}"
                    ) 
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFiltersAndSort()
    }

    fun updateLocationFilter(location: String) {
        _uiState.update { it.copy(selectedLocation = location) }
        applyFiltersAndSort()
    }

    fun updateTimeOfDayFilter(timeOfDay: String) {
        _uiState.update { it.copy(selectedTimeOfDay = timeOfDay) }
        applyFiltersAndSort()
    }

    fun updateMoodFilter(mood: String) {
        _uiState.update { it.copy(selectedMood = mood) }
        applyFiltersAndSort()
    }

    fun updateSortOption(sortOption: SortOption) {
        _uiState.update { it.copy(sortBy = sortOption) }
        applyFiltersAndSort()
    }

    private fun applyFiltersAndSort() {
        val currentState = _uiState.value
        var filteredScenes = currentState.scenes

        // Apply search query filter
        if (currentState.searchQuery.isNotBlank()) {
            filteredScenes = filteredScenes.filter { scene ->
                scene.title.contains(currentState.searchQuery, ignoreCase = true) ||
                scene.content.contains(currentState.searchQuery, ignoreCase = true) ||
                scene.summary.contains(currentState.searchQuery, ignoreCase = true) ||
                scene.location.contains(currentState.searchQuery, ignoreCase = true) ||
                scene.notes.contains(currentState.searchQuery, ignoreCase = true) ||
                scene.tags.any { it.contains(currentState.searchQuery, ignoreCase = true) }
            }
        }

        // Apply location filter
        if (currentState.selectedLocation.isNotBlank()) {
            filteredScenes = filteredScenes.filter { 
                it.location.equals(currentState.selectedLocation, ignoreCase = true) 
            }
        }

        // Apply time of day filter
        if (currentState.selectedTimeOfDay.isNotBlank()) {
            filteredScenes = filteredScenes.filter { 
                it.timeOfDay.equals(currentState.selectedTimeOfDay, ignoreCase = true) 
            }
        }

        // Apply mood filter
        if (currentState.selectedMood.isNotBlank()) {
            filteredScenes = filteredScenes.filter { 
                it.mood.equals(currentState.selectedMood, ignoreCase = true) 
            }
        }

        // Apply sorting
        filteredScenes = when (currentState.sortBy) {
            SortOption.ORDER -> filteredScenes.sortedBy { it.order }
            SortOption.TITLE -> filteredScenes.sortedBy { it.title.lowercase() }
            SortOption.LOCATION -> filteredScenes.sortedBy { it.location.lowercase() }
            SortOption.TIME_OF_DAY -> filteredScenes.sortedBy { it.timeOfDay.lowercase() }
            SortOption.MOOD -> filteredScenes.sortedBy { it.mood.lowercase() }
            SortOption.CREATED_DATE -> filteredScenes.sortedByDescending { it.createdAt }
            SortOption.WORD_COUNT -> filteredScenes.sortedByDescending { it.wordCount }
        }

        _uiState.update { it.copy(filteredScenes = filteredScenes) }
    }

    fun showCreateDialog() {
        _uiState.update { it.copy(showDialog = true, editingScene = null) }
    }

    fun showEditDialog(scene: Scene) {
        _uiState.update { it.copy(showDialog = true, editingScene = scene) }
    }

    fun hideDialog() {
        _uiState.update { it.copy(showDialog = false, editingScene = null) }
    }

    fun createScene(
        title: String,
        summary: String,
        location: String,
        timeOfDay: String,
        mood: String,
        purpose: String,
        pointOfView: String,
        conflictLevel: Int,
        tags: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                val maxOrder = repository.getMaxOrderForBook(currentBookId) ?: 0
                val newScene = Scene(
                    bookId = currentBookId,
                    title = title,
                    summary = summary,
                    location = location,
                    timeOfDay = timeOfDay,
                    mood = mood,
                    purpose = purpose,
                    pointOfView = pointOfView,
                    conflictLevel = conflictLevel,
                    tags = tags,
                    notes = notes,
                    order = maxOrder + 1
                )
                repository.insertScene(newScene)
                hideDialog()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to create scene: ${e.message}") 
                }
            }
        }
    }

    fun updateScene(
        scene: Scene,
        title: String,
        summary: String,
        location: String,
        timeOfDay: String,
        mood: String,
        purpose: String,
        pointOfView: String,
        conflictLevel: Int,
        tags: List<String>,
        notes: String
    ) {
        viewModelScope.launch {
            try {
                val updatedScene = scene.copy(
                    title = title,
                    summary = summary,
                    location = location,
                    timeOfDay = timeOfDay,
                    mood = mood,
                    purpose = purpose,
                    pointOfView = pointOfView,
                    conflictLevel = conflictLevel,
                    tags = tags,
                    notes = notes,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateScene(updatedScene)
                hideDialog()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to update scene: ${e.message}") 
                }
            }
        }
    }

    fun deleteScene(scene: Scene) {
        viewModelScope.launch {
            try {
                repository.deleteScene(scene)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to delete scene: ${e.message}") 
                }
            }
        }
    }

    fun reorderScenes(scenes: List<Scene>) {
        viewModelScope.launch {
            try {
                scenes.forEachIndexed { index, scene ->
                    val updatedScene = scene.copy(
                        order = index,
                        updatedAt = System.currentTimeMillis()
                    )
                    repository.updateScene(updatedScene)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to reorder scenes: ${e.message}") 
                }
            }
        }
    }

    fun markSceneCompleted(scene: Scene, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val updatedScene = scene.copy(
                    isCompleted = isCompleted,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateScene(updatedScene)
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to update scene status: ${e.message}") 
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun clearFilters() {
        _uiState.update { 
            it.copy(
                searchQuery = "",
                selectedLocation = "",
                selectedTimeOfDay = "",
                selectedMood = "",
                sortBy = SortOption.ORDER
            )
        }
        applyFiltersAndSort()
    }
}
