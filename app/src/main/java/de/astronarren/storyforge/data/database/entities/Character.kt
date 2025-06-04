package de.astronarren.storyforge.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "characters",
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
data class Character(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val name: String,
    val description: String = "",
    val age: Int? = null,
    val occupation: String = "",
    val backstory: String = "",
    val personality: String = "",
    val physicalDescription: String = "",
    val goals: String = "",
    val conflicts: String = "",
    val relationships: List<String> = emptyList(), // Character IDs
    val portraitImagePath: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isMainCharacter: Boolean = false,
    val characterArc: String = ""
)

