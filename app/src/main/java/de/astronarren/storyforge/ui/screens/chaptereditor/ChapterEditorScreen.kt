package de.astronarren.storyforge.ui.screens.chaptereditor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.astronarren.storyforge.data.database.entities.ChapterBackup
import de.astronarren.storyforge.data.models.AutoSaveState
import de.astronarren.storyforge.ui.components.autosave.AutoSaveStatusIndicator
import de.astronarren.storyforge.ui.components.backup.BackupManagerDialog

import de.astronarren.storyforge.ui.components.richtext.RichTextEditor

/**
 * Screen for editing chapter content with rich text formatting and auto-save functionality
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterEditorScreen(
    chapterId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChapterEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val autoSaveState by viewModel.autoSaveState.collectAsStateWithLifecycle()
    val backups by viewModel.backups.collectAsStateWithLifecycle()
    var showBackupDialog by remember { mutableStateOf(false) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var editingTitle by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    LaunchedEffect(chapterId) {
        viewModel.loadChapter(chapterId)
    }
    
    // Reset editing state when chapter changes
    LaunchedEffect(uiState.chapter?.title) {
        editingTitle = uiState.chapter?.title ?: ""
        isEditingTitle = false
    }
    
    // Function to handle title editing
    fun startEditingTitle() {
        editingTitle = uiState.chapter?.title ?: ""
        isEditingTitle = true
    }
    
    fun saveTitleEdit() {
        if (editingTitle.isNotBlank() && editingTitle != uiState.chapter?.title) {
            viewModel.updateChapterTitle(editingTitle)
        }
        isEditingTitle = false
        focusManager.clearFocus()
    }
    
    fun cancelTitleEdit() {
        editingTitle = uiState.chapter?.title ?: ""
        isEditingTitle = false
        focusManager.clearFocus()
    }

    Scaffold(        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isEditingTitle) {
                            OutlinedTextField(
                                value = editingTitle,
                                onValueChange = { editingTitle = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                textStyle = MaterialTheme.typography.titleLarge,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(
                                    onDone = { saveTitleEdit() }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                            
                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = uiState.chapter?.title ?: "Chapter",
                                    style = MaterialTheme.typography.titleLarge
                                )
                                IconButton(
                                    onClick = { startEditingTitle() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit chapter title",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        AutoSaveStatusIndicator(
                            autoSaveState = autoSaveState,
                            statusText = viewModel.getAutoSaveStatusText(autoSaveState)
                        )
                    }
                },                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditingTitle) {
                            cancelTitleEdit()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEditingTitle) "Cancel editing" else "Back"
                        )
                    }                },                actions = {
                    // Backup button
                    IconButton(
                        onClick = { showBackupDialog = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Backup Manager"
                        )
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
                    )
                }
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadChapter(chapterId) }) {
                            Text("Retry")
                        }
                    }
                }
                uiState.chapter != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        RichTextEditor(
                            value = uiState.richTextDocument,
                            onValueChange = { viewModel.updateContent(it) },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = "Start writing your chapter..."
                        )
                    }
                }
            }
        }
    }    // Backup Manager Dialog
    if (showBackupDialog) {
        BackupManagerDialog(
            backups = backups,
            onDismiss = { showBackupDialog = false },
            onRestoreBackup = { backup: ChapterBackup ->
                viewModel.restoreFromBackup(backup)
                showBackupDialog = false
            },
            onDeleteBackup = { backup: ChapterBackup ->
                viewModel.deleteBackup(backup)
            },
            onCreateManualBackup = {
                viewModel.createManualBackup()
            }        )
    }
}