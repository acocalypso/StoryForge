package de.astronarren.storyforge.data.database.dao

import androidx.room.*
import de.astronarren.storyforge.data.database.entities.Character
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    
    @Query("SELECT * FROM characters WHERE bookId = :bookId ORDER BY name ASC")
    fun getCharactersByBook(bookId: String): Flow<List<Character>>
      @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: String): Character?
    
    @Query("SELECT * FROM characters WHERE id = :id")
    fun getCharacterByIdFlow(id: String): Flow<Character?>
    
    @Query("SELECT * FROM characters WHERE bookId = :bookId AND isMainCharacter = 1")
    fun getMainCharactersByBook(bookId: String): Flow<List<Character>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacter(character: Character)
    
    @Update
    suspend fun updateCharacter(character: Character)
    
    @Delete
    suspend fun deleteCharacter(character: Character)
    
    @Query("SELECT * FROM characters WHERE bookId = :bookId AND name LIKE '%' || :searchQuery || '%'")
    fun searchCharacters(bookId: String, searchQuery: String): Flow<List<Character>>
}

