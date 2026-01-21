/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.settings.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.domain.contracts.FeatureConfig
import com.azikar24.wormaceptor.domain.contracts.FeatureConfigRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for managing feature toggle settings.
 */
class SettingsViewModel(
    private val repository: FeatureConfigRepository,
) : ViewModel() {

    val featureConfig: StateFlow<FeatureConfig> = repository.observeConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FeatureConfig.DEFAULT,
        )

    fun toggleNetworkTab() {
        updateConfig { copy(showNetworkTab = !showNetworkTab) }
    }

    fun toggleCrashesTab() {
        updateConfig { copy(showCrashesTab = !showCrashesTab) }
    }

    fun togglePreferences() {
        updateConfig { copy(showPreferences = !showPreferences) }
    }

    fun toggleConsoleLogs() {
        updateConfig { copy(showConsoleLogs = !showConsoleLogs) }
    }

    fun toggleDeviceInfo() {
        updateConfig { copy(showDeviceInfo = !showDeviceInfo) }
    }

    fun toggleSqliteBrowser() {
        updateConfig { copy(showSqliteBrowser = !showSqliteBrowser) }
    }

    fun toggleFileBrowser() {
        updateConfig { copy(showFileBrowser = !showFileBrowser) }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
        }
    }

    private fun updateConfig(transform: FeatureConfig.() -> FeatureConfig) {
        viewModelScope.launch {
            val newConfig = featureConfig.value.transform()
            repository.updateConfig(newConfig)
        }
    }
}
