package com.azikar24.wormaceptor.feature.recomposition.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.ui.RecompositionTracker
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for the Recomposition Summary screen.
 *
 * Polls [RecompositionTracker] every [POLL_INTERVAL_MS] milliseconds and
 * exposes the snapshot via [uiState].
 */
class RecompositionViewModel(
    private val tracker: RecompositionTracker = RecompositionTracker,
) : BaseViewModel<RecompositionViewState, RecompositionEffect, RecompositionEvent>(
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

    override fun handleEvent(event: RecompositionEvent) {
        when (event) {
            is RecompositionEvent.Reset -> tracker.reset()
        }
    }

    private fun buildState(): RecompositionViewState {
        val sessionDurationMs = tracker.getSessionDuration()
        val items = tracker.getTopRecomposers(TOP_LIMIT).map { data ->
            RecompositionItem(
                name = data.name,
                count = data.count,
                ratePerSecond = data.ratePerSecond,
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
        private const val TOP_LIMIT = 20
    }
}
