package com.azikar24.wormaceptor.feature.recomposition.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.NoOpNavigator
import com.azikar24.wormaceptor.core.ui.RecompositionTracker
import com.azikar24.wormaceptor.feature.recomposition.FormatRecompositionSummaryUseCase
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class RecompositionViewModel(
    private val tracker: RecompositionTracker = RecompositionTracker,
) : BaseViewModel<RecompositionViewState, RecompositionEffect, RecompositionViewEvent, NoOpNavigator>(
    initialState = RecompositionViewState(),
    navigator = NoOpNavigator,
) {

    private val formatSummary = FormatRecompositionSummaryUseCase()

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
            is RecompositionViewEvent.BackPressed -> emitEffect(RecompositionEffect.NavigateBack)
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
            formattedDuration = formatSummary.formatDuration(sessionDurationMs),
            formattedTotalRecompositions = formatSummary.formatCount(totalRecompositions),
            topRecomposers = items,
        )
    }

    companion object {
        private const val POLL_INTERVAL_MS = 500L
        private const val TOP_LIMIT = 20
    }
}
