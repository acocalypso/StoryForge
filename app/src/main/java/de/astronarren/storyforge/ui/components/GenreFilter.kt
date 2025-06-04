package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.data.model.BookGenre

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreFilter(
    selectedGenre: BookGenre?,
    onGenreSelected: (BookGenre?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.wrapContentHeight()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter by Genre",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            if (selectedGenre != null) {
                TextButton(
                    onClick = { onGenreSelected(null) }
                ) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Clear filter",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Clear")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            modifier = Modifier.height(40.dp), // Fixed height to prevent constraint issues
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(BookGenre.getAllGenres()) { genre ->
                FilterChip(
                    onClick = {
                        if (selectedGenre == genre) {
                            onGenreSelected(null) // Deselect if already selected
                        } else {
                            onGenreSelected(genre)
                        }
                    },
                    label = { Text(genre.displayName) },
                    selected = selectedGenre == genre,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }
    }
}

