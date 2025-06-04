package de.astronarren.storyforge.ui.screens.scenelist.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.astronarren.storyforge.ui.screens.scenelist.SceneListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedLocation: String,
    onLocationChange: (String) -> Unit,
    selectedTimeOfDay: String,
    onTimeOfDayChange: (String) -> Unit,
    selectedMood: String,
    onMoodChange: (String) -> Unit,
    sortBy: SceneListViewModel.SortOption,
    onSortByChange: (SceneListViewModel.SortOption) -> Unit,
    uniqueLocations: List<String>,
    uniqueTimesOfDay: List<String>,
    uniqueMoods: List<String>,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val hasActiveFilters = searchQuery.isNotBlank() || 
                          selectedLocation.isNotBlank() || 
                          selectedTimeOfDay.isNotBlank() || 
                          selectedMood.isNotBlank()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search scenes...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Filter Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Location Filter
                FilterDropdown(
                    label = "Location",
                    selectedValue = selectedLocation,
                    onValueChange = onLocationChange,
                    options = uniqueLocations,
                    icon = Icons.Default.LocationOn
                )

                // Time of Day Filter
                FilterDropdown(
                    label = "Time",
                    selectedValue = selectedTimeOfDay,
                    onValueChange = onTimeOfDayChange,
                    options = uniqueTimesOfDay,
                    icon = Icons.Default.Schedule
                )

                // Mood Filter
                FilterDropdown(
                    label = "Mood",
                    selectedValue = selectedMood,
                    onValueChange = onMoodChange,
                    options = uniqueMoods,
                    icon = Icons.Default.Mood
                )

                // Sort Button
                Box {
                    FilterChip(
                        onClick = { showSortMenu = true },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Sort,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = getSortDisplayName(sortBy),
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Icon(
                                    Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        selected = sortBy != SceneListViewModel.SortOption.ORDER
                    )

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        SceneListViewModel.SortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(getSortDisplayName(option)) },
                                onClick = {
                                    onSortByChange(option)
                                    showSortMenu = false
                                },
                                leadingIcon = {
                                    Icon(
                                        getSortIcon(option),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                // Clear Filters Button
                if (hasActiveFilters) {
                    FilterChip(
                        onClick = onClearFilters,
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Clear",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        },
                        selected = false
                    )
                }
            }

            // Active Filters Display
            if (hasActiveFilters) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedLocation.isNotBlank()) {
                        ActiveFilterChip(
                            label = "Location: $selectedLocation",
                            onRemove = { onLocationChange("") }
                        )
                    }
                    if (selectedTimeOfDay.isNotBlank()) {
                        ActiveFilterChip(
                            label = "Time: $selectedTimeOfDay",
                            onRemove = { onTimeOfDayChange("") }
                        )
                    }
                    if (selectedMood.isNotBlank()) {
                        ActiveFilterChip(
                            label = "Mood: $selectedMood",
                            onRemove = { onMoodChange("") }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterDropdown(
    label: String,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    options: List<String>,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FilterChip(
            onClick = { expanded = true },
            label = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (selectedValue.isNotBlank()) selectedValue else label,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            selected = selectedValue.isNotBlank()
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (selectedValue.isNotBlank()) {
                DropdownMenuItem(
                    text = { Text("Clear") },
                    onClick = {
                        onValueChange("")
                        expanded = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                HorizontalDivider()
            }
            
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ActiveFilterChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    InputChip(
        onClick = onRemove,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall
            )
        },
        selected = true,
        trailingIcon = {
            Icon(
                Icons.Default.Cancel,
                contentDescription = "Remove filter",
                modifier = Modifier.size(14.dp)
            )
        },
        modifier = modifier
    )
}

private fun getSortDisplayName(sortOption: SceneListViewModel.SortOption): String {
    return when (sortOption) {
        SceneListViewModel.SortOption.ORDER -> "Order"
        SceneListViewModel.SortOption.TITLE -> "Title"
        SceneListViewModel.SortOption.LOCATION -> "Location"
        SceneListViewModel.SortOption.TIME_OF_DAY -> "Time"
        SceneListViewModel.SortOption.MOOD -> "Mood"
        SceneListViewModel.SortOption.CREATED_DATE -> "Created"
        SceneListViewModel.SortOption.WORD_COUNT -> "Word Count"
    }
}

private fun getSortIcon(sortOption: SceneListViewModel.SortOption): androidx.compose.ui.graphics.vector.ImageVector {
    return when (sortOption) {
        SceneListViewModel.SortOption.ORDER -> Icons.Default.FormatListNumbered
        SceneListViewModel.SortOption.TITLE -> Icons.Default.Title
        SceneListViewModel.SortOption.LOCATION -> Icons.Default.LocationOn
        SceneListViewModel.SortOption.TIME_OF_DAY -> Icons.Default.Schedule
        SceneListViewModel.SortOption.MOOD -> Icons.Default.Mood
        SceneListViewModel.SortOption.CREATED_DATE -> Icons.Default.CalendarToday
        SceneListViewModel.SortOption.WORD_COUNT -> Icons.Default.TextFields
    }
}
