package com.azikar24.wormaceptor.feature.leakdetection.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakInfo.LeakSeverity
import com.azikar24.wormaceptor.domain.entities.LeakSummary
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Memory Leak Detection screen.
 *
 * Provides access to detected leaks with filtering and detail capabilities.
 */
class LeakDetectionViewModel(
    private val engine: LeakDetectionEngine,
) : ViewModel() {

    // Filter by severity
    private val _selectedSeverity = MutableStateFlow<LeakSeverity?>(null)
    val selectedSeverity: StateFlow<LeakSeverity?> = _selectedSeverity.asStateFlow()

    // Selected leak for detail view
    private val _selectedLeak = MutableStateFlow<LeakInfo?>(null)
    val selectedLeak: StateFlow<LeakInfo?> = _selectedLeak.asStateFlow()

    // Is running state
    val isRunning: StateFlow<Boolean> = engine.isRunning
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false,
        )

    // Leak summary
    val summary: StateFlow<LeakSummary> = engine.leakSummary
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LeakSummary.empty(),
        )

    // Filtered leaks
    val filteredLeaks: StateFlow<ImmutableList<LeakInfo>> =
        combine(
            engine.detectedLeaks,
            _selectedSeverity,
        ) { leaks, severity ->
            val filtered = if (severity != null) {
                leaks.filter { it.severity == severity }
            } else {
                leaks
            }
            filtered.toImmutableList()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            persistentListOf(),
        )

    /**
     * Sets the severity filter.
     *
     * @param severity The severity to filter by, or null for all
     */
    fun setSelectedSeverity(severity: LeakSeverity?) {
        _selectedSeverity.value = severity
    }

    /**
     * Selects a leak to view details.
     *
     * @param leak The leak to show details for
     */
    fun selectLeak(leak: LeakInfo) {
        _selectedLeak.value = leak
    }

    /**
     * Dismisses the detail view.
     */
    fun dismissDetail() {
        _selectedLeak.value = null
    }

    /**
     * Manually triggers a leak check.
     */
    fun triggerCheck() {
        engine.triggerCheck()
    }

    /**
     * Clears all detected leaks.
     */
    fun clearLeaks() {
        engine.clearLeaks()
    }
}
