package com.azikar24.wormaceptor.feature.recomposition.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.ui.RecompositionTracker
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Recomposition Summary screen.
 *
 * Polls [RecompositionTracker] every [POLL_INTERVAL_MS] milliseconds and
 * exposes the snapshot as an immutable [RecompositionViewState].
 */
class RecompositionViewModel(
    private val tracker: RecompositionTracker = RecompositionTracker,
) : ViewModel() {

    /**
     * Reactive state that refreshes on a fixed interval.
     */
    val state: StateFlow<RecompositionViewState> = flow {
        while (true) {
            emit(buildState())
            delay(POLL_INTERVAL_MS)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = RecompositionViewState(),
    )

    /**
     * Resets all tracking data and the session timer.
     */
    fun reset() {
        tracker.reset()
    }

    private fun buildState(): RecompositionViewState {
        val sessionDurationMs = tracker.getSessionDuration()
        val sessionSeconds = (sessionDurationMs / 1000f).coerceAtLeast(0.001f)
        val topRecomposers = tracker.getTopRecomposers(TOP_LIMIT)
        val items = topRecomposers.map { data ->
            RecompositionItem(
                name = data.name,
                count = data.count,
                ratePerSecond = data.count.toFloat() / sessionSeconds,
            )
        }.toImmutableList()

        return RecompositionViewState(
            sessionDurationMs = sessionDurationMs,
            totalRecompositions = tracker.getTotalRecompositions(),
            topRecomposers = items,
        )
    }

    companion object {
        private const val POLL_INTERVAL_MS = 500L
        private const val STOP_TIMEOUT_MS = 5_000L
        private const val TOP_LIMIT = 20
    }
}

/**
 * Immutable snapshot of the recomposition dashboard state.
 */
data class RecompositionViewState(
    val sessionDurationMs: Long = 0L,
    val totalRecompositions: Long = 0L,
    val topRecomposers: ImmutableList<RecompositionItem> = persistentListOf(),
)

/**
 * A single composable entry in the recomposition list.
 */
data class RecompositionItem(
    val name: String,
    val count: Long,
    val ratePerSecond: Float,
)
