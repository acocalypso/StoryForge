package de.astronarren.storyforge.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "project_versions",
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
data class ProjectVersion(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val versionNumber: Int,
    val title: String,
    val description: String = "",
    val dataSnapshot: String, // JSON snapshot of the entire project state
    val createdAt: Long = System.currentTimeMillis(),
    val isAutoSave: Boolean = false,
    val fileSize: Long = 0
)

