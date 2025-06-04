package de.astronarren.storyforge.data.database.dao

import androidx.room.*
import de.astronarren.storyforge.data.database.entities.Scene
import kotlinx.coroutines.flow.Flow

@Dao
interface SceneDao {
    
    @Query("SELECT * FROM scenes WHERE bookId = :bookId ORDER BY `order` ASC")
    fun getScenesByBook(bookId: String): Flow<List<Scene>>
    
    @Query("SELECT * FROM scenes WHERE chapterId = :chapterId ORDER BY `order` ASC")
    fun getScenesByChapter(chapterId: String): Flow<List<Scene>>
    
    @Query("SELECT * FROM scenes WHERE id = :id")
    suspend fun getSceneById(id: String): Scene?
    
    @Query("SELECT * FROM scenes WHERE bookId = :bookId AND location LIKE '%' || :location || '%'")
    fun getScenesByLocation(bookId: String, location: String): Flow<List<Scene>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScene(scene: Scene)
    
    @Update
    suspend fun updateScene(scene: Scene)
    
    @Delete
    suspend fun deleteScene(scene: Scene)
    
    @Query("UPDATE scenes SET wordCount = :wordCount, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateWordCount(id: String, wordCount: Int, updatedAt: Long = System.currentTimeMillis())
    
    @Query("SELECT MAX(`order`) FROM scenes WHERE bookId = :bookId")
    suspend fun getMaxOrderForBook(bookId: String): Int?
    
    @Query("SELECT MAX(`order`) FROM scenes WHERE chapterId = :chapterId")
    suspend fun getMaxOrderForChapter(chapterId: String): Int?
}

