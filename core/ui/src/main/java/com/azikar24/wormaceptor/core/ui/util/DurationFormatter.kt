package com.azikar24.wormaceptor.core.ui.util

import java.util.Locale

/**
 * Formats duration in milliseconds to a human-readable string.
 * - Values < 1000ms are displayed as "Xms"
 * - Values >= 1000ms are displayed as "X.XXs"
 */
fun formatDuration(durationMs: Long?): String {
    if (durationMs == null) return "?"
    return if (durationMs >= 1000) {
        String.format(Locale.US, "%.2fs", durationMs / 1000.0)
    } else {
        "${durationMs}ms"
    }
}

/**
 * Formats duration for average values (Double).
 */
fun formatDurationAvg(durationMs: Double): String {
    return if (durationMs >= 1000) {
        String.format(Locale.US, "%.2fs", durationMs / 1000.0)
    } else {
        "${durationMs.toInt()}ms"
    }
}
