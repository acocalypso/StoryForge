package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class DrawerAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val isSelected: Boolean = false,
    val badge: String? = null
)

data class DrawerSection(
    val title: String,
    val actions: List<DrawerAction>
)

@Composable
fun NavigationDrawerContent(
    sections: List<DrawerSection>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // App header
        item {
            Column(
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Text(
                    text = "StoryForge",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Navigation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }

        // Sections
        items(sections) { section ->
            DrawerSectionItem(section = section)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DrawerSectionItem(
    section: DrawerSection,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header
        Text(
            text = section.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Section actions
        section.actions.forEach { action ->
            NavigationDrawerItem(
                label = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(action.title)
                        action.badge?.let { badge ->
                            Badge {
                                Text(badge, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                },
                icon = { 
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title
                    )
                },
                selected = action.isSelected,
                onClick = action.onClick,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

// Helper functions for common drawer sections
object DrawerSections {
    
    fun createFilterSection(
        showOnlyMainCharacters: Boolean,
        onToggleMainCharacters: () -> Unit,
        showFavoritesOnly: Boolean? = null,
        onToggleFavorites: (() -> Unit)? = null,
        searchQuery: String = "",
        onSearchQueryChange: ((String) -> Unit)? = null,
        onClearFilters: (() -> Unit)? = null
    ): DrawerSection {
        val actions = mutableListOf<DrawerAction>()
        
        // Main characters filter
        actions.add(
            DrawerAction(
                title = "Main Characters Only",
                icon = if (showOnlyMainCharacters) Icons.Filled.Star else Icons.Outlined.Star,
                onClick = onToggleMainCharacters,
                isSelected = showOnlyMainCharacters
            )
        )
        
        // Favorites filter (for book lists)
        if (showFavoritesOnly != null && onToggleFavorites != null) {
            actions.add(
                DrawerAction(
                    title = "Favorites Only",
                    icon = if (showFavoritesOnly) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    onClick = onToggleFavorites,
                    isSelected = showFavoritesOnly
                )
            )
        }
        
        // Clear filters action
        if (onClearFilters != null && (showOnlyMainCharacters || searchQuery.isNotEmpty() || showFavoritesOnly == true)) {
            actions.add(
                DrawerAction(
                    title = "Clear All Filters",
                    icon = Icons.Filled.Clear,
                    onClick = onClearFilters
                )
            )
        }
        
        return DrawerSection(
            title = "Filters",
            actions = actions
        )
    }
    
    fun createActionsSection(
        onSearch: (() -> Unit)? = null,
        onAdvancedSearch: (() -> Unit)? = null,
        onAnalytics: (() -> Unit)? = null,
        onImport: (() -> Unit)? = null,
        onExport: (() -> Unit)? = null,
        onSettings: (() -> Unit)? = null,
        onCreateNew: (() -> Unit)? = null
    ): DrawerSection {
        val actions = mutableListOf<DrawerAction>()
        
        onSearch?.let {
            actions.add(
                DrawerAction(
                    title = "Search",
                    icon = Icons.Filled.Search,
                    onClick = it
                )
            )        }
        
        onAdvancedSearch?.let {
            actions.add(
                DrawerAction(
                    title = "Advanced Search",
                    icon = Icons.Filled.Tune,
                    onClick = it
                )
            )
        }
        
        onCreateNew?.let {
            actions.add(
                DrawerAction(
                    title = "Create New",
                    icon = Icons.Filled.Add,
                    onClick = it
                )
            )        }
        
        onAnalytics?.let {
            actions.add(
                DrawerAction(
                    title = "Analytics",
                    icon = Icons.Filled.Analytics,
                    onClick = it
                )
            )
        }
        
        onImport?.let {
            actions.add(
                DrawerAction(
                    title = "Import",
                    icon = Icons.Filled.Download,
                    onClick = it
                )
            )
        }
        
        onExport?.let {
            actions.add(
                DrawerAction(
                    title = "Export",
                    icon = Icons.Filled.Upload,
                    onClick = it
                )
            )
        }
        
        onSettings?.let {
            actions.add(
                DrawerAction(
                    title = "Settings",
                    icon = Icons.Filled.Settings,
                    onClick = it
                )
            )
        }
        
        return DrawerSection(
            title = "Actions",
            actions = actions
        )
    }
    
    fun createNavigationSection(
        onNavigateToChapters: (() -> Unit)? = null,
        onNavigateToCharacters: (() -> Unit)? = null,
        onNavigateToTimeline: (() -> Unit)? = null,
        onNavigateToNotes: (() -> Unit)? = null,
        currentScreen: String? = null
    ): DrawerSection {
        val actions = mutableListOf<DrawerAction>()
        
        onNavigateToChapters?.let {
            actions.add(
                DrawerAction(
                    title = "Chapters",
                    icon = Icons.Filled.Edit,
                    onClick = it,
                    isSelected = currentScreen == "chapters"
                )
            )
        }
        
        onNavigateToCharacters?.let {
            actions.add(
                DrawerAction(
                    title = "Characters",
                    icon = Icons.Filled.Person,
                    onClick = it,
                    isSelected = currentScreen == "characters"
                )
            )
        }
        
        onNavigateToTimeline?.let {
            actions.add(
                DrawerAction(
                    title = "Timeline",
                    icon = Icons.Filled.Edit,
                    onClick = it,
                    isSelected = currentScreen == "timeline"
                )
            )
        }
        
        onNavigateToNotes?.let {
            actions.add(
                DrawerAction(
                    title = "Notes",
                    icon = Icons.Filled.Edit,
                    onClick = it,
                    isSelected = currentScreen == "notes"
                )
            )
        }
        
        return DrawerSection(
            title = "Navigate",
            actions = actions
        )
    }
}
