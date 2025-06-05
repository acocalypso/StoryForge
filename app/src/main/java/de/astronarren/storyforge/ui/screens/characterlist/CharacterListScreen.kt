package de.astronarren.storyforge.ui.screens.characterlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import de.astronarren.storyforge.ui.components.EmptyState
import de.astronarren.storyforge.ui.components.LoadingSkeleton
import de.astronarren.storyforge.ui.components.NavigationDrawerContent
import de.astronarren.storyforge.ui.components.DrawerSections
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterListScreen(
    bookId: String,
    onNavigateBack: () -> Unit,
    onCharacterClick: (String) -> Unit,
    viewModel: CharacterListViewModel = hiltViewModel()
) {
    val haptic = HapticFeedbackManager.current
    val uiState by viewModel.getCharacterListUiState(bookId).collectAsStateWithLifecycle()
    
    // Stable dialog states
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
      // Navigation drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    
    // Memoize character count text to prevent recomposition
    val characterCountText = remember(uiState.characters.size) {
        "${uiState.characters.size} character${if (uiState.characters.size != 1) "s" else ""}"
    }
    
    // Stable callbacks to prevent lambda recreation
    val onSearchToggle = remember {
        { showSearchBar = !showSearchBar }
    }
    val onCreateDialogToggle = remember {
        { showCreateDialog = !showCreateDialog }
    }
    
    // Clear error when screen loads
    LaunchedEffect(Unit) {
        viewModel.clearError()
    }    // Create drawer sections (without search)
    val drawerSections = remember(uiState.showOnlyMainCharacters, uiState.searchQuery) {
        listOf(            DrawerSections.createFilterSection(
                showOnlyMainCharacters = uiState.showOnlyMainCharacters,
                onToggleMainCharacters = {
                    haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.LightTap)
                    viewModel.toggleMainCharactersFilter()
                },
                onClearFilters = {
                    viewModel.clearSearch()
                    if (uiState.showOnlyMainCharacters) {
                        viewModel.toggleMainCharactersFilter()
                    }
                }
            ),
            DrawerSections.createActionsSection(
                onCreateNew = onCreateDialogToggle
            )
        )
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(sections = drawerSections)
            }
        }
    ) {    
    Scaffold(
        topBar = {
            if (showSearchBar) {
                // Search bar
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            placeholder = { Text("Search characters...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            showSearchBar = false
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close search")
                        }
                    },
                    actions = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = viewModel::clearSearch) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    }
                )
            } else {                // Clean app bar with search and navigation drawer toggle
                TopAppBar(
                    title = { Text("Characters") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Search toggle
                        IconButton(
                            onClick = { 
                                haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.LightTap)
                                onSearchToggle()
                            }
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        }
                        // Navigation drawer toggle
                        IconButton(
                            onClick = { 
                                haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.LightTap)
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Menu, contentDescription = "Open navigation drawer")
                        }
                    }
                )
            }        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.MediumTap)
                    onCreateDialogToggle()
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add character")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.characters.isEmpty() -> {
                    // Empty state
                    EmptyState(
                        icon = Icons.Filled.Add,
                        title = if (uiState.searchQuery.isNotEmpty()) {
                            "No characters found"
                        } else if (uiState.showOnlyMainCharacters) {
                            "No main characters yet"
                        } else {
                            "No characters yet"
                        },
                        description = if (uiState.searchQuery.isNotEmpty()) {
                            "Try adjusting your search terms"
                        } else if (uiState.showOnlyMainCharacters) {
                            "Mark characters as main characters to see them here"
                        } else {
                            "Create your first character to start building your story"
                        },
                        actionText = if (uiState.searchQuery.isEmpty() && !uiState.showOnlyMainCharacters) {
                            "Create Character"
                        } else null,
                        onActionClick = if (uiState.searchQuery.isEmpty() && !uiState.showOnlyMainCharacters) {
                            { showCreateDialog = true }
                        } else null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                else -> {                    // Character list
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Results count
                        item(key = "character_count") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {                                Text(
                                    text = characterCountText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                
                                if (uiState.searchQuery.isNotEmpty() || uiState.showOnlyMainCharacters) {
                                    TextButton(
                                        onClick = {
                                            viewModel.clearSearch()
                                            if (uiState.showOnlyMainCharacters) {
                                                viewModel.toggleMainCharactersFilter()
                                            }
                                        }
                                    ) {
                                        Text("Clear filters")
                                    }
                                }
                            }
                        }
                          // Character cards
                        items(
                            items = uiState.characters,
                            key = { character -> character.id }
                        ) { character ->
                            // Use remember with character.id as key to ensure stable lambda references
                            val onCharacterClickStable = remember(character.id) {
                                { onCharacterClick(character.id) }
                            }
                            val onDeleteClickStable = remember(character.id) {
                                { viewModel.deleteCharacter(character) }
                            }
                            val onToggleMainCharacterStable = remember(character.id) {
                                { viewModel.toggleMainCharacterStatus(character) }
                            }
                            
                            CharacterCard(
                                character = character,
                                onCharacterClick = onCharacterClickStable,
                                onEditClick = onCharacterClickStable,
                                onDeleteClick = onDeleteClickStable,
                                onToggleMainCharacter = onToggleMainCharacterStable
                            )
                        }
                    }                }
            }
        }
    }
    } // Close ModalNavigationDrawer
    
    // Create character dialog
    if (showCreateDialog) {
        CreateCharacterDialog(
            onDismiss = { showCreateDialog = false },
            onCreateCharacter = { name, description, age, occupation, backstory, personality, physicalDescription, goals, conflicts, isMainCharacter, characterArc, notes, portraitImagePath ->
                viewModel.createCharacter(
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
                showCreateDialog = false
            }
        )
    }
    
    // Error snackbar
    uiState.errorMessage?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // You can show a snackbar here if you have SnackbarHostState
            viewModel.clearError()
        }
    }
}

