package com.azikar24.wormaceptor.feature.recomposition

import java.util.Locale

internal class FormatRecompositionSummaryUseCase {

    fun formatDuration(durationMs: Long): String {
        val totalSeconds = durationMs / MILLIS_PER_SECOND
        val minutes = totalSeconds / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    fun formatCount(count: Long): String = when {
        count >= MILLION -> String.format(Locale.US, "%.1fM", count / MILLION.toDouble())
        count >= THOUSAND -> String.format(Locale.US, "%.1fK", count / THOUSAND.toDouble())
        else -> count.toString()
    }

    companion object {
        private const val SECONDS_PER_MINUTE = 60
        private const val MILLIS_PER_SECOND = 1000
        private const val THOUSAND = 1_000
        private const val MILLION = 1_000_000
    }
}
