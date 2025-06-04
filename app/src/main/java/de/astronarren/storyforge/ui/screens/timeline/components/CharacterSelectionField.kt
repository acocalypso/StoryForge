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
import de.astronarren.storyforge.data.database.entities.Character

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharacterSelectionField(
    availableCharacters: List<Character>,
    selectedCharacterIds: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    val selectedCharacters = availableCharacters.filter { character ->
        selectedCharacterIds.contains(character.id)
    }
    
    Column(modifier = modifier) {
        // Selection display field
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = when {
                    selectedCharacters.isEmpty() -> ""
                    selectedCharacters.size == 1 -> selectedCharacters.first().name
                    else -> "${selectedCharacters.size} characters selected"
                },
                onValueChange = { },
                readOnly = true,
                label = { Text("Characters Involved") },
                placeholder = { Text("Select characters from your story") },
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
                if (availableCharacters.isEmpty()) {
                    DropdownMenuItem(
                        text = { 
                            Text(
                                "No characters available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        },
                        onClick = { },
                        enabled = false
                    )
                } else {
                    availableCharacters.forEach { character ->
                        val isSelected = selectedCharacterIds.contains(character.id)
                        
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
                                            text = character.name,
                                            style = MaterialTheme.typography.bodyMedium,                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (character.occupation.isNotBlank()) {
                                            Text(
                                                text = character.occupation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            },
                            onClick = {
                                val newSelection = if (isSelected) {
                                    selectedCharacterIds - character.id
                                } else {
                                    selectedCharacterIds + character.id
                                }
                                onSelectionChanged(newSelection)
                            }
                        )
                    }
                }
            }
        }
        
        // Selected characters display
        if (selectedCharacters.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 120.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(selectedCharacters) { character ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = character.name,                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (character.occupation.isNotBlank()) {
                                    Text(
                                        text = character.occupation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    onSelectionChanged(selectedCharacterIds - character.id)
                                }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove ${character.name}",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
