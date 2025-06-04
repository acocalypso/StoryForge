package de.astronarren.storyforge.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(tableName = "books")
@Serializable
data class Book(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val author: String = "",
    val genre: String = "",
    val wordCount: Int = 0,
    val targetWordCount: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val coverImagePath: String? = null,
    val isFavorite: Boolean = false,
    val isActive: Boolean = true,
    val version: Int = 1
)

