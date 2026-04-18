package com.azikar24.wormaceptor.feature.leakdetection.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.NoOpNavigator
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

class LeakDetectionViewModel(
    private val engine: LeakDetectionEngine,
) : BaseViewModel<LeakDetectionViewState, LeakDetectionViewEffect, LeakDetectionViewEvent, NoOpNavigator>(
    initialState = LeakDetectionViewState(),
    navigator = NoOpNavigator,
) {

    init {
        observeEngineState()
    }

    override fun handleEvent(event: LeakDetectionViewEvent) {
        when (event) {
            is LeakDetectionViewEvent.SelectSeverity -> updateState {
                copy(
                    selectedSeverity = event.severity,
                    filteredLeaks = filterLeaks(engine.detectedLeaks.value, event.severity),
                )
            }
            is LeakDetectionViewEvent.SelectLeak -> updateState {
                copy(selectedLeak = event.leak)
            }
            is LeakDetectionViewEvent.DismissDetail -> updateState {
                copy(selectedLeak = null)
            }
            is LeakDetectionViewEvent.TriggerCheck -> engine.triggerCheck()
            is LeakDetectionViewEvent.ClearLeaks -> engine.clearLeaks()
        }
    }

    private fun observeEngineState() {
        combine(
            engine.detectedLeaks,
            engine.leakSummary,
            engine.isRunning,
        ) { leaks, summary, isRunning ->
            updateState {
                copy(
                    filteredLeaks = filterLeaks(leaks, selectedSeverity),
                    summary = summary,
                    isRunning = isRunning,
                    isLeaksLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun filterLeaks(
        leaks: List<LeakInfo>,
        severity: LeakSeverity?,
    ): ImmutableList<LeakInfo> {
        val filtered = if (severity != null) {
            leaks.filter { it.severity == severity }
        } else {
            leaks
        }
        return filtered.toImmutableList()
    }
}
