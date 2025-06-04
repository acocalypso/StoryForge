package de.astronarren.storyforge.data.preferences

import kotlinx.serialization.Serializable

/**
 * Enum representing different theme options available in the app
 */
enum class AppTheme {
    SYSTEM_DEFAULT,  // Follow system dark/light mode
    LIGHT,           // Always light
    DARK,            // Always dark (default night writing palette)
    AMOLED,          // True black for OLED displays  
    HIGH_CONTRAST    // High contrast for accessibility
}

/**
 * Enum representing dark theme variants
 */
enum class DarkThemeVariant {
    STANDARD,        // Default night writing palette
    AMOLED,          // True black for OLED battery savings
    HIGH_CONTRAST    // High contrast for accessibility
}

/**
 * Data class representing user theme preferences
 */
@Serializable
data class ThemePreferences(
    val appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    val darkThemeVariant: DarkThemeVariant = DarkThemeVariant.STANDARD,
    val useDynamicColors: Boolean = false // Keep disabled to preserve our custom themes
)

