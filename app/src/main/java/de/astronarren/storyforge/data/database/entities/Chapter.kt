package de.astronarren.storyforge.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "chapters",
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
data class Chapter(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val title: String,
    val description: String = "",
    val content: String = "", // Rich text content with formatting tags
    val order: Int = 0,
    val wordCount: Int = 0,
    val targetWordCount: Int? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val color: String? = null // For visual organization
)

