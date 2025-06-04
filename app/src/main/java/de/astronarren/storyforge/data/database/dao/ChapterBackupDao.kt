package de.astronarren.storyforge.data.database.dao

import androidx.room.*
import de.astronarren.storyforge.data.database.entities.ChapterBackup
import kotlinx.coroutines.flow.Flow

/**
 * DAO for chapter backup operations
 */
@Dao
interface ChapterBackupDao {
    
    @Query("SELECT * FROM chapter_backups WHERE chapterId = :chapterId ORDER BY createdAt DESC")
    fun getBackupsByChapter(chapterId: String): Flow<List<ChapterBackup>>
    
    @Query("SELECT * FROM chapter_backups WHERE chapterId = :chapterId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentBackups(chapterId: String, limit: Int): List<ChapterBackup>
    
    @Query("SELECT * FROM chapter_backups WHERE id = :backupId")
    suspend fun getBackupById(backupId: String): ChapterBackup?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: ChapterBackup)
    
    @Delete
    suspend fun deleteBackup(backup: ChapterBackup)
    
    @Query("DELETE FROM chapter_backups WHERE chapterId = :chapterId AND id NOT IN (SELECT id FROM chapter_backups WHERE chapterId = :chapterId ORDER BY createdAt DESC LIMIT :keepCount)")
    suspend fun deleteOldBackups(chapterId: String, keepCount: Int)
    
    @Query("DELETE FROM chapter_backups WHERE chapterId = :chapterId")
    suspend fun deleteAllBackupsForChapter(chapterId: String)
    
    @Query("SELECT COUNT(*) FROM chapter_backups WHERE chapterId = :chapterId")
    suspend fun getBackupCount(chapterId: String): Int
}
