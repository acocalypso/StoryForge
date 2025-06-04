package de.astronarren.storyforge.ui.screens.characterdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.data.database.entities.CharacterRelationship
import de.astronarren.storyforge.data.repository.StoryForgeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CharacterDetailUiState(
    val character: Character? = null,
    val allCharacters: List<Character> = emptyList(),
    val relatedCharacters: List<Character> = emptyList(),
    val relationships: List<CharacterRelationship> = emptyList(),
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val errorMessage: String? = null,
    val isSaving: Boolean = false
)

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val repository: StoryForgeRepository
) : ViewModel() {
    private val _isEditing = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _isSaving = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
      fun getCharacterDetailUiState(bookId: String, characterId: String?): StateFlow<CharacterDetailUiState> {
        return combine(
            if (characterId != null) {
                repository.getCharacterByIdFlow(characterId)
            } else {
                flowOf(null)
            },
            repository.getCharactersByBook(bookId),            if (characterId != null) {
                repository.getRelationshipsByCharacter(characterId)
            } else {
                flowOf(emptyList())
            },
            _isLoading,
            _isEditing,
            _isSaving,
            _errorMessage
        ) { values ->
            val character = values[0] as Character?
            val allCharacters = values[1] as List<Character>
            val relationships = values[2] as List<CharacterRelationship>
            val isLoading = values[3] as Boolean
            val isEditing = values[4] as Boolean
            val isSaving = values[5] as Boolean
            val errorMessage = values[6] as String?
            
            // Get related characters from both old system (for backward compatibility) and new relationships
            val relatedCharacters = if (character != null) {
                val oldRelatedCharacterIds = character.relationships
                val newRelatedCharacterIds = relationships.map { it.relatedCharacterId }
                val allRelatedIds = (oldRelatedCharacterIds + newRelatedCharacterIds).distinct()
                allCharacters.filter { it.id in allRelatedIds }
            } else {
                emptyList()
            }
            
            CharacterDetailUiState(
                character = character,
                allCharacters = allCharacters,
                relatedCharacters = relatedCharacters,
                relationships = relationships,
                isLoading = isLoading,
                isEditing = isEditing,
                isSaving = isSaving,
                errorMessage = errorMessage
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CharacterDetailUiState(isLoading = true)
        )
    }
    
    fun toggleEditMode() {
        _isEditing.value = !_isEditing.value
    }
    
    fun updateCharacter(
        character: Character,
        name: String,
        description: String,
        age: Int?,
        occupation: String,
        backstory: String,
        personality: String,
        physicalDescription: String,
        goals: String,
        conflicts: String,
        isMainCharacter: Boolean,
        characterArc: String,
        notes: String,
        relationships: List<String> = character.relationships,        portraitImagePath: String? = character.portraitImagePath
    ) {
        viewModelScope.launch {
            try {
                _isSaving.value = true
                _errorMessage.value = null
                
                val updatedCharacter = character.copy(
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
                    relationships = relationships,
                    portraitImagePath = portraitImagePath,
                    updatedAt = System.currentTimeMillis()
                )
                
                repository.updateCharacter(updatedCharacter)
                _isEditing.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update character: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }
      fun addRelationship(character: Character, relatedCharacterId: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                
                val updatedRelationships = character.relationships.toMutableList()
                if (!updatedRelationships.contains(relatedCharacterId)) {
                    updatedRelationships.add(relatedCharacterId)
                    
                    val updatedCharacter = character.copy(
                        relationships = updatedRelationships,
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    repository.updateCharacter(updatedCharacter)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add relationship: ${e.message}"
            }
        }
    }
    
    fun removeRelationship(character: Character, relatedCharacterId: String) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                
                val updatedRelationships = character.relationships.toMutableList()
                updatedRelationships.remove(relatedCharacterId)
                
                val updatedCharacter = character.copy(
                    relationships = updatedRelationships,
                    updatedAt = System.currentTimeMillis()
                )
                
                repository.updateCharacter(updatedCharacter)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove relationship: ${e.message}"
            }
        }
    }
      // Enhanced relationship management methods    
    fun addEnhancedRelationship(relationship: CharacterRelationship) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                repository.insertRelationship(relationship)
                
                // Create reciprocal relationship if requested
                if (relationship.isReciprocal) {
                    val reciprocalRelationship = CharacterRelationship(
                        characterId = relationship.relatedCharacterId,
                        relatedCharacterId = relationship.characterId,
                        relationshipType = relationship.relationshipType,
                        strength = relationship.strength,
                        description = relationship.description,
                        notes = relationship.notes,
                        isReciprocal = true
                    )
                    repository.insertRelationship(reciprocalRelationship)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add relationship: ${e.message}"            }
        }
    }
    
    fun updateEnhancedRelationship(relationship: CharacterRelationship) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                repository.updateRelationship(relationship)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update relationship: ${e.message}"
            }
        }
    }
    
    fun removeEnhancedRelationship(relationship: CharacterRelationship) {
        viewModelScope.launch {
            try {
                _errorMessage.value = null
                repository.deleteRelationship(relationship)
                
                // Remove reciprocal relationship if it exists
                if (relationship.isReciprocal) {
                    val reciprocalRelationship = repository.getRelationshipBetween(
                        relationship.relatedCharacterId, 
                        relationship.characterId
                    )
                    reciprocalRelationship?.let { 
                        repository.deleteRelationship(it)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove relationship: ${e.message}"
            }
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
}

