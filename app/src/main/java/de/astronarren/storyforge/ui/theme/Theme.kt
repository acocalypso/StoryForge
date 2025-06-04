package de.astronarren.storyforge.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import de.astronarren.storyforge.data.preferences.AppTheme
import de.astronarren.storyforge.data.preferences.DarkThemeVariant

private val StoryForgeDarkColorScheme = darkColorScheme(
    primary = MidnightInk80,
    onPrimary = Color.White,
    primaryContainer = MidnightInk40,
    onPrimaryContainer = MidnightInk80,
    secondary = NightPaper80,
    onSecondary = Color.White,
    secondaryContainer = NightPaper40,
    onSecondaryContainer = NightPaper80,
    tertiary = MoonlightAccent80,
    onTertiary = Color.White,
    tertiaryContainer = MoonlightAccent40,
    onTertiaryContainer = MoonlightAccent80,
    error = ErrorRed,
    errorContainer = Color(0xFF93000A),
    onError = Color.White,
    onErrorContainer = Color(0xFFFFDAD6),
    background = DarkSurface,
    onBackground = Color(0xFFE6E1E5),
    surface = DarkSurface,
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

// AMOLED theme for OLED displays - true black for battery savings
private val StoryForgeAmoledColorScheme = darkColorScheme(
    primary = AmoledAccent80,
    onPrimary = AmoledBlack,
    primaryContainer = AmoledAccent40,
    onPrimaryContainer = AmoledAccent80,
    secondary = AmoledAccent80,
    onSecondary = AmoledBlack,
    secondaryContainer = AmoledGrey20,
    onSecondaryContainer = Color.White,
    tertiary = AmoledAccent80,
    onTertiary = AmoledBlack,
    tertiaryContainer = AmoledGrey20,
    onTertiaryContainer = Color.White,
    error = ErrorRed,
    errorContainer = Color(0xFF93000A),
    onError = Color.White,
    onErrorContainer = Color(0xFFFFDAD6),
    background = AmoledBlack,
    onBackground = Color.White,
    surface = AmoledBlack,
    onSurface = Color.White,
    surfaceVariant = AmoledGrey10,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = AmoledGrey20,
    outlineVariant = AmoledGrey10
)

// High contrast theme for accessibility
private val StoryForgeHighContrastColorScheme = darkColorScheme(
    primary = ContrastYellow,
    onPrimary = ContrastBlack,
    primaryContainer = ContrastBlue,
    onPrimaryContainer = ContrastWhite,
    secondary = ContrastWhite,
    onSecondary = ContrastBlack,
    secondaryContainer = ContrastGrey10,
    onSecondaryContainer = ContrastWhite,
    tertiary = ContrastYellow,
    onTertiary = ContrastBlack,
    tertiaryContainer = ContrastBlue,
    onTertiaryContainer = ContrastWhite,
    error = ContrastRed,
    errorContainer = Color(0xFF330000),
    onError = ContrastWhite,
    onErrorContainer = ContrastWhite,
    background = ContrastBlack,
    onBackground = ContrastWhite,
    surface = ContrastBlack,
    onSurface = ContrastWhite,
    surfaceVariant = ContrastGrey10,
    onSurfaceVariant = ContrastGrey90,
    outline = ContrastWhite,
    outlineVariant = ContrastGrey10
)

private val StoryForgeLightColorScheme = lightColorScheme(
    primary = WritingInk40,
    onPrimary = Color.White,
    primaryContainer = WritingInk80,
    onPrimaryContainer = WritingInk40,
    secondary = ParchmentGrey40,
    onSecondary = Color.White,
    secondaryContainer = ParchmentGrey80,
    onSecondaryContainer = ParchmentGrey40,
    tertiary = QuillGold40,
    onTertiary = Color.White,
    tertiaryContainer = QuillGold80,
    onTertiaryContainer = QuillGold40,
    error = ErrorRed,
    errorContainer = Color(0xFFFFDAD6),
    onError = Color.White,
    onErrorContainer = Color(0xFF410E0B),
    background = LightSurface,
    onBackground = Color(0xFF1C1B1F),
    surface = LightSurface,
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

@Composable
fun StoryForgeTheme(
    appTheme: AppTheme = AppTheme.SYSTEM_DEFAULT,
    darkThemeVariant: DarkThemeVariant = DarkThemeVariant.STANDARD,
    dynamicColor: Boolean = false, // Disabled to use our custom theme
    content: @Composable () -> Unit
) {
    val systemDarkTheme = isSystemInDarkTheme()
    
    // Determine if we should use dark theme
    val useDarkTheme = when (appTheme) {
        AppTheme.SYSTEM_DEFAULT -> systemDarkTheme
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.AMOLED -> true
        AppTheme.HIGH_CONTRAST -> true
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        !useDarkTheme -> StoryForgeLightColorScheme
        appTheme == AppTheme.AMOLED -> StoryForgeAmoledColorScheme
        appTheme == AppTheme.HIGH_CONTRAST -> StoryForgeHighContrastColorScheme
        darkThemeVariant == DarkThemeVariant.AMOLED -> StoryForgeAmoledColorScheme
        darkThemeVariant == DarkThemeVariant.HIGH_CONTRAST -> StoryForgeHighContrastColorScheme
        else -> StoryForgeDarkColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Use WindowCompat for modern status bar handling
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
