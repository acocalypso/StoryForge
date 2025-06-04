package de.astronarren.storyforge.ui.components.richtext

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.data.database.entities.Scene
import de.astronarren.storyforge.data.database.entities.TimelineEvent

/**
 * Dialog for selecting story elements to reference in the text
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryReferencePickerDialog(
    characters: List<Character> = emptyList(),
    scenes: List<Scene> = emptyList(),
    timelineEvents: List<TimelineEvent> = emptyList(),
    onReferenceSelected: (StoryElementReference) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Story Reference",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                // Tab row
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Characters") },
                        icon = { Icon(Icons.Default.Person, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Scenes") },
                        icon = { Icon(Icons.Default.MovieCreation, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Events") },
                        icon = { Icon(Icons.Default.Event, contentDescription = null) }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Content based on selected tab
                Box(modifier = Modifier.heightIn(max = 300.dp)) {
                    when (selectedTab) {
                        0 -> CharacterList(
                            characters = characters,
                            onCharacterSelected = { character ->
                                onReferenceSelected(
                                    StoryElementReference(
                                        id = character.id,
                                        type = ReferenceType.CHARACTER,
                                        displayName = character.name,
                                        startIndex = 0,
                                        endIndex = 0
                                    )
                                )
                            }
                        )
                        1 -> SceneList(
                            scenes = scenes,
                            onSceneSelected = { scene ->
                                onReferenceSelected(
                                    StoryElementReference(
                                        id = scene.id,
                                        type = ReferenceType.SCENE,
                                        displayName = scene.title,
                                        startIndex = 0,
                                        endIndex = 0
                                    )
                                )
                            }
                        )
                        2 -> TimelineEventList(
                            events = timelineEvents,
                            onEventSelected = { event ->
                                onReferenceSelected(
                                    StoryElementReference(
                                        id = event.id,
                                        type = ReferenceType.TIMELINE_EVENT,
                                        displayName = event.title,
                                        startIndex = 0,
                                        endIndex = 0
                                    )
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun CharacterList(
    characters: List<Character>,
    onCharacterSelected: (Character) -> Unit
) {
    LazyColumn {
        items(characters) { character ->
            StoryElementItem(
                title = character.name,
                subtitle = character.description.take(50) + if (character.description.length > 50) "..." else "",
                icon = Icons.Default.Person,
                onClick = { onCharacterSelected(character) }
            )
        }
        
        if (characters.isEmpty()) {
            item {
                EmptyStateMessage("No characters found")
            }
        }
    }
}

@Composable
private fun SceneList(
    scenes: List<Scene>,
    onSceneSelected: (Scene) -> Unit
) {
    LazyColumn {
        items(scenes) { scene ->
            StoryElementItem(
                title = scene.title,
                subtitle = scene.summary.take(50) + if (scene.summary.length > 50) "..." else "",
                icon = Icons.Default.MovieCreation,
                onClick = { onSceneSelected(scene) }
            )
        }
        
        if (scenes.isEmpty()) {
            item {
                EmptyStateMessage("No scenes found")
            }
        }
    }
}

@Composable
private fun TimelineEventList(
    events: List<TimelineEvent>,
    onEventSelected: (TimelineEvent) -> Unit
) {
    LazyColumn {
        items(events) { event ->
            StoryElementItem(
                title = event.title,
                subtitle = event.description.take(50) + if (event.description.length > 50) "..." else "",
                icon = Icons.Default.Event,
                onClick = { onEventSelected(event) }
            )
        }
        
        if (events.isEmpty()) {
            item {
                EmptyStateMessage("No timeline events found")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoryElementItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
