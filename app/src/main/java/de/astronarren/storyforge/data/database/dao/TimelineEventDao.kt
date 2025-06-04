package de.astronarren.storyforge.data.database.dao

import androidx.room.*
import de.astronarren.storyforge.data.database.entities.TimelineEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface TimelineEventDao {
    
    @Query("SELECT * FROM timeline_events WHERE bookId = :bookId ORDER BY `order` ASC")
    fun getTimelineEventsByBook(bookId: String): Flow<List<TimelineEvent>>
    
    @Query("SELECT * FROM timeline_events WHERE id = :id")
    suspend fun getTimelineEventById(id: String): TimelineEvent?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimelineEvent(event: TimelineEvent)
    
    @Update
    suspend fun updateTimelineEvent(event: TimelineEvent)
    
    @Delete
    suspend fun deleteTimelineEvent(event: TimelineEvent)
    
    @Query("SELECT MAX(`order`) FROM timeline_events WHERE bookId = :bookId")
    suspend fun getMaxOrder(bookId: String): Int?
    
    @Query("SELECT * FROM timeline_events WHERE bookId = :bookId AND importance >= :minImportance ORDER BY `order` ASC")
    fun getImportantTimelineEvents(bookId: String, minImportance: Int): Flow<List<TimelineEvent>>
}

