package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Add,
    title: String,
    description: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Large icon with subtle tint
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Description
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        // Optional action button
        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onActionClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun BookListEmptyState(
    onCreateBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {    EmptyState(
        icon = Icons.Default.Add,
        title = "No books yet",
        description = "Start your writing journey by creating your first book. Every great story begins with a single idea.",
        actionText = "Create Your First Book",
        onActionClick = onCreateBookClick,
        modifier = modifier
    )
}

@Composable
fun SearchEmptyState(
    searchQuery: String,
    modifier: Modifier = Modifier
) {    EmptyState(
        icon = Icons.Default.Search,
        title = "No books found",
        description = "We couldn't find any books matching \"$searchQuery\". Try adjusting your search terms or create a new book.",
        modifier = modifier
    )
}

