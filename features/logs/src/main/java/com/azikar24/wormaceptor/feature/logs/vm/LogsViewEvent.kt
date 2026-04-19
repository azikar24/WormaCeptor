package com.azikar24.wormaceptor.feature.logs.vm

import com.azikar24.wormaceptor.domain.entities.LogLevel

/**
 * User-initiated events for the Logs feature.
 */
sealed class LogsViewEvent {

    /**
     * Updates the search query.
     *
     * @property query The new search text.
     */
    data class SearchQueryChanged(val query: String) : LogsViewEvent()

    /**
     * Sets the minimum log level filter. Cascades to selected levels.
     *
     * @property level The new minimum log level.
     */
    data class MinimumLevelSet(val level: LogLevel) : LogsViewEvent()

    /**
     * Toggles a specific log level in the filter.
     *
     * @property level The log level to toggle.
     */
    data class LevelToggled(val level: LogLevel) : LogsViewEvent()

    /** Selects all log levels. */
    data object AllLevelsSelected : LogsViewEvent()

    /** Clears all level selections. */
    data object LevelSelectionCleared : LogsViewEvent()

    /** Toggles auto-scroll behavior. */
    data object AutoScrollToggled : LogsViewEvent()

    /**
     * Sets auto-scroll state explicitly.
     *
     * @property enabled Whether auto-scroll should be enabled.
     */
    data class AutoScrollSet(val enabled: Boolean) : LogsViewEvent()

    /** Starts capturing logs. */
    data object CaptureStarted : LogsViewEvent()

    /** Stops capturing logs. */
    data object CaptureStopped : LogsViewEvent()

    /** Clears all captured logs. */
    data object LogsCleared : LogsViewEvent()

    /** Clears search and filters. */
    data object FiltersCleared : LogsViewEvent()
}
