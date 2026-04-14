package com.azikar24.wormaceptor.feature.viewer.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.NoOpNavigator
import com.azikar24.wormaceptor.core.engine.QueryEngine
import com.azikar24.wormaceptor.domain.entities.Crash
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * MVI ViewModel for the crash list tab.
 *
 * Manages crash-related UI state via [CrashListViewState] and exposes
 * engine-driven reactive data stream ([crashes]) as a [StateFlow] property.
 */
class CrashListViewModel(
    private val queryEngine: QueryEngine,
) : BaseViewModel<CrashListViewState, CrashListViewEffect, CrashListViewEvent, NoOpNavigator>(
    CrashListViewState(),
    NoOpNavigator,
) {

    /** Reactive stream of all recorded crash entries, ordered by most recent first. Syncs into [CrashListViewState]. */
    private val crashes: StateFlow<ImmutableList<Crash>> = queryEngine.observeCrashes()
        .map { it.toImmutableList() }
        .onEach { updateState { copy(crashes = it) } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    override fun handleEvent(event: CrashListViewEvent) {
        when (event) {
            is CrashListViewEvent.ClearAllCrashes -> handleClearAllCrashes()
            is CrashListViewEvent.RefreshCrashes -> handleRefreshCrashes()
            is CrashListViewEvent.ClearCrashesDialogVisibilityChanged ->
                updateState { copy(showClearCrashesDialog = event.visible) }
            is CrashListViewEvent.ExportCrashesClicked -> handleExportCrashes()
        }
    }

    private fun handleClearAllCrashes() {
        viewModelScope.launch {
            updateState { copy(showClearCrashesDialog = false) }
            queryEngine.clearCrashes()
        }
    }

    private fun handleRefreshCrashes() {
        if (uiState.value.isRefreshingCrashes) return

        viewModelScope.launch {
            updateState { copy(isRefreshingCrashes = true) }
            delay(REFRESH_DELAY)
            updateState { copy(isRefreshingCrashes = false) }
        }
    }

    private fun handleExportCrashes() {
        emitEffect(CrashListViewEffect.ExportCrashes(crashes.value))
    }

    companion object {
        private const val REFRESH_DELAY = 500L
    }
}
