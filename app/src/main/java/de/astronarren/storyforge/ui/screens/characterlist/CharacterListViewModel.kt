package de.astronarren.storyforge.ui.screens.characterlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@OptIn(kotlinx.coroutines.FlowPreview::class)

data class CharacterListUiState(
    val characters: List<Character> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val showOnlyMainCharacters: Boolean = false
)

@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val repository: StoryForgeRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    private val _showOnlyMainCharacters = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
      // Cache StateFlows per bookId to prevent recreation and flickering
    private val _uiStateCache = mutableMapOf<String, StateFlow<CharacterListUiState>>()
    
    fun getCharacterListUiState(bookId: String): StateFlow<CharacterListUiState> {
        Log.d("CharacterListVM", "getCharacterListUiState called for bookId: $bookId")
        
        // Return cached StateFlow if it exists, otherwise create and cache it
        return _uiStateCache.getOrPut(bookId) {
            Log.d("CharacterListVM", "Creating NEW StateFlow for bookId: $bookId")
            createCharacterListUiState(bookId)
        }
    }
      private fun createCharacterListUiState(bookId: String): StateFlow<CharacterListUiState> {
        // Debounce search query to prevent rapid updates that cause flickering
        val debouncedSearchQuery = _searchQuery
            .debounce(300) // Wait 300ms after user stops typing
            .distinctUntilChanged()
            .onEach { query -> Log.d("CharacterListVM", "Search query changed: '$query'") }
        
        return combine(
            repository.getCharactersByBook(bookId)
                .debounce(100) // Add small debounce to prevent rapid database emissions
                .distinctUntilChanged()
                .onEach { characters -> 
                    Log.d("CharacterListVM", "Characters from DB: ${characters.size} characters, ids: ${characters.map { it.id }}")
                },
            debouncedSearchQuery,
            _showOnlyMainCharacters
                .onEach { showOnlyMain -> Log.d("CharacterListVM", "Show only main characters: $showOnlyMain") },
            _errorMessage
                .onEach { error -> Log.d("CharacterListVM", "Error message: $error") }        ) { values ->
            val characters = values[0] as List<Character>
            val searchQuery = values[1] as String
            val showOnlyMain = values[2] as Boolean
            val errorMessage = values[3] as String?
            
            Log.d("CharacterListVM", "Creating new UI state - Characters: ${characters.size}, Query: '$searchQuery', ShowOnlyMain: $showOnlyMain, Error: $errorMessage")
            
            // Create stable filtered and sorted list to prevent unnecessary recompositions
            val filteredCharacters = if (searchQuery.isBlank() && !showOnlyMain) {
                // No filtering needed, just sort
                characters.sortedWith(compareByDescending<Character> { it.isMainCharacter }.thenBy { it.name })
            } else {                characters
                    .filter { character ->
                        val matchesSearch = if (searchQuery.isBlank()) {
                            true
                        } else {                            character.name.contains(searchQuery, ignoreCase = true) ||
                            character.description.contains(searchQuery, ignoreCase = true) ||
                            character.occupation.contains(searchQuery, ignoreCase = true)
                        }
                        val matchesMainFilter = if (showOnlyMain) {
                            character.isMainCharacter
                        } else {
                            true
                        }
                        matchesSearch && matchesMainFilter
                    }
                    .sortedWith(compareByDescending<Character> { it.isMainCharacter }.thenBy { it.name })
            }
              CharacterListUiState(
                characters = filteredCharacters,
                searchQuery = searchQuery, // Use consistent search query
                errorMessage = errorMessage,
                showOnlyMainCharacters = showOnlyMain
            ).also { newState ->
                Log.d("CharacterListVM", "Created UI state with ${newState.characters.size} filtered characters")
            }
        }
        .distinctUntilChanged { old, new ->
            // Custom equality check to prevent unnecessary emissions
            val charactersEqual = old.characters.size == new.characters.size &&
            old.characters.zip(new.characters).all { (oldChar, newChar) -> 
                oldChar.id == newChar.id && 
                oldChar.name == newChar.name && 
                oldChar.isMainCharacter == newChar.isMainCharacter &&
                oldChar.updatedAt == newChar.updatedAt
            }
            val searchEqual = old.searchQuery == new.searchQuery
            val filterEqual = old.showOnlyMainCharacters == new.showOnlyMainCharacters
            val errorEqual = old.errorMessage == new.errorMessage
            
            val areEqual = charactersEqual && searchEqual && filterEqual && errorEqual
            
            Log.d("CharacterListVM", "Equality check - Characters: $charactersEqual, Search: $searchEqual, Filter: $filterEqual, Error: $errorEqual, Result: $areEqual")
            
            areEqual
        }
        .onEach { state ->
            Log.d("CharacterListVM", "Emitting UI state with ${state.characters.size} characters")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = CharacterListUiState() // Remove initial loading state
        )
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun toggleMainCharactersFilter() {
        _showOnlyMainCharacters.value = !_showOnlyMainCharacters.value
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
    }
      fun createCharacter(
        bookId: String,
        name: String,
        description: String = "",
        age: Int? = null,
        occupation: String = "",
        backstory: String = "",
        personality: String = "",
        physicalDescription: String = "",
        goals: String = "",
        conflicts: String = "",
        isMainCharacter: Boolean = false,
        characterArc: String = "",
        notes: String = "",
        portraitImagePath: String? = null
    ) {
        Log.d("CharacterListVM", "Creating character: $name")
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                  val character = Character(
                    bookId = bookId,
                    name = name,
                    description = description,
                    age = age,
                    occupation = occupation,
                    backstory = backstory,
                    personality = personality,
                    physicalDescription = physicalDescription,
                    goals = goals,
                    conflicts = conflicts,
                    isMainCharacter = isMainCharacter,
                    characterArc = characterArc,
                    notes = notes,
                    portraitImagePath = portraitImagePath
                )
                repository.insertCharacter(character)
                Log.d("CharacterListVM", "Character created successfully: ${character.id}")
            } catch (e: Exception) {
                Log.e("CharacterListVM", "Failed to create character", e)
                _errorMessage.value = "Failed to create character: ${e.message}"
            }
        }
    }
      fun deleteCharacter(character: Character) {
        Log.d("CharacterListVM", "Deleting character: ${character.name} (${character.id})")
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                repository.deleteCharacter(character)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete character: ${e.message}"
            }
        }
    }
      fun toggleMainCharacterStatus(character: Character) {
        Log.d("CharacterListVM", "Toggling main character status for: ${character.name} (${character.id}) from ${character.isMainCharacter} to ${!character.isMainCharacter}")
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                
                val updatedCharacter = character.copy(
                    isMainCharacter = !character.isMainCharacter,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateCharacter(updatedCharacter)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update character: ${e.message}"
            }
        }
    }
      fun clearError() {
        _errorMessage.value = null
    }
    
    // Clear cache if needed (e.g., when switching books or memory cleanup)
    fun clearStateCache() {
        Log.d("CharacterListVM", "Clearing StateFlow cache for all books")
        _uiStateCache.clear()
    }
}

