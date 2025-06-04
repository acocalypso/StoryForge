package de.astronarren.storyforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import de.astronarren.storyforge.data.preferences.ThemePreferencesManager
import de.astronarren.storyforge.navigation.StoryForgeNavigation
import de.astronarren.storyforge.ui.theme.StoryForgeTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var themePreferencesManager: ThemePreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreferences by themePreferencesManager.themePreferencesFlow.collectAsState(
                initial = de.astronarren.storyforge.data.preferences.ThemePreferences()
            )
            
            StoryForgeTheme(
                appTheme = themePreferences.appTheme,
                darkThemeVariant = themePreferences.darkThemeVariant,
                dynamicColor = themePreferences.useDynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    StoryForgeNavigation(navController = navController)
                }
            }
        }
    }
}
