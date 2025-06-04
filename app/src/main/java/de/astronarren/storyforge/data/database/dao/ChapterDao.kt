package de.astronarren.storyforge.data.database.dao

import androidx.room.*
import de.astronarren.storyforge.data.database.entities.Chapter
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {
    
    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY `order` ASC")
    fun getChaptersByBook(bookId: String): Flow<List<Chapter>>
    
    @Query("SELECT * FROM chapters WHERE id = :id")
    suspend fun getChapterById(id: String): Chapter?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: Chapter)
    
    @Update
    suspend fun updateChapter(chapter: Chapter)
    
    @Delete
    suspend fun deleteChapter(chapter: Chapter)
    
    @Query("UPDATE chapters SET wordCount = :wordCount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateWordCount(id: String, wordCount: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT MAX(`order`) FROM chapters WHERE bookId = :bookId")
    suspend fun getMaxOrder(bookId: String): Int?
}

