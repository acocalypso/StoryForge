package de.astronarren.storyforge.ui.screens.scenelist.components

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.astronarren.storyforge.data.database.entities.Scene

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneDialog(
    scene: Scene?,
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        summary: String,
        location: String,
        timeOfDay: String,
        mood: String,
        purpose: String,
        pointOfView: String,
        conflictLevel: Int,
        tags: List<String>,
        notes: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(scene?.title ?: "") }
    var summary by remember { mutableStateOf(scene?.summary ?: "") }
    var location by remember { mutableStateOf(scene?.location ?: "") }
    var timeOfDay by remember { mutableStateOf(scene?.timeOfDay ?: "") }
    var mood by remember { mutableStateOf(scene?.mood ?: "") }
    var purpose by remember { mutableStateOf(scene?.purpose ?: "") }
    var pointOfView by remember { mutableStateOf(scene?.pointOfView ?: "") }
    var conflictLevel by remember { mutableStateOf(scene?.conflictLevel ?: 0) }
    var tagsText by remember { mutableStateOf(scene?.tags?.joinToString(", ") ?: "") }
    var notes by remember { mutableStateOf(scene?.notes ?: "") }

    val isEditing = scene != null
    val scrollState = rememberScrollState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Scene" else "Create New Scene",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Basic Information Section
                    Text(
                        text = "Basic Information",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Scene Title") },
                        leadingIcon = {
                            Icon(Icons.Default.Title, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Summary") },
                        leadingIcon = {
                            Icon(Icons.Default.Description, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    // Location and Setting Section
                    Text(
                        text = "Setting",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = timeOfDay,
                            onValueChange = { timeOfDay = it },
                            label = { Text("Time of Day") },
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = mood,
                            onValueChange = { mood = it },
                            label = { Text("Mood/Atmosphere") },
                            leadingIcon = {
                                Icon(Icons.Default.Mood, contentDescription = null)
                            },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Story Elements Section
                    Text(
                        text = "Story Elements",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = purpose,
                        onValueChange = { purpose = it },
                        label = { Text("Scene Purpose") },
                        leadingIcon = {
                            Icon(Icons.Default.Flag, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    OutlinedTextField(
                        value = pointOfView,
                        onValueChange = { pointOfView = it },
                        label = { Text("Point of View Character") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Conflict Level
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Conflict Level")
                            }
                            Text(
                                text = "$conflictLevel/10",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        
                        Slider(
                            value = conflictLevel.toFloat(),
                            onValueChange = { conflictLevel = it.toInt() },
                            valueRange = 0f..10f,
                            steps = 9,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Additional Information Section
                    Text(
                        text = "Additional Information",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    OutlinedTextField(
                        value = tagsText,
                        onValueChange = { tagsText = it },
                        label = { Text("Tags (comma separated)") },
                        leadingIcon = {
                            Icon(Icons.Default.Tag, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("action, dialogue, revelation") }
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        leadingIcon = {
                            Icon(Icons.Default.Notes, contentDescription = null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val tags = tagsText.split(",")
                                .map { it.trim() }
                                .filter { it.isNotBlank() }
                            
                            onConfirm(
                                title,
                                summary,
                                location,
                                timeOfDay,
                                mood,
                                purpose,
                                pointOfView,
                                conflictLevel,
                                tags,
                                notes
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank()
                    ) {
                        Text(if (isEditing) "Update" else "Create")
                    }
                }
            }
        }
    }
}

// Predefined suggestions for common scene elements
object SceneSuggestions {
    val timeOfDayOptions = listOf(
        "Dawn", "Morning", "Midday", "Afternoon", "Dusk", "Evening", "Night", "Midnight"
    )
    
    val moodOptions = listOf(
        "Tense", "Peaceful", "Mysterious", "Romantic", "Melancholy", "Joyful", 
        "Ominous", "Hopeful", "Angry", "Contemplative", "Chaotic", "Serene"
    )
    
    val commonTags = listOf(
        "dialogue", "action", "revelation", "flashback", "foreshadowing",
        "character development", "plot twist", "climax", "resolution", "conflict"
    )
}
