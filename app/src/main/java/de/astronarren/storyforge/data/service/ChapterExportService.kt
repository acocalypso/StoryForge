package de.astronarren.storyforge.data.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import de.astronarren.storyforge.data.database.entities.Book
import de.astronarren.storyforge.data.database.entities.Chapter
import de.astronarren.storyforge.data.model.ExportFormat
import de.astronarren.storyforge.data.model.ExportResult
import de.astronarren.storyforge.ui.components.richtext.RichTextDocument
import de.astronarren.storyforge.ui.components.richtext.RichTextFormatter
import de.astronarren.storyforge.utils.FileExportHelper
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.apache.poi.xwpf.usermodel.XWPFTableRow
import org.apache.poi.util.Units
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChapterExportService @Inject constructor(
    private val fileExportHelper: FileExportHelper
) {
      /**
     * Exports a chapter to the specified format
     */
    suspend fun exportChapter(
        chapter: Chapter,
        format: ExportFormat,
        fileName: String
    ): ExportResult {
        return when (format) {
            ExportFormat.TXT -> exportChapterToText(chapter, fileName)
            ExportFormat.DOCX -> exportChapterToDocx(chapter, fileName)
            ExportFormat.PDF -> exportChapterToPdf(chapter, fileName)
        }
    }
      /**
     * Exports an entire book to the specified format
     */
    suspend fun exportBook(
        book: Book,
        chapters: List<Chapter>,
        format: ExportFormat,
        fileName: String
    ): ExportResult {
        return when (format) {
            ExportFormat.TXT -> exportBookToText(book, chapters, fileName)
            ExportFormat.DOCX -> exportBookToDocx(book, chapters, fileName)
            ExportFormat.PDF -> exportBookToPdf(book, chapters, fileName)
        }
    }

    // =========================
    // TEXT EXPORT METHODS
    // =========================
    
    /**
     * Exports a single chapter to a text file
     */
    suspend fun exportChapterToText(
        chapter: Chapter,
        fileName: String
    ): ExportResult {
        return try {
            val content = buildString {
                appendLine("=".repeat(50))
                appendLine("CHAPTER: ${chapter.title}")
                appendLine("=".repeat(50))
                appendLine()
                
                // Convert rich text content to plain text
                val richTextContent = try {
                    Json.decodeFromString<RichTextDocument>(chapter.content)
                } catch (e: Exception) {
                    RichTextDocument.fromPlainText(chapter.content)
                }
                
                val plainText = RichTextFormatter.toPlainText(richTextContent)
                appendLine(plainText)
                appendLine()
                
                appendLine("-".repeat(50))
                appendLine("End of Chapter")
                appendLine("-".repeat(50))
            }

            val file = fileExportHelper.saveTextFile(fileName, content)
            ExportResult.Success(file.absolutePath, 1)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export chapter: ${e.message}")
        }
    }
    
    /**
     * Exports an entire book to a text file
     */
    suspend fun exportBookToText(
        book: Book,
        chapters: List<Chapter>,
        fileName: String
    ): ExportResult {
        return try {
            val content = buildString {
                appendLine("=".repeat(60))
                appendLine("BOOK: ${book.title}")
                if (book.author.isNotBlank()) {
                    appendLine("AUTHOR: ${book.author}")
                }
                if (book.description.isNotBlank()) {
                    appendLine("DESCRIPTION: ${book.description}")
                }
                appendLine("=".repeat(60))
                appendLine()

                chapters.sortedBy { it.order }.forEach { chapter ->
                    appendLine("=".repeat(50))
                    appendLine("${chapter.title}")
                    appendLine("=".repeat(50))
                    appendLine()

                    // Convert rich text content to plain text
                    val richTextContent = try {
                        Json.decodeFromString<RichTextDocument>(chapter.content)
                    } catch (e: Exception) {
                        RichTextDocument.fromPlainText(chapter.content)
                    }

                    val plainText = RichTextFormatter.toPlainText(richTextContent)
                    appendLine(plainText)
                    appendLine()
                    appendLine()
                }
                
                appendLine("-".repeat(60))
                appendLine("End of Book")
                appendLine("-".repeat(60))
            }

            val file = fileExportHelper.saveTextFile(fileName, content, book.title)
            ExportResult.Success(file.absolutePath, chapters.size)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export book: ${e.message}")
        }
    }

    // =========================
    // DOCX EXPORT METHODS
    // =========================
      /**
     * Exports a single chapter to DOCX format using Apache POI
     */
    suspend fun exportChapterToDocx(
        chapter: Chapter,
        fileName: String
    ): ExportResult {
        return try {
            // Set system properties for POI XML parsing on Android
            setPoiSystemProperties()
            
            val file = fileExportHelper.saveBinaryFile(fileName, chapter.title) { file ->
                val document = XWPFDocument()
                
                try {
                    // Add page numbering to the document
                    addPageNumbering(document)
                    
                    // Create title with enhanced formatting
                    val titleParagraph = document.createParagraph()
                    titleParagraph.alignment = ParagraphAlignment.CENTER
                    val titleRun = titleParagraph.createRun()
                    titleRun.setText(chapter.title)
                    titleRun.isBold = true
                    titleRun.fontSize = 20
                    
                    // Add decorative line under title
                    val decorativeParagraph = document.createParagraph()
                    decorativeParagraph.alignment = ParagraphAlignment.CENTER
                    val decorativeRun = decorativeParagraph.createRun()
                    decorativeRun.setText("—————————————————")
                    decorativeRun.fontSize = 12
                    
                    // Add blank lines
                    document.createParagraph()
                    document.createParagraph()
                    
                    // Convert rich text content to DOCX
                    val richTextContent = try {
                        Json.decodeFromString<RichTextDocument>(chapter.content)
                    } catch (e: Exception) {
                        RichTextDocument.fromPlainText(chapter.content)
                    }
                    
                    // Parse and add rich text content
                    parseRichTextToDocx(document, richTextContent)
                    
                    // Write to file
                    FileOutputStream(file).use { outputStream ->
                        document.write(outputStream)
                    }
                } finally {
                    document.close()
                }
            }
            ExportResult.Success(file.absolutePath, 1)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export chapter to DOCX: ${e.message}")
        }
    }
      /**
     * Exports an entire book to DOCX format using Apache POI
     */
    suspend fun exportBookToDocx(
        book: Book,
        chapters: List<Chapter>,
        fileName: String
    ): ExportResult {
        return try {
            // Set system properties for POI XML parsing on Android
            setPoiSystemProperties()
            
            val file = fileExportHelper.saveBinaryFile(fileName, book.title) { file ->
                val document = XWPFDocument()
                
                try {
                    // Add page numbering to the document
                    addPageNumbering(document)
                    
                    // Create cover page if cover image is available
                    if (book.coverImagePath != null) {
                        val coverImage = loadCoverImage(book.coverImagePath)
                        if (coverImage != null) {
                            try {
                                val coverParagraph = document.createParagraph()
                                coverParagraph.alignment = ParagraphAlignment.CENTER
                                
                                // Convert bitmap to byte array
                                val outputStream = java.io.ByteArrayOutputStream()
                                coverImage.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                                val imageBytes = outputStream.toByteArray()
                                
                                // Add image to document
                                val run = coverParagraph.createRun()
                                run.addPicture(
                                    java.io.ByteArrayInputStream(imageBytes),
                                    XWPFDocument.PICTURE_TYPE_JPEG,
                                    "cover.jpg",
                                    Units.toEMU(300.0), // width
                                    Units.toEMU(400.0)  // height
                                )
                                  // Page break after cover
                                val pageBreakAfterCover = document.createParagraph()
                                pageBreakAfterCover.createRun().addBreak()
                                
                            } catch (e: Exception) {
                                // If image insertion fails, continue without cover
                                e.printStackTrace()
                            }
                        }
                    }
                    
                    // Create title page
                    val titleParagraph = document.createParagraph()
                    titleParagraph.alignment = ParagraphAlignment.CENTER
                    val titleRun = titleParagraph.createRun()
                    titleRun.setText(book.title)
                    titleRun.isBold = true
                    titleRun.fontSize = 24
                    
                    // Add author if available
                    if (book.author.isNotBlank()) {
                        document.createParagraph() // blank line
                        val authorParagraph = document.createParagraph()
                        authorParagraph.alignment = ParagraphAlignment.CENTER
                        val authorRun = authorParagraph.createRun()
                        authorRun.setText("by ${book.author}")
                        authorRun.fontSize = 16
                    }
                    
                    // Add description if available
                    if (book.description.isNotBlank()) {
                        document.createParagraph() // blank line
                        val descParagraph = document.createParagraph()
                        descParagraph.alignment = ParagraphAlignment.CENTER
                        val descRun = descParagraph.createRun()
                        descRun.setText(book.description)
                        descRun.fontSize = 12
                        descRun.isItalic = true
                    }
                      // Page break after title page
                    val pageBreakParagraph = document.createParagraph()
                    pageBreakParagraph.createRun().addBreak()
                    
                    // Create Table of Contents if multiple chapters
                    if (chapters.size > 1) {
                        val tocParagraph = document.createParagraph()
                        tocParagraph.alignment = ParagraphAlignment.CENTER
                        val tocRun = tocParagraph.createRun()
                        tocRun.setText("Table of Contents")
                        tocRun.isBold = true
                        tocRun.fontSize = 18
                        
                        document.createParagraph() // blank line
                        
                        // Add TOC entries with better formatting
                        chapters.sortedBy { it.order }.forEachIndexed { index, chapter ->
                            val tocEntryParagraph = document.createParagraph()
                            val tocEntryRun = tocEntryParagraph.createRun()
                            tocEntryRun.setText("${index + 1}. ${chapter.title}")
                            tocEntryRun.fontSize = 12
                            
                            // Add tab and page reference (simplified)
                            val pageRun = tocEntryParagraph.createRun()
                            pageRun.setText(" .......................... ${index + 3}") // Approximate page numbers
                            pageRun.fontSize = 12
                        }
                          // Page break after TOC
                        val tocBreakParagraph = document.createParagraph()
                        tocBreakParagraph.createRun().addBreak()
                    }
                    
                    // Add chapters
                    chapters.sortedBy { it.order }.forEach { chapter ->
                        // Chapter title with enhanced formatting
                        val chapterTitleParagraph = document.createParagraph()
                        chapterTitleParagraph.alignment = ParagraphAlignment.CENTER
                        val chapterTitleRun = chapterTitleParagraph.createRun()
                        chapterTitleRun.setText(chapter.title)
                        chapterTitleRun.isBold = true
                        chapterTitleRun.fontSize = 18
                        
                        // Add decorative line under chapter title
                        val decorativeParagraph = document.createParagraph()
                        decorativeParagraph.alignment = ParagraphAlignment.CENTER
                        val decorativeRun = decorativeParagraph.createRun()
                        decorativeRun.setText("—————————————————")
                        decorativeRun.fontSize = 12
                        
                        // Add blank line
                        document.createParagraph()
                        
                        // Convert rich text content to DOCX
                        val richTextContent = try {
                            Json.decodeFromString<RichTextDocument>(chapter.content)
                        } catch (e: Exception) {
                            RichTextDocument.fromPlainText(chapter.content)
                        }
                        
                        // Parse and add rich text content
                        parseRichTextToDocx(document, richTextContent)
                        
                        // Add page break between chapters (except for the last one)
                        if (chapter != chapters.last()) {                            val chapterBreakParagraph = document.createParagraph()
                            chapterBreakParagraph.createRun().addBreak()
                        } else {
                            // Just add some space for the last chapter
                            document.createParagraph()
                            document.createParagraph()
                        }
                    }
                    
                    // Write to file
                    FileOutputStream(file).use { outputStream ->
                        document.write(outputStream)
                    }
                } finally {
                    document.close()
                }
            }
            ExportResult.Success(file.absolutePath, chapters.size)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export book to DOCX: ${e.message}")
        }
    }

    // =========================
    // PDF EXPORT METHODS
    // =========================
    
    /**
     * Exports a single chapter to PDF format
     */
    suspend fun exportChapterToPdf(
        chapter: Chapter,
        fileName: String
    ): ExportResult {
        return try {
            val file = fileExportHelper.saveBinaryFile(fileName, chapter.title) { file ->
                generatePdfDocument(
                    title = chapter.title,
                    content = listOf(PdfChapterContent(chapter.title, chapter.content)),
                    outputFile = file,
                    book = null
                )
            }
            ExportResult.Success(file.absolutePath, 1)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export chapter to PDF: ${e.message}")
        }
    }
    
    /**
     * Exports an entire book to PDF format
     */
    suspend fun exportBookToPdf(
        book: Book,
        chapters: List<Chapter>,
        fileName: String
    ): ExportResult {
        return try {
            val file = fileExportHelper.saveBinaryFile(fileName, book.title) { file ->
                val bookMetadata = buildString {
                    if (book.author.isNotBlank()) {
                        appendLine("Author: ${book.author}")
                    }
                    if (book.description.isNotBlank()) {
                        appendLine("Description: ${book.description}")
                    }
                }
                
                val pdfChapters = chapters.sortedBy { it.order }.map { chapter ->
                    PdfChapterContent(chapter.title, chapter.content)
                }
                
                generatePdfDocument(
                    title = book.title,
                    content = pdfChapters,
                    metadata = bookMetadata.takeIf { it.isNotBlank() },
                    outputFile = file,
                    book = book
                )
            }
            ExportResult.Success(file.absolutePath, chapters.size)
        } catch (e: Exception) {
            ExportResult.Error("Failed to export book to PDF: ${e.message}")
        }    }    // =========================
    // APACHE POI HELPER METHODS
    // =========================
      /**
     * Sets system properties required for Apache POI XML parsing on Android
     */
    private fun setPoiSystemProperties() {
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLInputFactory",
            "com.fasterxml.aalto.stax.InputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLOutputFactory",
            "com.fasterxml.aalto.stax.OutputFactoryImpl"
        )
        System.setProperty(
            "org.apache.poi.javax.xml.stream.XMLEventFactory",
            "com.fasterxml.aalto.stax.EventFactoryImpl"
        )
    }
    
    /**
     * Loads a cover image from the given file path
     */
    private fun loadCoverImage(coverImagePath: String?): Bitmap? {
        return try {
            if (coverImagePath != null && File(coverImagePath).exists()) {
                BitmapFactory.decodeFile(coverImagePath)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }    /**
     * Adds page numbering to a DOCX document
     * Note: Full page numbering support requires Apache POI extensions
     */
    private fun addPageNumbering(document: XWPFDocument) {
        // TODO: Implement proper page numbering with Apache POI
        // For now, this is a placeholder that doesn't break the build
        try {
            // Page numbering functionality would go here
            // This requires specific Apache POI methods that may not be available
            // in the current version or may require additional dependencies
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Parses rich text content and adds it to a DOCX document
     */
    private fun parseRichTextToDocx(document: XWPFDocument, richTextContent: RichTextDocument) {
        val lines = richTextContent.plainText.split('\n')
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            when {
                // Empty lines
                trimmedLine.isEmpty() -> {
                    document.createParagraph()
                }
                
                // Headers
                trimmedLine.startsWith("### ") -> {
                    val headerText = trimmedLine.removePrefix("### ")
                    val headerParagraph = document.createParagraph()
                    val headerRun = headerParagraph.createRun()
                    headerRun.setText(headerText)
                    headerRun.isBold = true
                    headerRun.fontSize = 12
                }
                trimmedLine.startsWith("## ") -> {
                    val headerText = trimmedLine.removePrefix("## ")
                    val headerParagraph = document.createParagraph()
                    val headerRun = headerParagraph.createRun()
                    headerRun.setText(headerText)
                    headerRun.isBold = true
                    headerRun.fontSize = 14
                }
                trimmedLine.startsWith("# ") -> {
                    val headerText = trimmedLine.removePrefix("# ")
                    val headerParagraph = document.createParagraph()
                    val headerRun = headerParagraph.createRun()
                    headerRun.setText(headerText)
                    headerRun.isBold = true
                    headerRun.fontSize = 16
                }
                
                // Quotes
                trimmedLine.startsWith("> ") -> {
                    val quoteText = trimmedLine.removePrefix("> ")
                    val quoteParagraph = document.createParagraph()
                    val quoteRun = quoteParagraph.createRun()
                    quoteRun.setText("  $quoteText")
                    quoteRun.isItalic = true
                    quoteRun.fontSize = 11
                }
                
                // Lists
                trimmedLine.startsWith("- ") -> {
                    val listText = trimmedLine.removePrefix("- ")
                    val listParagraph = document.createParagraph()
                    val listRun = listParagraph.createRun()
                    listRun.setText("  • $listText")
                    listRun.fontSize = 11
                }
                trimmedLine.matches(Regex("^\\d+\\. .*")) -> {
                    val numberPattern = Regex("^(\\d+)\\. (.*)")
                    val matchResult = numberPattern.find(trimmedLine)
                    if (matchResult != null) {
                        val number = matchResult.groupValues[1]
                        val listText = matchResult.groupValues[2]
                        val listParagraph = document.createParagraph()
                        val listRun = listParagraph.createRun()
                        listRun.setText("  $number. $listText")
                        listRun.fontSize = 11
                    }
                }
                
                // Regular paragraph
                else -> {
                    val paragraph = document.createParagraph()
                    
                    // Process inline formatting (bold, italic)
                    processInlineFormattingDocx(paragraph, trimmedLine)
                }
            }
        }
    }
    
    /**
     * Processes inline formatting like bold and italic for DOCX
     */
    private fun processInlineFormattingDocx(paragraph: XWPFParagraph, text: String) {
        if (!text.contains("**") && !text.contains("*")) {
            // No formatting, create simple run
            val run = paragraph.createRun()
            run.setText(text)
            run.fontSize = 11
            return
        }
        
        // Process text with bold and italic formatting
        val segments = parseTextWithFormatting(text)
        
        segments.forEach { segment ->
            if (segment.text.isNotBlank()) {
                val run = paragraph.createRun()
                run.setText(segment.text)
                run.isBold = segment.isBold
                run.isItalic = segment.isItalic
                run.fontSize = 11
            }
        }
    }
    
    /**
     * Parses text with bold (**text**) and italic (*text*) formatting
     */
    private fun parseTextWithFormatting(text: String): List<TextSegment> {
        val segments = mutableListOf<TextSegment>()
        var currentIndex = 0
        val textLength = text.length
        
        while (currentIndex < textLength) {
            // Look for the next formatting marker
            val boldStart = text.indexOf("**", currentIndex)
            val italicStart = text.indexOf("*", currentIndex)
            
            // Find the closest marker
            val nextMarker = when {
                boldStart == -1 && italicStart == -1 -> -1
                boldStart == -1 -> italicStart
                italicStart == -1 -> boldStart
                boldStart < italicStart -> boldStart
                else -> italicStart
            }
            
            if (nextMarker == -1) {
                // No more markers, add remaining text
                if (currentIndex < textLength) {
                    segments.add(TextSegment(text.substring(currentIndex), false, false))
                }
                break
            }
            
            // Add text before the marker
            if (nextMarker > currentIndex) {
                segments.add(TextSegment(text.substring(currentIndex, nextMarker), false, false))
            }
            
            // Process the marker
            if (nextMarker == boldStart && text.startsWith("**", nextMarker)) {
                // Bold formatting
                val endMarker = text.indexOf("**", nextMarker + 2)
                if (endMarker != -1) {
                    val boldText = text.substring(nextMarker + 2, endMarker)
                    segments.add(TextSegment(boldText, true, false))
                    currentIndex = endMarker + 2
                } else {
                    // No closing marker, treat as plain text
                    segments.add(TextSegment("**", false, false))
                    currentIndex = nextMarker + 2
                }
            } else if (nextMarker == italicStart) {
                // Italic formatting (but not bold)
                val endMarker = text.indexOf("*", nextMarker + 1)
                if (endMarker != -1 && !text.startsWith("**", nextMarker)) {
                    val italicText = text.substring(nextMarker + 1, endMarker)
                    segments.add(TextSegment(italicText, false, true))
                    currentIndex = endMarker + 1
                } else {
                    // No closing marker or part of bold, treat as plain text
                    segments.add(TextSegment("*", false, false))
                    currentIndex = nextMarker + 1
                }
            }
        }
        
        return segments
    }
    
    /**
     * Data class to represent text segments with formatting
     */
    private data class TextSegment(
        val text: String,
        val isBold: Boolean,
        val isItalic: Boolean
    )

    // =========================
    // PDF GENERATION CLASSES
    // =========================
    
    /**
     * Data class to represent chapter content for PDF generation
     */
    private data class PdfChapterContent(
        val title: String,
        val content: String
    )
    
    /**
     * Generates a PDF document with the given content
     */
    private fun generatePdfDocument(
        title: String,
        content: List<PdfChapterContent>,
        metadata: String? = null,
        outputFile: File,
        book: Book? = null
    ) {        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        try {
            // Set up paint objects for different text styles
            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 24f
                isAntiAlias = true
                isFakeBoldText = true
            }
            
            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
                isAntiAlias = true
                isFakeBoldText = true
            }
            
            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                isAntiAlias = true
            }
            
            val leftMargin = 50f
            val rightMargin = 545f
            val pageHeight = 792f
            val bottomMargin = 50f
            
            // Create first page
            var currentPage = pdfDocument.startPage(pageInfo)
            var canvas = currentPage.canvas
            var yPosition = 80f
            
            // Draw title
            canvas.drawText(title, leftMargin, yPosition, titlePaint)
            yPosition += 50f
            
            // Draw metadata if available
            if (metadata != null) {
                val metadataPaint = Paint().apply {
                    color = Color.GRAY
                    textSize = 10f
                    isAntiAlias = true
                }
                
                metadata.lines().forEach { line ->
                    canvas.drawText(line, leftMargin, yPosition, metadataPaint)
                    yPosition += 15f
                }
                yPosition += 20f
            }
            
            // Process each chapter
            content.forEach { chapter ->
                // Check if we need a new page for chapter header
                if (yPosition > pageHeight - bottomMargin - 100) {
                    pdfDocument.finishPage(currentPage)
                    currentPage = pdfDocument.startPage(pageInfo)
                    canvas = currentPage.canvas
                    yPosition = 80f
                }
                
                // Draw chapter title
                canvas.drawText(chapter.title, leftMargin, yPosition, headerPaint)
                yPosition += 30f
                
                // Process chapter content
                val richTextContent = try {
                    Json.decodeFromString<RichTextDocument>(chapter.content)
                } catch (e: Exception) {
                    RichTextDocument.fromPlainText(chapter.content)
                }
                
                val plainText = RichTextFormatter.toPlainText(richTextContent)
                val lines = plainText.split('\n')
                
                lines.forEach { line ->
                    // Check if we need a new page
                    if (yPosition > pageHeight - bottomMargin) {
                        pdfDocument.finishPage(currentPage)
                        currentPage = pdfDocument.startPage(pageInfo)
                        canvas = currentPage.canvas
                        yPosition = 80f
                    }
                    
                    // Draw line with word wrapping if needed
                    yPosition = drawWrappedText(
                        canvas = canvas,
                        text = line,
                        paint = bodyPaint,
                        startX = leftMargin,
                        startY = yPosition,
                        maxWidth = rightMargin - leftMargin,
                        lineHeight = 16f
                    ) { newY ->
                        if (newY > pageHeight - bottomMargin) {
                            pdfDocument.finishPage(currentPage)
                            currentPage = pdfDocument.startPage(pageInfo)
                            canvas = currentPage.canvas
                            80f
                        } else {
                            newY
                        }
                    }
                    
                    yPosition += 5f // Small gap between lines
                }
                
                yPosition += 20f // Space between chapters
            }
            
            pdfDocument.finishPage(currentPage)
            
            // Write to file
            FileOutputStream(outputFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
        } finally {
            pdfDocument.close()
        }
    }
    
    /**
     * Draws wrapped text on a canvas, handling page breaks
     */
    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        paint: Paint,
        startX: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float,
        onNewPageNeeded: (Float) -> Float
    ): Float {
        var currentY = startY
        val words = text.split(" ")
        var currentLine = StringBuilder()
        
        val textBounds = Rect()
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            paint.getTextBounds(testLine, 0, testLine.length, textBounds)
            
            if (textBounds.width() <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                // Draw current line and start new one
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine.toString(), startX, currentY, paint)
                    currentY += lineHeight
                    currentY = onNewPageNeeded(currentY)
                }
                currentLine = StringBuilder(word)
            }
        }
        
        // Draw the last line
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), startX, currentY, paint)
            currentY += lineHeight
        }
        
        return currentY
    }
}