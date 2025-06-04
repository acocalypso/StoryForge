package de.astronarren.storyforge.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Entity representing a backup of chapter content
 */
@Entity(tableName = "chapter_backups")
@Serializable
data class ChapterBackup(
    @PrimaryKey
    val id: String,
    val chapterId: String,
    val backupType: BackupType,
    val title: String,
    val content: String, // JSON serialized RichTextDocument
    val wordCount: Int,
    val createdAt: Long,
    val description: String? = null // Optional description for manual backups
)

/**
 * Types of backups
 */
enum class BackupType {
    AUTO,           // Automatic periodic backup
    MANUAL,         // User-triggered backup
    PRE_EDIT        // Backup before major edits
}
