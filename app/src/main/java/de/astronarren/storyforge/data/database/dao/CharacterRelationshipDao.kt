package de.astronarren.storyforge.data.database.dao

import androidx.room.*
import de.astronarren.storyforge.data.database.entities.CharacterRelationship
import de.astronarren.storyforge.data.database.entities.RelationshipType
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterRelationshipDao {
    
    @Query("SELECT * FROM character_relationships WHERE characterId = :characterId")
    fun getRelationshipsByCharacter(characterId: String): Flow<List<CharacterRelationship>>
    
    @Query("SELECT * FROM character_relationships WHERE characterId = :characterId OR (relatedCharacterId = :characterId AND isReciprocal = 1)")
    fun getAllRelationshipsForCharacter(characterId: String): Flow<List<CharacterRelationship>>
    
    @Query("SELECT * FROM character_relationships WHERE (characterId = :characterId AND relatedCharacterId = :relatedCharacterId) OR (characterId = :relatedCharacterId AND relatedCharacterId = :characterId AND isReciprocal = 1)")
    suspend fun getRelationshipBetween(characterId: String, relatedCharacterId: String): CharacterRelationship?
    
    @Query("SELECT * FROM character_relationships WHERE relationshipType = :type")
    fun getRelationshipsByType(type: RelationshipType): Flow<List<CharacterRelationship>>
    
    @Query("SELECT * FROM character_relationships WHERE id = :id")
    suspend fun getRelationshipById(id: String): CharacterRelationship?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelationship(relationship: CharacterRelationship)
    
    @Update
    suspend fun updateRelationship(relationship: CharacterRelationship)
    
    @Delete
    suspend fun deleteRelationship(relationship: CharacterRelationship)
    
    @Query("DELETE FROM character_relationships WHERE characterId = :characterId OR relatedCharacterId = :characterId")
    suspend fun deleteAllRelationshipsForCharacter(characterId: String)
    
    @Query("DELETE FROM character_relationships WHERE (characterId = :characterId AND relatedCharacterId = :relatedCharacterId) OR (characterId = :relatedCharacterId AND relatedCharacterId = :characterId)")
    suspend fun deleteRelationshipBetween(characterId: String, relatedCharacterId: String)
    
    // Analytics queries
    @Query("SELECT COUNT(*) FROM character_relationships WHERE characterId = :characterId")
    suspend fun getRelationshipCount(characterId: String): Int
      @Query("SELECT relationshipType, COUNT(*) as count FROM character_relationships WHERE characterId = :characterId GROUP BY relationshipType")
    suspend fun getRelationshipTypeDistribution(characterId: String): List<RelationshipTypeCount>
    
    data class RelationshipTypeCount(
        val relationshipType: RelationshipType,
        val count: Int
    )
}
