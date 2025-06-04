package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.astronarren.storyforge.data.model.*
import de.astronarren.storyforge.ui.components.haptic.rememberHapticFeedback
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchDialog(
    currentCriteria: AdvancedSearchCriteria,
    availableAuthors: List<String>,
    onCriteriaChanged: (AdvancedSearchCriteria) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberHapticFeedback()
    var localCriteria by remember { mutableStateOf(currentCriteria) }
    
    Dialog(
        onDismissRequest = { 
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LightTap)
            onDismiss() 
        }
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Advanced Search",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = { 
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LightTap)
                            onDismiss() 
                        }
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Scrollable content
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Search Query Section
                    item {
                        SearchQuerySection(
                            query = localCriteria.searchQuery,
                            onQueryChanged = { query ->
                                localCriteria = localCriteria.copy(searchQuery = query)
                            }
                        )
                    }
                    
                    // Genre Filter Section
                    item {
                        GenreFilterSection(
                            selectedGenres = localCriteria.genres,
                            onGenresChanged = { genres ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LightTap)
                                localCriteria = localCriteria.copy(genres = genres)
                            }
                        )
                    }
                    
                    // Author Filter Section
                    item {
                        AuthorFilterSection(
                            availableAuthors = availableAuthors,
                            selectedAuthors = localCriteria.authors,
                            onAuthorsChanged = { authors ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LightTap)
                                localCriteria = localCriteria.copy(authors = authors)
                            }
                        )
                    }
                    
                    // Word Count Range Section
                    item {
                        WordCountFilterSection(
                            selectedRange = localCriteria.wordCountRange,
                            onRangeChanged = { range ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LightTap)
                                localCriteria = localCriteria.copy(wordCountRange = range)
                            }
                        )
                    }
                    
                    // Sort Options Section
                    item {
                        SortOptionsSection(
                            selectedSort = localCriteria.sortBy,
                            onSortChanged = { sortBy ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LightTap)
                                localCriteria = localCriteria.copy(sortBy = sortBy)
                            }
                        )
                    }
                    
                    // Additional Options Section
                    item {
                        AdditionalOptionsSection(
                            favoritesOnly = localCriteria.favoritesOnly,
                            onFavoritesOnlyChanged = { favoritesOnly ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LightTap)
                                localCriteria = localCriteria.copy(favoritesOnly = favoritesOnly)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.MediumTap)
                            localCriteria = AdvancedSearchCriteria()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear All")
                    }
                    
                    Button(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.Success)
                            onCriteriaChanged(localCriteria)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchQuerySection(
    query: String,
    onQueryChanged: (String) -> Unit
) {
    Column {
        Text(
            text = "Search Text",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search titles, authors, descriptions...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear search")
                    }
                }
            },
            singleLine = true
        )
    }
}

@Composable
private fun GenreFilterSection(
    selectedGenres: Set<BookGenre>,
    onGenresChanged: (Set<BookGenre>) -> Unit
) {
    Column {
        Text(
            text = "Genres",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BookGenre.values()) { genre ->
                FilterChip(
                    selected = genre in selectedGenres,
                    onClick = {
                        val newGenres = if (genre in selectedGenres) {
                            selectedGenres - genre
                        } else {
                            selectedGenres + genre
                        }
                        onGenresChanged(newGenres)
                    },
                    label = { Text(genre.displayName) }
                )
            }
        }
        
        if (selectedGenres.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${selectedGenres.size} genre(s) selected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AuthorFilterSection(
    availableAuthors: List<String>,
    selectedAuthors: Set<String>,
    onAuthorsChanged: (Set<String>) -> Unit
) {
    Column {
        Text(
            text = "Authors",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        if (availableAuthors.isNotEmpty()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableAuthors.take(10)) { author ->
                    FilterChip(
                        selected = author in selectedAuthors,
                        onClick = {
                            val newAuthors = if (author in selectedAuthors) {
                                selectedAuthors - author
                            } else {
                                selectedAuthors + author
                            }
                            onAuthorsChanged(newAuthors)
                        },
                        label = { Text(author) }
                    )
                }
            }
            
            if (selectedAuthors.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${selectedAuthors.size} author(s) selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Text(
                text = "No authors available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WordCountFilterSection(
    selectedRange: WordCountRange?,
    onRangeChanged: (WordCountRange?) -> Unit
) {
    Column {
        Text(
            text = "Word Count Range",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(WordCountRange.getAllRanges()) { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = {
                        onRangeChanged(if (selectedRange == range) null else range)
                    },
                    label = { Text(range.getDisplayName()) }
                )
            }
        }
        
        selectedRange?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Selected: ${it.getDisplayName()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SortOptionsSection(
    selectedSort: SortBy,
    onSortChanged: (SortBy) -> Unit
) {
    Column {
        Text(
            text = "Sort By",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            modifier = Modifier.selectableGroup()
        ) {
            SortBy.values().forEach { sortOption ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = selectedSort == sortOption,
                            onClick = { onSortChanged(sortOption) }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedSort == sortOption,
                        onClick = { onSortChanged(sortOption) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = sortOption.displayName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AdditionalOptionsSection(
    favoritesOnly: Boolean,
    onFavoritesOnlyChanged: (Boolean) -> Unit
) {
    Column {
        Text(
            text = "Additional Options",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = favoritesOnly,
                onCheckedChange = onFavoritesOnlyChanged
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Show favorites only",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = if (favoritesOnly) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

