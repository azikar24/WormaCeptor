package com.azikar24.wormaceptor.feature.database.vm

import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Consolidated UI state for the Database Browser feature.
 *
 * @property databaseSearchQuery Current search filter for the database list.
 * @property isDatabasesLoading Whether the database list is loading.
 * @property databasesError Error message for the database list, or null.
 * @property selectedDatabaseName Name of the currently selected database.
 * @property tableSearchQuery Current search filter for the table list.
 * @property isTablesLoading Whether the table list is loading.
 * @property tablesError Error message for the table list, or null.
 * @property selectedTableName Name of the currently selected table.
 * @property tableSchema Column schema for the selected table.
 * @property queryResult Paginated data for the selected table.
 * @property currentPage Zero-based page index for table data pagination.
 * @property isDataLoading Whether table data is loading.
 * @property showSchema Whether the schema view is visible.
 * @property sqlQuery Current SQL query text in the editor.
 * @property queryExecutionResult Result of the last executed SQL query.
 * @property isQueryExecuting Whether a SQL query is currently executing.
 * @property queryHistory Previously executed SQL queries.
 */
data class DatabaseViewState(
    val databaseSearchQuery: String = "",
    val isDatabasesLoading: Boolean = false,
    val databasesError: String? = null,

    val selectedDatabaseName: String? = null,
    val tableSearchQuery: String = "",
    val isTablesLoading: Boolean = false,
    val tablesError: String? = null,

    val selectedTableName: String? = null,
    val tableSchema: ImmutableList<ColumnInfo> = persistentListOf(),
    val queryResult: QueryResult? = null,
    val currentPage: Int = 0,
    val isDataLoading: Boolean = false,
    val showSchema: Boolean = false,

    val sqlQuery: String = "",
    val queryExecutionResult: QueryResult? = null,
    val isQueryExecuting: Boolean = false,
    val queryHistory: ImmutableList<String> = persistentListOf(),
)
