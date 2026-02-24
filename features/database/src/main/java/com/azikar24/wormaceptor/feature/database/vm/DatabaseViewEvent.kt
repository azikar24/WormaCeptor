package com.azikar24.wormaceptor.feature.database.vm

/**
 * User-initiated events for the Database Browser feature.
 */
sealed class DatabaseViewEvent {

    /** Triggers a refresh of the database list. */
    data object LoadDatabases : DatabaseViewEvent()

    /**
     * Updates the database list search filter.
     *
     * @property query The new search text.
     */
    data class DatabaseSearchQueryChanged(val query: String) : DatabaseViewEvent()

    /**
     * Selects a database by name.
     *
     * @property name The database name.
     */
    data class DatabaseSelected(val name: String) : DatabaseViewEvent()

    /** Clears the current database selection. */
    data object DatabaseSelectionCleared : DatabaseViewEvent()

    /**
     * Updates the table list search filter.
     *
     * @property query The new search text.
     */
    data class TableSearchQueryChanged(val query: String) : DatabaseViewEvent()

    /**
     * Selects a table by name.
     *
     * @property name The table name.
     */
    data class TableSelected(val name: String) : DatabaseViewEvent()

    /** Clears the current table selection. */
    data object TableSelectionCleared : DatabaseViewEvent()

    /** Toggles between schema view and data view. */
    data object ToggleSchema : DatabaseViewEvent()

    /** Navigates to the next page of table data. */
    data object NextPage : DatabaseViewEvent()

    /** Navigates to the previous page of table data. */
    data object PreviousPage : DatabaseViewEvent()

    /**
     * Updates the SQL query text in the editor.
     *
     * @property query The new SQL text.
     */
    data class SqlQueryChanged(val query: String) : DatabaseViewEvent()

    /** Executes the current SQL query. */
    data object ExecuteQuery : DatabaseViewEvent()

    /** Clears the SQL query editor and its result. */
    data object ClearQuery : DatabaseViewEvent()

    /**
     * Populates the editor with a previously executed query.
     *
     * @property query The historical query text.
     */
    data class QuerySelectedFromHistory(val query: String) : DatabaseViewEvent()

    /**
     * Populates the editor with a prefilled query template.
     *
     * @property tableName The target table name.
     * @property queryType The template type (select, count, schema).
     */
    data class PrefilledQueryRequested(val tableName: String, val queryType: String) : DatabaseViewEvent()
}
