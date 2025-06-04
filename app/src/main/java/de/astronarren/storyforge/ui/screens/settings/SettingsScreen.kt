package de.astronarren.storyforge.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.astronarren.storyforge.data.preferences.AppTheme
import de.astronarren.storyforge.data.preferences.DarkThemeVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Theme Settings Section
            ThemeSettingsSection(
                currentTheme = uiState.themePreferences.appTheme,
                currentDarkVariant = uiState.themePreferences.darkThemeVariant,
                useDynamicColors = uiState.themePreferences.useDynamicColors,
                onThemeChanged = viewModel::setAppTheme,
                onDarkVariantChanged = viewModel::setDarkThemeVariant,
                onDynamicColorsChanged = viewModel::setUseDynamicColors
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsSection(
    currentTheme: AppTheme,
    currentDarkVariant: DarkThemeVariant,
    useDynamicColors: Boolean,
    onThemeChanged: (AppTheme) -> Unit,
    onDarkVariantChanged: (DarkThemeVariant) -> Unit,
    onDynamicColorsChanged: (Boolean) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Theme Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // App Theme Selection
            Text(
                "App Theme",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                AppTheme.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentTheme == theme,
                                onClick = { onThemeChanged(theme) },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeChanged(theme) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = getThemeDisplayName(theme),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = getThemeDescription(theme),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
              // Dark Theme Variant Selection (only shown for system default and dark themes)
            if (currentTheme == AppTheme.SYSTEM_DEFAULT || currentTheme == AppTheme.DARK) {
                HorizontalDivider()
                
                Text(
                    "Dark Theme Variant",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Column(
                    modifier = Modifier.selectableGroup()
                ) {
                    DarkThemeVariant.values().forEach { variant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentDarkVariant == variant,
                                    onClick = { onDarkVariantChanged(variant) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentDarkVariant == variant,
                                onClick = { onDarkVariantChanged(variant) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = getDarkVariantDisplayName(variant),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = getDarkVariantDescription(variant),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
              // Dynamic Colors Toggle (Android 12+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                HorizontalDivider()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Dynamic Colors",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Use colors from your wallpaper (Android 12+)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = useDynamicColors,
                        onCheckedChange = onDynamicColorsChanged
                    )
                }
            }
        }
    }
}

private fun getThemeDisplayName(theme: AppTheme): String = when (theme) {
    AppTheme.SYSTEM_DEFAULT -> "System Default"
    AppTheme.LIGHT -> "Light"
    AppTheme.DARK -> "Dark"
    AppTheme.AMOLED -> "AMOLED"
    AppTheme.HIGH_CONTRAST -> "High Contrast"
}

private fun getThemeDescription(theme: AppTheme): String = when (theme) {
    AppTheme.SYSTEM_DEFAULT -> "Follow system settings"
    AppTheme.LIGHT -> "Always use light theme"
    AppTheme.DARK -> "Always use dark theme"
    AppTheme.AMOLED -> "Pure black for OLED displays"
    AppTheme.HIGH_CONTRAST -> "High contrast for accessibility"
}

private fun getDarkVariantDisplayName(variant: DarkThemeVariant): String = when (variant) {
    DarkThemeVariant.STANDARD -> "Standard"
    DarkThemeVariant.AMOLED -> "AMOLED"
    DarkThemeVariant.HIGH_CONTRAST -> "High Contrast"
}

private fun getDarkVariantDescription(variant: DarkThemeVariant): String = when (variant) {
    DarkThemeVariant.STANDARD -> "Default night writing palette"
    DarkThemeVariant.AMOLED -> "True black for battery savings"
    DarkThemeVariant.HIGH_CONTRAST -> "Enhanced contrast for accessibility"
}

