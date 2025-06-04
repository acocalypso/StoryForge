package de.astronarren.storyforge.ui.screens.timeline.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.astronarren.storyforge.data.database.entities.EventType
import de.astronarren.storyforge.data.database.entities.TimelineEvent
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.data.database.entities.Scene

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineEventDialog(
    isVisible: Boolean,
    event: TimelineEvent? = null,
    availableCharacters: List<Character> = emptyList(),
    availableScenes: List<Scene> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        date: String,
        time: String,
        eventType: EventType,
        charactersInvolved: List<String>,
        location: String,
        importance: Int,
        notes: String,
        tags: List<String>,
        color: String?,
        relatedScenes: List<String>    ) -> Unit
) {
    if (!isVisible) return

    var title by remember { mutableStateOf(event?.title ?: "") }
    var description by remember { mutableStateOf(event?.description ?: "") }
    var date by remember { mutableStateOf(event?.date ?: "") }
    var time by remember { mutableStateOf(event?.time ?: "") }
    var duration by remember { mutableStateOf(event?.duration ?: "") }
    var eventType by remember { mutableStateOf(event?.eventType ?: EventType.PLOT) }
    var selectedCharacterIds by remember { mutableStateOf(event?.charactersInvolved ?: emptyList()) }
    var selectedSceneIds by remember { mutableStateOf(event?.relatedScenes ?: emptyList()) }
    var location by remember { mutableStateOf(event?.location ?: "") }
    var importance by remember { mutableStateOf(event?.importance ?: 3) }
    var notes by remember { mutableStateOf(event?.notes ?: "") }
    var tagsText by remember { mutableStateOf(event?.tags?.joinToString(", ") ?: "") }
    var color by remember { mutableStateOf(event?.color ?: "") }
    
    var eventTypeExpanded by remember { mutableStateOf(false) }
    var isFormValid by remember { mutableStateOf(false) }

    // Validate form
    LaunchedEffect(title) {
        isFormValid = title.isNotBlank()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (event == null) "Create Timeline Event" else "Edit Timeline Event",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title (Required)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title *") },
                        isError = title.isBlank(),
                        supportingText = if (title.isBlank()) {
                            { Text("Title is required") }
                        } else null,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Date and Time row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date") },
                            placeholder = { Text("Day 1, Chapter 2, etc.") },
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Time") },
                            placeholder = { Text("Morning, 3:00 PM, etc.") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Event Type
                    ExposedDropdownMenuBox(
                        expanded = eventTypeExpanded,
                        onExpandedChange = { eventTypeExpanded = !eventTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = eventType.name.lowercase().replace('_', ' ').split(' ').joinToString(" ") {
                                it.replaceFirstChar { char -> char.uppercase() }
                            },
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Event Type") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventTypeExpanded)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = eventTypeExpanded,
                            onDismissRequest = { eventTypeExpanded = false }
                        ) {
                            EventType.values().forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            type.name.lowercase().replace('_', ' ').split(' ').joinToString(" ") {
                                                it.replaceFirstChar { char -> char.uppercase() }
                                            }
                                        )
                                    },
                                    onClick = {
                                        eventType = type
                                        eventTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Importance
                    Column {
                        Text(
                            text = "Importance: $importance",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Slider(
                            value = importance.toFloat(),
                            onValueChange = { importance = it.toInt() },
                            valueRange = 1f..5f,
                            steps = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Minor", style = MaterialTheme.typography.labelSmall)
                            Text("Critical", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    // Location
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        placeholder = { Text("City, building, room, etc.") },
                        modifier = Modifier.fillMaxWidth()
                    )                    // Characters Involved
                    CharacterSelectionField(
                        availableCharacters = availableCharacters,
                        selectedCharacterIds = selectedCharacterIds,
                        onSelectionChanged = { selectedCharacterIds = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Related Scenes
                    SceneSelectionField(
                        availableScenes = availableScenes,
                        selectedSceneIds = selectedSceneIds,
                        onSelectionChanged = { selectedSceneIds = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Duration
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { duration = it },
                        label = { Text("Duration") },
                        placeholder = { Text("2 hours, all day, instant, etc.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tags
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = { tagsText = it },
                        label = { Text("Tags") },
                        placeholder = { Text("action, romance, mystery, etc.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Color
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color (Optional)") },
                        placeholder = { Text("#FF5722, red, etc.") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Additional Notes") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(                        onClick = {
                            if (isFormValid) {
                                onSave(
                                    title,
                                    description,
                                    date,
                                    time,
                                    eventType,
                                    selectedCharacterIds,
                                    location,
                                    importance,
                                    notes,
                                    tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    color.takeIf { it.isNotEmpty() },
                                    selectedSceneIds
                                )
                            }
                        },
                        enabled = isFormValid
                    ) {
                        Text(if (event == null) "Create" else "Save")
                    }
                }
            }
        }
    }
}
