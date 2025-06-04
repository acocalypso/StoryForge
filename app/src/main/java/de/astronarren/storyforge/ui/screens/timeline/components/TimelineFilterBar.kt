package de.astronarren.storyforge.ui.screens.timeline.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.data.database.entities.EventType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineFilterBar(
    selectedEventType: EventType?,
    selectedMinImportance: Int?,
    onEventTypeFilterChanged: (EventType?) -> Unit,
    onImportanceFilterChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var importanceExpanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Event Type Filters
            Text(
                text = "Filter by Event Type",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                // All Events filter
                item {
                    FilterChip(
                        selected = selectedEventType == null,
                        onClick = { onEventTypeFilterChanged(null) },
                        label = { Text("All Events") },
                        leadingIcon = if (selectedEventType == null) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
                
                // Individual event type filters
                items(EventType.values()) { eventType ->
                    FilterChip(
                        selected = selectedEventType == eventType,
                        onClick = { 
                            onEventTypeFilterChanged(
                                if (selectedEventType == eventType) null else eventType
                            )
                        },
                        label = { 
                            Text(
                                eventType.name.lowercase().replace('_', ' ').split(' ').joinToString(" ") {
                                    it.replaceFirstChar { char -> char.uppercase() }
                                }
                            )
                        },
                        leadingIcon = if (selectedEventType == eventType) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else {
                            { Icon(getEventTypeIcon(eventType), contentDescription = null, modifier = Modifier.size(18.dp)) }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Importance Filter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Minimum Importance",
                    style = MaterialTheme.typography.labelMedium
                )
                
                ExposedDropdownMenuBox(
                    expanded = importanceExpanded,
                    onExpandedChange = { importanceExpanded = !importanceExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedMinImportance?.let { "$it stars" } ?: "All",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = importanceExpanded)
                        },
                        modifier = Modifier
                            .width(120.dp)
                            .menuAnchor(),
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    
                    ExposedDropdownMenu(
                        expanded = importanceExpanded,
                        onDismissRequest = { importanceExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All") },
                            onClick = {
                                onImportanceFilterChanged(null)
                                importanceExpanded = false
                            },
                            leadingIcon = if (selectedMinImportance == null) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null
                        )
                        
                        for (i in 1..5) {
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("$i")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        repeat(i) {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(" & up")
                                    }
                                },
                                onClick = {
                                    onImportanceFilterChanged(i)
                                    importanceExpanded = false
                                },
                                leadingIcon = if (selectedMinImportance == i) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }
            }
            
            // Clear Filters Button
            if (selectedEventType != null || selectedMinImportance != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = {
                        onEventTypeFilterChanged(null)
                        onImportanceFilterChanged(null)
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear Filters")
                }
            }
        }
    }
}

private fun getEventTypeIcon(eventType: EventType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (eventType) {
        EventType.PLOT -> Icons.Default.Timeline
        EventType.CHARACTER_DEVELOPMENT -> Icons.Default.Person
        EventType.WORLD_BUILDING -> Icons.Default.Public
        EventType.CONFLICT -> Icons.Default.Warning
        EventType.RESOLUTION -> Icons.Default.CheckCircle
        EventType.BACKSTORY -> Icons.Default.History
        EventType.FORESHADOWING -> Icons.Default.Visibility
    }
}
