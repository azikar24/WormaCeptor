package com.azikar24.wormaceptor.feature.logs.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.LogLevel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel for the Logs screen.
 *
 * Provides filtered and searchable access to captured log entries,
 * along with controls for log level filtering and auto-scroll behavior.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class LogsViewModel(
    private val logCaptureEngine: LogCaptureEngine,
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    /** Current search query used to filter log entries by tag or message. */
    val searchQuery: StateFlow<String> = _searchQuery

    private val _minimumLevel = MutableStateFlow(LogLevel.VERBOSE)

    /** Minimum log level threshold; levels below this are hidden. */
    val minimumLevel: StateFlow<LogLevel> = _minimumLevel

    private val _selectedLevels = MutableStateFlow<Set<LogLevel>>(LogLevel.entries.toSet())

    /** Set of currently selected log levels in multi-select filter mode. */
    val selectedLevels: StateFlow<Set<LogLevel>> = _selectedLevels

    private val _autoScroll = MutableStateFlow(true)

    /** Whether the log list automatically scrolls to new entries. */
    val autoScroll: StateFlow<Boolean> = _autoScroll

    /** Whether the log capture engine is actively capturing logs. */
    val isCapturing: StateFlow<Boolean> = logCaptureEngine.isCapturing

    /** Process ID of the current application. */
    val currentPid: Int = logCaptureEngine.getCurrentPid()

    // Raw logs from engine
    private val rawLogs: StateFlow<List<LogEntry>> = logCaptureEngine.logs

    /** Filtered log entries combining search query and selected level filters. */
    val logs: StateFlow<ImmutableList<LogEntry>> = combine(
        rawLogs,
        _searchQuery.debounce(100),
        _selectedLevels,
    ) { logs, query, levels ->
        logs.filter { entry ->
            // Filter by selected levels
            val matchesLevel = entry.level in levels

            // Filter by search query
            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                entry.tag.contains(query, ignoreCase = true) ||
                    entry.message.contains(query, ignoreCase = true)
            }

            matchesLevel && matchesSearch
        }.toImmutableList()
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    /** Total number of unfiltered log entries. */
    val totalCount: StateFlow<Int> = rawLogs
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /** Number of log entries per log level, used for filter badge counts. */
    val levelCounts: StateFlow<Map<LogLevel, Int>> = rawLogs
        .map { logs ->
            logs.groupingBy { it.level }.eachCount()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /**
     * Updates the search query.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Sets the minimum log level filter.
     */
    fun setMinimumLevel(level: LogLevel) {
        _minimumLevel.value = level
        // Update selected levels to include this level and above
        _selectedLevels.value = LogLevel.entries
            .filter { it.priority >= level.priority }
            .toSet()
    }

    /**
     * Toggles a specific log level in the filter.
     */
    fun toggleLevel(level: LogLevel) {
        _selectedLevels.value = if (level in _selectedLevels.value) {
            _selectedLevels.value - level
        } else {
            _selectedLevels.value + level
        }
    }

    /**
     * Selects all log levels.
     */
    fun selectAllLevels() {
        _selectedLevels.value = LogLevel.entries.toSet()
    }

    /**
     * Clears all level selections.
     */
    fun clearLevelSelection() {
        _selectedLevels.value = emptySet()
    }

    /**
     * Toggles auto-scroll behavior.
     */
    fun toggleAutoScroll() {
        _autoScroll.value = !_autoScroll.value
    }

    /**
     * Sets auto-scroll state.
     */
    fun setAutoScroll(enabled: Boolean) {
        _autoScroll.value = enabled
    }

    /**
     * Starts capturing logs.
     */
    fun startCapture() {
        logCaptureEngine.start()
    }

    /**
     * Stops capturing logs.
     */
    fun stopCapture() {
        logCaptureEngine.stop()
    }

    /**
     * Clears all captured logs.
     */
    fun clearLogs() {
        logCaptureEngine.clear()
    }

    /**
     * Clears search and filters.
     */
    fun clearFilters() {
        _searchQuery.value = ""
        _selectedLevels.value = LogLevel.entries.toSet()
    }

    override fun onCleared() {
        super.onCleared()
        // Don't stop capture when ViewModel is cleared - let the engine continue
        // The engine lifecycle is managed separately
    }
}
