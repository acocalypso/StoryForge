package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.astronarren.storyforge.data.model.BookGenre
import de.astronarren.storyforge.ui.components.haptic.rememberHapticFeedback
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType
import de.astronarren.storyforge.utils.rememberImagePicker

data class ValidationError(
    val field: String,
    val message: String
)

@Composable
fun CreateBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, author: String, genre: String, targetWordCount: Int?, coverImagePath: String?) -> Unit,
    isLoading: Boolean = false
) {    val haptics = rememberHapticFeedback()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(BookGenre.FICTION) }
    var targetWordCountText by remember { mutableStateOf("") }
    var coverImagePath by remember { mutableStateOf<String?>(null) }
    var validationErrors by remember { mutableStateOf<List<ValidationError>>(emptyList()) }
    
    val pickImage = rememberImagePicker { imagePath ->
        coverImagePath = imagePath
    }
    
    fun validateInput(): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        if (title.isBlank()) {
            errors.add(ValidationError("title", "Title is required"))
        } else if (title.length < 2) {
            errors.add(ValidationError("title", "Title must be at least 2 characters"))
        } else if (title.length > 100) {
            errors.add(ValidationError("title", "Title must be less than 100 characters"))
        }
        
        if (author.isNotBlank() && author.length > 50) {
            errors.add(ValidationError("author", "Author name must be less than 50 characters"))
        }
          if (description.length > 500) {
            errors.add(ValidationError("description", "Description must be less than 500 characters"))
        }
        
        if (targetWordCountText.isNotBlank()) {
            val targetWordCount = targetWordCountText.toIntOrNull()
            if (targetWordCount == null) {
                errors.add(ValidationError("targetWordCount", "Word count must be a valid number"))
            } else if (targetWordCount <= 0) {
                errors.add(ValidationError("targetWordCount", "Word count must be greater than 0"))
            } else if (targetWordCount > 1000000) {
                errors.add(ValidationError("targetWordCount", "Word count must be less than 1,000,000"))
            }
        }
        
        return errors
    }
      fun getFieldError(field: String): String? {
        return validationErrors.find { it.field == field }?.message
    }
    
    fun handleCreate() {
        val errors = validateInput()
        validationErrors = errors
        
        if (errors.isEmpty()) {
            val targetWordCount = if (targetWordCountText.isBlank()) null else targetWordCountText.toIntOrNull()
            onConfirm(title.trim(), description.trim(), author.trim(), selectedGenre.displayName, targetWordCount, coverImagePath)
        }
    }
      Dialog(onDismissRequest = onDismiss) {
        val configuration = LocalConfiguration.current
        val maxHeight = (configuration.screenHeightDp * 0.9).dp
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {Text(
                    "Create New Book",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Cover Image Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BookCoverImage(
                        coverImagePath = coverImagePath,
                        title = title.ifBlank { "New Book" },
                        isEditable = true,
                        onImageClick = { pickImage() },
                        size = BookCoverSize.MEDIUM
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Book Cover",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (coverImagePath != null) "Tap to change cover image" else "Tap to add cover image",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                  OutlinedTextField(
                    value = title,
                    onValueChange = { 
                        title = it
                        // Clear title-specific errors when user types
                        validationErrors = validationErrors.filter { error -> error.field != "title" }
                    },                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = getFieldError("title") != null,
                    supportingText = {
                        getFieldError("title")?.let { error ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                )
                
                OutlinedTextField(
                    value = author,
                    onValueChange = { 
                        author = it
                        validationErrors = validationErrors.filter { error -> error.field != "author" }
                    },
                    label = { Text("Author") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = getFieldError("author") != null,
                    supportingText = {                        getFieldError("author")?.let { error ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall                                )
                            }
                        }
                    }
                )
                
                GenreSelector(
                    selectedGenre = selectedGenre,
                    onGenreSelected = { selectedGenre = it },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = targetWordCountText,
                    onValueChange = { 
                        targetWordCountText = it.filter { char -> char.isDigit() }
                        validationErrors = validationErrors.filter { error -> error.field != "targetWordCount" }
                    },
                    label = { Text("Target Word Count (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = getFieldError("targetWordCount") != null,
                    supportingText = {
                        getFieldError("targetWordCount")?.let { error ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { 
                        description = it
                        validationErrors = validationErrors.filter { error -> error.field != "description" }
                    },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),                    
                    minLines = 3,
                    maxLines = 5,
                    isError = getFieldError("description") != null,
                    supportingText = {
                        val error = getFieldError("description")
                        if (error != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Text(
                                text = "${description.length}/500 characters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )                }
                
                // Buttons section - always visible at bottom
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { 
                                haptics.performHapticFeedback(HapticFeedbackType.LightTap)
                                onDismiss() 
                            },
                            enabled = !isLoading
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(                            onClick = { 
                                haptics.performHapticFeedback(HapticFeedbackType.MediumTap)
                                handleCreate() 
                            },
                            enabled = !isLoading && title.isNotBlank(),
                            modifier = Modifier.widthIn(min = 80.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Create")
                            }
                        }
                    }
                }
            }
        }
    }
}

