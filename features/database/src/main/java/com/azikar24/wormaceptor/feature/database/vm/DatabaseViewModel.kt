package com.azikar24.wormaceptor.feature.database.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.TableInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SearchDebounceMs = 300L
private const val FlowTimeoutMs = 5000L
private const val QueryHistoryLimit = 20
private const val DefaultPageSize = 100

/**
 * ViewModel for the Database Browser feature, using MVI via BaseViewModel.
 */
class DatabaseViewModel(
    private val repository: DatabaseRepository,
) : BaseViewModel<DatabaseViewState, DatabaseViewEffect, DatabaseViewEvent>(DatabaseViewState()) {

    private val _allDatabases = MutableStateFlow<List<DatabaseInfo>>(emptyList())
    private val _allTables = MutableStateFlow<List<TableInfo>>(emptyList())

    /** Filtered database list, derived from the raw list and the search query. */
    @OptIn(FlowPreview::class)
    val databases: StateFlow<ImmutableList<DatabaseInfo>> = combine(
        _allDatabases,
        uiState.map { it.databaseSearchQuery }.debounce(SearchDebounceMs),
    ) { databases, query ->
        if (query.isBlank()) {
            databases.toImmutableList()
        } else {
            databases.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.path.contains(query, ignoreCase = true)
            }.toImmutableList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(FlowTimeoutMs), persistentListOf())

    /** Filtered table list, derived from the raw list and the search query. */
    @OptIn(FlowPreview::class)
    val tables: StateFlow<ImmutableList<TableInfo>> = combine(
        _allTables,
        uiState.map { it.tableSearchQuery }.debounce(SearchDebounceMs),
    ) { tables, query ->
        if (query.isBlank()) {
            tables.toImmutableList()
        } else {
            tables.filter {
                it.name.contains(query, ignoreCase = true)
            }.toImmutableList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(FlowTimeoutMs), persistentListOf())

    init {
        loadDatabases()
    }

    override fun handleEvent(event: DatabaseViewEvent) {
        when (event) {
            DatabaseViewEvent.LoadDatabases -> loadDatabases()
            is DatabaseViewEvent.DatabaseSearchQueryChanged -> {
                updateState { copy(databaseSearchQuery = event.query) }
            }
            is DatabaseViewEvent.DatabaseSelected -> selectDatabase(event.name)
            DatabaseViewEvent.DatabaseSelectionCleared -> clearDatabaseSelection()

            is DatabaseViewEvent.TableSearchQueryChanged -> {
                updateState { copy(tableSearchQuery = event.query) }
            }
            is DatabaseViewEvent.TableSelected -> selectTable(event.name)
            DatabaseViewEvent.TableSelectionCleared -> clearTableSelection()

            DatabaseViewEvent.ToggleSchema -> {
                updateState { copy(showSchema = !showSchema) }
            }
            DatabaseViewEvent.NextPage -> nextPage()
            DatabaseViewEvent.PreviousPage -> previousPage()

            is DatabaseViewEvent.SqlQueryChanged -> {
                updateState { copy(sqlQuery = event.query) }
            }
            DatabaseViewEvent.ExecuteQuery -> executeQuery()
            DatabaseViewEvent.ClearQuery -> {
                updateState { copy(sqlQuery = "", queryExecutionResult = null) }
            }
            is DatabaseViewEvent.QuerySelectedFromHistory -> {
                updateState { copy(sqlQuery = event.query) }
            }
            is DatabaseViewEvent.PrefilledQueryRequested -> setPrefilledQuery(event.tableName, event.queryType)
        }
    }

    private fun loadDatabases() {
        viewModelScope.launch {
            updateState { copy(isDatabasesLoading = true, databasesError = null) }

            try {
                val databases = withContext(Dispatchers.IO) {
                    repository.getDatabases()
                }
                _allDatabases.value = databases
            } catch (e: IllegalStateException) {
                updateState { copy(databasesError = e.message ?: "Failed to load databases") }
            } finally {
                updateState { copy(isDatabasesLoading = false) }
            }
        }
    }

    private fun selectDatabase(name: String) {
        updateState { copy(selectedDatabaseName = name, tableSearchQuery = "") }
        loadTables()
    }

    private fun clearDatabaseSelection() {
        updateState { copy(selectedDatabaseName = null) }
        _allTables.value = emptyList()
        clearTableSelection()
    }

    private fun loadTables() {
        val dbName = uiState.value.selectedDatabaseName ?: return

        viewModelScope.launch {
            updateState { copy(isTablesLoading = true, tablesError = null) }

            try {
                val tables = withContext(Dispatchers.IO) {
                    repository.getTables(dbName)
                }
                _allTables.value = tables
            } catch (e: IllegalStateException) {
                updateState { copy(tablesError = e.message ?: "Failed to load tables") }
            } finally {
                updateState { copy(isTablesLoading = false) }
            }
        }
    }

    private fun selectTable(name: String) {
        updateState { copy(selectedTableName = name, currentPage = 0, showSchema = false) }
        loadTableSchema()
        loadTableData()
    }

    private fun clearTableSelection() {
        updateState {
            copy(
                selectedTableName = null,
                tableSchema = persistentListOf(),
                queryResult = null,
                currentPage = 0,
            )
        }
    }

    private fun loadTableSchema() {
        val state = uiState.value
        val dbName = state.selectedDatabaseName ?: return
        val tableName = state.selectedTableName ?: return

        viewModelScope.launch {
            try {
                val schema = withContext(Dispatchers.IO) {
                    repository.getTableSchema(dbName, tableName)
                }
                updateState { copy(tableSchema = schema.toImmutableList()) }
            } catch (_: IllegalStateException) {
                // Schema loading is optional, don't show error
            }
        }
    }

    private fun loadTableData() {
        val state = uiState.value
        val dbName = state.selectedDatabaseName ?: return
        val tableName = state.selectedTableName ?: return

        viewModelScope.launch {
            updateState { copy(isDataLoading = true) }

            try {
                val offset = state.currentPage * DefaultPageSize
                val result = withContext(Dispatchers.IO) {
                    repository.queryTable(dbName, tableName, DefaultPageSize, offset)
                }
                updateState { copy(queryResult = result) }
            } catch (e: IllegalStateException) {
                updateState {
                    copy(
                        queryResult = QueryResult(
                            columns = emptyList(),
                            rows = emptyList(),
                            rowCount = 0,
                            error = e.message ?: "Failed to load data",
                        ),
                    )
                }
            } finally {
                updateState { copy(isDataLoading = false) }
            }
        }
    }

    private fun nextPage() {
        val result = uiState.value.queryResult ?: return
        if (result.rowCount == DefaultPageSize) {
            updateState { copy(currentPage = currentPage + 1) }
            loadTableData()
        }
    }

    private fun previousPage() {
        if (uiState.value.currentPage > 0) {
            updateState { copy(currentPage = currentPage - 1) }
            loadTableData()
        }
    }

    private fun executeQuery() {
        val state = uiState.value
        val dbName = state.selectedDatabaseName ?: return
        val query = state.sqlQuery.trim()

        if (query.isEmpty()) {
            updateState {
                copy(
                    queryExecutionResult = QueryResult(
                        columns = emptyList(),
                        rows = emptyList(),
                        rowCount = 0,
                        error = "Query is empty",
                    ),
                )
            }
            return
        }

        viewModelScope.launch {
            updateState { copy(isQueryExecuting = true) }

            try {
                val result = withContext(Dispatchers.IO) {
                    repository.executeQuery(dbName, query)
                }
                updateState {
                    val updatedHistory = if (result.isSuccess && !queryHistory.contains(query)) {
                        (queryHistory + query).takeLast(QueryHistoryLimit).toImmutableList()
                    } else {
                        queryHistory
                    }
                    copy(queryExecutionResult = result, queryHistory = updatedHistory)
                }
            } catch (e: IllegalStateException) {
                updateState {
                    copy(
                        queryExecutionResult = QueryResult(
                            columns = emptyList(),
                            rows = emptyList(),
                            rowCount = 0,
                            error = e.message ?: "Query execution failed",
                        ),
                    )
                }
            } finally {
                updateState { copy(isQueryExecuting = false) }
            }
        }
    }

    private fun setPrefilledQuery(
        tableName: String,
        queryType: String,
    ) {
        val query = when (queryType) {
            "select" -> "SELECT * FROM `$tableName` LIMIT 10"
            "count" -> "SELECT COUNT(*) FROM `$tableName`"
            "schema" -> "PRAGMA table_info('$tableName')"
            else -> ""
        }
        updateState { copy(sqlQuery = query) }
    }
}
