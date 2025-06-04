package de.astronarren.storyforge.ui.screens.bookdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import de.astronarren.storyforge.data.model.ExportResult
import de.astronarren.storyforge.ui.components.export.BookExportDialog
import de.astronarren.storyforge.ui.components.export.ExportSuccessDialog
import de.astronarren.storyforge.utils.PermissionManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: String,
    onNavigateBack: () -> Unit,
    onNavigateToChapters: () -> Unit,
    onNavigateToCharacters: () -> Unit,
    onNavigateToTimeline: () -> Unit,
    onNavigateToScenes: () -> Unit,
    viewModel: BookDetailViewModel = hiltViewModel()
) {    val currentBook by viewModel.currentBook.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val exportState by viewModel.exportState.collectAsState()
    val context = LocalContext.current
    
    // Export dialog state
    var showExportDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(bookId) {
        viewModel.loadBook(bookId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentBook?.title ?: "Book Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Export button in toolbar
                    if (currentBook != null) {
                        IconButton(
                            onClick = { showExportDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = "Export Book"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                currentBook != null -> {
                    Text(
                        text = currentBook!!.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (currentBook!!.description.isNotBlank()) {
                        Text(
                            text = currentBook!!.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    Text(
                        text = "Book not found",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToChapters
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Chapters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Organize your story into chapters",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToCharacters
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Characters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Develop your characters and their relationships",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
              Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToTimeline
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Timeline",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Plan your story events and timeline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToScenes
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Scenes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Manage individual scenes and their details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
          // Export dialog
        if (showExportDialog && currentBook != null) {
            BookExportDialog(
                bookTitle = currentBook!!.title,
                chapterCount = viewModel.getChapterCount(),
                onDismiss = { showExportDialog = false },
                onExport = { format ->
                    viewModel.exportBook(format)
                    showExportDialog = false
                },
                isExporting = exportState.isExporting,
                permissionManager = PermissionManager(context)
            )
        }
        
        // Export success dialog
        exportState.result?.let { result ->
            when (result) {
                is ExportResult.Success -> {                    ExportSuccessDialog(
                        filePath = result.filePath,
                        exportedItemCount = result.bookCount,
                        itemType = "chapters",
                        onDismiss = { viewModel.clearExportResult() }
                    )
                }
                is ExportResult.Error -> {
                    // Error is already handled in the exportState.error
                    viewModel.clearExportResult()
                }
            }
        }
        
        // Export result handling
        LaunchedEffect(exportState.result) {
            exportState.result?.let { result ->
                // Don't clear here anymore - let the dialog handle it
            }
        }
    }
}

