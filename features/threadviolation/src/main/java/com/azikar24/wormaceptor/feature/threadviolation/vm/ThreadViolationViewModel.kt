/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.threadviolation.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType
import com.azikar24.wormaceptor.domain.entities.ViolationStats
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
 * ViewModel for the Thread Violation Detection screen.
 *
 * Provides access to detected violations with filtering and detail capabilities.
 */
class ThreadViolationViewModel(
    private val engine: ThreadViolationEngine,
) : ViewModel() {

    // Filter by type
    private val _selectedType = MutableStateFlow<ViolationType?>(null)
    val selectedType: StateFlow<ViolationType?> = _selectedType.asStateFlow()

    // Selected violation for detail view
    private val _selectedViolation = MutableStateFlow<ThreadViolation?>(null)
    val selectedViolation: StateFlow<ThreadViolation?> = _selectedViolation.asStateFlow()

    // Is monitoring state
    val isMonitoring: StateFlow<Boolean> = engine.isMonitoring
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            false,
        )

    // Stats
    val stats: StateFlow<ViolationStats> = engine.stats
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ViolationStats.empty(),
        )

    // Filtered violations
    val filteredViolations: StateFlow<ImmutableList<ThreadViolation>> =
        combine(
            engine.violations,
            _selectedType,
        ) { violations, type ->
            val filtered = if (type != null) {
                violations.filter { it.violationType == type }
            } else {
                violations
            }
            filtered.toImmutableList()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            persistentListOf(),
        )

    /**
     * Toggles monitoring on/off.
     */
    fun toggleMonitoring() {
        if (engine.isMonitoring.value) {
            engine.disable()
        } else {
            engine.enable()
        }
    }

    /**
     * Sets the violation type filter.
     */
    fun setSelectedType(type: ViolationType?) {
        _selectedType.value = type
    }

    /**
     * Selects a violation to view details.
     */
    fun selectViolation(violation: ThreadViolation) {
        _selectedViolation.value = violation
    }

    /**
     * Dismisses the detail view.
     */
    fun dismissDetail() {
        _selectedViolation.value = null
    }

    /**
     * Clears all violations.
     */
    fun clearViolations() {
        engine.clearViolations()
    }
}
