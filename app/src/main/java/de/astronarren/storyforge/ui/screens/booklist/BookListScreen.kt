package de.astronarren.storyforge.ui.screens.booklist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import de.astronarren.storyforge.data.model.BookGenre
import de.astronarren.storyforge.ui.components.*
import de.astronarren.storyforge.ui.components.NavigationDrawerContent
import de.astronarren.storyforge.ui.components.DrawerSections
import de.astronarren.storyforge.ui.components.export.ExportSuccessDialog
import de.astronarren.storyforge.ui.components.importexport.ComprehensiveExportDialog
import de.astronarren.storyforge.ui.components.importexport.ComprehensiveImportDialog
import de.astronarren.storyforge.ui.components.importexport.ImportResultDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    onBookClick: (String) -> Unit,
    onCreateBook: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: BookListViewModel = hiltViewModel()
) {    val uiState by viewModel.uiState.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var showAdvancedSearch by remember { mutableStateOf(false) }
    var showAnalytics by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showComprehensiveExportDialog by remember { mutableStateOf(false) }
    var showComprehensiveImportDialog by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
      // Navigation drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
      // Create drawer sections (without search)
    val drawerSections = remember(uiState.showFavoritesOnly, uiState.searchQuery, uiState.searchCriteria) {
        listOf(
            DrawerSections.createFilterSection(
                showOnlyMainCharacters = false, // Not applicable for books
                onToggleMainCharacters = { }, // Not applicable 
                showFavoritesOnly = uiState.showFavoritesOnly,
                onToggleFavorites = { viewModel.toggleFavoritesFilter() },
                searchQuery = uiState.searchQuery,
                onClearFilters = { 
                    viewModel.clearFilters()
                    viewModel.clearSearch()
                }
            ),            DrawerSections.createActionsSection(
                onAnalytics = { showAnalytics = true },
                onImport = { showImportDialog = true },
                onExport = { showExportDialog = true },
                onComprehensiveImport = { showComprehensiveImportDialog = true },
                onComprehensiveExport = { showComprehensiveExportDialog = true },
                onSettings = onNavigateToSettings,
                onCreateNew = { showCreateDialog = true }
            )
        )
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerContent(sections = drawerSections)
            }
        }
    ) {
      Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = viewModel::updateSearchQuery,
                            placeholder = { Text("Search books...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = viewModel::clearSearch) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearchActive = false
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )            } else {
                TopAppBar(
                    title = { 
                        Text(
                            "StoryForge",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    actions = {
                        // Show active filter count if any filters are applied
                        val activeFilters = uiState.searchCriteria.getActiveFilterCount()
                        if (activeFilters > 0) {
                            Badge(
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text("$activeFilters")
                            }
                        }                        // Search toggle
                        IconButton(
                            onClick = { isSearchActive = true }
                        ) {
                            Icon(
                                Icons.Default.Search, 
                                contentDescription = "Quick search",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }                        // Advanced search toggle  
                        IconButton(
                            onClick = { showAdvancedSearch = true }
                        ) {
                            Icon(
                                Icons.Default.Tune, 
                                contentDescription = "Advanced search & filters",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        // Navigation drawer toggle
                        IconButton(
                            onClick = { 
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open navigation drawer")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Book")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {            when {
                uiState.isLoading -> {
                    BookListSkeleton(
                        itemCount = 3,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.books.isEmpty() && uiState.searchQuery.isNotEmpty() -> {
                    SearchEmptyState(
                        searchQuery = uiState.searchQuery,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.books.isEmpty() -> {
                    BookListEmptyState(
                        onCreateBookClick = { showCreateDialog = true },
                        modifier = Modifier.fillMaxSize()
                    )
                }                else -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Genre Filter (only show when not searching)
                        if (uiState.searchQuery.isEmpty()) {
                            GenreFilter(
                                selectedGenre = uiState.selectedGenre,
                                onGenreSelected = viewModel::filterByGenre,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f), // Use weight to prevent constraint conflicts
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {                            items(uiState.books) { book ->
                                BookCard(
                                    book = book,
                                    onClick = { onBookClick(book.id) },
                                    onArchive = { viewModel.archiveBook(book.id) },
                                    onDelete = { viewModel.deleteBook(book) },
                                    onDuplicate = { viewModel.duplicateBook(book) },
                                    onToggleFavorite = { viewModel.toggleFavorite(book) }
                                )
                            }
                        }
                    }
                }            }
            
            // Error Snackbar
            if (uiState.error != null) {
                Snackbar(
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(uiState.error ?: "")
                }
            }
              // Success Snackbar
            if (uiState.showCreateSuccess) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Success",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))                        
                        Text("Book created successfully!")
                    }
                }
            }
            
            // Import Success Snackbar
            if (uiState.showImportSuccess) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Success",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))                        
                        Text("Books imported successfully!")
                    }
                }
            }
        }
        
        if (showCreateDialog) {
            CreateBookDialog(
                onDismiss = { showCreateDialog = false },
                onConfirm = { title, description, author, genreString, targetWordCount, coverImagePath ->
                    val genre = BookGenre.fromString(genreString)
                    viewModel.createBook(title, description, author, genre, targetWordCount, coverImagePath)
                    showCreateDialog = false
                },
                isLoading = uiState.isCreatingBook
            )
        }
          if (showAdvancedSearch) {
            AdvancedSearchDialog(
                currentCriteria = uiState.searchCriteria,
                availableAuthors = viewModel.getAvailableAuthors(),
                onCriteriaChanged = { criteria ->
                    viewModel.updateSearchCriteria(criteria)
                },
                onDismiss = { showAdvancedSearch = false }
            )
        }
          if (showAnalytics) {
            AnalyticsDashboardDialog(
                analytics = viewModel.getAnalytics(),
                onDismiss = { showAnalytics = false }
            )
        }
        
        if (showExportDialog) {
            ExportDialog(
                availableGenres = viewModel.getAvailableGenres(),
                onExport = { options ->
                    viewModel.exportBooks(options)
                    showExportDialog = false
                },
                onDismiss = { showExportDialog = false },
                isLoading = uiState.isExporting
            )
        }
          if (showImportDialog) {
            ImportDialog(
                onImport = { uri ->
                    viewModel.importBooks(uri)
                },
                onDismiss = { 
                    showImportDialog = false
                    viewModel.clearImportPreview()
                },
                isLoading = uiState.isImporting,
                importPreview = uiState.importPreview,
                onConfirmImport = {
                    viewModel.confirmImport()
                    showImportDialog = false
                }            )
        }        // Export success dialog
        uiState.exportResult?.let { result ->
            ExportSuccessDialog(
                filePath = result.filePath,
                exportedItemCount = result.bookCount,
                itemType = "books",
                onDismiss = { viewModel.clearExportResult() }
            )
        }
          // Comprehensive Export Dialog
        if (showComprehensiveExportDialog) {
            ComprehensiveExportDialog(
                onExport = { filename ->
                    viewModel.exportAllData(filename)
                    showComprehensiveExportDialog = false
                },
                onDismiss = { showComprehensiveExportDialog = false }
            )
        }        // Comprehensive Import Dialog
        if (showComprehensiveImportDialog) {
            ComprehensiveImportDialog(
                onImport = { uri, mode ->
                    viewModel.importAllData(uri, mode)
                    showComprehensiveImportDialog = false
                },
                onDismiss = { showComprehensiveImportDialog = false }
            )
        }
          // Comprehensive Import Result Dialog
        uiState.comprehensiveImportResult?.let { result: de.astronarren.storyforge.data.service.StoryForgeImportExportService.ImportResult ->
            ImportResultDialog(
                importResult = result,
                onDismiss = { viewModel.clearComprehensiveImportResult() }
            )
        }
        
        // Comprehensive Export Success Dialog
        uiState.comprehensiveExportResult?.let { filePath ->
            ExportSuccessDialog(
                filePath = filePath,
                exportedItemCount = 1, // Represents the .storyforge file
                itemType = "comprehensive data",
                onDismiss = { viewModel.clearComprehensiveExportResult() }
            )
        }
    }
    } // Close ModalNavigationDrawer
}

