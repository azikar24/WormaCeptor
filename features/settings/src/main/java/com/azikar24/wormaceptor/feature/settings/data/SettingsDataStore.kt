/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.settings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.azikar24.wormaceptor.domain.contracts.FeatureConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// File-level DataStore delegate to ensure singleton behavior
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "wormaceptor_feature_config",
)

/**
 * DataStore-based storage for feature configuration.
 * Provides type-safe access to feature toggle preferences.
 */
class SettingsDataStore(private val context: Context) {

    /**
     * Observes the current feature configuration from DataStore.
     * Returns default config if no preferences are stored yet.
     */
    fun observeFeatureConfig(): Flow<FeatureConfig> {
        return context.settingsDataStore.data.map { preferences ->
            FeatureConfig(
                showNetworkTab = preferences[Keys.SHOW_NETWORK_TAB] ?: FeatureConfig.DEFAULT.showNetworkTab,
                showCrashesTab = preferences[Keys.SHOW_CRASHES_TAB] ?: FeatureConfig.DEFAULT.showCrashesTab,
                showPreferences = preferences[Keys.SHOW_PREFERENCES] ?: FeatureConfig.DEFAULT.showPreferences,
                showConsoleLogs = preferences[Keys.SHOW_CONSOLE_LOGS] ?: FeatureConfig.DEFAULT.showConsoleLogs,
                showDeviceInfo = preferences[Keys.SHOW_DEVICE_INFO] ?: FeatureConfig.DEFAULT.showDeviceInfo,
                showSqliteBrowser = preferences[Keys.SHOW_SQLITE_BROWSER] ?: FeatureConfig.DEFAULT.showSqliteBrowser,
                showFileBrowser = preferences[Keys.SHOW_FILE_BROWSER] ?: FeatureConfig.DEFAULT.showFileBrowser,
            )
        }
    }

    /**
     * Saves the feature configuration to DataStore.
     */
    suspend fun saveFeatureConfig(config: FeatureConfig) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.SHOW_NETWORK_TAB] = config.showNetworkTab
            preferences[Keys.SHOW_CRASHES_TAB] = config.showCrashesTab
            preferences[Keys.SHOW_PREFERENCES] = config.showPreferences
            preferences[Keys.SHOW_CONSOLE_LOGS] = config.showConsoleLogs
            preferences[Keys.SHOW_DEVICE_INFO] = config.showDeviceInfo
            preferences[Keys.SHOW_SQLITE_BROWSER] = config.showSqliteBrowser
            preferences[Keys.SHOW_FILE_BROWSER] = config.showFileBrowser
        }
    }

    /**
     * Clears all feature configuration, reverting to defaults.
     */
    suspend fun clear() {
        context.settingsDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private object Keys {
        val SHOW_NETWORK_TAB = booleanPreferencesKey("show_network_tab")
        val SHOW_CRASHES_TAB = booleanPreferencesKey("show_crashes_tab")
        val SHOW_PREFERENCES = booleanPreferencesKey("show_preferences")
        val SHOW_CONSOLE_LOGS = booleanPreferencesKey("show_console_logs")
        val SHOW_DEVICE_INFO = booleanPreferencesKey("show_device_info")
        val SHOW_SQLITE_BROWSER = booleanPreferencesKey("show_sqlite_browser")
        val SHOW_FILE_BROWSER = booleanPreferencesKey("show_file_browser")
    }
}
