package de.astronarren.storyforge.ui.screens.timeline

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.astronarren.storyforge.data.database.entities.TimelineEvent
import de.astronarren.storyforge.ui.screens.timeline.components.TimelineFilterBar
import de.astronarren.storyforge.ui.screens.timeline.components.DraggableTimelineList
import de.astronarren.storyforge.ui.screens.timeline.components.TimelineEventDialog
import de.astronarren.storyforge.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    bookId: String,
    onNavigateBack: () -> Unit,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    
    // Initialize view model
    LaunchedEffect(bookId) {
        viewModel.initialize(bookId)
    }
    
    // Filter events based on current filters
    val filteredEvents = remember(uiState.timelineEvents, uiState.filterEventType, uiState.filterMinImportance) {
        uiState.timelineEvents.filter { event ->
            val matchesEventType = uiState.filterEventType?.let { it == event.eventType } ?: true
            val matchesImportance = uiState.filterMinImportance?.let { event.importance >= it } ?: true
            matchesEventType && matchesImportance
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timeline") },                
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showCreateEventDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Event")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateEventDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Timeline Event")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Bar
            TimelineFilterBar(
                selectedEventType = uiState.filterEventType,
                selectedMinImportance = uiState.filterMinImportance,
                onEventTypeFilterChanged = { viewModel.setFilterEventType(it) },
                onImportanceFilterChanged = { viewModel.setFilterImportance(it) }
            )
            
            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                  filteredEvents.isEmpty() && uiState.timelineEvents.isNotEmpty() -> {
                    // No events match current filters
                    EmptyState(
                        icon = Icons.Default.FilterList,
                        title = "No Events Match Filters",
                        description = "Try adjusting your filters or create a new timeline event",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                  uiState.timelineEvents.isEmpty() -> {
                    // No timeline events at all
                    EmptyState(
                        icon = Icons.Default.Timeline,
                        title = "No Timeline Events",
                        description = "Create your first timeline event to start organizing your story",
                        actionText = "Create Event",
                        onActionClick = { viewModel.showCreateEventDialog() },
                        modifier = Modifier.fillMaxSize()
                    )
                }                else -> {
                    // Show timeline events
                    DraggableTimelineList(
                        events = filteredEvents,
                        characters = viewModel.characters.collectAsState().value,
                        scenes = viewModel.scenes.collectAsState().value,
                        currentBook = viewModel.currentBook.collectAsState().value,
                        onReorder = { fromIndex, toIndex ->
                            viewModel.reorderTimelineEvents(fromIndex, toIndex)
                        },
                        onEditEvent = { event ->
                            viewModel.showEditEventDialog(event)
                        },
                        onDeleteEvent = { event ->
                            viewModel.deleteTimelineEvent(event)
                        },
                        listState = listState,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
      // Create Event Dialog
    TimelineEventDialog(
        isVisible = uiState.isCreateDialogVisible,
        availableCharacters = viewModel.characters.collectAsState().value,
        availableScenes = viewModel.scenes.collectAsState().value,
        onDismiss = { viewModel.clearCreateEventDialog() },
        onSave = { title, description, date, time, eventType, characters, location, importance, notes, tags, color, relatedScenes ->
            viewModel.createTimelineEvent(
                title = title,
                description = description,
                date = date,
                time = time,
                eventType = eventType,
                charactersInvolved = characters,
                location = location,
                importance = importance,
                notes = notes,
                tags = tags,
                color = color,
                relatedScenes = relatedScenes
            )
        }
    )
      // Edit Event Dialog
    TimelineEventDialog(
        isVisible = uiState.isEditDialogVisible,
        event = uiState.selectedEvent,
        availableCharacters = viewModel.characters.collectAsState().value,
        availableScenes = viewModel.scenes.collectAsState().value,
        onDismiss = { viewModel.clearEditEventDialog() },
        onSave = { title, description, date, time, eventType, characters, location, importance, notes, tags, color, relatedScenes ->
            uiState.selectedEvent?.let { event ->
                viewModel.updateTimelineEvent(
                    event.copy(
                        title = title,
                        description = description,
                        date = date,
                        time = time,
                        eventType = eventType,
                        charactersInvolved = characters,
                        location = location,
                        importance = importance,
                        notes = notes,
                        tags = tags,
                        color = color,
                        relatedScenes = relatedScenes,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
            viewModel.clearEditEventDialog()
        }
    )
    
    // Error Snackbar
    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            // You could show a snackbar here if you have a SnackbarHost
            viewModel.clearErrorMessage()
        }
    }
}

