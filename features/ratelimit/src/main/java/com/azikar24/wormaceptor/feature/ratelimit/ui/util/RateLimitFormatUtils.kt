package com.azikar24.wormaceptor.feature.ratelimit.ui.util

import java.util.Locale

internal fun formatSpeed(kbps: Long): String {
    return if (kbps >= 1000) {
        String.format(Locale.US, "%.1f Mbps", kbps / 1000.0)
    } else {
        "$kbps Kbps"
    }
}
