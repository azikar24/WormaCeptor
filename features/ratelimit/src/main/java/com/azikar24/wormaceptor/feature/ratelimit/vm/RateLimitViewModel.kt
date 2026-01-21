/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.ratelimit.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.RateLimitEngine
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig.NetworkPreset
import com.azikar24.wormaceptor.domain.entities.ThrottleStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Network Rate Limiting screen.
 *
 * Provides access to rate limiting configuration and statistics.
 * Allows users to configure network throttling presets or custom values.
 */
class RateLimitViewModel(
    private val engine: RateLimitEngine,
) : ViewModel() {

    // Current configuration
    val config: StateFlow<RateLimitConfig> = engine.config
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RateLimitConfig.default(),
        )

    // Throttle statistics
    val stats: StateFlow<ThrottleStats> = engine.stats
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ThrottleStats.empty(),
        )

    // Currently selected preset (null if custom)
    private val _selectedPreset = MutableStateFlow<NetworkPreset?>(null)
    val selectedPreset: StateFlow<NetworkPreset?> = _selectedPreset.asStateFlow()

    /**
     * Toggles rate limiting on/off.
     */
    fun toggleEnabled() {
        val current = config.value
        if (current.enabled) {
            engine.disable()
        } else {
            engine.enable()
        }
    }

    /**
     * Selects a network preset and applies its configuration.
     *
     * @param preset The preset to apply, or null to clear preset
     */
    fun selectPreset(preset: NetworkPreset?) {
        _selectedPreset.value = preset
        if (preset != null) {
            engine.applyPreset(preset)
        }
    }

    /**
     * Sets the download speed limit.
     *
     * @param speedKbps Download speed in kilobits per second
     */
    fun setDownloadSpeed(speedKbps: Long) {
        val current = config.value
        engine.setConfig(
            current.copy(
                downloadSpeedKbps = speedKbps.coerceIn(
                    RateLimitEngine.MIN_SPEED_KBPS,
                    RateLimitEngine.MAX_SPEED_KBPS,
                ),
                preset = null,
            ),
        )
        _selectedPreset.value = null
    }

    /**
     * Sets the upload speed limit.
     *
     * @param speedKbps Upload speed in kilobits per second
     */
    fun setUploadSpeed(speedKbps: Long) {
        val current = config.value
        engine.setConfig(
            current.copy(
                uploadSpeedKbps = speedKbps.coerceIn(
                    RateLimitEngine.MIN_SPEED_KBPS,
                    RateLimitEngine.MAX_SPEED_KBPS,
                ),
                preset = null,
            ),
        )
        _selectedPreset.value = null
    }

    /**
     * Sets the latency injection value.
     *
     * @param latencyMs Latency in milliseconds
     */
    fun setLatency(latencyMs: Long) {
        val current = config.value
        engine.setConfig(
            current.copy(
                latencyMs = latencyMs.coerceIn(
                    RateLimitEngine.MIN_LATENCY_MS,
                    RateLimitEngine.MAX_LATENCY_MS,
                ),
                preset = null,
            ),
        )
        _selectedPreset.value = null
    }

    /**
     * Sets the packet loss percentage.
     *
     * @param percent Packet loss percentage (0-100)
     */
    fun setPacketLoss(percent: Float) {
        val current = config.value
        engine.setConfig(
            current.copy(
                packetLossPercent = percent.coerceIn(
                    RateLimitEngine.MIN_PACKET_LOSS,
                    RateLimitEngine.MAX_PACKET_LOSS,
                ),
                preset = null,
            ),
        )
        _selectedPreset.value = null
    }

    /**
     * Clears throttle statistics.
     */
    fun clearStats() {
        engine.clearStats()
    }

    /**
     * Resets configuration to defaults.
     */
    fun resetToDefaults() {
        engine.setConfig(RateLimitConfig.default())
        _selectedPreset.value = null
    }
}
