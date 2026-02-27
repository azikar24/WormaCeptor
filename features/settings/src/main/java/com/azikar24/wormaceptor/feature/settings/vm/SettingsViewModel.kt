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

    /** Observable stream of the current feature toggle configuration. */
    val featureConfig: StateFlow<FeatureConfig> = repository.observeConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FeatureConfig.DEFAULT,
        )

    /** Toggles visibility of the Network tab in the main screen. */
    fun toggleNetworkTab() {
        updateConfig { copy(showNetworkTab = !showNetworkTab) }
    }

    /** Toggles visibility of the Crashes tab in the main screen. */
    fun toggleCrashesTab() {
        updateConfig { copy(showCrashesTab = !showCrashesTab) }
    }

    /** Toggles visibility of the Preferences inspector tool. */
    fun togglePreferences() {
        updateConfig { copy(showPreferences = !showPreferences) }
    }

    /** Toggles visibility of the Console Logs tool. */
    fun toggleConsoleLogs() {
        updateConfig { copy(showConsoleLogs = !showConsoleLogs) }
    }

    /** Toggles visibility of the Device Info tool. */
    fun toggleDeviceInfo() {
        updateConfig { copy(showDeviceInfo = !showDeviceInfo) }
    }

    /** Toggles visibility of the SQLite Browser tool. */
    fun toggleSqliteBrowser() {
        updateConfig { copy(showSqliteBrowser = !showSqliteBrowser) }
    }

    /** Toggles visibility of the File Browser tool. */
    fun toggleFileBrowser() {
        updateConfig { copy(showFileBrowser = !showFileBrowser) }
    }

    /** Restores all feature toggles to their default values. */
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
