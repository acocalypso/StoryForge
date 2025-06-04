package de.astronarren.storyforge.data.database.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    
    /**
     * Migration from version 1 to 2
     * Initial setup if needed for legacy compatibility
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add any initial schema changes if needed
            // This handles legacy installations
        }
    }
    
    /**
     * Migration from version 2 to 3
     * Adding support for enhanced book metadata
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add additional book fields if they were introduced in v3
            // Preserves all existing book data
        }
    }
    
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add content field to chapters table
            database.execSQL("ALTER TABLE chapters ADD COLUMN content TEXT NOT NULL DEFAULT ''")
        }
    }
    
    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create chapter_backups table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS chapter_backups (
                    id TEXT NOT NULL PRIMARY KEY,
                    chapterId TEXT NOT NULL,
                    backupType TEXT NOT NULL,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    wordCount INTEGER NOT NULL,
                    createdAt INTEGER NOT NULL,
                    description TEXT
                )
            """.trimIndent())
            
            // Create index for faster queries by chapterId
            database.execSQL("CREATE INDEX IF NOT EXISTS index_chapter_backups_chapterId ON chapter_backups(chapterId)")
        }
    }
    
    /**
     * Migration from version 5 to 6
     * Future-proofing for upcoming features
     * This migration preserves ALL existing data
     */
    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Future migration placeholder
            // When we need to update to version 6, implement the actual migration here
            // This ensures we have a migration path ready
        }
    }
    
    /**
     * Comprehensive migration strategy that preserves user data
     * Use this as a template for future migrations
     */
    fun createSafeMigration(fromVersion: Int, toVersion: Int, migrationLogic: (SupportSQLiteDatabase) -> Unit): Migration {
        return object : Migration(fromVersion, toVersion) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // Begin transaction for atomic migration
                    database.beginTransaction()
                    
                    // Execute the migration logic
                    migrationLogic(database)
                    
                    // Commit the transaction
                    database.setTransactionSuccessful()
                } catch (e: Exception) {
                    // Log the error and let Room handle the fallback
                    // This prevents silent data loss
                    throw IllegalStateException("Migration from $fromVersion to $toVersion failed: ${e.message}", e)
                } finally {
                    database.endTransaction()
                }
            }
        }
    }
}
