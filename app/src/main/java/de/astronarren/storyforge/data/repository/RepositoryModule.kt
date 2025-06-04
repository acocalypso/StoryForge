package de.astronarren.storyforge.data.repository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.astronarren.storyforge.data.database.dao.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {    @Provides
    @Singleton
    fun provideStoryForgeRepository(
        bookDao: BookDao,
        chapterDao: ChapterDao,
        chapterBackupDao: ChapterBackupDao,
        sceneDao: SceneDao,
        characterDao: CharacterDao,
        characterRelationshipDao: CharacterRelationshipDao,
        timelineEventDao: TimelineEventDao,
        projectVersionDao: ProjectVersionDao
    ): StoryForgeRepository {
        return StoryForgeRepository(
            bookDao = bookDao,
            chapterDao = chapterDao,
            chapterBackupDao = chapterBackupDao,
            sceneDao = sceneDao,
            characterDao = characterDao,
            characterRelationshipDao = characterRelationshipDao,
            timelineEventDao = timelineEventDao,
            projectVersionDao = projectVersionDao
        )
    }
}

