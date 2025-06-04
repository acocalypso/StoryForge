package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.astronarren.storyforge.data.model.BookGenre
import de.astronarren.storyforge.data.model.ExportFormat
import de.astronarren.storyforge.data.model.ExportOptions
import de.astronarren.storyforge.ui.components.haptic.rememberHapticFeedback
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportDialog(
    availableGenres: List<String>,
    onExport: (ExportOptions) -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false
) {
    val haptic = rememberHapticFeedback()
    
    var selectedFormat by remember { mutableStateOf(ExportFormat.TXT) }
    var includeArchived by remember { mutableStateOf(false) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var selectedGenres by remember { mutableStateOf<Set<String>>(emptySet()) }
    var customFilename by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Export Books",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                            onDismiss()
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Export Format Selection
                    item {
                        Text(
                            text = "Export Format",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Column(modifier = Modifier.selectableGroup()) {
                            ExportFormat.values().forEach { format ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (selectedFormat == format),
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                                                selectedFormat = format
                                            },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = (selectedFormat == format),
                                        onClick = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = format.displayName,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = ".${format.extension}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // Custom Filename
                    item {
                        Text(
                            text = "Filename (optional)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = customFilename,
                            onValueChange = { customFilename = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Leave empty for auto-generated name") },
                            singleLine = true,
                            trailingIcon = {
                                Text(
                                    text = ".${selectedFormat.extension}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        )
                    }
                    
                    // Filter Options
                    item {
                        Text(
                            text = "Filter Options",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Include Archived
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = includeArchived,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                                    includeArchived = it
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Include archived books")
                        }
                        
                        // Favorites Only
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = favoritesOnly,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                                    favoritesOnly = it
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export favorites only")
                        }
                    }
                    
                    // Genre Selection
                    if (availableGenres.isNotEmpty()) {
                        item {
                            Text(
                                text = "Select Genres (optional)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = "Leave all unchecked to export all genres",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        items(availableGenres.chunked(2)) { genreChunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                genreChunk.forEach { genre ->
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = genre in selectedGenres,
                                            onCheckedChange = { checked ->
                                                haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                                                selectedGenres = if (checked) {
                                                    selectedGenres + genre
                                                } else {
                                                    selectedGenres - genre
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = genre,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                
                                // Fill empty space if odd number of genres
                                if (genreChunk.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LightTap)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.MediumTap)
                            val options = ExportOptions(
                                format = selectedFormat,
                                includeArchived = includeArchived,
                                includeFavoritesOnly = favoritesOnly,
                                selectedGenres = selectedGenres,                                filename = customFilename.ifBlank { 
                                    "storyforge_books_${java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm", java.util.Locale.getDefault()).format(java.util.Date())}"
                                }
                            )
                            onExport(options)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {                            Icon(
                                Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export")
                        }
                    }
                }
            }
        }
    }
}

