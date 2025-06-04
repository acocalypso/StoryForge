package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import de.astronarren.storyforge.data.model.BookGenre

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreSelector(
    selectedGenre: BookGenre,
    onGenreSelected: (BookGenre) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Genre"
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        Box {
            OutlinedTextField(
                readOnly = true,
                value = selectedGenre.displayName,
                onValueChange = { },
                label = { Text(label) },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Select genre")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
            )
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                BookGenre.getAllGenres().forEach { genre ->
                    DropdownMenuItem(
                        text = { Text(genre.displayName) },
                        onClick = {
                            onGenreSelected(genre)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

