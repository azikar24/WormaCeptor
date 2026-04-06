package com.azikar24.wormaceptor.feature.ratelimit.ui.util

import java.util.Locale

private const val KbpsPerMbps = 1000

internal fun formatSpeed(kbps: Long): String {
    return if (kbps >= KbpsPerMbps) {
        String.format(Locale.US, "%.1f Mbps", kbps.toDouble() / KbpsPerMbps)
    } else {
        "$kbps Kbps"
    }
}
