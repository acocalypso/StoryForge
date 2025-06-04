package de.astronarren.storyforge.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import de.astronarren.storyforge.data.model.BookAnalytics
import de.astronarren.storyforge.data.model.MonthlyData
import de.astronarren.storyforge.ui.components.haptic.rememberHapticFeedback
import de.astronarren.storyforge.ui.components.haptic.HapticFeedbackType
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardDialog(
    analytics: BookAnalytics,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = rememberHapticFeedback()
    
    Dialog(        onDismissRequest = { 
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
                        text = "Writing Analytics",
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
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Overview Statistics
                    item {
                        OverviewStatistics(analytics = analytics)
                    }
                    
                    // Recent Activity
                    item {
                        RecentActivity(analytics = analytics)
                    }
                    
                    // Genre Distribution
                    item {
                        GenreDistribution(analytics = analytics)
                    }
                    
                    // Author Statistics
                    if (analytics.authorDistribution.isNotEmpty()) {
                        item {
                            AuthorStatistics(analytics = analytics)
                        }
                    }
                    
                    // Book Creation Trend
                    item {
                        BookCreationTrend(analytics = analytics)
                    }
                    
                    // Notable Books
                    item {
                        NotableBooks(analytics = analytics)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewStatistics(analytics: BookAnalytics) {
    Column {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {            StatCard(
                title = "Total Books",
                value = analytics.totalBooks.toString(),
                icon = Icons.Default.Add, // Using available icon
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                title = "Favorites",
                value = "${analytics.favoriteBooks} (${String.format(java.util.Locale.getDefault(), "%.1f", analytics.favoritePercentage)}%)",
                icon = Icons.Default.Favorite,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {            StatCard(
                title = "Target Words",
                value = if (analytics.totalTargetWords > 0) {
                    "${analytics.totalTargetWords / 1000}K"
                } else "None set",                icon = Icons.Default.Edit,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                title = "Avg. per Book",
                value = if (analytics.averageTargetWords > 0) {
                    "${analytics.averageTargetWords / 1000}K words"
                } else "N/A",
                icon = Icons.Default.Info, // Using available icon
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RecentActivity(analytics: BookAnalytics) {
    Column {
        Text(
            text = "Recent Activity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActivityCard(
                title = "This Month",
                value = analytics.booksCreatedThisMonth.toString(),
                subtitle = "books created",
                modifier = Modifier.weight(1f)
            )
            
            ActivityCard(
                title = "This Year",
                value = analytics.booksCreatedThisYear.toString(),
                subtitle = "books created",
                modifier = Modifier.weight(1f)
            )
        }
        
        if (analytics.mostProductiveMonth.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            ActivityCard(
                title = "Most Productive",
                value = analytics.mostProductiveMonth,
                subtitle = "highest book creation",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GenreDistribution(analytics: BookAnalytics) {
    Column {
        Text(
            text = "Genre Distribution",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        if (analytics.genreDistribution.isNotEmpty()) {
            val sortedGenres = analytics.genreDistribution.toList()
                .sortedByDescending { it.second }
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedGenres) { (genre, count) ->
                    GenreChip(
                        genre = genre,
                        count = count,
                        total = analytics.totalBooks
                    )
                }
            }
        } else {
            Text(
                text = "No genre data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AuthorStatistics(analytics: BookAnalytics) {
    Column {
        Text(
            text = "Authors",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        val sortedAuthors = analytics.authorDistribution.toList()
            .sortedByDescending { it.second }
            .take(5) // Show top 5 authors
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedAuthors.forEach { (author, count) ->
                AuthorStatItem(
                    author = author,
                    count = count,
                    total = analytics.totalBooks
                )
            }
        }
    }
}

@Composable
private fun BookCreationTrend(analytics: BookAnalytics) {
    Column {
        Text(
            text = "Creation Trend (Last 12 Months)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        if (analytics.bookCreationTrend.isNotEmpty()) {
            SimpleBarChart(
                data = analytics.bookCreationTrend,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "No trend data available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NotableBooks(analytics: BookAnalytics) {
    Column {
        Text(
            text = "Notable Books",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            analytics.oldestBook?.let { book ->                NotableBookItem(
                    title = "Oldest Project",
                    bookTitle = book.title,
                    subtitle = "Created ${java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault()).format(book.createdAt)}",
                    icon = Icons.Default.DateRange // Using available icon
                )
            }
            
            analytics.newestBook?.let { book ->                NotableBookItem(
                    title = "Latest Project",
                    bookTitle = book.title,
                    subtitle = "Created ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(book.createdAt)}",
                    icon = Icons.Default.Clear // Using available icon  
                )
            }
            
            analytics.longestBook?.let { book ->                NotableBookItem(
                    title = "Longest Project",
                    bookTitle = book.title,
                    subtitle = "${book.targetWordCount?.let { "${it / 1000}K" } ?: "Unknown"} words target",
                    icon = Icons.Default.MoreVert // Using available icon
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ActivityCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GenreChip(
    genre: String,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) (count * 100.0 / total) else 0.0
    
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.clip(RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = genre,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count books",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${String.format(java.util.Locale.getDefault(), "%.1f", percentage)}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
private fun AuthorStatItem(
    author: String,
    count: Int,
    total: Int
) {
    val percentage = if (total > 0) (count * 100.0 / total) else 0.0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = author,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$count books (${String.format(java.util.Locale.getDefault(), "%.1f", percentage)}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SimpleBarChart(
    data: List<MonthlyData>,
    modifier: Modifier = Modifier
) {
    val maxCount = data.maxOfOrNull { it.count } ?: 1
    
    Column(modifier = modifier) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(data) { monthData ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(50.dp)
                ) {                    // Bar
                    val barHeight = if (maxCount > 0) {
                        val calculatedHeight = (monthData.count * 80.0 / maxCount).dp
                        if (calculatedHeight < 4.dp) 4.dp else calculatedHeight
                    } else 4.dp
                    
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(barHeight)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                            )
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Count
                    Text(
                        text = monthData.count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Month label
                    Text(
                        text = monthData.month,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NotableBookItem(
    title: String,
    bookTitle: String,
    subtitle: String,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = bookTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

