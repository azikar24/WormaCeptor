package com.azikar24.wormaceptor.feature.recomposition.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.ui.RecompositionTracker
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * ViewModel for the Recomposition Summary screen.
 *
 * Polls [RecompositionTracker] every [POLL_INTERVAL_MS] milliseconds and
 * exposes the snapshot via [uiState].
 */
class RecompositionViewModel(
    private val tracker: RecompositionTracker = RecompositionTracker,
) : BaseViewModel<RecompositionViewState, RecompositionEffect, RecompositionViewEvent>(
    initialState = RecompositionViewState(),
) {

    init {
        viewModelScope.launch {
            while (true) {
                updateState { buildState() }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    override fun handleEvent(event: RecompositionViewEvent) {
        when (event) {
            is RecompositionViewEvent.Reset -> tracker.reset()
        }
    }

    private fun buildState(): RecompositionViewState {
        val sessionDurationMs = tracker.getSessionDuration()
        val totalRecompositions = tracker.getTotalRecompositions()
        val items = tracker.getTopRecomposers(TOP_LIMIT).map { data ->
            RecompositionItem(
                name = data.name,
                count = data.count,
                ratePerSecond = data.ratePerSecond,
            )
        }.toImmutableList()

        return RecompositionViewState(
            sessionDurationMs = sessionDurationMs,
            totalRecompositions = totalRecompositions,
            formattedDuration = formatDuration(sessionDurationMs),
            formattedTotalRecompositions = formatCount(totalRecompositions),
            topRecomposers = items,
        )
    }

    companion object {
        private const val POLL_INTERVAL_MS = 500L
        private const val TOP_LIMIT = 20
        private const val SECONDS_PER_MINUTE = 60
        private const val MILLIS_PER_SECOND = 1000

        private fun formatDuration(durationMs: Long): String {
            val totalSeconds = durationMs / MILLIS_PER_SECOND
            val minutes = totalSeconds / SECONDS_PER_MINUTE
            val seconds = totalSeconds % SECONDS_PER_MINUTE
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }

        private fun formatCount(count: Long): String = when {
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }
}
