package de.astronarren.storyforge.ui.screens.characterdetail.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.ColorUtils
import coil.compose.AsyncImage
import coil.request.ImageRequest
import de.astronarren.storyforge.data.database.entities.Character
import de.astronarren.storyforge.data.database.entities.CharacterRelationship
import de.astronarren.storyforge.data.database.entities.RelationshipType
import de.astronarren.storyforge.data.database.entities.RelationshipStrength
import de.astronarren.storyforge.ui.components.haptic.rememberHapticFeedback
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedRelationshipMappingSection(
    character: Character,
    allCharacters: List<Character>,
    relationships: List<CharacterRelationship>,
    onAddRelationship: (CharacterRelationship) -> Unit,
    onUpdateRelationship: (CharacterRelationship) -> Unit,
    onRemoveRelationship: (CharacterRelationship) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    var showAddRelationshipDialog by remember { mutableStateOf(false) }
    var editingRelationship by remember { mutableStateOf<CharacterRelationship?>(null) }
    
    Column(modifier = modifier) {
        // Section header with enhanced design
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Character Relationships",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${relationships.size} relationship${if (relationships.size != 1) "s" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Add relationship button
                FilledTonalButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                        showAddRelationshipDialog = true
                    },
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (relationships.isEmpty()) {
            // Enhanced empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.GroupAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No relationships defined",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Build ${character.name}'s social network by adding connections to other characters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Relationship types summary
            RelationshipTypesSummary(
                relationships = relationships,
                allCharacters = allCharacters
            )
            
            Spacer(modifier = Modifier.height(16.dp))
              // Enhanced relationships list
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                relationships.forEach { relationship ->
                    val relatedCharacter = allCharacters.find { 
                        it.id == relationship.relatedCharacterId 
                    }
                    
                    if (relatedCharacter != null) {
                        EnhancedRelationshipCard(
                            relationship = relationship,
                            relatedCharacter = relatedCharacter,
                            onEdit = {
                                haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                                editingRelationship = relationship
                            },
                            onRemove = {
                                haptic.performHapticFeedback(HapticFeedbackType.MediumTap)
                                onRemoveRelationship(relationship)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Add relationship dialog
    if (showAddRelationshipDialog) {
        AddRelationshipDialog(
            character = character,
            availableCharacters = allCharacters.filter { otherChar ->
                otherChar.id != character.id && 
                relationships.none { rel -> rel.relatedCharacterId == otherChar.id }
            },
            onDismiss = { showAddRelationshipDialog = false },
            onAddRelationship = { newRelationship ->
                onAddRelationship(newRelationship)
                showAddRelationshipDialog = false
            }
        )
    }
    
    // Edit relationship dialog
    editingRelationship?.let { relationship ->
        val relatedCharacter = allCharacters.find { it.id == relationship.relatedCharacterId }
        if (relatedCharacter != null) {
            EditRelationshipDialog(
                relationship = relationship,
                relatedCharacter = relatedCharacter,
                onDismiss = { editingRelationship = null },
                onUpdateRelationship = { updatedRelationship ->
                    onUpdateRelationship(updatedRelationship)
                    editingRelationship = null
                }
            )
        }
    }
}

@Composable
private fun RelationshipTypesSummary(
    relationships: List<CharacterRelationship>,
    allCharacters: List<Character>,
    modifier: Modifier = Modifier
) {
    val typeGroups = relationships.groupBy { it.relationshipType }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Relationship Types",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(typeGroups.toList()) { (type, relationshipsOfType) ->
                    RelationshipTypeChip(
                        type = type,
                        count = relationshipsOfType.size
                    )
                }
            }
        }
    }
}

@Composable
private fun RelationshipTypeChip(
    type: RelationshipType,
    count: Int,
    modifier: Modifier = Modifier
) {
    val backgroundColor = Color(android.graphics.Color.parseColor(type.color))
    val contentColor = if (ColorUtils.calculateLuminance(backgroundColor.toArgb()) > 0.5) {
        Color.Black
    } else {
        Color.White
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
            if (count > 1) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($count)",
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun EnhancedRelationshipCard(
    relationship: CharacterRelationship,
    relatedCharacter: Character,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = rememberHapticFeedback()
    val backgroundColor = Color(android.graphics.Color.parseColor(relationship.relationshipType.color))
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            width = 2.dp,
            color = backgroundColor.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with character info and relationship type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Character portrait
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (relatedCharacter.portraitImagePath != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(relatedCharacter.portraitImagePath)
                                .build(),
                            contentDescription = "Portrait of ${relatedCharacter.name}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Character info and relationship type
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = relatedCharacter.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RelationshipTypeChip(
                            type = relationship.relationshipType,
                            count = 0 // Don't show count in individual cards
                        )
                        
                        // Relationship strength indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = if (index < relationship.strength.level) {
                                        Icons.Filled.Star
                                    } else {
                                        Icons.Outlined.StarBorder
                                    },
                                    contentDescription = null,
                                    tint = if (index < relationship.strength.level) {
                                        backgroundColor
                                    } else {
                                        MaterialTheme.colorScheme.outline
                                    },
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                    
                    if (relatedCharacter.occupation.isNotBlank()) {
                        Text(
                            text = relatedCharacter.occupation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Action buttons
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit relationship",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove relationship",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
            
            // Relationship description
            if (relationship.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = relationship.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Additional details
            if (relationship.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Notes: ${relationship.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRelationshipDialog(
    character: Character,
    availableCharacters: List<Character>,
    onDismiss: () -> Unit,
    onAddRelationship: (CharacterRelationship) -> Unit,
    modifier: Modifier = Modifier
) {    var selectedCharacter by remember { mutableStateOf<Character?>(null) }
    var selectedType by remember { mutableStateOf(RelationshipType.FRIEND) }
    var selectedStrength by remember { mutableStateOf(RelationshipStrength.MODERATE) }
    var description by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isReciprocal by remember { mutableStateOf(true) }
    var showCharacterPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Dialog title
                Text(
                    text = "Add Relationship",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Character selection
                OutlinedCard(
                    onClick = { showCharacterPicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (selectedCharacter != null) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedCharacter!!.portraitImagePath != null) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(selectedCharacter!!.portraitImagePath)
                                            .build(),
                                        contentDescription = "Portrait of ${selectedCharacter!!.name}",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column {
                                Text(
                                    text = selectedCharacter!!.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (selectedCharacter!!.occupation.isNotBlank()) {
                                    Text(
                                        text = selectedCharacter!!.occupation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            Icon(
                                Icons.Filled.PersonAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Select Character",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Relationship type selection
                Text(
                    text = "Relationship Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(RelationshipType.values()) { type ->
                        val isSelected = selectedType == type
                        val backgroundColor = Color(android.graphics.Color.parseColor(type.color))
                        
                        FilterChip(
                            onClick = { selectedType = type },
                            label = { Text(type.displayName) },
                            selected = isSelected,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = backgroundColor.copy(alpha = 0.8f),
                                selectedLabelColor = if (ColorUtils.calculateLuminance(backgroundColor.toArgb()) > 0.5) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Relationship strength
                Text(
                    text = "Relationship Strength",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween                ) 
                {
                    RelationshipStrength.values().forEach { strength ->
                        val isSelected = selectedStrength == strength
                        
                        FilterChip(
                            onClick = { selectedStrength = strength },
                            label = { 
                                Text(
                                    text = strength.level.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = isSelected,
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe this relationship...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Additional notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Reciprocal relationship toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isReciprocal,
                        onCheckedChange = { isReciprocal = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create reciprocal relationship",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
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
                            selectedCharacter?.let { relatedChar ->
                                val relationship = CharacterRelationship(
                                    characterId = character.id,
                                    relatedCharacterId = relatedChar.id,
                                    relationshipType = selectedType,
                                    strength = selectedStrength,
                                    description = description,
                                    notes = notes,
                                    isReciprocal = isReciprocal
                                )
                                onAddRelationship(relationship)
                            }
                        },
                        enabled = selectedCharacter != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
    
    // Character selection dialog
    if (showCharacterPicker) {
        CharacterSelectionDialog(
            availableCharacters = availableCharacters,
            onDismiss = { showCharacterPicker = false },
            onCharacterSelected = { character ->
                selectedCharacter = character
                showCharacterPicker = false
            }
        )
    }
}

@Composable
private fun EditRelationshipDialog(
    relationship: CharacterRelationship,
    relatedCharacter: Character,
    onDismiss: () -> Unit,
    onUpdateRelationship: (CharacterRelationship) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedType by remember { mutableStateOf(relationship.relationshipType) }
    var selectedStrength by remember { mutableStateOf(relationship.strength) }
    var description by remember { mutableStateOf(relationship.description) }
    var notes by remember { mutableStateOf(relationship.notes) }
    var isReciprocal by remember { mutableStateOf(relationship.isReciprocal) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Dialog title
                Text(
                    text = "Edit Relationship",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Related character info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (relatedCharacter.portraitImagePath != null) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(relatedCharacter.portraitImagePath)
                                        .build(),
                                    contentDescription = "Portrait of ${relatedCharacter.name}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = relatedCharacter.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (relatedCharacter.occupation.isNotBlank()) {
                                Text(
                                    text = relatedCharacter.occupation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Relationship type selection
                Text(
                    text = "Relationship Type",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(RelationshipType.values()) { type ->
                        val isSelected = selectedType == type
                        val backgroundColor = Color(android.graphics.Color.parseColor(type.color))
                        
                        FilterChip(
                            onClick = { selectedType = type },
                            label = { Text(type.displayName) },
                            selected = isSelected,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = backgroundColor.copy(alpha = 0.8f),
                                selectedLabelColor = if (ColorUtils.calculateLuminance(backgroundColor.toArgb()) > 0.5) {
                                    Color.Black
                                } else {
                                    Color.White
                                }
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Relationship strength
                Text(
                    text = "Relationship Strength",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    RelationshipStrength.values().forEach { strength ->
                        val isSelected = selectedStrength == strength
                        
                        FilterChip(
                            onClick = { selectedStrength = strength },
                            label = { 
                                Text(
                                    text = strength.level.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = isSelected,
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    placeholder = { Text("Describe this relationship...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Additional notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Reciprocal relationship toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isReciprocal,
                        onCheckedChange = { isReciprocal = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Is reciprocal relationship",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
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
                            val updatedRelationship = relationship.copy(
                                relationshipType = selectedType,
                                strength = selectedStrength,
                                description = description,
                                notes = notes,
                                isReciprocal = isReciprocal
                            )
                            onUpdateRelationship(updatedRelationship)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Update")
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterSelectionDialog(
    availableCharacters: List<Character>,
    onDismiss: () -> Unit,
    onCharacterSelected: (Character) -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select Character",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (availableCharacters.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.PersonAdd,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No characters available",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "All characters already have relationships or there are no other characters in this story.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }                
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableCharacters) { character ->
                            OutlinedCard(
                                onClick = { onCharacterSelected(character) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (character.portraitImagePath != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(character.portraitImagePath)
                                                    .build(),
                                                contentDescription = "Portrait of ${character.name}",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                Icons.Filled.Person,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column {
                                        Text(
                                            text = character.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        if (character.occupation.isNotBlank()) {
                                            Text(
                                                text = character.occupation,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
