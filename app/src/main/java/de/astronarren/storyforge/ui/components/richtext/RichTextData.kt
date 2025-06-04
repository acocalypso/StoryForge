package de.astronarren.storyforge.ui.components.richtext

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.serialization.Serializable

/**
 * Represents formatting options for rich text
 */
@Serializable
data class TextFormat(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isStrikethrough: Boolean = false,
    val textSize: Float = 16f,
    val textColor: String? = null
)

/**
 * Represents a text span with formatting and content
 */
@Serializable
data class FormattedSpan(
    val text: String,
    val format: TextFormat = TextFormat(),
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Represents a reference to a story element within the text
 */
@Serializable
data class StoryElementReference(
    val id: String,
    val type: ReferenceType,
    val displayName: String,
    val startIndex: Int,
    val endIndex: Int
)

@Serializable
enum class ReferenceType {
    CHARACTER,
    SCENE,
    TIMELINE_EVENT,
    LOCATION,
    NOTE
}

/**
 * Rich text document containing formatted spans and story references
 */
@Serializable
data class RichTextDocument(
    val plainText: String = "",
    val formattedSpans: List<FormattedSpan> = emptyList(),
    val storyReferences: List<StoryElementReference> = emptyList(),
    val wordCount: Int = 0
) {
    companion object {
        fun fromPlainText(text: String): RichTextDocument {
            return RichTextDocument(
                plainText = text,
                wordCount = text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
            )
        }
    }
}

/**
 * Extension functions to convert between RichTextDocument and AnnotatedString
 */
fun RichTextDocument.toAnnotatedString(): AnnotatedString {
    val builder = AnnotatedString.Builder(plainText)
    
    // Apply formatting spans
    formattedSpans.forEach { span ->
        val spanStyle = SpanStyle(
            fontWeight = if (span.format.isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (span.format.isItalic) FontStyle.Italic else FontStyle.Normal,
            textDecoration = buildList {
                if (span.format.isUnderline) add(TextDecoration.Underline)
                if (span.format.isStrikethrough) add(TextDecoration.LineThrough)
            }.let { decorations ->
                if (decorations.isEmpty()) TextDecoration.None
                else TextDecoration.combine(decorations)
            }
        )
        
        builder.addStyle(
            style = spanStyle,
            start = span.startIndex.coerceAtMost(plainText.length),
            end = span.endIndex.coerceAtMost(plainText.length)
        )
    }
    
    // Add story reference annotations
    storyReferences.forEach { reference ->
        builder.addStringAnnotation(
            tag = "story_reference",
            annotation = "${reference.type}:${reference.id}",
            start = reference.startIndex.coerceAtMost(plainText.length),
            end = reference.endIndex.coerceAtMost(plainText.length)
        )
    }
    
    return builder.toAnnotatedString()
}

fun AnnotatedString.toRichTextDocument(): RichTextDocument {
    val formattedSpans = mutableListOf<FormattedSpan>()
    val storyReferences = mutableListOf<StoryElementReference>()
    
    // Extract style spans
    spanStyles.forEach { spanStyle ->
        val format = TextFormat(
            isBold = spanStyle.item.fontWeight == FontWeight.Bold,
            isItalic = spanStyle.item.fontStyle == FontStyle.Italic,
            isUnderline = spanStyle.item.textDecoration?.contains(TextDecoration.Underline) == true,
            isStrikethrough = spanStyle.item.textDecoration?.contains(TextDecoration.LineThrough) == true
        )
        
        formattedSpans.add(
            FormattedSpan(
                text = text.substring(spanStyle.start, spanStyle.end),
                format = format,
                startIndex = spanStyle.start,
                endIndex = spanStyle.end
            )
        )
    }
    
    // Extract story references
    getStringAnnotations("story_reference", 0, text.length).forEach { annotation ->
        val parts = annotation.item.split(":")
        if (parts.size == 2) {
            val type = try {
                ReferenceType.valueOf(parts[0])
            } catch (e: IllegalArgumentException) {
                ReferenceType.NOTE
            }
            
            storyReferences.add(
                StoryElementReference(
                    id = parts[1],
                    type = type,
                    displayName = text.substring(annotation.start, annotation.end),
                    startIndex = annotation.start,
                    endIndex = annotation.end
                )
            )
        }
    }
    
    return RichTextDocument(
        plainText = text,
        formattedSpans = formattedSpans,
        storyReferences = storyReferences,
        wordCount = text.split("\\s+".toRegex()).filter { it.isNotBlank() }.size
    )
}
