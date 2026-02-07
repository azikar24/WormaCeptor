package com.azikar24.wormaceptor.feature.database.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.TableInfo
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the Database Browser feature.
 */
class DatabaseViewModel(
    private val repository: DatabaseRepository,
) : ViewModel() {

    // ===== Database List State =====

    private val _allDatabases = MutableStateFlow<List<DatabaseInfo>>(emptyList())
    private val _databaseSearchQuery = MutableStateFlow("")
    val databaseSearchQuery: StateFlow<String> = _databaseSearchQuery
    private val _isDatabasesLoading = MutableStateFlow(false)
    val isDatabasesLoading: StateFlow<Boolean> = _isDatabasesLoading
    private val _databasesError = MutableStateFlow<String?>(null)
    val databasesError: StateFlow<String?> = _databasesError

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val databases: StateFlow<ImmutableList<DatabaseInfo>> = combine(
        _allDatabases,
        _databaseSearchQuery.debounce(300),
    ) { databases, query ->
        if (query.isBlank()) {
            databases.toImmutableList()
        } else {
            databases.filter {
                it.name.contains(query, ignoreCase = true) ||
                    it.path.contains(query, ignoreCase = true)
            }.toImmutableList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // ===== Table List State =====

    private val _selectedDatabaseName = MutableStateFlow<String?>(null)
    val selectedDatabaseName: StateFlow<String?> = _selectedDatabaseName

    private val _allTables = MutableStateFlow<List<TableInfo>>(emptyList())
    private val _tableSearchQuery = MutableStateFlow("")
    val tableSearchQuery: StateFlow<String> = _tableSearchQuery
    private val _isTablesLoading = MutableStateFlow(false)
    val isTablesLoading: StateFlow<Boolean> = _isTablesLoading
    private val _tablesError = MutableStateFlow<String?>(null)
    val tablesError: StateFlow<String?> = _tablesError

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val tables: StateFlow<ImmutableList<TableInfo>> = combine(
        _allTables,
        _tableSearchQuery.debounce(300),
    ) { tables, query ->
        if (query.isBlank()) {
            tables.toImmutableList()
        } else {
            tables.filter {
                it.name.contains(query, ignoreCase = true)
            }.toImmutableList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // ===== Table Data State =====

    private val _selectedTableName = MutableStateFlow<String?>(null)
    val selectedTableName: StateFlow<String?> = _selectedTableName

    private val _tableSchema = MutableStateFlow<ImmutableList<ColumnInfo>>(persistentListOf())
    val tableSchema: StateFlow<ImmutableList<ColumnInfo>> = _tableSchema

    private val _queryResult = MutableStateFlow<QueryResult?>(null)
    val queryResult: StateFlow<QueryResult?> = _queryResult

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _pageSize = MutableStateFlow(100)

    private val _isDataLoading = MutableStateFlow(false)
    val isDataLoading: StateFlow<Boolean> = _isDataLoading

    private val _showSchema = MutableStateFlow(false)
    val showSchema: StateFlow<Boolean> = _showSchema

    // ===== Query State =====

    private val _sqlQuery = MutableStateFlow("")
    val sqlQuery: StateFlow<String> = _sqlQuery

    private val _queryExecutionResult = MutableStateFlow<QueryResult?>(null)
    val queryExecutionResult: StateFlow<QueryResult?> = _queryExecutionResult

    private val _isQueryExecuting = MutableStateFlow(false)
    val isQueryExecuting: StateFlow<Boolean> = _isQueryExecuting

    private val _queryHistory = MutableStateFlow<ImmutableList<String>>(persistentListOf())
    val queryHistory: StateFlow<ImmutableList<String>> = _queryHistory

    init {
        loadDatabases()
    }

    // ===== Database Actions =====

    fun loadDatabases() {
        viewModelScope.launch {
            _isDatabasesLoading.value = true
            _databasesError.value = null

            try {
                val databases = withContext(Dispatchers.IO) {
                    repository.getDatabases()
                }
                _allDatabases.value = databases
            } catch (e: Exception) {
                _databasesError.value = e.message ?: "Failed to load databases"
            } finally {
                _isDatabasesLoading.value = false
            }
        }
    }

    fun onDatabaseSearchQueryChanged(query: String) {
        _databaseSearchQuery.value = query
    }

    fun selectDatabase(name: String) {
        _selectedDatabaseName.value = name
        _tableSearchQuery.value = ""
        loadTables()
    }

    fun clearDatabaseSelection() {
        _selectedDatabaseName.value = null
        _allTables.value = emptyList()
        clearTableSelection()
    }

    // ===== Table Actions =====

    fun loadTables() {
        val dbName = _selectedDatabaseName.value ?: return

        viewModelScope.launch {
            _isTablesLoading.value = true
            _tablesError.value = null

            try {
                val tables = withContext(Dispatchers.IO) {
                    repository.getTables(dbName)
                }
                _allTables.value = tables
            } catch (e: Exception) {
                _tablesError.value = e.message ?: "Failed to load tables"
            } finally {
                _isTablesLoading.value = false
            }
        }
    }

    fun onTableSearchQueryChanged(query: String) {
        _tableSearchQuery.value = query
    }

    fun selectTable(name: String) {
        _selectedTableName.value = name
        _currentPage.value = 0
        _showSchema.value = false
        loadTableSchema()
        loadTableData()
    }

    fun clearTableSelection() {
        _selectedTableName.value = null
        _tableSchema.value = persistentListOf()
        _queryResult.value = null
        _currentPage.value = 0
    }

    fun toggleSchema() {
        _showSchema.value = !_showSchema.value
    }

    // ===== Table Data Actions =====

    fun loadTableSchema() {
        val dbName = _selectedDatabaseName.value ?: return
        val tableName = _selectedTableName.value ?: return

        viewModelScope.launch {
            try {
                val schema = withContext(Dispatchers.IO) {
                    repository.getTableSchema(dbName, tableName)
                }
                _tableSchema.value = schema.toImmutableList()
            } catch (e: Exception) {
                // Schema loading is optional, don't show error
            }
        }
    }

    fun loadTableData() {
        val dbName = _selectedDatabaseName.value ?: return
        val tableName = _selectedTableName.value ?: return

        viewModelScope.launch {
            _isDataLoading.value = true

            try {
                val offset = _currentPage.value * _pageSize.value
                val result = withContext(Dispatchers.IO) {
                    repository.queryTable(dbName, tableName, _pageSize.value, offset)
                }
                _queryResult.value = result
            } catch (e: Exception) {
                _queryResult.value = QueryResult(
                    columns = emptyList(),
                    rows = emptyList(),
                    rowCount = 0,
                    error = e.message ?: "Failed to load data",
                )
            } finally {
                _isDataLoading.value = false
            }
        }
    }

    fun nextPage() {
        val result = _queryResult.value ?: return
        if (result.rowCount == _pageSize.value) {
            _currentPage.value += 1
            loadTableData()
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
            loadTableData()
        }
    }

    // ===== Query Actions =====

    fun onSqlQueryChanged(query: String) {
        _sqlQuery.value = query
    }

    fun executeQuery() {
        val dbName = _selectedDatabaseName.value ?: return
        val query = _sqlQuery.value.trim()

        if (query.isEmpty()) {
            _queryExecutionResult.value = QueryResult(
                columns = emptyList(),
                rows = emptyList(),
                rowCount = 0,
                error = "Query is empty",
            )
            return
        }

        viewModelScope.launch {
            _isQueryExecuting.value = true

            try {
                val result = withContext(Dispatchers.IO) {
                    repository.executeQuery(dbName, query)
                }
                _queryExecutionResult.value = result

                // Add to history if successful
                if (result.isSuccess && !_queryHistory.value.contains(query)) {
                    _queryHistory.value = (_queryHistory.value + query).takeLast(20).toImmutableList()
                }
            } catch (e: Exception) {
                _queryExecutionResult.value = QueryResult(
                    columns = emptyList(),
                    rows = emptyList(),
                    rowCount = 0,
                    error = e.message ?: "Query execution failed",
                )
            } finally {
                _isQueryExecuting.value = false
            }
        }
    }

    fun clearQuery() {
        _sqlQuery.value = ""
        _queryExecutionResult.value = null
    }

    fun selectQueryFromHistory(query: String) {
        _sqlQuery.value = query
    }

    fun setPrefilledQuery(tableName: String, queryType: String) {
        _sqlQuery.value = when (queryType) {
            "select" -> "SELECT * FROM `$tableName` LIMIT 10"
            "count" -> "SELECT COUNT(*) FROM `$tableName`"
            "schema" -> "PRAGMA table_info('$tableName')"
            else -> ""
        }
    }
}
