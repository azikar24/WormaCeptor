package com.azikar24.wormaceptor.feature.ratelimit

import java.util.Locale

internal class FormatRateLimitSpeedUseCase {

    operator fun invoke(kbps: Long): String = if (kbps >= KBPS_PER_MBPS) {
        String.format(Locale.US, "%.1f Mbps", kbps.toDouble() / KBPS_PER_MBPS)
    } else {
        "$kbps Kbps"
    }

    companion object {
        private const val KBPS_PER_MBPS = 1000
    }
}
