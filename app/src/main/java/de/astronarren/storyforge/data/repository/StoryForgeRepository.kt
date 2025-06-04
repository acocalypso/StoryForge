package de.astronarren.storyforge.data.repository

import de.astronarren.storyforge.data.database.dao.*
import de.astronarren.storyforge.data.database.entities.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class StoryForgeRepository(
    private val bookDao: BookDao,
    private val chapterDao: ChapterDao,
    private val chapterBackupDao: ChapterBackupDao,
    private val sceneDao: SceneDao,
    private val characterDao: CharacterDao,
    private val characterRelationshipDao: CharacterRelationshipDao,
    private val timelineEventDao: TimelineEventDao,
    private val projectVersionDao: ProjectVersionDao
) {
    
    // Book operations
    fun getAllActiveBooks(): Flow<List<Book>> = bookDao.getAllActiveBooks()
    
    suspend fun getBookById(id: String): Book? = bookDao.getBookById(id)
    
    fun getBookByIdFlow(id: String): Flow<Book?> = bookDao.getBookByIdFlow(id)
    
    suspend fun insertBook(book: Book) = bookDao.insertBook(book)
    
    suspend fun updateBook(book: Book) = bookDao.updateBook(book)
    
    suspend fun deleteBook(book: Book) = bookDao.deleteBook(book)
    
    suspend fun archiveBook(id: String) = bookDao.archiveBook(id)
      // Chapter operations
    fun getChaptersByBook(bookId: String): Flow<List<Chapter>> = chapterDao.getChaptersByBook(bookId)
    
    fun getChaptersByBookId(bookId: String): Flow<List<Chapter>> = chapterDao.getChaptersByBook(bookId)
    
    suspend fun getChapterById(id: String): Chapter? = chapterDao.getChapterById(id)
    
    suspend fun insertChapter(chapter: Chapter) = chapterDao.insertChapter(chapter)
    
    suspend fun updateChapter(chapter: Chapter) = chapterDao.updateChapter(chapter)
    
    suspend fun deleteChapter(chapter: Chapter) = chapterDao.deleteChapter(chapter)
    
    suspend fun getMaxChapterOrder(bookId: String): Int? = chapterDao.getMaxOrder(bookId)
    
    // Chapter Backup operations
    fun getChapterBackupsByChapter(chapterId: String): Flow<List<ChapterBackup>> = 
        chapterBackupDao.getBackupsByChapter(chapterId)
    
    suspend fun getChapterBackups(chapterId: String): List<ChapterBackup> = 
        chapterBackupDao.getRecentBackups(chapterId, 50)
    
    suspend fun getChapterBackupById(backupId: String): ChapterBackup? = 
        chapterBackupDao.getBackupById(backupId)
    
    suspend fun insertChapterBackup(backup: ChapterBackup) = 
        chapterBackupDao.insertBackup(backup)
    
    suspend fun deleteChapterBackup(backup: ChapterBackup) = 
        chapterBackupDao.deleteBackup(backup)
    
    suspend fun deleteOldChapterBackups(chapterId: String, keepCount: Int) = 
        chapterBackupDao.deleteOldBackups(chapterId, keepCount)
    
    suspend fun deleteAllChapterBackups(chapterId: String) = 
        chapterBackupDao.deleteAllBackupsForChapter(chapterId)
    
    suspend fun getChapterBackupCount(chapterId: String): Int = 
        chapterBackupDao.getBackupCount(chapterId)
      // Scene operations
    fun getScenesByBook(bookId: String): Flow<List<Scene>> = sceneDao.getScenesByBook(bookId)
    
    suspend fun getScenesByBookId(bookId: String): List<Scene> = sceneDao.getScenesByBook(bookId).first()
    
    fun getScenesByChapter(chapterId: String): Flow<List<Scene>> = sceneDao.getScenesByChapter(chapterId)
    
    suspend fun getSceneById(id: String): Scene? = sceneDao.getSceneById(id)
    
    suspend fun insertScene(scene: Scene) = sceneDao.insertScene(scene)
    
    suspend fun updateScene(scene: Scene) = sceneDao.updateScene(scene)
      suspend fun deleteScene(scene: Scene) = sceneDao.deleteScene(scene)
    
    suspend fun getMaxOrderForBook(bookId: String): Int? = sceneDao.getMaxOrderForBook(bookId)
      // Character operations
    fun getCharactersByBook(bookId: String): Flow<List<Character>> = characterDao.getCharactersByBook(bookId)
    
    suspend fun getCharactersByBookId(bookId: String): List<Character> = characterDao.getCharactersByBook(bookId).first()
    
    suspend fun getCharacterById(id: String): Character? = characterDao.getCharacterById(id)
    
    fun getCharacterByIdFlow(id: String): Flow<Character?> = characterDao.getCharacterByIdFlow(id)
    
    suspend fun insertCharacter(character: Character) = characterDao.insertCharacter(character)
    
    suspend fun updateCharacter(character: Character) = characterDao.updateCharacter(character)
      suspend fun deleteCharacter(character: Character) = characterDao.deleteCharacter(character)
    
    // Character Relationship operations
    fun getRelationshipsByCharacter(characterId: String): Flow<List<CharacterRelationship>> = 
        characterRelationshipDao.getRelationshipsByCharacter(characterId)
    
    fun getAllRelationshipsForCharacter(characterId: String): Flow<List<CharacterRelationship>> = 
        characterRelationshipDao.getAllRelationshipsForCharacter(characterId)
    
    suspend fun getRelationshipBetween(characterId: String, relatedCharacterId: String): CharacterRelationship? = 
        characterRelationshipDao.getRelationshipBetween(characterId, relatedCharacterId)
    
    suspend fun insertRelationship(relationship: CharacterRelationship) = 
        characterRelationshipDao.insertRelationship(relationship)
    
    suspend fun updateRelationship(relationship: CharacterRelationship) = 
        characterRelationshipDao.updateRelationship(relationship)
    
    suspend fun deleteRelationship(relationship: CharacterRelationship) = 
        characterRelationshipDao.deleteRelationship(relationship)
    
    suspend fun deleteRelationshipBetween(characterId: String, relatedCharacterId: String) = 
        characterRelationshipDao.deleteRelationshipBetween(characterId, relatedCharacterId)
    
    suspend fun getRelationshipCount(characterId: String): Int = 
        characterRelationshipDao.getRelationshipCount(characterId)
      // Timeline operations
    fun getTimelineEventsByBook(bookId: String): Flow<List<TimelineEvent>> = timelineEventDao.getTimelineEventsByBook(bookId)
    
    suspend fun getTimelineEventsByBookId(bookId: String): List<TimelineEvent> = timelineEventDao.getTimelineEventsByBook(bookId).first()
    
    suspend fun getTimelineEventById(id: String): TimelineEvent? = timelineEventDao.getTimelineEventById(id)
    
    suspend fun insertTimelineEvent(event: TimelineEvent) = timelineEventDao.insertTimelineEvent(event)
    
    suspend fun updateTimelineEvent(event: TimelineEvent) = timelineEventDao.updateTimelineEvent(event)
    
    suspend fun deleteTimelineEvent(event: TimelineEvent) = timelineEventDao.deleteTimelineEvent(event)
    
    // Version control operations
    fun getVersionsByBook(bookId: String): Flow<List<ProjectVersion>> = projectVersionDao.getVersionsByBook(bookId)
    
    suspend fun getLatestVersion(bookId: String): ProjectVersion? = projectVersionDao.getLatestVersion(bookId)
    
    suspend fun insertVersion(version: ProjectVersion) = projectVersionDao.insertVersion(version)
    
    suspend fun deleteVersion(version: ProjectVersion) = projectVersionDao.deleteVersion(version)
    
    suspend fun cleanupOldAutoSaves(bookId: String, keepCount: Int = 10) = projectVersionDao.cleanupOldAutoSaves(bookId, keepCount)
}

