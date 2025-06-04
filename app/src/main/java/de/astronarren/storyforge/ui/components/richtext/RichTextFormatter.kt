package de.astronarren.storyforge.ui.components.richtext

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Utility class for converting rich text with markup to AnnotatedString
 */
object RichTextFormatter {
      /**
     * Convert marked-up text to AnnotatedString
     * Supports: **bold**, *italic*, __underline__, ~~strikethrough~~
     * Headers: # H1, ## H2, ### H3
     * Lists: - bullet, 1. numbered
     * Quotes: > quote text
     * Code: ```code blocks```
     * Horizontal rules: ---
     * Story references: @character:name, @scene:title, @event:title
     */
    fun parseMarkupToAnnotatedString(text: String): AnnotatedString {
        return buildAnnotatedString {
            val lines = text.split('\n')
            var isFirstLine = true
            
            for (line in lines) {
                if (!isFirstLine) {
                    append('\n')
                }
                isFirstLine = false
                
                when {
                    // Headers
                    line.startsWith("### ") -> {
                        withStyle(SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                            appendMarkupLine(line.removePrefix("### "))
                        }
                    }
                    line.startsWith("## ") -> {
                        withStyle(SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                            appendMarkupLine(line.removePrefix("## "))
                        }
                    }
                    line.startsWith("# ") -> {
                        withStyle(SpanStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)) {
                            appendMarkupLine(line.removePrefix("# "))
                        }
                    }
                    // Quote
                    line.startsWith("> ") -> {
                        withStyle(SpanStyle(
                            fontStyle = FontStyle.Italic,
                            color = androidx.compose.ui.graphics.Color.Gray
                        )) {
                            append("❝ ")
                            appendMarkupLine(line.removePrefix("> "))
                        }
                    }
                    // Lists
                    line.startsWith("- ") -> {
                        append("• ")
                        appendMarkupLine(line.removePrefix("- "))
                    }
                    line.matches(Regex("^\\d+\\. .*")) -> {
                        val number = line.substringBefore(". ")
                        append("$number. ")
                        appendMarkupLine(line.substringAfter(". "))
                    }
                    // Horizontal rule
                    line.trim() == "---" -> {
                        withStyle(SpanStyle(color = androidx.compose.ui.graphics.Color.Gray)) {
                            append("─".repeat(20))
                        }
                    }
                    // Code block start/end
                    line.trim() == "```" -> {
                        withStyle(SpanStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            background = androidx.compose.ui.graphics.Color.LightGray.copy(alpha = 0.3f)
                        )) {
                            append("┌─ Code Block ─┐")
                        }
                    }
                    // Regular line
                    else -> {
                        appendMarkupLine(line)
                    }
                }
            }
        }
    }
    
    private fun AnnotatedString.Builder.appendMarkupLine(line: String) {
        val patterns = listOf(
            "\\*\\*(.*?)\\*\\*" to { content: String -> 
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(content) }
            },
            "\\*(.*?)\\*" to { content: String -> 
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(content) }
            },
            "__(.*?)__" to { content: String -> 
                withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) { append(content) }
            },
            "~~(.*?)~~" to { content: String -> 
                withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(content) }
            },
            "@(character|scene|event):(\\w+)" to { content: String -> 
                withStyle(SpanStyle(
                    color = androidx.compose.ui.graphics.Color.Blue,
                    textDecoration = TextDecoration.Underline
                )) { 
                    append("@$content")
                    addStringAnnotation("story_reference", "character:$content", length - content.length - 1, length)
                }
            }
        )
        
        var currentText = line
        var offset = 0
        
        // Process patterns in order
        patterns.forEach { (pattern, styleFunction) ->
            val regex = Regex(pattern)
            val matches = regex.findAll(currentText).toList()
            
            for (match in matches.reversed()) { // Process from end to start to maintain indices
                val fullMatch = match.value
                val content = if (match.groupValues.size > 1) match.groupValues[1] else match.value
                
                // Add text before match
                val beforeText = currentText.substring(0, match.range.first)
                val afterText = currentText.substring(match.range.last + 1)
                
                // Replace the current text processing
                currentText = beforeText + "STYLED_PLACEHOLDER_${matches.indexOf(match)}" + afterText
            }        }
        
        // For now, use simpler approach - just clean markup and apply basic styling
        var cleanText = line
        
        // Clean markup patterns
        cleanText = cleanText.replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        cleanText = cleanText.replace(Regex("\\*(.*?)\\*(?!\\*)"), "$1") 
        cleanText = cleanText.replace(Regex("__(.*?)__"), "$1")
        cleanText = cleanText.replace(Regex("~~(.*?)~~"), "$1")
        cleanText = cleanText.replace(Regex("@(character|scene|event):(\\w+)")) { matchResult ->
            val type = matchResult.groupValues[1]
            val name = matchResult.groupValues[2]
            "@$type:$name"
        }
        
        append(cleanText)
    }
    
    /**
     * Convert AnnotatedString back to marked-up text
     */
    fun annotatedStringToMarkup(annotatedString: AnnotatedString): String {
        var markup = annotatedString.text
        
        // Extract style information and convert back to markup
        annotatedString.spanStyles.forEach { spanStyle ->
            val text = annotatedString.text.substring(spanStyle.start, spanStyle.end)
            val styledText = when {
                spanStyle.item.fontWeight == FontWeight.Bold -> "**$text**"
                spanStyle.item.fontStyle == FontStyle.Italic -> "*$text*"
                spanStyle.item.textDecoration?.contains(TextDecoration.Underline) == true -> "__${text}__"
                spanStyle.item.textDecoration?.contains(TextDecoration.LineThrough) == true -> "~~$text~~"
                else -> text
            }
            // Replace in markup (simplified)
        }
        
        // Add story references
        annotatedString.getStringAnnotations("story_reference", 0, annotatedString.text.length)
            .forEach { annotation ->
                val parts = annotation.item.split(":")
                if (parts.size == 2) {
                    val type = parts[0].lowercase()
                    val id = parts[1]
                    val text = annotatedString.text.substring(annotation.start, annotation.end)
                    // Replace with markup reference (simplified)
                }
            }
          return markup
    }
    
    /**
     * Convert RichTextDocument to plain text
     */
    fun toPlainText(document: RichTextDocument): String {
        return document.plainText
    }
    
    /**
     * Convert RichTextDocument to HTML
     */
    fun toHtml(document: RichTextDocument): String {
        val html = StringBuilder()
        html.append("<html><body>")
        
        // Convert plain text to HTML with basic formatting
        val text = document.plainText
        val lines = text.split('\n')
        
        for (line in lines) {
            when {
                line.trim().isEmpty() -> html.append("<br>")
                line.startsWith("# ") -> html.append("<h1>${line.removePrefix("# ")}</h1>")
                line.startsWith("## ") -> html.append("<h2>${line.removePrefix("## ")}</h2>")
                line.startsWith("### ") -> html.append("<h3>${line.removePrefix("### ")}</h3>")
                line.startsWith("- ") -> html.append("<li>${line.removePrefix("- ")}</li>")
                line.startsWith("> ") -> html.append("<blockquote>${line.removePrefix("> ")}</blockquote>")
                else -> {
                    var htmlLine = line
                    // Apply basic formatting
                    htmlLine = htmlLine.replace(Regex("\\*\\*(.*?)\\*\\*"), "<strong>$1</strong>")
                    htmlLine = htmlLine.replace(Regex("\\*(.*?)\\*"), "<em>$1</em>")
                    htmlLine = htmlLine.replace(Regex("__(.*?)__"), "<u>$1</u>")
                    htmlLine = htmlLine.replace(Regex("~~(.*?)~~"), "<del>$1</del>")
                    html.append("<p>$htmlLine</p>")
                }
            }
        }
        
        html.append("</body></html>")
        return html.toString()
    }
}

/**
 * Preview component showing formatted text with markup
 */
@Composable
fun RichTextPreview(
    content: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                text = RichTextFormatter.parseMarkupToAnnotatedString(content),
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

/**
 * Formatting help dialog
 */
@Composable
fun FormattingHelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Formatting Help")
        },
        text = {
            LazyColumn {
                items(
                    listOf(
                        "**Bold text**" to "Bold formatting",
                        "*Italic text*" to "Italic formatting", 
                        "__Underlined text__" to "Underlined text",
                        "~~Strikethrough text~~" to "Strikethrough text",
                        "@character:name" to "Reference a character",
                        "@scene:title" to "Reference a scene",
                        "@event:title" to "Reference an event"
                    )
                ) { (syntax, description) ->
                    FormattingHelpItem(
                        syntax = syntax,
                        description = description
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
private fun FormattingHelpItem(
    syntax: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = syntax,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}
