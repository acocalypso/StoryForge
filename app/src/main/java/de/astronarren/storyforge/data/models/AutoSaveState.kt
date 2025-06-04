package de.astronarren.storyforge.data.models

/**
 * Represents the current autosave state for UI feedback
 */
enum class AutoSaveState {
    IDLE,           // No pending changes
    PENDING,        // Changes pending, will save soon
    SAVING,         // Currently saving
    SAVED,          // Recently saved successfully
    ERROR           // Error occurred during save
}

/**
 * AutoSave configuration settings
 */
data class AutoSaveConfig(
    val isEnabled: Boolean = true,
    val contentDebounceMs: Long = 2000L,
    val titleDebounceMs: Long = 1000L,
    val maxBackups: Int = 10,
    val backupIntervalMs: Long = 300000L // 5 minutes
)
