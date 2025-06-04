package de.astronarren.storyforge.data.database.dao

import androidx.room.*
import de.astronarren.storyforge.data.database.entities.ProjectVersion
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectVersionDao {
    
    @Query("SELECT * FROM project_versions WHERE bookId = :bookId ORDER BY versionNumber DESC")
    fun getVersionsByBook(bookId: String): Flow<List<ProjectVersion>>
    
    @Query("SELECT * FROM project_versions WHERE id = :id")
    suspend fun getVersionById(id: String): ProjectVersion?
    
    @Query("SELECT * FROM project_versions WHERE bookId = :bookId ORDER BY versionNumber DESC LIMIT 1")
    suspend fun getLatestVersion(bookId: String): ProjectVersion?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: ProjectVersion)
    
    @Delete
    suspend fun deleteVersion(version: ProjectVersion)
    
    @Query("DELETE FROM project_versions WHERE bookId = :bookId AND isAutoSave = 1 AND id NOT IN (SELECT id FROM project_versions WHERE bookId = :bookId AND isAutoSave = 1 ORDER BY createdAt DESC LIMIT :keepCount)")
    suspend fun cleanupOldAutoSaves(bookId: String, keepCount: Int = 10)
    
    @Query("SELECT COUNT(*) FROM project_versions WHERE bookId = :bookId")
    suspend fun getVersionCount(bookId: String): Int
}

