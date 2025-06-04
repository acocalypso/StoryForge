package de.astronarren.storyforge.ui.screens.scenelist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.astronarren.storyforge.ui.components.EmptyState
import de.astronarren.storyforge.ui.screens.scenelist.SceneListViewModel
import de.astronarren.storyforge.ui.screens.scenelist.components.SceneCard
import de.astronarren.storyforge.ui.screens.scenelist.components.SceneDialog
import de.astronarren.storyforge.ui.screens.scenelist.components.SceneFilterBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneListScreen(
    bookId: String,
    onNavigateBack: () -> Unit,
    onSceneClick: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SceneListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedSceneId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(bookId) {
        viewModel.initialize(bookId)
    }

    // Handle error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Error will be displayed in the UI
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scenes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Statistics Badge
                    if (uiState.scenes.isNotEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${uiState.filteredScenes.size}/${uiState.scenes.size}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Scene"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Bar
            SceneFilterBar(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                selectedLocation = uiState.selectedLocation,
                onLocationChange = viewModel::updateLocationFilter,
                selectedTimeOfDay = uiState.selectedTimeOfDay,
                onTimeOfDayChange = viewModel::updateTimeOfDayFilter,
                selectedMood = uiState.selectedMood,
                onMoodChange = viewModel::updateMoodFilter,
                sortBy = uiState.sortBy,
                onSortByChange = viewModel::updateSortOption,
                uniqueLocations = uiState.uniqueLocations,
                uniqueTimesOfDay = uiState.uniqueTimesOfDay,
                uniqueMoods = uiState.uniqueMoods,
                onClearFilters = viewModel::clearFilters,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text(
                                text = "Loading scenes...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = uiState.error!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                TextButton(
                                    onClick = viewModel::clearError
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }                uiState.filteredScenes.isEmpty() && uiState.scenes.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.MovieCreation,
                        title = "No Scenes Yet",
                        description = "Create your first scene to start organizing your story. Scenes help you break down your narrative into manageable pieces.",
                        actionText = "Create Scene",
                        onActionClick = { viewModel.showCreateDialog() },
                        modifier = Modifier.fillMaxSize()
                    )
                }                uiState.filteredScenes.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Default.FilterList,
                        title = "No Scenes Match Filters",
                        description = "Try adjusting your search or filter criteria to find scenes.",
                        actionText = "Clear Filters",
                        onActionClick = { viewModel.clearFilters() },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.filteredScenes,
                            key = { it.id }
                        ) { scene ->
                            SceneCard(
                                scene = scene,
                                isExpanded = expandedSceneId == scene.id,
                                onExpandToggle = {
                                    expandedSceneId = if (expandedSceneId == scene.id) {
                                        null
                                    } else {
                                        scene.id
                                    }
                                },
                                onEdit = { viewModel.showEditDialog(scene) },
                                onDelete = { 
                                    // TODO: Show confirmation dialog
                                    viewModel.deleteScene(scene)
                                },
                                onToggleComplete = { isCompleted ->
                                    viewModel.markSceneCompleted(scene, isCompleted)
                                }
                            )
                        }

                        // Statistics Footer
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Scene Statistics",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    
                                    val totalWordCount = uiState.filteredScenes.sumOf { it.wordCount }
                                    val completedScenes = uiState.filteredScenes.count { it.isCompleted }
                                    val totalScenes = uiState.filteredScenes.size
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = "Total Scenes: $totalScenes",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Completed: $completedScenes",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = "Total Words: $totalWordCount",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                            Text(
                                                text = "Progress: ${if (totalScenes > 0) (completedScenes * 100 / totalScenes) else 0}%",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                    
                                    if (totalScenes > 0) {
                                        LinearProgressIndicator(
                                            progress = completedScenes.toFloat() / totalScenes.toFloat(),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Scene Dialog
    if (uiState.showDialog) {
        SceneDialog(
            scene = uiState.editingScene,
            onDismiss = viewModel::hideDialog,
            onConfirm = { title, summary, location, timeOfDay, mood, purpose, pointOfView, conflictLevel, tags, notes ->
                if (uiState.editingScene != null) {
                    viewModel.updateScene(
                        uiState.editingScene!!,
                        title, summary, location, timeOfDay, mood, purpose, pointOfView, conflictLevel, tags, notes
                    )
                } else {
                    viewModel.createScene(
                        title, summary, location, timeOfDay, mood, purpose, pointOfView, conflictLevel, tags, notes
                    )
                }
            }
        )
    }
}
