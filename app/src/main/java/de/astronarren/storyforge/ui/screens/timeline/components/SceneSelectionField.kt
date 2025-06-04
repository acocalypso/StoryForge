package de.astronarren.storyforge.ui.screens.timeline.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.data.database.entities.Scene

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneSelectionField(
    availableScenes: List<Scene>,
    selectedSceneIds: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedScenes = availableScenes.filter { scene ->
        selectedSceneIds.contains(scene.id)
    }
    
    Column(modifier = modifier) {
        // Selection display field
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = when {
                    selectedScenes.isEmpty() -> ""
                    selectedScenes.size == 1 -> selectedScenes.first().title
                    else -> "${selectedScenes.size} scenes selected"
                },
                onValueChange = { },
                readOnly = true,
                label = { Text("Related Scenes") },
                placeholder = { Text("Select scenes related to this event") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (availableScenes.isEmpty()) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "No scenes available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        onClick = { },
                        enabled = false
                    )
                } else {
                    availableScenes.forEach { scene ->
                        val isSelected = selectedSceneIds.contains(scene.id)
                        
                        DropdownMenuItem(
                            text = {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = scene.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (scene.summary.isNotBlank()) {
                                            Text(
                                                text = scene.summary,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )                                        }
                                        // Show scene order if available
                                        if (scene.order > 0) {
                                            Text(
                                                text = "Scene ${scene.order}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                val newSelection = if (isSelected) {
                                    selectedSceneIds - scene.id
                                } else {
                                    selectedSceneIds + scene.id
                                }
                                onSelectionChanged(newSelection)
                            }
                        )
                    }
                }
            }
        }
        
        // Selected scenes display
        if (selectedScenes.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(selectedScenes) { scene ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {                                Text(
                                    text = scene.title,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (scene.order > 0) {
                                            Text(
                                                text = "Scene ${scene.order}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                        if (scene.summary.isNotBlank() && scene.order > 0) {
                                        Text(
                                            text = "•",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = scene.summary,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                            IconButton(
                                onClick = {
                                    onSelectionChanged(selectedSceneIds - scene.id)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove ${scene.title}",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
