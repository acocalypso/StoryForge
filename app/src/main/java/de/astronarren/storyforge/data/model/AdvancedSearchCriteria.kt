package de.astronarren.storyforge.data.model

import de.astronarren.storyforge.data.database.entities.Book
import java.util.*

/**
 * Advanced search criteria for filtering books
 */
data class AdvancedSearchCriteria(
    val searchQuery: String = "",
    val genres: Set<BookGenre> = emptySet(),
    val authors: Set<String> = emptySet(),
    val favoritesOnly: Boolean = false,
    val wordCountRange: WordCountRange? = null,
    val dateRange: DateRange? = null,
    val sortBy: SortBy = SortBy.TITLE_ASC,
    val tags: Set<String> = emptySet()
) {
    
    /**
     * Check if search criteria is empty (no filters applied)
     */
    fun isEmpty(): Boolean {
        return searchQuery.isBlank() &&
                genres.isEmpty() &&
                authors.isEmpty() &&
                !favoritesOnly &&
                wordCountRange == null &&
                dateRange == null &&
                tags.isEmpty()
    }
    
    /**
     * Get active filter count for UI display
     */
    fun getActiveFilterCount(): Int {
        var count = 0
        if (searchQuery.isNotBlank()) count++
        if (genres.isNotEmpty()) count++
        if (authors.isNotEmpty()) count++
        if (favoritesOnly) count++
        if (wordCountRange != null) count++
        if (dateRange != null) count++
        if (tags.isNotEmpty()) count++
        return count
    }
    
    /**
     * Apply filters to a list of books
     */
    fun filterBooks(books: List<Book>): List<Book> {
        var filteredBooks = books
        
        // Text search filter
        if (searchQuery.isNotBlank()) {
            filteredBooks = filteredBooks.filter { book ->
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.author.contains(searchQuery, ignoreCase = true) ||
                book.description.contains(searchQuery, ignoreCase = true) ||
                book.genre.contains(searchQuery, ignoreCase = true)
            }
        }
        
        // Genre filter
        if (genres.isNotEmpty()) {
            filteredBooks = filteredBooks.filter { book ->
                genres.any { genre ->
                    book.genre.equals(genre.displayName, ignoreCase = true)
                }
            }
        }
        
        // Author filter
        if (authors.isNotEmpty()) {
            filteredBooks = filteredBooks.filter { book ->
                authors.any { author ->
                    book.author.equals(author, ignoreCase = true)
                }
            }
        }
        
        // Favorites filter
        if (favoritesOnly) {
            filteredBooks = filteredBooks.filter { it.isFavorite }
        }
        
        // Word count range filter
        wordCountRange?.let { range ->
            filteredBooks = filteredBooks.filter { book ->
                book.targetWordCount?.let { wordCount ->
                    wordCount >= range.min && (range.max == null || wordCount <= range.max)
                } ?: false
            }
        }
        
        // Date range filter
        dateRange?.let { range ->
            filteredBooks = filteredBooks.filter { book ->
                book.createdAt >= range.startDate && book.createdAt <= range.endDate
            }
        }
        
        // Apply sorting
        return when (sortBy) {
            SortBy.TITLE_ASC -> filteredBooks.sortedBy { it.title.lowercase() }
            SortBy.TITLE_DESC -> filteredBooks.sortedByDescending { it.title.lowercase() }
            SortBy.AUTHOR_ASC -> filteredBooks.sortedBy { it.author.lowercase() }
            SortBy.AUTHOR_DESC -> filteredBooks.sortedByDescending { it.author.lowercase() }
            SortBy.DATE_CREATED_ASC -> filteredBooks.sortedBy { it.createdAt }
            SortBy.DATE_CREATED_DESC -> filteredBooks.sortedByDescending { it.createdAt }
            SortBy.DATE_UPDATED_ASC -> filteredBooks.sortedBy { it.updatedAt }
            SortBy.DATE_UPDATED_DESC -> filteredBooks.sortedByDescending { it.updatedAt }
            SortBy.WORD_COUNT_ASC -> filteredBooks.sortedBy { it.targetWordCount ?: 0 }
            SortBy.WORD_COUNT_DESC -> filteredBooks.sortedByDescending { it.targetWordCount ?: 0 }
            SortBy.FAVORITES_FIRST -> filteredBooks.sortedWith(
                compareByDescending<Book> { it.isFavorite }.thenBy { it.title.lowercase() }
            )
        }
    }
}

/**
 * Word count range for filtering
 */
data class WordCountRange(
    val min: Int,
    val max: Int? = null
) {
    companion object {
        val SHORT_STORY = WordCountRange(0, 7500)
        val NOVELETTE = WordCountRange(7500, 17500)
        val NOVELLA = WordCountRange(17500, 40000)
        val NOVEL = WordCountRange(40000, null)
        
        fun getAllRanges() = listOf(SHORT_STORY, NOVELETTE, NOVELLA, NOVEL)
    }
    
    fun getDisplayName(): String = when {
        this == SHORT_STORY -> "Short Story (< 7.5K words)"
        this == NOVELETTE -> "Novelette (7.5K - 17.5K words)"
        this == NOVELLA -> "Novella (17.5K - 40K words)"
        this == NOVEL -> "Novel (40K+ words)"
        max == null -> "${min}+ words"
        else -> "$min - $max words"
    }
}

/**
 * Date range for filtering
 */
data class DateRange(
    val startDate: Long,
    val endDate: Long
) {
    companion object {
        fun today(): DateRange {
            val now = System.currentTimeMillis()
            val startOfDay = Calendar.getInstance().apply {
                timeInMillis = now
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1
            return DateRange(startOfDay, endOfDay)
        }
        
        fun thisWeek(): DateRange {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfWeek = calendar.timeInMillis
            val endOfWeek = startOfWeek + 7 * 24 * 60 * 60 * 1000 - 1
            return DateRange(startOfWeek, endOfWeek)
        }
        
        fun thisMonth(): DateRange {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val endOfMonth = calendar.timeInMillis - 1
            return DateRange(startOfMonth, endOfMonth)
        }
        
        fun thisYear(): DateRange {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfYear = calendar.timeInMillis
            calendar.add(Calendar.YEAR, 1)
            val endOfYear = calendar.timeInMillis - 1
            return DateRange(startOfYear, endOfYear)
        }
    }
}

/**
 * Sort options for book list
 */
enum class SortBy(val displayName: String) {
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    AUTHOR_ASC("Author A-Z"),
    AUTHOR_DESC("Author Z-A"),
    DATE_CREATED_ASC("Date Created (Oldest)"),
    DATE_CREATED_DESC("Date Created (Newest)"),
    DATE_UPDATED_ASC("Date Updated (Oldest)"),
    DATE_UPDATED_DESC("Date Updated (Newest)"),
    WORD_COUNT_ASC("Word Count (Low to High)"),
    WORD_COUNT_DESC("Word Count (High to Low)"),
    FAVORITES_FIRST("Favorites First")
}

