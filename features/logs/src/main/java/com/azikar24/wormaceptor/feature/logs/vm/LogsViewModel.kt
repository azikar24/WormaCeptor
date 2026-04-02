package com.azikar24.wormaceptor.feature.logs.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.domain.entities.LogLevel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

private const val SearchDebounceMs = 100L

/**
 * ViewModel for the Logs screen, using MVI via BaseViewModel.
 *
 * Provides filtered and searchable access to captured log entries,
 * along with controls for log level filtering and auto-scroll behavior.
 */
@OptIn(FlowPreview::class)
class LogsViewModel(
    private val logCaptureEngine: LogCaptureEngine,
) : BaseViewModel<LogsViewState, LogsViewEffect, LogsViewEvent>(
    LogsViewState(
        currentPid = logCaptureEngine.getCurrentPid(),
        isCapturing = logCaptureEngine.isCapturing.value,
    ),
) {

    // Internal flows used for debounced filtering pipelines
    private val _searchQuery = MutableStateFlow("")
    private val _selectedLevels = MutableStateFlow(LogLevel.entries.toSet())

    // Raw flows from engine
    private val rawLogs = logCaptureEngine.logs

    init {
        observeFilteredLogs()
        observeTotalCount()
        observeLevelCounts()
        observeIsCapturing()
    }

    override fun handleEvent(event: LogsViewEvent) {
        when (event) {
            is LogsViewEvent.SearchQueryChanged -> onSearchQueryChanged(event.query)
            is LogsViewEvent.MinimumLevelSet -> setMinimumLevel(event.level)
            is LogsViewEvent.LevelToggled -> toggleLevel(event.level)
            is LogsViewEvent.AllLevelsSelected -> selectAllLevels()
            is LogsViewEvent.LevelSelectionCleared -> clearLevelSelection()
            is LogsViewEvent.AutoScrollToggled -> toggleAutoScroll()
            is LogsViewEvent.AutoScrollSet -> setAutoScroll(event.enabled)
            is LogsViewEvent.CaptureStarted -> startCapture()
            is LogsViewEvent.CaptureStopped -> stopCapture()
            is LogsViewEvent.LogsCleared -> clearLogs()
            is LogsViewEvent.FiltersCleared -> clearFilters()
        }
    }

    private fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        updateState { copy(searchQuery = query) }
    }

    private fun setMinimumLevel(level: LogLevel) {
        val newSelectedLevels = LogLevel.entries
            .filter { it.priority >= level.priority }
            .toSet()
        _selectedLevels.value = newSelectedLevels
        updateState {
            copy(
                minimumLevel = level,
                selectedLevels = newSelectedLevels,
            )
        }
    }

    private fun toggleLevel(level: LogLevel) {
        val current = _selectedLevels.value
        val newSelectedLevels = if (level in current) current - level else current + level
        _selectedLevels.value = newSelectedLevels
        updateState { copy(selectedLevels = newSelectedLevels) }
    }

    private fun selectAllLevels() {
        val allLevels = LogLevel.entries.toSet()
        _selectedLevels.value = allLevels
        updateState { copy(selectedLevels = allLevels) }
    }

    private fun clearLevelSelection() {
        _selectedLevels.value = emptySet()
        updateState { copy(selectedLevels = emptySet()) }
    }

    private fun toggleAutoScroll() {
        updateState { copy(autoScroll = !autoScroll) }
    }

    private fun setAutoScroll(enabled: Boolean) {
        updateState { copy(autoScroll = enabled) }
    }

    private fun startCapture() {
        logCaptureEngine.start()
    }

    private fun stopCapture() {
        logCaptureEngine.stop()
    }

    private fun clearLogs() {
        logCaptureEngine.clear()
    }

    private fun clearFilters() {
        _searchQuery.value = ""
        val allLevels = LogLevel.entries.toSet()
        _selectedLevels.value = allLevels
        updateState {
            copy(
                searchQuery = "",
                selectedLevels = allLevels,
            )
        }
    }

    private fun observeFilteredLogs() {
        combine(
            rawLogs,
            _searchQuery.debounce(SearchDebounceMs),
            _selectedLevels,
        ) { logs, query, levels ->
            logs.filter { entry ->
                val matchesLevel = entry.level in levels
                val matchesSearch = if (query.isBlank()) {
                    true
                } else {
                    entry.tag.contains(query, ignoreCase = true) ||
                        entry.message.contains(query, ignoreCase = true)
                }
                matchesLevel && matchesSearch
            }.toImmutableList()
        }.flowOn(Dispatchers.Default)
            .onEach { filtered ->
                updateState { copy(logs = filtered) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeTotalCount() {
        rawLogs
            .map { it.size }
            .onEach { count ->
                updateState { copy(totalCount = count) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeLevelCounts() {
        rawLogs
            .map { logs -> logs.groupingBy { it.level }.eachCount() }
            .onEach { counts ->
                updateState { copy(levelCounts = counts) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeIsCapturing() {
        logCaptureEngine.isCapturing
            .onEach { capturing ->
                updateState { copy(isCapturing = capturing) }
            }
            .launchIn(viewModelScope)
    }
}
