/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.touchvisualization.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.TouchVisualizationEngine
import com.azikar24.wormaceptor.domain.entities.TouchPoint
import com.azikar24.wormaceptor.domain.entities.TouchVisualizationConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Touch Visualization settings screen.
 *
 * Provides access to touch visualization configuration and controls
 * for enabling/disabling the overlay and adjusting settings.
 */
class TouchVisualizationViewModel(
    private val engine: TouchVisualizationEngine,
) : ViewModel() {

    /**
     * Current touch visualization configuration.
     */
    val config: StateFlow<TouchVisualizationConfig> = engine.config

    /**
     * Whether the touch visualization overlay is currently enabled.
     */
    val isEnabled: StateFlow<Boolean> = engine.isRunning

    /**
     * Active touch points for preview visualization.
     */
    val activeTouches: StateFlow<ImmutableList<TouchPoint>> = engine.activeTouches
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    /**
     * Touch trail for preview visualization.
     */
    val touchTrail: StateFlow<ImmutableList<TouchPoint>> = engine.touchTrail
        .map { it.toImmutableList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    /**
     * Enables the touch visualization overlay.
     */
    fun enable() {
        engine.enable()
    }

    /**
     * Disables the touch visualization overlay.
     */
    fun disable() {
        engine.disable()
    }

    /**
     * Toggles the touch visualization overlay.
     */
    fun toggle() {
        if (isEnabled.value) {
            disable()
        } else {
            enable()
        }
    }

    /**
     * Updates the circle color.
     */
    fun setCircleColor(color: Long) {
        engine.updateConfig(config.value.copy(circleColor = color))
    }

    /**
     * Updates the circle size.
     */
    fun setCircleSize(size: Float) {
        val clampedSize = size.coerceIn(
            TouchVisualizationConfig.MIN_CIRCLE_SIZE,
            TouchVisualizationConfig.MAX_CIRCLE_SIZE,
        )
        engine.updateConfig(config.value.copy(circleSize = clampedSize))
    }

    /**
     * Toggles trail visualization.
     */
    fun setTrailEnabled(enabled: Boolean) {
        engine.updateConfig(config.value.copy(trailEnabled = enabled))
        if (!enabled) {
            engine.clearTrail()
        }
    }

    /**
     * Toggles coordinate display.
     */
    fun setShowCoordinates(show: Boolean) {
        engine.updateConfig(config.value.copy(showCoordinates = show))
    }

    /**
     * Resets configuration to defaults.
     */
    fun resetToDefaults() {
        engine.updateConfig(TouchVisualizationConfig.DEFAULT)
    }

    override fun onCleared() {
        super.onCleared()
        // Note: We don't disable the engine here as it might be used elsewhere
        // The engine lifecycle should be managed by the app
    }
}
