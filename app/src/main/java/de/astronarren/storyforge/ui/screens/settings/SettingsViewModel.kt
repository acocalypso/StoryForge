package de.astronarren.storyforge.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.data.preferences.AppTheme
import de.astronarren.storyforge.data.preferences.DarkThemeVariant
import de.astronarren.storyforge.data.preferences.ThemePreferences
import de.astronarren.storyforge.data.preferences.ThemePreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themePreferences: ThemePreferences = ThemePreferences(),
    val isLoading: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferencesManager: ThemePreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        observeThemePreferences()
    }
    
    private fun observeThemePreferences() {
        viewModelScope.launch {
            themePreferencesManager.themePreferencesFlow.collect { preferences ->
                _uiState.value = _uiState.value.copy(
                    themePreferences = preferences
                )
            }
        }
    }
    
    fun setAppTheme(theme: AppTheme) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                themePreferencesManager.setAppTheme(theme)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun setDarkThemeVariant(variant: DarkThemeVariant) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                themePreferencesManager.setDarkThemeVariant(variant)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
    
    fun setUseDynamicColors(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                themePreferencesManager.setUseDynamicColors(enabled)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

