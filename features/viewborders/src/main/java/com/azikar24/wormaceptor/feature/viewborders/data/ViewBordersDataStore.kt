/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewborders.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.azikar24.wormaceptor.domain.entities.ViewBordersConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore-based storage for View Borders configuration.
 * Provides persistent storage for user preferences.
 */
class ViewBordersDataStore(private val context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "wormaceptor_view_borders_config",
    )

    /**
     * Observes the current View Borders configuration from DataStore.
     * Returns default config if no preferences are stored yet.
     */
    fun observeConfig(): Flow<ViewBordersConfig> {
        return context.dataStore.data.map { preferences ->
            ViewBordersConfig(
                enabled = preferences[Keys.ENABLED] ?: ViewBordersConfig.DEFAULT.enabled,
                borderWidth = preferences[Keys.BORDER_WIDTH] ?: ViewBordersConfig.DEFAULT.borderWidth,
                marginColor = preferences[Keys.MARGIN_COLOR] ?: ViewBordersConfig.DEFAULT.marginColor,
                paddingColor = preferences[Keys.PADDING_COLOR] ?: ViewBordersConfig.DEFAULT.paddingColor,
                contentColor = preferences[Keys.CONTENT_COLOR] ?: ViewBordersConfig.DEFAULT.contentColor,
                showDimensions = preferences[Keys.SHOW_DIMENSIONS] ?: ViewBordersConfig.DEFAULT.showDimensions,
            )
        }
    }

    /**
     * Saves the View Borders configuration to DataStore.
     */
    suspend fun saveConfig(config: ViewBordersConfig) {
        context.dataStore.edit { preferences ->
            preferences[Keys.ENABLED] = config.enabled
            preferences[Keys.BORDER_WIDTH] = config.borderWidth
            preferences[Keys.MARGIN_COLOR] = config.marginColor
            preferences[Keys.PADDING_COLOR] = config.paddingColor
            preferences[Keys.CONTENT_COLOR] = config.contentColor
            preferences[Keys.SHOW_DIMENSIONS] = config.showDimensions
        }
    }

    /**
     * Clears all View Borders configuration, reverting to defaults.
     */
    suspend fun clear() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private object Keys {
        val ENABLED = booleanPreferencesKey("view_borders_enabled")
        val BORDER_WIDTH = intPreferencesKey("view_borders_width")
        val MARGIN_COLOR = longPreferencesKey("view_borders_margin_color")
        val PADDING_COLOR = longPreferencesKey("view_borders_padding_color")
        val CONTENT_COLOR = longPreferencesKey("view_borders_content_color")
        val SHOW_DIMENSIONS = booleanPreferencesKey("view_borders_show_dimensions")
    }
}
