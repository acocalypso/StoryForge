package de.astronarren.storyforge.ui.screens.characterlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.astronarren.storyforge.ui.components.haptic.rememberHapticFeedback
import de.astronarren.storyforge.ui.components.CharacterPortraitImage
import de.astronarren.storyforge.ui.components.CharacterPortraitSize
import de.astronarren.storyforge.utils.rememberCharacterPortraitPicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCharacterDialog(
    onDismiss: () -> Unit,
    onCreateCharacter: (
        name: String,
        description: String,
        age: Int?,
        occupation: String,
        backstory: String,
        personality: String,
        physicalDescription: String,
        goals: String,
        conflicts: String,
        isMainCharacter: Boolean,
        characterArc: String,
        notes: String,
        portraitImagePath: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var backstory by remember { mutableStateOf("") }
    var personality by remember { mutableStateOf("") }
    var physicalDescription by remember { mutableStateOf("") }
    var goals by remember { mutableStateOf("") }
    var conflicts by remember { mutableStateOf("") }    
    var isMainCharacter by remember { mutableStateOf(false) }
    var characterArc by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var portraitImagePath by remember { mutableStateOf<String?>(null) }
    
    var nameError by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    
    val portraitPicker = rememberCharacterPortraitPicker { imagePath ->
        portraitImagePath = imagePath
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Create New Character",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                ) {                    // Basic Information Section
                    Text(
                        "Basic Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Character Portrait Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CharacterPortraitImage(
                            portraitImagePath = portraitImagePath,
                            characterName = name.ifBlank { "New Character" },
                            isEditable = true,
                            onImageClick = { portraitPicker() },
                            size = CharacterPortraitSize.LARGE
                        )
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Character Portrait",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (portraitImagePath != null) "Tap image to change" else "Tap to add portrait image",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            if (portraitImagePath != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { portraitImagePath = null }
                                ) {
                                    Text("Remove Image")
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Character Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            nameError = if (it.isBlank()) "Name is required" else ""
                        },
                        label = { Text("Character Name *") },
                        isError = nameError.isNotEmpty(),
                        supportingText = if (nameError.isNotEmpty()) { { Text(nameError) } } else null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Brief Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Age
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { 
                                if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                    ageText = it
                                }
                            },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Occupation
                        OutlinedTextField(
                            value = occupation,
                            onValueChange = { occupation = it },
                            label = { Text("Occupation") },
                            modifier = Modifier.weight(2f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Main Character Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.LightTap)
                                isMainCharacter = !isMainCharacter
                            }
                        ) {
                            Icon(
                                imageVector = if (isMainCharacter) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = if (isMainCharacter) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "Main Character",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Character Development Section
                    Text(
                        "Character Development",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Physical Description
                    OutlinedTextField(
                        value = physicalDescription,
                        onValueChange = { physicalDescription = it },
                        label = { Text("Physical Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Personality
                    OutlinedTextField(
                        value = personality,
                        onValueChange = { personality = it },
                        label = { Text("Personality Traits") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Backstory
                    OutlinedTextField(
                        value = backstory,
                        onValueChange = { backstory = it },
                        label = { Text("Backstory") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Story Arc Section
                    Text(
                        "Story Arc",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Goals
                    OutlinedTextField(
                        value = goals,
                        onValueChange = { goals = it },
                        label = { Text("Goals & Motivations") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Conflicts
                    OutlinedTextField(
                        value = conflicts,
                        onValueChange = { conflicts = it },
                        label = { Text("Conflicts & Obstacles") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Character Arc
                    OutlinedTextField(
                        value = characterArc,
                        onValueChange = { characterArc = it },
                        label = { Text("Character Arc") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Notes Section
                    Text(
                        "Additional Notes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.LightTap)
                            onDismiss()
                        }
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                nameError = "Name is required"
                                return@Button
                            }
                            
                            haptic.performHapticFeedback(de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType.MediumTap)
                            
                            val age = ageText.toIntOrNull()                            
                            onCreateCharacter(
                                name,
                                description,
                                age,
                                occupation,
                                backstory,
                                personality,
                                physicalDescription,
                                goals,
                                conflicts,
                                isMainCharacter,
                                characterArc,
                                notes,
                                portraitImagePath
                            )
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Create Character")
                    }
                }
            }
        }
    }
}

