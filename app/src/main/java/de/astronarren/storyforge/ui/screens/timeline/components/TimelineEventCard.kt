package de.astronarren.storyforge.ui.screens.timeline.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.data.database.entities.EventType
import de.astronarren.storyforge.data.database.entities.TimelineEvent
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.data.database.entities.Scene
import de.astronarren.storyforge.data.database.entities.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineEventCard(
    event: TimelineEvent,
    characters: List<Character> = emptyList(),
    scenes: List<Scene> = emptyList(),
    currentBook: Book? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (event.date.isNotEmpty() || event.time.isNotEmpty()) {
                        Text(
                            text = buildString {
                                if (event.date.isNotEmpty()) append(event.date)
                                if (event.date.isNotEmpty() && event.time.isNotEmpty()) append(" • ")
                                if (event.time.isNotEmpty()) append(event.time)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Importance indicator
                    ImportanceIndicator(importance = event.importance)
                    
                    // Event type chip
                    EventTypeChip(eventType = event.eventType, color = event.color)
                    
                    // Actions
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Description (always visible if not empty)
            if (event.description.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
              // Expanded content
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Book information
                currentBook?.let { book ->
                    DetailItem(
                        icon = Icons.Default.Book,
                        label = "Book",
                        value = book.title
                    )
                }
                
                // Location
                if (event.location.isNotEmpty()) {
                    DetailItem(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        value = event.location
                    )
                }                // Characters involved
                if (event.charactersInvolved.isNotEmpty()) {
                    // Resolve character IDs to names
                    val characterNames = event.charactersInvolved.mapNotNull { characterId ->
                        characters.find { it.id == characterId }?.name
                    }
                    
                    if (characterNames.isNotEmpty()) {
                        DetailItem(
                            icon = Icons.Default.People,
                            label = "Characters",
                            value = characterNames.joinToString(", ")
                        )
                    }
                }
                
                // Related scenes
                if (event.relatedScenes.isNotEmpty()) {
                    // Resolve scene IDs to titles
                    val sceneNames = event.relatedScenes.mapNotNull { sceneId ->
                        scenes.find { it.id == sceneId }?.title
                    }
                    
                    if (sceneNames.isNotEmpty()) {
                        DetailItem(
                            icon = Icons.Default.Movie,
                            label = "Related Scenes",
                            value = sceneNames.joinToString(", ")
                        )
                    }
                }
                
                // Duration
                if (event.duration.isNotEmpty()) {
                    DetailItem(
                        icon = Icons.Default.Schedule,
                        label = "Duration",
                        value = event.duration
                    )
                }
                
                // Tags
                if (event.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TagsRow(tags = event.tags)
                }
                
                // Notes
                if (event.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notes:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Action buttons
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }
                    
                    TextButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportanceIndicator(
    importance: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        repeat(5) { index ->
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = if (index < importance) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                }
            )
        }
    }
}

@Composable
private fun EventTypeChip(
    eventType: EventType,
    color: String?,
    modifier: Modifier = Modifier
) {
    val chipColor = color?.let { 
        try { Color(android.graphics.Color.parseColor(it)) } 
        catch (e: Exception) { null }
    } ?: getEventTypeColor(eventType)
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = chipColor.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, chipColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = eventType.name.lowercase().replace('_', ' ').split(' ').joinToString(" ") { 
                it.replaceFirstChar { char -> char.uppercase() } 
            },
            style = MaterialTheme.typography.labelSmall,
            color = chipColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun TagsRow(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tags.take(3).forEach { tag ->
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "#$tag",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
        if (tags.size > 3) {
            Text(
                text = "+${tags.size - 3}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getEventTypeColor(eventType: EventType): Color {
    return when (eventType) {
        EventType.PLOT -> Color(0xFF2196F3)
        EventType.CHARACTER_DEVELOPMENT -> Color(0xFF4CAF50)
        EventType.WORLD_BUILDING -> Color(0xFF9C27B0)
        EventType.CONFLICT -> Color(0xFFF44336)
        EventType.RESOLUTION -> Color(0xFF00BCD4)
        EventType.BACKSTORY -> Color(0xFF795548)
        EventType.FORESHADOWING -> Color(0xFFFF9800)
    }
}
