package de.astronarren.storyforge.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore for persisting user preferences
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

@Singleton
class ThemePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val APP_THEME_KEY = stringPreferencesKey("app_theme")
        private val DARK_THEME_VARIANT_KEY = stringPreferencesKey("dark_theme_variant")
        private val USE_DYNAMIC_COLORS_KEY = booleanPreferencesKey("use_dynamic_colors")
    }
    
    /**
     * Flow of current theme preferences
     */
    val themePreferencesFlow: Flow<ThemePreferences> = context.dataStore.data.map { preferences ->
        ThemePreferences(
            appTheme = try {
                AppTheme.valueOf(preferences[APP_THEME_KEY] ?: AppTheme.SYSTEM_DEFAULT.name)
            } catch (e: IllegalArgumentException) {
                AppTheme.SYSTEM_DEFAULT
            },
            darkThemeVariant = try {
                DarkThemeVariant.valueOf(preferences[DARK_THEME_VARIANT_KEY] ?: DarkThemeVariant.STANDARD.name)
            } catch (e: IllegalArgumentException) {
                DarkThemeVariant.STANDARD
            },
            useDynamicColors = preferences[USE_DYNAMIC_COLORS_KEY] ?: false
        )
    }
    
    /**
     * Update app theme preference
     */
    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME_KEY] = theme.name
        }
    }
    
    /**
     * Update dark theme variant preference
     */
    suspend fun setDarkThemeVariant(variant: DarkThemeVariant) {
        context.dataStore.edit { preferences ->
            preferences[DARK_THEME_VARIANT_KEY] = variant.name
        }
    }
    
    /**
     * Update dynamic colors preference
     */
    suspend fun setUseDynamicColors(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_DYNAMIC_COLORS_KEY] = enabled
        }
    }
    
    /**
     * Update all theme preferences at once
     */
    suspend fun updateThemePreferences(themePreferences: ThemePreferences) {
        context.dataStore.edit { preferences ->
            preferences[APP_THEME_KEY] = themePreferences.appTheme.name
            preferences[DARK_THEME_VARIANT_KEY] = themePreferences.darkThemeVariant.name
            preferences[USE_DYNAMIC_COLORS_KEY] = themePreferences.useDynamicColors
        }
    }
}

