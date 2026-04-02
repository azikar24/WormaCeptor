package com.azikar24.wormaceptor.feature.logs.vm

import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.LogLevel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Consolidated UI state for the Logs feature.
 *
 * @property searchQuery Current search query used to filter log entries by tag or message.
 * @property minimumLevel Minimum log level threshold; levels below this are hidden.
 * @property selectedLevels Set of currently selected log levels in multi-select filter mode.
 * @property autoScroll Whether the log list automatically scrolls to new entries.
 * @property isCapturing Whether the log capture engine is actively capturing logs.
 * @property currentPid Process ID of the current application.
 * @property logs Filtered log entries combining search query and selected level filters.
 * @property totalCount Total number of unfiltered log entries.
 * @property levelCounts Number of log entries per log level, used for filter badge counts.
 */
data class LogsViewState(
    val searchQuery: String = "",
    val minimumLevel: LogLevel = LogLevel.VERBOSE,
    val selectedLevels: Set<LogLevel> = LogLevel.entries.toSet(),
    val autoScroll: Boolean = true,
    val isCapturing: Boolean = false,
    val currentPid: Int = 0,
    val logs: ImmutableList<LogEntry> = persistentListOf(),
    val totalCount: Int = 0,
    val levelCounts: Map<LogLevel, Int> = emptyMap(),
)
