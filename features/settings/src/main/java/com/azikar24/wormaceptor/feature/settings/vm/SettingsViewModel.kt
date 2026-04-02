package com.azikar24.wormaceptor.feature.settings.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.domain.contracts.FeatureConfig
import com.azikar24.wormaceptor.domain.contracts.FeatureConfigRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing feature toggle settings.
 */
class SettingsViewModel(
    private val repository: FeatureConfigRepository,
) : BaseViewModel<SettingsViewState, SettingsViewEffect, SettingsViewEvent>(
    SettingsViewState(),
) {

    init {
        observeConfig()
    }

    override fun handleEvent(event: SettingsViewEvent) {
        when (event) {
            is SettingsViewEvent.ToggleNetworkTab -> toggleConfig { copy(showNetworkTab = !showNetworkTab) }
            is SettingsViewEvent.ToggleCrashesTab -> toggleConfig { copy(showCrashesTab = !showCrashesTab) }
            is SettingsViewEvent.TogglePreferences -> toggleConfig { copy(showPreferences = !showPreferences) }
            is SettingsViewEvent.ToggleConsoleLogs -> toggleConfig { copy(showConsoleLogs = !showConsoleLogs) }
            is SettingsViewEvent.ToggleDeviceInfo -> toggleConfig { copy(showDeviceInfo = !showDeviceInfo) }
            is SettingsViewEvent.ToggleSqliteBrowser -> toggleConfig { copy(showSqliteBrowser = !showSqliteBrowser) }
            is SettingsViewEvent.ToggleFileBrowser -> toggleConfig { copy(showFileBrowser = !showFileBrowser) }
            is SettingsViewEvent.ResetToDefaults -> handleResetToDefaults()
        }
    }

    private fun observeConfig() {
        viewModelScope.launch {
            repository.observeConfig().collect { config ->
                updateState { copy(featureConfig = config) }
            }
        }
    }

    private fun toggleConfig(transform: FeatureConfig.() -> FeatureConfig) {
        viewModelScope.launch {
            val newConfig = uiState.value.featureConfig.transform()
            repository.updateConfig(newConfig)
        }
    }

    private fun handleResetToDefaults() {
        viewModelScope.launch {
            repository.resetToDefaults()
        }
    }
}
