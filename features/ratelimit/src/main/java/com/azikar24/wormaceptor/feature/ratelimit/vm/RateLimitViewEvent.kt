package com.azikar24.wormaceptor.feature.ratelimit.vm

import com.azikar24.wormaceptor.domain.entities.RateLimitConfig.NetworkPreset

/** User actions dispatched from the Rate Limiting UI. */
sealed class RateLimitViewEvent {
    data object ToggleEnabled : RateLimitViewEvent()
    data class SelectPreset(val preset: NetworkPreset?) : RateLimitViewEvent()
    data class SetDownloadSpeed(val speedKbps: Long) : RateLimitViewEvent()
    data class SetUploadSpeed(val speedKbps: Long) : RateLimitViewEvent()
    data class SetLatency(val latencyMs: Long) : RateLimitViewEvent()
    data class SetPacketLoss(val percent: Float) : RateLimitViewEvent()
    data object ClearStats : RateLimitViewEvent()
    data object ResetToDefaults : RateLimitViewEvent()
}
