package de.astronarren.storyforge.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "scenes",
    foreignKeys = [
        ForeignKey(
            entity = Book::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Chapter::class,
            parentColumns = ["id"],
            childColumns = ["chapterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bookId"]), Index(value = ["chapterId"])]
)
@Serializable
data class Scene(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val chapterId: String? = null,
    val title: String,
    val content: String = "",
    val summary: String = "",
    val wordCount: Int = 0,
    val order: Int = 0,
    val location: String = "",
    val timeOfDay: String = "",
    val charactersPresent: List<String> = emptyList(), // Character IDs
    val tags: List<String> = emptyList(),
    val mood: String = "",
    val purpose: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val pointOfView: String = "", // Character ID or name
    val conflictLevel: Int = 0 // 0-10 scale
)

