package de.astronarren.storyforge.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "timeline_events",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"])]
)
@Serializable
data class TimelineEvent(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val title: String,
    val description: String = "",
    val date: String = "", // Could be relative like "Day 1" or absolute
    val time: String = "", // Time of day
    val duration: String = "", // How long the event lasts
    val order: Int = 0,
    val eventType: EventType = EventType.PLOT,
    val charactersInvolved: List<String> = emptyList(), // Character IDs
    val location: String = "",
    val relatedScenes: List<String> = emptyList(), // Scene IDs
    val importance: Int = 1, // 1-5 scale
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val color: String? = null // For visual organization in timeline
)

enum class EventType {
    PLOT,
    CHARACTER_DEVELOPMENT,
    WORLD_BUILDING,
    CONFLICT,
    RESOLUTION,
    BACKSTORY,
    FORESHADOWING
}

