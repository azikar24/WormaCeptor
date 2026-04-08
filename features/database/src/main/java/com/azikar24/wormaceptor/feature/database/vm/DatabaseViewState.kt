package com.azikar24.wormaceptor.feature.database.vm

import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.TableInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class DatabaseViewState(
    val databases: ImmutableList<DatabaseInfo> = persistentListOf(),
    val databaseSearchQuery: String = "",
    val isDatabaseSearchActive: Boolean = false,
    val isDatabasesLoading: Boolean = false,
    val databasesError: String? = null,

    val selectedDatabaseName: String? = null,
    val tables: ImmutableList<TableInfo> = persistentListOf(),
    val tableSearchQuery: String = "",
    val isTableSearchActive: Boolean = false,
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
