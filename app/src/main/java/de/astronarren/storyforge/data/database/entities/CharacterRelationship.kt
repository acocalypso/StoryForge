package de.astronarren.storyforge.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable
import java.util.UUID

@Entity(
    tableName = "character_relationships",
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["relatedCharacterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["characterId"]),
        Index(value = ["relatedCharacterId"]),
        Index(value = ["characterId", "relatedCharacterId"], unique = true)
    ]
)
@Serializable
data class CharacterRelationship(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val characterId: String,
    val relatedCharacterId: String,
    val relationshipType: RelationshipType,
    val description: String = "",
    val strength: RelationshipStrength = RelationshipStrength.MODERATE,
    val isReciprocal: Boolean = true, // If true, relationship exists in both directions
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class RelationshipType(val displayName: String, val color: String) {
    FAMILY("Family", "#E91E63"),           // Pink
    FRIEND("Friend", "#4CAF50"),           // Green
    ROMANTIC("Romantic", "#F44336"),       // Red
    ENEMY("Enemy", "#FF5722"),             // Deep Orange
    ALLY("Ally", "#2196F3"),               // Blue
    RIVAL("Rival", "#FF9800"),             // Orange
    MENTOR("Mentor", "#9C27B0"),           // Purple
    STUDENT("Student", "#673AB7"),         // Deep Purple
    COLLEAGUE("Colleague", "#795548"),     // Brown
    ACQUAINTANCE("Acquaintance", "#607D8B"), // Blue Grey
    NEUTRAL("Neutral", "#9E9E9E"),         // Grey
    UNKNOWN("Unknown", "#424242")          // Dark Grey
}

@Serializable
enum class RelationshipStrength(val displayName: String, val level: Int) {
    VERY_WEAK("Very Weak", 1),
    WEAK("Weak", 2),
    MODERATE("Moderate", 3),
    STRONG("Strong", 4),
    VERY_STRONG("Very Strong", 5)
}
