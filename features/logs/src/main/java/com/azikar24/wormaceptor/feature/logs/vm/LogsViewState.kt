package com.azikar24.wormaceptor.feature.logs.vm

import com.azikar24.wormaceptor.domain.entities.LogEntry
import com.azikar24.wormaceptor.domain.entities.LogLevel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class LogsViewState(
    val searchQuery: String = "",
    val minimumLevel: LogLevel = LogLevel.VERBOSE,
    val selectedLevels: Set<LogLevel> = LogLevel.entries.toSet(),
    val autoScroll: Boolean = true,
    val isCapturing: Boolean = false,
    val currentPid: Int = 0,
    val isLogsLoading: Boolean = true,
    val logs: ImmutableList<LogEntry> = persistentListOf(),
    val totalCount: Int = 0,
    val levelCounts: Map<LogLevel, Int> = emptyMap(),
)
