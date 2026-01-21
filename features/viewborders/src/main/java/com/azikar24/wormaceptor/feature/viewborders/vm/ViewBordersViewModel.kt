/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewborders.vm

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.ViewBordersEngine
import com.azikar24.wormaceptor.domain.entities.ViewBordersConfig
import com.azikar24.wormaceptor.feature.viewborders.data.ViewBordersDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the View Borders feature.
 *
 * Manages the configuration state and coordinates between the UI,
 * data store (persistence), and the rendering engine.
 */
class ViewBordersViewModel(
    private val dataStore: ViewBordersDataStore,
    private val engine: ViewBordersEngine,
) : ViewModel() {

    /**
     * Current View Borders configuration, persisted via DataStore.
     */
    val config: StateFlow<ViewBordersConfig> = dataStore.observeConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ViewBordersConfig.DEFAULT,
        )

    /**
     * Whether the view borders overlay is currently enabled.
     */
    val isEnabled: StateFlow<Boolean> = engine.isEnabled

    /**
     * Toggles the enabled state of view borders.
     *
     * @param activity The activity to attach/detach the overlay from
     */
    fun toggleEnabled(activity: Activity) {
        viewModelScope.launch {
            val currentConfig = config.value
            val newEnabled = !currentConfig.enabled

            if (newEnabled) {
                engine.enable(activity)
            } else {
                engine.disable()
            }

            val newConfig = currentConfig.copy(enabled = newEnabled)
            dataStore.saveConfig(newConfig)
            engine.updateConfig(newConfig)
        }
    }

    /**
     * Sets the enabled state of view borders.
     *
     * @param enabled Whether to enable or disable
     * @param activity The activity to attach/detach the overlay from
     */
    fun setEnabled(enabled: Boolean, activity: Activity) {
        viewModelScope.launch {
            if (enabled) {
                engine.enable(activity)
            } else {
                engine.disable()
            }

            val newConfig = config.value.copy(enabled = enabled)
            dataStore.saveConfig(newConfig)
            engine.updateConfig(newConfig)
        }
    }

    /**
     * Updates the border width.
     *
     * @param width Border width in dp (1-5)
     */
    fun setBorderWidth(width: Int) {
        viewModelScope.launch {
            val clampedWidth = width.coerceIn(
                ViewBordersConfig.MIN_BORDER_WIDTH,
                ViewBordersConfig.MAX_BORDER_WIDTH,
            )
            val newConfig = config.value.copy(borderWidth = clampedWidth)
            dataStore.saveConfig(newConfig)
            engine.updateConfig(newConfig)
        }
    }

    /**
     * Updates the margin color.
     *
     * @param color Color in ARGB Long format
     */
    fun setMarginColor(color: Long) {
        viewModelScope.launch {
            val newConfig = config.value.copy(marginColor = color)
            dataStore.saveConfig(newConfig)
            engine.updateConfig(newConfig)
        }
    }

    /**
     * Updates the padding color.
     *
     * @param color Color in ARGB Long format
     */
    fun setPaddingColor(color: Long) {
        viewModelScope.launch {
            val newConfig = config.value.copy(paddingColor = color)
            dataStore.saveConfig(newConfig)
            engine.updateConfig(newConfig)
        }
    }

    /**
     * Updates the content color.
     *
     * @param color Color in ARGB Long format
     */
    fun setContentColor(color: Long) {
        viewModelScope.launch {
            val newConfig = config.value.copy(contentColor = color)
            dataStore.saveConfig(newConfig)
            engine.updateConfig(newConfig)
        }
    }

    /**
     * Toggles whether to show view dimensions.
     */
    fun toggleShowDimensions() {
        viewModelScope.launch {
            val newConfig = config.value.copy(showDimensions = !config.value.showDimensions)
            dataStore.saveConfig(newConfig)
            engine.updateConfig(newConfig)
        }
    }

    /**
     * Sets whether to show view dimensions.
     *
     * @param show Whether to show dimensions
     */
    fun setShowDimensions(show: Boolean) {
        viewModelScope.launch {
            val newConfig = config.value.copy(showDimensions = show)
            dataStore.saveConfig(newConfig)
            engine.updateConfig(newConfig)
        }
    }

    /**
     * Resets all settings to default values.
     *
     * @param activity Activity reference for re-enabling if currently enabled
     */
    fun resetToDefaults(activity: Activity) {
        viewModelScope.launch {
            val wasEnabled = config.value.enabled

            // Disable first
            engine.disable()

            // Clear stored config
            dataStore.clear()

            // Re-enable if it was enabled before
            if (wasEnabled) {
                engine.enable(activity)
                engine.updateConfig(ViewBordersConfig.DEFAULT.copy(enabled = true))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Disable the overlay when ViewModel is cleared
        engine.disable()
    }
}
