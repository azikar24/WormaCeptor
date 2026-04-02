package com.azikar24.wormaceptor.feature.ratelimit.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.RateLimitEngine
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * ViewModel for the Network Rate Limiting screen.
 *
 * Consolidates rate-limiting configuration and statistics from [RateLimitEngine]
 * into a single [RateLimitViewState] and exposes user actions via [RateLimitViewEvent].
 */
class RateLimitViewModel(
    private val engine: RateLimitEngine,
) : BaseViewModel<RateLimitViewState, RateLimitViewEffect, RateLimitViewEvent>(
    initialState = RateLimitViewState(),
) {

    init {
        combine(
            engine.config,
            engine.stats,
        ) { config, stats ->
            updateState {
                copy(
                    config = config,
                    stats = stats,
                    selectedPreset = config.preset,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleEvent(event: RateLimitViewEvent) {
        when (event) {
            is RateLimitViewEvent.ToggleEnabled -> handleToggleEnabled()
            is RateLimitViewEvent.SelectPreset -> handleSelectPreset(event.preset)
            is RateLimitViewEvent.SetDownloadSpeed -> handleSetDownloadSpeed(event.speedKbps)
            is RateLimitViewEvent.SetUploadSpeed -> handleSetUploadSpeed(event.speedKbps)
            is RateLimitViewEvent.SetLatency -> handleSetLatency(event.latencyMs)
            is RateLimitViewEvent.SetPacketLoss -> handleSetPacketLoss(event.percent)
            is RateLimitViewEvent.ClearStats -> engine.clearStats()
            is RateLimitViewEvent.ResetToDefaults -> handleResetToDefaults()
        }
    }

    private fun handleToggleEnabled() {
        val current = uiState.value.config
        if (current.enabled) {
            engine.disable()
        } else {
            engine.enable()
        }
    }

    private fun handleSelectPreset(preset: RateLimitConfig.NetworkPreset?) {
        if (preset != null) {
            engine.applyPreset(preset)
        } else {
            val current = uiState.value.config
            engine.setConfig(current.copy(preset = null))
        }
    }

    private fun handleSetDownloadSpeed(speedKbps: Long) {
        val current = uiState.value.config
        engine.setConfig(
            current.copy(
                downloadSpeedKbps = speedKbps.coerceIn(
                    RateLimitEngine.MIN_SPEED_KBPS,
                    RateLimitEngine.MAX_SPEED_KBPS,
                ),
                preset = null,
            ),
        )
    }

    private fun handleSetUploadSpeed(speedKbps: Long) {
        val current = uiState.value.config
        engine.setConfig(
            current.copy(
                uploadSpeedKbps = speedKbps.coerceIn(
                    RateLimitEngine.MIN_SPEED_KBPS,
                    RateLimitEngine.MAX_SPEED_KBPS,
                ),
                preset = null,
            ),
        )
    }

    private fun handleSetLatency(latencyMs: Long) {
        val current = uiState.value.config
        engine.setConfig(
            current.copy(
                latencyMs = latencyMs.coerceIn(
                    RateLimitEngine.MIN_LATENCY_MS,
                    RateLimitEngine.MAX_LATENCY_MS,
                ),
                preset = null,
            ),
        )
    }

    private fun handleSetPacketLoss(percent: Float) {
        val current = uiState.value.config
        engine.setConfig(
            current.copy(
                packetLossPercent = percent.coerceIn(
                    RateLimitEngine.MIN_PACKET_LOSS,
                    RateLimitEngine.MAX_PACKET_LOSS,
                ),
                preset = null,
            ),
        )
    }

    private fun handleResetToDefaults() {
        val current = uiState.value.config
        engine.setConfig(
            RateLimitConfig
                .default()
                .copy(enabled = current.enabled),
        )
    }
}
