package de.astronarren.storyforge.ui.components.permissions

import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.astronarren.storyforge.utils.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoragePermissionViewModel @Inject constructor(
    private val permissionManager: PermissionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StoragePermissionUiState())
    val uiState: StateFlow<StoragePermissionUiState> = _uiState.asStateFlow()
    
    init {
        checkInitialPermissionState()
    }    private fun checkInitialPermissionState() {
        viewModelScope.launch {
            val hasPermissions = permissionManager.hasExportPermissions()
            val requiredPermissions = permissionManager.getRequiredStoragePermissions()
            
            Log.d("StoragePermission", "hasPermissions: $hasPermissions")
            Log.d("StoragePermission", "requiredPermissions: ${requiredPermissions.contentToString()}")
            Log.d("StoragePermission", "showPermissionScreen: ${!hasPermissions && requiredPermissions.isNotEmpty()}")
            
            _uiState.value = _uiState.value.copy(
                hasPermissions = hasPermissions,
                isLoading = false,
                showPermissionScreen = !hasPermissions && requiredPermissions.isNotEmpty()
            )
        }
    }
    
    fun onPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(
            hasPermissions = granted,
            showPermissionScreen = false,
            permissionCompleted = true
        )
    }
    
    fun onSkipPermission() {
        _uiState.value = _uiState.value.copy(
            showPermissionScreen = false,
            permissionCompleted = true,
            hasPermissions = false
        )
    }
    
    fun retryPermissionCheck() {
        _uiState.value = _uiState.value.copy(
            isLoading = true
        )
        checkInitialPermissionState()
    }
}

data class StoragePermissionUiState(
    val isLoading: Boolean = true,
    val hasPermissions: Boolean = false,
    val showPermissionScreen: Boolean = false,
    val permissionCompleted: Boolean = false
)
