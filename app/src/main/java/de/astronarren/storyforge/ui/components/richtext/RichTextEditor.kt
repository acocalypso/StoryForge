package de.astronarren.storyforge.ui.components.richtext

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.astronarren.storyforge.ui.components.richtext.RichTextFormatter
import de.astronarren.storyforge.R

/**
 * A rich text editor with formatting toolbar and story reference support
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RichTextEditor(
    value: RichTextDocument,
    onValueChange: (RichTextDocument) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Start writing your chapter...",
    onStoryReferenceClick: ((StoryElementReference) -> Unit)? = null,
    onAddReference: (() -> Unit)? = null,
    onInsertReference: ((StoryElementReference) -> Unit)? = null
) {    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value.plainText,
                selection = TextRange.Zero
            )
        )
    }
    
    // Update text field value when the external value changes, but preserve cursor position
    LaunchedEffect(value.plainText) {
        if (textFieldValue.text != value.plainText) {
            val currentSelection = textFieldValue.selection
            textFieldValue = textFieldValue.copy(
                text = value.plainText,
                selection = if (currentSelection.start <= value.plainText.length) {
                    currentSelection
                } else {
                    TextRange(value.plainText.length)
                }
            )
        }
    }
    
    var showHelp by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    
    // Handle story reference insertion
    LaunchedEffect(onInsertReference) {
        if (onInsertReference != null) {
            // This will be triggered from parent when a reference is selected
        }
    }
      fun insertStoryReference(reference: StoryElementReference) {
        val referenceText = "@${reference.type.toString().lowercase()}:${reference.displayName}"
        val selection = textFieldValue.selection
        val newText = StringBuilder(textFieldValue.text).apply {
            insert(selection.start, referenceText)
        }.toString()
        
        textFieldValue = textFieldValue.copy(
            text = newText,
            selection = TextRange(selection.start + referenceText.length)
        )
        
        onValueChange(RichTextDocument.fromPlainText(newText))
    }
      Column(modifier = modifier) {
        // Formatting toolbar
        RichTextToolbar(
            onInsertMarkup = { markup ->
                val selection = textFieldValue.selection
                val newText = StringBuilder(textFieldValue.text).apply {
                    if (selection.collapsed) {
                        // Insert at cursor
                        when (markup) {
                            "```\n" -> {
                                // Code block - insert template
                                val codeBlock = "```\nYour code here\n```"
                                insert(selection.start, codeBlock)
                            }
                            "\n---\n" -> {
                                // Horizontal rule - ensure proper spacing
                                val rule = if (selection.start == 0) "---\n" else "\n---\n"
                                insert(selection.start, rule)
                            }
                            "# ", "## ", "### " -> {
                                // Headings - ensure they're at start of line
                                val lineStart = textFieldValue.text.lastIndexOf('\n', selection.start - 1) + 1
                                if (selection.start == lineStart) {
                                    insert(selection.start, markup)
                                } else {
                                    insert(selection.start, "\n$markup")
                                }
                            }
                            "- ", "1. ", "> " -> {
                                // List items and quotes - ensure they're at start of line
                                val lineStart = textFieldValue.text.lastIndexOf('\n', selection.start - 1) + 1
                                if (selection.start == lineStart) {
                                    insert(selection.start, markup)
                                } else {
                                    insert(selection.start, "\n$markup")
                                }
                            }
                            else -> {
                                insert(selection.start, markup)
                            }
                        }
                    } else {
                        // Wrap selection
                        val selectedText = textFieldValue.text.substring(selection.start, selection.end)
                        val wrappedText = when (markup) {
                            "**" -> "**$selectedText**"
                            "*" -> "*$selectedText*"
                            "__" -> "__${selectedText}__"
                            "~~" -> "~~$selectedText~~"
                            "```\n" -> "```\n$selectedText\n```"
                            "> " -> "> $selectedText"
                            "# " -> "# $selectedText"
                            "## " -> "## $selectedText"
                            "### " -> "### $selectedText"
                            "- " -> "- $selectedText"
                            "1. " -> "1. $selectedText"
                            else -> markup
                        }
                        replace(selection.start, selection.end, wrappedText)
                    }
                }.toString()
                
                val newCursorPos = when (markup) {
                    "```\n" -> if (selection.collapsed) selection.start + 4 else selection.start + markup.length + textFieldValue.text.substring(selection.start, selection.end).length + 5
                    "\n---\n" -> selection.start + markup.length
                    else -> selection.start + markup.length
                }
                
                textFieldValue = textFieldValue.copy(
                    text = newText,
                    selection = TextRange(newCursorPos)
                )
                
                onValueChange(RichTextDocument.fromPlainText(newText))
            },
            onAddReference = onAddReference,
            onShowHelp = { showHelp = true },
            onTogglePreview = { showPreview = !showPreview },
            showPreview = showPreview,
            modifier = Modifier.fillMaxWidth()
        )
        
        Divider()
        
        if (showPreview) {
            // Preview mode
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = RichTextFormatter.parseMarkupToAnnotatedString(textFieldValue.text),
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                    )
                }
            }
        } else {
            // Edit mode
            OutlinedTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    textFieldValue = newValue
                    val newDocument = RichTextDocument.fromPlainText(newValue.text)
                    onValueChange(newDocument)
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                placeholder = {
                    Text(
                        text = placeholder,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 24.sp
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
        }
        
        // Word count and status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${value.wordCount} words",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (value.storyReferences.isNotEmpty()) {
                    Text(
                        text = "${value.storyReferences.size} references",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Text(
                    text = if (showPreview) "Preview" else "Edit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
      // Help dialog
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = {
                Text("Formatting Help")
            },            text = {
                Column {
                    Text("**Bold text** - Bold formatting", style = MaterialTheme.typography.bodySmall)
                    Text("*Italic text* - Italic formatting", style = MaterialTheme.typography.bodySmall)
                    Text("__Underlined text__ - Underlined text", style = MaterialTheme.typography.bodySmall)
                    Text("~~Strikethrough text~~ - Strikethrough text", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Headers:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("# Heading 1", style = MaterialTheme.typography.bodySmall)
                    Text("## Heading 2", style = MaterialTheme.typography.bodySmall)
                    Text("### Heading 3", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Lists & Structure:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("- Bullet list item", style = MaterialTheme.typography.bodySmall)
                    Text("1. Numbered list item", style = MaterialTheme.typography.bodySmall)
                    Text("> Quote text", style = MaterialTheme.typography.bodySmall)
                    Text("```code block```", style = MaterialTheme.typography.bodySmall)
                    Text("--- Horizontal rule", style = MaterialTheme.typography.bodySmall)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Story References:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text("@character:name - Reference a character", style = MaterialTheme.typography.bodySmall)
                    Text("@scene:title - Reference a scene", style = MaterialTheme.typography.bodySmall)
                    Text("@event:title - Reference an event", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelp = false }) {
                    Text("Got it")
                }
            }
        )
    }
}

@Composable
private fun RichTextToolbar(
    onInsertMarkup: (String) -> Unit,
    onAddReference: (() -> Unit)?,
    onShowHelp: () -> Unit,
    onTogglePreview: () -> Unit,
    showPreview: Boolean,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {        // Text formatting buttons
        items(
            listOf(
                FormatButton("Bold", icon = Icons.Default.FormatBold, markup = "**"),
                FormatButton("Italic", icon = Icons.Default.FormatItalic, markup = "*"),
                FormatButton("Underline", icon = Icons.Default.FormatUnderlined, markup = "__"),
                FormatButton("Strikethrough", icon = Icons.Default.FormatStrikethrough, markup = "~~")
            )
        ) { button ->
            IconButton(
                onClick = { onInsertMarkup(button.markup) }
            ) {
                if (button.icon != null) {
                    Icon(
                        imageVector = button.icon,
                        contentDescription = button.description,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                } else if (button.drawableRes != null) {
                    Icon(
                        painter = painterResource(button.drawableRes),
                        contentDescription = button.description,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Divider
        item {
            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }        // Heading buttons with custom drawable resources
        items(
            listOf(
                FormatButton("Heading 1", drawableRes = R.drawable.format_h1_24dp_e3e3e3_fill0_wght400_grad0_opsz24, markup = "# "),
                FormatButton("Heading 2", drawableRes = R.drawable.format_h2_24dp_e3e3e3_fill0_wght400_grad0_opsz24, markup = "## "),
                FormatButton("Heading 3", drawableRes = R.drawable.format_h3_24dp_e3e3e3_fill0_wght400_grad0_opsz24, markup = "### ")
            )
        ) { button ->
            IconButton(
                onClick = { onInsertMarkup(button.markup) }
            ) {
                if (button.icon != null) {
                    Icon(
                        imageVector = button.icon,
                        contentDescription = button.description,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                } else if (button.drawableRes != null) {
                    Icon(
                        painter = painterResource(button.drawableRes),
                        contentDescription = button.description,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Divider
        item {
            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }        // List and structure buttons
        items(
            listOf(
                FormatButton("Bullet List", icon = Icons.AutoMirrored.Filled.List, markup = "- "),
                FormatButton("Numbered List", icon = Icons.Default.FormatListNumbered, markup = "1. "),
                FormatButton("Quote", icon = Icons.Default.FormatQuote, markup = "> "),
                FormatButton("Code Block", icon = Icons.Default.Code, markup = "```\n"),
                FormatButton("Horizontal Rule", icon = Icons.Default.Remove, markup = "\n---\n")
            )
        ) { button ->
            IconButton(
                onClick = { onInsertMarkup(button.markup) }
            ) {
                if (button.icon != null) {
                    Icon(
                        imageVector = button.icon,
                        contentDescription = button.description,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                } else if (button.drawableRes != null) {
                    Icon(
                        painter = painterResource(button.drawableRes),
                        contentDescription = button.description,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Divider
        item {
            VerticalDivider(
                modifier = Modifier.height(24.dp),
                color = MaterialTheme.colorScheme.outline
            )
        }
        
        // Reference and utility buttons
        if (onAddReference != null) {
            item {
                IconButton(onClick = onAddReference) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Add story reference",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
          item {
            IconButton(onClick = onShowHelp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Help,
                    contentDescription = "Formatting help",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        item {
            IconButton(onClick = onTogglePreview) {
                Icon(
                    imageVector = if (showPreview) Icons.Default.Edit else Icons.Default.Preview,
                    contentDescription = if (showPreview) "Edit mode" else "Preview mode",
                    tint = if (showPreview) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private data class FormatButton(
    val description: String,
    val icon: ImageVector? = null,
    val drawableRes: Int? = null,
    val markup: String
) {
    init {
        require((icon != null) xor (drawableRes != null)) {
            "FormatButton must have either icon or drawableRes, but not both"
        }
    }
}
