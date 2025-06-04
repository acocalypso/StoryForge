package de.astronarren.storyforge.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import de.astronarren.storyforge.data.database.converters.ListConverters
import de.astronarren.storyforge.data.database.dao.*
import de.astronarren.storyforge.data.database.entities.*
import de.astronarren.storyforge.data.database.migrations.DatabaseMigrations

@Database(
    entities = [
        Book::class,
        Chapter::class,
        ChapterBackup::class,
        Scene::class,
        Character::class,
        CharacterRelationship::class,
        TimelineEvent::class,
        ProjectVersion::class    ],    version = 5,
    exportSchema = true
)
@TypeConverters(ListConverters::class)
abstract class StoryForgeDatabase : RoomDatabase() {
      abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun chapterBackupDao(): ChapterBackupDao
    abstract fun sceneDao(): SceneDao
    abstract fun characterDao(): CharacterDao
    abstract fun characterRelationshipDao(): CharacterRelationshipDao
    abstract fun timelineEventDao(): TimelineEventDao
    abstract fun projectVersionDao(): ProjectVersionDao
    
    companion object {
        const val DATABASE_NAME = "storyforge_database"
        
        @Volatile
        private var INSTANCE: StoryForgeDatabase? = null
        
        fun getDatabase(context: Context): StoryForgeDatabase {            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StoryForgeDatabase::class.java,
                    DATABASE_NAME
                )                .addMigrations(
                    DatabaseMigrations.MIGRATION_1_2,
                    DatabaseMigrations.MIGRATION_2_3,
                    DatabaseMigrations.MIGRATION_3_4,
                    DatabaseMigrations.MIGRATION_4_5
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

