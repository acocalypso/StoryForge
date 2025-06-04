package de.astronarren.storyforge.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.astronarren.storyforge.data.database.StoryForgeDatabase
import de.astronarren.storyforge.data.database.dao.*
import de.astronarren.storyforge.data.database.migrations.DatabaseMigrations
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
      @Provides
    @Singleton
    fun provideStoryForgeDatabase(@ApplicationContext context: Context): StoryForgeDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            StoryForgeDatabase::class.java,
            StoryForgeDatabase.DATABASE_NAME
        )        .addMigrations(
            DatabaseMigrations.MIGRATION_1_2,
            DatabaseMigrations.MIGRATION_2_3,
            DatabaseMigrations.MIGRATION_3_4,
            DatabaseMigrations.MIGRATION_4_5
        )
        // CRITICAL: Never add .fallbackToDestructiveMigration() here!
        // It would delete ALL user data during schema changes.
        // Always use proper migrations to preserve user books and progress.
        .build()
    }
    
    @Provides
    fun provideBookDao(database: StoryForgeDatabase): BookDao = database.bookDao()
    
    @Provides
    fun provideChapterDao(database: StoryForgeDatabase): ChapterDao = database.chapterDao()
    
    @Provides
    fun provideChapterBackupDao(database: StoryForgeDatabase): ChapterBackupDao = database.chapterBackupDao()
    
    @Provides
    fun provideSceneDao(database: StoryForgeDatabase): SceneDao = database.sceneDao()
      @Provides
    fun provideCharacterDao(database: StoryForgeDatabase): CharacterDao = database.characterDao()
    
    @Provides
    fun provideCharacterRelationshipDao(database: StoryForgeDatabase): CharacterRelationshipDao = database.characterRelationshipDao()
    
    @Provides
    fun provideTimelineEventDao(database: StoryForgeDatabase): TimelineEventDao = database.timelineEventDao()
    
    @Provides
    fun provideProjectVersionDao(database: StoryForgeDatabase): ProjectVersionDao = database.projectVersionDao()
}

