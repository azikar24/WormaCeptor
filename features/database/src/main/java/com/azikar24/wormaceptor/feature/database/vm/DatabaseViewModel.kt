package com.azikar24.wormaceptor.feature.database.vm

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.SearchDebounce
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.TableInfo
import com.azikar24.wormaceptor.feature.database.R
import com.azikar24.wormaceptor.feature.database.navigator.DatabaseNavigator
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseViewModel(
    private val repository: DatabaseRepository,
    private val application: Application,
    navigator: DatabaseNavigator,
) : BaseViewModel<DatabaseViewState, DatabaseViewEffect, DatabaseViewEvent, DatabaseNavigator>(
    DatabaseViewState(),
    navigator,
) {

    private val _allDatabases = MutableStateFlow<List<DatabaseInfo>>(emptyList())
    private val _allTables = MutableStateFlow<List<TableInfo>>(emptyList())

    init {
        observeFilteredLists()
        loadDatabases()
    }

    @OptIn(FlowPreview::class)
    private fun observeFilteredLists() {
        combine(
            _allDatabases,
            uiState.map { it.databaseSearchQuery }.debounce { if (it.isBlank()) 0L else SearchDebounce.HEAVY },
        ) { databases, query ->
            if (query.isBlank()) {
                databases.toImmutableList()
            } else {
                databases.filter {
                    it.name.contains(query, ignoreCase = true) ||
                        it.path.contains(query, ignoreCase = true)
                }.toImmutableList()
            }
        }.onEach { filtered ->
            updateState { copy(databases = filtered) }
        }.launchIn(viewModelScope)

        combine(
            _allTables,
            uiState.map { it.tableSearchQuery }.debounce { if (it.isBlank()) 0L else SearchDebounce.HEAVY },
        ) { tables, query ->
            if (query.isBlank()) {
                tables.toImmutableList()
            } else {
                tables.filter {
                    it.name.contains(query, ignoreCase = true)
                }.toImmutableList()
            }
        }.onEach { filtered ->
            updateState { copy(tables = filtered) }
        }.launchIn(viewModelScope)
    }

    override fun handleEvent(event: DatabaseViewEvent) {
        when (event) {
            is DatabaseViewEvent.List -> handleListEvent(event)
            is DatabaseViewEvent.Tables -> handleTablesEvent(event)
            is DatabaseViewEvent.Data -> handleDataEvent(event)
            is DatabaseViewEvent.Query -> handleQueryEvent(event)
        }
    }

    private fun handleListEvent(event: DatabaseViewEvent.List) {
        when (event) {
            DatabaseViewEvent.List.Load -> loadDatabases()
            is DatabaseViewEvent.List.SearchQueryChanged -> {
                updateState { copy(databaseSearchQuery = event.query) }
            }
            DatabaseViewEvent.List.ToggleSearch -> {
                val wasActive = uiState.value.isDatabaseSearchActive
                updateState {
                    copy(
                        isDatabaseSearchActive = !wasActive,
                        databaseSearchQuery = if (wasActive) "" else databaseSearchQuery,
                    )
                }
            }
            is DatabaseViewEvent.List.Selected -> {
                selectDatabase(event.name)
                navigator.navigateToTables()
            }
            DatabaseViewEvent.List.SelectionCleared -> clearDatabaseSelection()
        }
    }

    private fun handleTablesEvent(event: DatabaseViewEvent.Tables) {
        when (event) {
            is DatabaseViewEvent.Tables.SearchQueryChanged -> {
                updateState { copy(tableSearchQuery = event.query) }
            }
            DatabaseViewEvent.Tables.ToggleSearch -> {
                val wasActive = uiState.value.isTableSearchActive
                updateState {
                    copy(
                        isTableSearchActive = !wasActive,
                        tableSearchQuery = if (wasActive) "" else tableSearchQuery,
                    )
                }
            }
            is DatabaseViewEvent.Tables.Selected -> {
                selectTable(event.name)
                navigator.navigateToTableData()
            }
            DatabaseViewEvent.Tables.SelectionCleared -> clearTableSelection()
            DatabaseViewEvent.Tables.NavigateToQuery -> navigator.navigateToQuery()
            DatabaseViewEvent.Tables.BackPressed -> {
                clearDatabaseSelection()
                navigator.navigateBack()
            }
        }
    }

    private fun handleDataEvent(event: DatabaseViewEvent.Data) {
        when (event) {
            DatabaseViewEvent.Data.ToggleSchema -> updateState { copy(showSchema = !showSchema) }
            DatabaseViewEvent.Data.NextPage -> nextPage()
            DatabaseViewEvent.Data.PreviousPage -> previousPage()
            DatabaseViewEvent.Data.BackPressed -> {
                clearTableSelection()
                navigator.navigateBack()
            }
        }
    }

    private fun handleQueryEvent(event: DatabaseViewEvent.Query) {
        when (event) {
            is DatabaseViewEvent.Query.SqlChanged -> updateState { copy(sqlQuery = event.query) }
            DatabaseViewEvent.Query.Execute -> executeQuery()
            DatabaseViewEvent.Query.Clear -> updateState { copy(sqlQuery = "", queryExecutionResult = null) }
            DatabaseViewEvent.Query.BackPressed -> {
                updateState { copy(sqlQuery = "", queryExecutionResult = null) }
                navigator.navigateBack()
            }
            is DatabaseViewEvent.Query.HistorySelected -> updateState { copy(sqlQuery = event.query) }
            is DatabaseViewEvent.Query.PrefilledRequested -> setPrefilledQuery(event.tableName, event.queryType)
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
                // Eagerly reflect the load so the UI transitions loading → content in one frame,
                // without flashing the empty state while combine's query debounce is pending.
                updateState {
                    copy(
                        databases = databases.toImmutableList(),
                        isDatabasesLoading = false,
                    )
                }
            } catch (e: IllegalStateException) {
                updateState {
                    copy(
                        databasesError = e.message ?: application.getString(R.string.database_error_load_databases),
                        isDatabasesLoading = false,
                    )
                }
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
                // Eagerly reflect the load so the UI transitions loading → content in one frame,
                // without flashing the empty state while combine's query debounce is pending.
                updateState {
                    copy(
                        tables = tables.toImmutableList(),
                        isTablesLoading = false,
                    )
                }
            } catch (e: IllegalStateException) {
                updateState {
                    copy(
                        tablesError = e.message ?: application.getString(R.string.database_error_load_tables),
                        isTablesLoading = false,
                    )
                }
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
                val offset = state.currentPage * DEFAULT_PAGE_SIZE
                val result = withContext(Dispatchers.IO) {
                    repository.queryTable(dbName, tableName, DEFAULT_PAGE_SIZE, offset)
                }
                updateState { copy(queryResult = result) }
            } catch (e: IllegalStateException) {
                updateState {
                    copy(
                        queryResult = QueryResult(
                            columns = emptyList(),
                            rows = emptyList(),
                            rowCount = 0,
                            error = e.message ?: application.getString(R.string.database_error_load_data),
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
        if (result.rowCount == DEFAULT_PAGE_SIZE) {
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
                        error = application.getString(R.string.database_error_query_empty),
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
                        (queryHistory + query).takeLast(QUERY_HISTORY_LIMIT).toImmutableList()
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
                            error = e.message ?: application.getString(R.string.database_error_query_failed),
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

    companion object {
        private const val QUERY_HISTORY_LIMIT = 20
        private const val DEFAULT_PAGE_SIZE = 100
    }
}
