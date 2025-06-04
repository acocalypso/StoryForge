package de.astronarren.storyforge.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Comprehensive data model for StoryForge export/import functionality.
 * Contains all user data including books, chapters, characters, scenes, and timeline events.
 */
@Serializable
data class StoryForgeExport(
    val exportInfo: ExportInfo,
    val books: List<BookExportData>,
    val orphanedCharacters: List<CharacterExportData> = emptyList(),
    val orphanedScenes: List<SceneExportData> = emptyList(),
    val orphanedTimelineEvents: List<TimelineEventExportData> = emptyList()
)

@Serializable
data class ExportInfo(
    val version: String = "1.0.0",
    val appVersion: String,
    val exportDate: String, // ISO 8601 format
    val totalBooks: Int,
    val totalCharacters: Int,
    val totalScenes: Int,
    val totalChapters: Int,
    val totalTimelineEvents: Int,
    val exportedBy: String = "StoryForge Android"
)

@Serializable
data class BookExportData(
    val id: String,
    val title: String,
    val author: String,
    val description: String,
    val genre: String,
    val targetWordCount: Int,
    val currentWordCount: Int,
    val isFavorite: Boolean,
    val coverImagePath: String?,
    val createdAt: String, // ISO 8601 format
    val updatedAt: String, // ISO 8601 format
    val chapters: List<ChapterExportData>,
    val characters: List<CharacterExportData>,
    val scenes: List<SceneExportData>,
    val timelineEvents: List<TimelineEventExportData>
)

@Serializable
data class ChapterExportData(
    val id: String,
    val bookId: String,
    val title: String,
    val content: String, // Rich text content as JSON
    val orderIndex: Int,
    val wordCount: Int,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CharacterExportData(
    val id: String,
    val bookId: String?,
    val name: String,
    val description: String,
    val age: Int?,
    val occupation: String,
    val backstory: String,
    val personality: String,
    val appearance: String,
    val goals: String,
    val conflicts: String,
    val notes: String,
    val imageUrl: String?,
    val relationships: List<CharacterRelationshipExportData>,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class CharacterRelationshipExportData(
    val targetCharacterId: String,
    val relationshipType: String, // RelationshipType enum as string
    val relationshipStrength: String, // RelationshipStrength enum as string
    val description: String
)

@Serializable
data class SceneExportData(
    val id: String,
    val bookId: String?,
    val title: String,
    val description: String,
    val location: String,
    val timeOfDay: String, // TimeOfDay enum as string
    val mood: String, // SceneMood enum as string
    val tags: List<String>,
    val wordCount: Int,
    val orderIndex: Int,
    val content: String, // Rich text content as JSON
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class TimelineEventExportData(
    val id: String,
    val bookId: String?,
    val title: String,
    val description: String,
    val eventDate: String, // LocalDate as ISO string
    val orderIndex: Int,
    val relatedCharacters: List<String>, // Character IDs
    val relatedScenes: List<String>, // Scene IDs
    val eventType: String, // TimelineEventType enum as string
    val importance: String, // EventImportance enum as string
    val createdAt: String,
    val updatedAt: String
)
