package com.azikar24.wormaceptor.feature.threadviolation.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

/**
 * ViewModel for the Thread Violation Detection screen.
 *
 * Consolidates violation data from [ThreadViolationEngine] into a single
 * [ThreadViolationViewState] and exposes user actions via [ThreadViolationViewEvent].
 */
class ThreadViolationViewModel(
    private val engine: ThreadViolationEngine,
) : BaseViewModel<ThreadViolationViewState, ThreadViolationViewEffect, ThreadViolationViewEvent>(
    initialState = ThreadViolationViewState(),
) {

    init {
        combine(
            engine.violations,
            engine.stats,
            engine.isMonitoring,
        ) { violations, stats, isMonitoring ->
            updateState {
                copy(
                    filteredViolations = filterViolations(violations, selectedType),
                    stats = stats,
                    isMonitoring = isMonitoring,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun handleEvent(event: ThreadViolationViewEvent) {
        when (event) {
            is ThreadViolationViewEvent.SelectType -> updateState {
                copy(
                    selectedType = event.type,
                    filteredViolations = filterViolations(engine.violations.value, event.type),
                )
            }
            is ThreadViolationViewEvent.SelectViolation -> updateState {
                copy(selectedViolation = event.violation)
            }
            is ThreadViolationViewEvent.DismissDetail -> updateState {
                copy(selectedViolation = null)
            }
            is ThreadViolationViewEvent.ToggleMonitoring -> {
                if (engine.isMonitoring.value) {
                    engine.disable()
                } else {
                    engine.enable()
                }
            }
            is ThreadViolationViewEvent.ClearViolations -> engine.clearViolations()
        }
    }

    private fun filterViolations(
        violations: List<ThreadViolation>,
        type: ViolationType?,
    ): ImmutableList<ThreadViolation> {
        val filtered = if (type != null) {
            violations.filter { it.violationType == type }
        } else {
            violations
        }
        return filtered.toImmutableList()
    }
}
