package de.astronarren.storyforge.ui.screens.characterdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.ui.components.LoadingSkeleton
import de.astronarren.storyforge.ui.components.EmptyState
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackManager
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType
import de.astronarren.storyforge.ui.components.CharacterPortraitImage
import de.astronarren.storyforge.ui.components.CharacterPortraitSize
import de.astronarren.storyforge.ui.screens.characterdetail.components.EnhancedRelationshipMappingSection
import de.astronarren.storyforge.utils.rememberCharacterPortraitPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterDetailScreen(
    bookId: String,
    characterId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CharacterDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.getCharacterDetailUiState(bookId, characterId).collectAsStateWithLifecycle()
    val haptic = HapticFeedbackManager.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.character?.name ?: "Character Details",
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                        onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    if (uiState.character != null) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                                viewModel.toggleEditMode()
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.isEditing) Icons.Filled.Close else Icons.Filled.Edit,
                                contentDescription = if (uiState.isEditing) "Cancel editing" else "Edit character"
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingSkeletons()
                }
                
                uiState.character == null -> {
                    EmptyState(
                        icon = Icons.Filled.Person,
                        title = "Character not found",
                        description = "The character you're looking for doesn't exist or has been deleted.",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                  uiState.isEditing -> {
                    uiState.character?.let { character ->
                        CharacterEditForm(
                            character = character,
                            isSaving = uiState.isSaving,
                            onSave = { updatedCharacter ->
                                viewModel.updateCharacter(
                                    character = character,
                                    name = updatedCharacter.name,
                                    description = updatedCharacter.description,
                                    age = updatedCharacter.age,
                                    occupation = updatedCharacter.occupation,
                                    backstory = updatedCharacter.backstory,
                                    personality = updatedCharacter.personality,
                                    physicalDescription = updatedCharacter.physicalDescription,
                                    goals = updatedCharacter.goals,
                                    conflicts = updatedCharacter.conflicts,
                                    isMainCharacter = updatedCharacter.isMainCharacter,
                                    characterArc = updatedCharacter.characterArc,
                                    notes = updatedCharacter.notes
                                )
                            },
                            onCancel = { viewModel.toggleEditMode() }
                        )
                    }
                }
                  else -> {
                    uiState.character?.let { character ->
                        CharacterDetailContent(
                            character = character,
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }
            
            // Error handling
            uiState.errorMessage?.let { errorMessage ->
                LaunchedEffect(errorMessage) {
                    viewModel.clearError()
                }
            }
        }
    }
}

@Composable
private fun LoadingSkeletons(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header skeleton
        LoadingSkeleton(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )
        
        // Content sections skeleton
        repeat(3) {
            LoadingSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            )
        }
    }
}

@Composable
private fun CharacterDetailContent(
    character: Character,
    uiState: CharacterDetailUiState,
    viewModel: CharacterDetailViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Character header
        CharacterHeader(character = character)
        
        // Basic information
        CharacterBasicInfo(character = character)
        
        // Character development
        CharacterDevelopment(character = character)        // Relationships section
        EnhancedRelationshipMappingSection(
            character = character,
            allCharacters = uiState.allCharacters,
            relationships = uiState.relationships,
            onAddRelationship = { relationship ->
                viewModel.addEnhancedRelationship(relationship)
            },
            onUpdateRelationship = { relationship ->
                viewModel.updateEnhancedRelationship(relationship)
            },
            onRemoveRelationship = { relationship ->
                viewModel.removeEnhancedRelationship(relationship)
            }
        )
        
        // Notes section
        if (character.notes.isNotBlank()) {
            CharacterNotes(character = character)
        }
    }
}

@Composable
private fun CharacterHeader(
    character: Character,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Character portrait
                CharacterPortraitImage(
                    portraitImagePath = character.portraitImagePath,
                    characterName = character.name,
                    size = CharacterPortraitSize.LARGE
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (character.occupation.isNotBlank()) {
                        Text(
                            text = character.occupation,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (character.age != null) {
                        Text(
                            text = "Age: ${character.age}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (character.isMainCharacter) {
                        Spacer(modifier = Modifier.height(8.dp))
                        AssistChip(
                            onClick = { },
                            label = { Text("Main Character") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
            
            if (character.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = character.description,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
private fun CharacterBasicInfo(
    character: Character,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Basic Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (character.physicalDescription.isNotBlank()) {
                InfoSection(
                    title = "Physical Description",
                    content = character.physicalDescription
                )
            }
            
            if (character.backstory.isNotBlank()) {
                InfoSection(
                    title = "Backstory",
                    content = character.backstory
                )
            }
        }
    }
}

@Composable
private fun CharacterDevelopment(
    character: Character,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Character Development",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (character.personality.isNotBlank()) {
                InfoSection(
                    title = "Personality",
                    content = character.personality
                )
            }
            
            if (character.goals.isNotBlank()) {
                InfoSection(
                    title = "Goals",
                    content = character.goals
                )
            }
            
            if (character.conflicts.isNotBlank()) {
                InfoSection(
                    title = "Conflicts",
                    content = character.conflicts
                )
            }
            
            if (character.characterArc.isNotBlank()) {
                InfoSection(
                    title = "Character Arc",
                    content = character.characterArc
                )
            }
        }
    }
}

@Composable
private fun CharacterNotes(
    character: Character,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = character.notes,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InfoSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CharacterEditForm(
    character: Character,
    isSaving: Boolean,
    onSave: (Character) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(character.name) }
    var description by remember { mutableStateOf(character.description) }
    var age by remember { mutableStateOf(character.age?.toString() ?: "") }
    var occupation by remember { mutableStateOf(character.occupation) }
    var backstory by remember { mutableStateOf(character.backstory) }
    var personality by remember { mutableStateOf(character.personality) }
    var physicalDescription by remember { mutableStateOf(character.physicalDescription) }
    var goals by remember { mutableStateOf(character.goals) }
    var conflicts by remember { mutableStateOf(character.conflicts) }
    var characterArc by remember { mutableStateOf(character.characterArc) }    
    var notes by remember { mutableStateOf(character.notes) }
    var isMainCharacter by remember { mutableStateOf(character.isMainCharacter) }
    var portraitImagePath by remember { mutableStateOf(character.portraitImagePath) }
    
    val haptic = HapticFeedbackManager.current
    val portraitPicker = rememberCharacterPortraitPicker { imagePath ->
        portraitImagePath = imagePath
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Basic Information Section
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)            ) {
                Text(
                    text = "Basic Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Character Portrait Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CharacterPortraitImage(
                        portraitImagePath = portraitImagePath,
                        characterName = name.ifBlank { "Character" },
                        isEditable = true,
                        onImageClick = { portraitPicker() },
                        size = CharacterPortraitSize.LARGE
                    )
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Character Portrait",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (portraitImagePath != null) "Tap image to change" else "Tap to add portrait image",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (portraitImagePath != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { portraitImagePath = null }
                            ) {
                                Text("Remove Image")
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = name.isBlank()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { if (it.all { char -> char.isDigit() }) age = it },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation") },
                        modifier = Modifier.weight(2f)
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Main Character",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = isMainCharacter,
                        onCheckedChange = { 
                            isMainCharacter = it
                            haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                        }
                    )
                }
            }
        }
        
        // Character Development Section
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Character Development",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = physicalDescription,
                    onValueChange = { physicalDescription = it },
                    label = { Text("Physical Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                OutlinedTextField(
                    value = backstory,
                    onValueChange = { backstory = it },
                    label = { Text("Backstory") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                OutlinedTextField(
                    value = personality,
                    onValueChange = { personality = it },
                    label = { Text("Personality") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                OutlinedTextField(
                    value = goals,
                    onValueChange = { goals = it },
                    label = { Text("Goals") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                
                OutlinedTextField(
                    value = conflicts,
                    onValueChange = { conflicts = it },
                    label = { Text("Conflicts") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                
                OutlinedTextField(
                    value = characterArc,
                    onValueChange = { characterArc = it },
                    label = { Text("Character Arc") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        }
        
        // Notes Section
        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6
                )
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                    onCancel()
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) {
                Text("Cancel")
            }
            
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                    if (name.isNotBlank()) {                        onSave(
                            character.copy(
                                name = name.trim(),
                                description = description.trim(),
                                age = age.toIntOrNull(),
                                occupation = occupation.trim(),
                                backstory = backstory.trim(),
                                personality = personality.trim(),
                                physicalDescription = physicalDescription.trim(),
                                goals = goals.trim(),
                                conflicts = conflicts.trim(),
                                characterArc = characterArc.trim(),
                                notes = notes.trim(),
                                isMainCharacter = isMainCharacter,
                                portraitImagePath = portraitImagePath
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving && name.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save Changes")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

