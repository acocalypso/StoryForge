package de.astronarren.storyforge.ui.screens.chapterlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.astronarren.storyforge.data.database.entities.Chapter
import de.astronarren.storyforge.data.model.ExportResult
import de.astronarren.storyforge.ui.components.export.BookExportDialog
import de.astronarren.storyforge.ui.components.export.ExportSuccessDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    bookId: String,
    onNavigateBack: () -> Unit,
    onChapterEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChapterListViewModel = hiltViewModel()
) {    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showExportDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(bookId) {
        viewModel.loadChapters(bookId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chapters") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },                actions = {
                    // Export book button
                    IconButton(
                        onClick = { showExportDialog = true },
                        enabled = uiState.chapters.isNotEmpty() && !uiState.isExporting
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = "Export Book")
                    }
                    
                    // Add chapter button
                    IconButton(onClick = { viewModel.addNewChapter(bookId) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Chapter")
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )                }
                uiState.error != null -> {
                    val errorMessage = uiState.error ?: "Unknown error"
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error loading chapters",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadChapters(bookId) }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.chapters.isEmpty() -> {
                    EmptyChaptersList(
                        onAddChapter = { viewModel.addNewChapter(bookId) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.chapters) { chapter ->
                            ChapterCard(
                                chapter = chapter,
                                onEdit = { onChapterEdit(chapter.id) },
                                onDelete = { viewModel.deleteChapter(chapter) }
                            )
                        }
                    }                }
            }
        }
    }    // Export Dialog
    if (showExportDialog && uiState.chapters.isNotEmpty()) {
        BookExportDialog(
            bookTitle = "Book", // We could pass this as a parameter if needed
            chapterCount = uiState.chapters.size,
            onDismiss = { showExportDialog = false },
            onExport = { format ->
                viewModel.exportBook(bookId, format)
                showExportDialog = false
            },
            isExporting = uiState.isExporting,
            permissionManager = viewModel.permissionManager
        )
    }
    
    // Export success dialog
    uiState.exportResult?.let { result ->
        when (result) {
            is ExportResult.Success -> {
                ExportSuccessDialog(
                    filePath = result.filePath,
                    exportedItemCount = result.bookCount,
                    itemType = "chapters",
                    onDismiss = { viewModel.clearExportResult() },
                    onShare = {
                        // TODO: Implement sharing functionality
                        viewModel.clearExportResult()
                    }
                )
            }
            is ExportResult.Error -> {
                // Error is already handled in the uiState.error
                viewModel.clearExportResult()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChapterCard(
    chapter: Chapter,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (chapter.color != null) {
                // Parse color string to Color if needed
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chapter.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (chapter.description.isNotBlank()) {
                        Text(
                            text = chapter.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                // Action buttons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit chapter",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete chapter",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Word count
                Text(
                    text = "${chapter.wordCount} words",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Target progress
                if (chapter.targetWordCount != null && chapter.targetWordCount > 0) {
                    val progress = (chapter.wordCount.toFloat() / chapter.targetWordCount).coerceAtMost(1f)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.width(80.dp),
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Completion status
                if (chapter.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Chapter") },
            text = { Text("Are you sure you want to delete \"${chapter.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptyChaptersList(
    onAddChapter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No chapters yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Start writing by creating your first chapter",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onAddChapter) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Chapter")
        }
    }
}



