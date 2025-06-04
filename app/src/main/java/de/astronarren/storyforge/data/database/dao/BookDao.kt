package de.astronarren.storyforge.data.database.dao

import androidx.room.*
import de.astronarren.storyforge.data.database.entities.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    
    @Query("SELECT * FROM books WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllActiveBooks(): Flow<List<Book>>
    
    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: String): Book?
    
    @Query("SELECT * FROM books WHERE id = :id")
    fun getBookByIdFlow(id: String): Flow<Book?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: Book)
    
    @Update
    suspend fun updateBook(book: Book)
    
    @Delete
    suspend fun deleteBook(book: Book)
    
    @Query("UPDATE books SET isActive = 0 WHERE id = :id")
    suspend fun archiveBook(id: String)
    
    @Query("UPDATE books SET wordCount = :wordCount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateWordCount(id: String, wordCount: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT COUNT(*) FROM books WHERE isActive = 1")
    suspend fun getActiveBooksCount(): Int
}

