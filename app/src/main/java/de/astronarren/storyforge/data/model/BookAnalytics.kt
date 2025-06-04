package de.astronarren.storyforge.data.model

import de.astronarren.storyforge.data.database.entities.Book
import java.text.SimpleDateFormat
import java.util.*

/**
 * Comprehensive analytics data for books
 */
data class BookAnalytics(
    val totalBooks: Int = 0,
    val favoriteBooks: Int = 0,
    val completedBooks: Int = 0,
    val inProgressBooks: Int = 0,
    val plannedBooks: Int = 0,
    val totalTargetWords: Long = 0,
    val averageTargetWords: Int = 0,
    val genreDistribution: Map<String, Int> = emptyMap(),
    val authorDistribution: Map<String, Int> = emptyMap(),
    val booksCreatedThisMonth: Int = 0,
    val booksCreatedThisYear: Int = 0,
    val mostProductiveMonth: String = "",
    val oldestBook: Book? = null,
    val newestBook: Book? = null,
    val longestBook: Book? = null,
    val shortestBook: Book? = null,
    val averageBooksPerMonth: Double = 0.0,
    val bookCreationTrend: List<MonthlyData> = emptyList(),
    val genreGrowthTrend: Map<String, List<MonthlyData>> = emptyMap()
) {
    
    val favoritePercentage: Double
        get() = if (totalBooks > 0) (favoriteBooks * 100.0 / totalBooks) else 0.0
    
    val completionRate: Double
        get() = if (totalBooks > 0) (completedBooks * 100.0 / totalBooks) else 0.0
    
    val averageWordsPerBook: Int
        get() = if (totalBooks > 0) (totalTargetWords / totalBooks).toInt() else 0
    
    companion object {
        fun fromBooks(books: List<Book>): BookAnalytics {
            if (books.isEmpty()) return BookAnalytics()
            
            val currentTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance()
            
            // Current month boundaries
            calendar.timeInMillis = currentTime
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val startOfNextMonth = calendar.timeInMillis
            
            // Current year boundaries
            calendar.timeInMillis = currentTime
            calendar.set(Calendar.DAY_OF_YEAR, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfYear = calendar.timeInMillis
            
            val totalBooks = books.size
            val favoriteBooks = books.count { it.isFavorite }
            val booksWithTargetWords = books.filter { it.targetWordCount != null && it.targetWordCount!! > 0 }
            val totalTargetWords = booksWithTargetWords.sumOf { it.targetWordCount?.toLong() ?: 0L }
            val averageTargetWords = if (booksWithTargetWords.isNotEmpty()) {
                (totalTargetWords / booksWithTargetWords.size).toInt()
            } else 0
            
            // Genre distribution
            val genreDistribution = books.groupBy { it.genre }
                .mapValues { it.value.size }
                .toMap()
            
            // Author distribution
            val authorDistribution = books.filter { it.author.isNotBlank() }
                .groupBy { it.author }
                .mapValues { it.value.size }
                .toMap()
            
            // Books created this month/year
            val booksCreatedThisMonth = books.count { book ->
                book.createdAt >= startOfMonth && book.createdAt < startOfNextMonth
            }
            val booksCreatedThisYear = books.count { book ->
                book.createdAt >= startOfYear
            }
            
            // Find oldest and newest books
            val oldestBook = books.minByOrNull { it.createdAt }
            val newestBook = books.maxByOrNull { it.createdAt }
            
            // Find longest and shortest books by target word count
            val longestBook = booksWithTargetWords.maxByOrNull { it.targetWordCount ?: 0 }
            val shortestBook = booksWithTargetWords.minByOrNull { it.targetWordCount ?: 0 }
            
            // Calculate most productive month
            val monthlyCreation = books.groupBy { book ->
                val cal = Calendar.getInstance()
                cal.timeInMillis = book.createdAt
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            }
            val mostProductiveMonth = monthlyCreation.maxByOrNull { it.value.size }?.let { entry ->
                val parts = entry.key.split("-")
                val year = parts[0].toInt()
                val month = parts[1].toInt()
                val cal = Calendar.getInstance()
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
            } ?: ""
            
            // Calculate average books per month
            val firstBookTime = oldestBook?.createdAt ?: currentTime
            val monthsSinceFirstBook = if (firstBookTime < currentTime) {
                val timeDiff = currentTime - firstBookTime
                val daysDiff = timeDiff / (1000 * 60 * 60 * 24)
                maxOf(1, daysDiff / 30) // At least 1 month
            } else 1
            val averageBooksPerMonth = totalBooks.toDouble() / monthsSinceFirstBook
            
            // Generate book creation trend (last 12 months)
            val bookCreationTrend = generateMonthlyTrend(books, 12) { it.createdAt }
            
            // Generate genre growth trends
            val genreGrowthTrend = genreDistribution.keys.associateWith { genre ->
                val genreBooks = books.filter { it.genre == genre }
                generateMonthlyTrend(genreBooks, 6) { it.createdAt }
            }
            
            return BookAnalytics(
                totalBooks = totalBooks,
                favoriteBooks = favoriteBooks,
                totalTargetWords = totalTargetWords,
                averageTargetWords = averageTargetWords,
                genreDistribution = genreDistribution,
                authorDistribution = authorDistribution,
                booksCreatedThisMonth = booksCreatedThisMonth,
                booksCreatedThisYear = booksCreatedThisYear,
                mostProductiveMonth = mostProductiveMonth,
                oldestBook = oldestBook,
                newestBook = newestBook,
                longestBook = longestBook,
                shortestBook = shortestBook,
                averageBooksPerMonth = averageBooksPerMonth,
                bookCreationTrend = bookCreationTrend,
                genreGrowthTrend = genreGrowthTrend
            )
        }
        
        private fun generateMonthlyTrend(
            books: List<Book>,
            monthsCount: Int,
            timeExtractor: (Book) -> Long
        ): List<MonthlyData> {
            val result = mutableListOf<MonthlyData>()
            val calendar = Calendar.getInstance()
            
            repeat(monthsCount) { i ->
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.MONTH, -i)
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                
                // Calculate month boundaries
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfMonth = calendar.timeInMillis
                
                calendar.add(Calendar.MONTH, 1)
                val endOfMonth = calendar.timeInMillis
                
                val count = books.count { book ->
                    val time = timeExtractor(book)
                    time >= startOfMonth && time < endOfMonth
                }
                
                val monthName = calendar.apply {
                    timeInMillis = startOfMonth
                }.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) ?: ""
                
                result.add(MonthlyData(
                    month = monthName,
                    year = year,
                    count = count
                ))
            }
            
            return result.reversed() // Show oldest to newest
        }
    }
}

/**
 * Monthly data point for analytics trends
 */
data class MonthlyData(
    val month: String,
    val year: Int,
    val count: Int
) {
    val label: String get() = "$month $year"
}

