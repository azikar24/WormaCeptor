package com.azikar24.wormaceptor.feature.ratelimit.vm

import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig.NetworkPreset
import com.azikar24.wormaceptor.domain.entities.ThrottleStats

data class RateLimitViewState(
    val config: RateLimitConfig = RateLimitConfig.default(),
    val stats: ThrottleStats = ThrottleStats.empty(),
    val selectedPreset: NetworkPreset? = null,
)
