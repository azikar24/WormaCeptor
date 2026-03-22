package com.azikar24.wormaceptor.feature.database.vm

import android.app.Application
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.TableInfo
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val sampleDatabases = listOf(
        DatabaseInfo("app.db", "/data/app.db", 1024L, 5),
        DatabaseInfo("cache.db", "/data/cache.db", 512L, 2),
        DatabaseInfo("analytics.db", "/data/analytics.db", 2048L, 10),
    )

    private val sampleTables = listOf(
        TableInfo("users", 100L, 5),
        TableInfo("posts", 200L, 8),
        TableInfo("comments", 50L, 4),
    )

    private val sampleSchema = listOf(
        ColumnInfo("id", "INTEGER", isPrimaryKey = true, isNullable = false),
        ColumnInfo("name", "TEXT", isPrimaryKey = false, isNullable = false),
    )

    private val sampleQueryResult = QueryResult(
        columns = listOf("id", "name"),
        rows = listOf(listOf(1, "Alice"), listOf(2, "Bob")),
        rowCount = 2,
    )

    private val repository = mockk<DatabaseRepository>(relaxed = true) {
        every { getDatabases() } returns sampleDatabases
        every { getTables(any()) } returns sampleTables
        every { getTableSchema(any(), any()) } returns sampleSchema
        every { queryTable(any(), any(), any(), any()) } returns sampleQueryResult
        every { executeQuery(any(), any()) } returns sampleQueryResult
    }

    private val application = mockk<Application>(relaxed = true) {
        every { getString(any()) } returns "Error"
    }

    private lateinit var viewModel: DatabaseViewModel

    /**
     * Helper that keeps consuming items from the turbine until [predicate] is satisfied.
     * Needed because debounce + flowOn + withContext(Dispatchers.IO) produce async emissions.
     */
    private suspend fun <T> ReceiveTurbine<T>.awaitUntil(predicate: (T) -> Boolean): T {
        while (true) {
            val item = awaitItem()
            if (predicate(item)) return item
        }
    }

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DatabaseViewModel(repository, application)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class `initial state` {

        @Test
        fun `loads databases on init`() = runTest {
            viewModel.databases.test {
                awaitUntil { it.size == 3 }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `state has no selected database`() = runTest {
            viewModel.uiState.value.selectedDatabaseName shouldBe null
        }

        @Test
        fun `state is not loading after init completes`() = runTest {
            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `DatabaseSearchQueryChanged event` {

        @Test
        fun `updates database search query in state`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSearchQueryChanged("app"))

            viewModel.uiState.value.databaseSearchQuery shouldBe "app"
        }

        @Test
        fun `filters databases by name`() = runTest {
            viewModel.databases.test {
                awaitUntil { it.size == 3 }

                viewModel.sendEvent(DatabaseViewEvent.DatabaseSearchQueryChanged("app"))

                val dbs = awaitUntil { it.size == 1 }
                dbs.first().name shouldBe "app.db"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `filters databases by path`() = runTest {
            viewModel.databases.test {
                awaitUntil { it.size == 3 }

                viewModel.sendEvent(DatabaseViewEvent.DatabaseSearchQueryChanged("analytics"))

                val dbs = awaitUntil { it.size == 1 }
                dbs.first().name shouldBe "analytics.db"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `blank query shows all databases`() = runTest {
            viewModel.databases.test {
                awaitUntil { it.size == 3 }

                viewModel.sendEvent(DatabaseViewEvent.DatabaseSearchQueryChanged("app"))
                awaitUntil { it.size == 1 }

                viewModel.sendEvent(DatabaseViewEvent.DatabaseSearchQueryChanged(""))
                awaitUntil { it.size == 3 }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `DatabaseSelected event` {

        @Test
        fun `sets selected database name`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

            viewModel.uiState.value.selectedDatabaseName shouldBe "app.db"
        }

        @Test
        fun `clears table search query`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSearchQueryChanged("users"))
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("cache.db"))

            viewModel.uiState.value.tableSearchQuery shouldBe ""
        }

        @Test
        fun `loads tables for selected database`() = runTest {
            viewModel.tables.test {
                viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

                awaitUntil { it.size == 3 }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `DatabaseSelectionCleared event` {

        @Test
        fun `clears database selection`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelectionCleared)

            viewModel.uiState.value.selectedDatabaseName shouldBe null
        }

        @Test
        fun `clears tables`() = runTest {
            viewModel.tables.test {
                viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
                awaitUntil { it.size == 3 }

                viewModel.sendEvent(DatabaseViewEvent.DatabaseSelectionCleared)
                awaitUntil { it.isEmpty() }
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `TableSearchQueryChanged event` {

        @Test
        fun `updates table search query`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.TableSearchQueryChanged("users"))

            viewModel.uiState.value.tableSearchQuery shouldBe "users"
        }

        @Test
        fun `filters tables by name`() = runTest {
            viewModel.tables.test {
                viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
                awaitUntil { it.size == 3 }

                viewModel.sendEvent(DatabaseViewEvent.TableSearchQueryChanged("user"))
                val tables = awaitUntil { it.size == 1 }
                tables.first().name shouldBe "users"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `TableSelected event` {

        @Test
        fun `sets selected table name`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            viewModel.uiState.test {
                awaitUntil { it.selectedTableName == "users" }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `resets pagination to page 0`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            viewModel.uiState.test {
                awaitUntil { it.currentPage == 0 && it.selectedTableName == "users" }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `hides schema view`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.ToggleSchema)
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            viewModel.uiState.test {
                awaitUntil { it.selectedTableName == "users" && !it.showSchema }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `loads table schema`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            viewModel.uiState.test {
                awaitUntil { it.tableSchema.size == 2 }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `loads table data`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            viewModel.uiState.test {
                val state = awaitUntil { it.queryResult != null }
                state.queryResult shouldBe sampleQueryResult
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `TableSelectionCleared event` {

        @Test
        fun `clears table selection and schema`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            viewModel.uiState.test {
                awaitUntil { it.queryResult != null }

                viewModel.sendEvent(DatabaseViewEvent.TableSelectionCleared)

                val state = awaitUntil { it.selectedTableName == null }
                state.tableSchema shouldBe persistentListOf<ColumnInfo>()
                state.queryResult shouldBe null
                state.currentPage shouldBe 0
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `ToggleSchema event` {

        @Test
        fun `toggles schema visibility`() = runTest {
            viewModel.uiState.value.showSchema shouldBe false

            viewModel.sendEvent(DatabaseViewEvent.ToggleSchema)
            viewModel.uiState.value.showSchema shouldBe true

            viewModel.sendEvent(DatabaseViewEvent.ToggleSchema)
            viewModel.uiState.value.showSchema shouldBe false
        }
    }

    @Nested
    inner class `pagination` {

        @Test
        fun `NextPage increments page when result is full page`() = runTest {
            val fullPage = sampleQueryResult.copy(rowCount = 100)
            every { repository.queryTable(any(), any(), any(), any()) } returns fullPage

            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            viewModel.uiState.test {
                awaitUntil { it.queryResult != null && !it.isDataLoading }

                viewModel.sendEvent(DatabaseViewEvent.NextPage)
                awaitUntil { it.currentPage == 1 }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `NextPage does not increment when result is partial page`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            // Wait for table data to load
            viewModel.uiState.test {
                awaitUntil { it.queryResult != null && !it.isDataLoading }
                cancelAndIgnoreRemainingEvents()
            }

            // NextPage is a no-op when rowCount (2) < DefaultPageSize (100)
            viewModel.sendEvent(DatabaseViewEvent.NextPage)
            viewModel.uiState.value.currentPage shouldBe 0
        }

        @Test
        fun `PreviousPage decrements when page greater than 0`() = runTest {
            val fullPage = sampleQueryResult.copy(rowCount = 100)
            every { repository.queryTable(any(), any(), any(), any()) } returns fullPage

            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            viewModel.uiState.test {
                awaitUntil { it.queryResult != null && !it.isDataLoading }

                viewModel.sendEvent(DatabaseViewEvent.NextPage)
                awaitUntil { it.currentPage == 1 && !it.isDataLoading }

                viewModel.sendEvent(DatabaseViewEvent.PreviousPage)
                awaitUntil { it.currentPage == 0 }
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `PreviousPage does nothing at page 0`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            // Wait for table data to load
            viewModel.uiState.test {
                awaitUntil { it.queryResult != null && !it.isDataLoading }
                cancelAndIgnoreRemainingEvents()
            }

            // PreviousPage is a no-op at page 0
            viewModel.sendEvent(DatabaseViewEvent.PreviousPage)
            viewModel.uiState.value.currentPage shouldBe 0
        }
    }

    @Nested
    inner class `SqlQueryChanged event` {

        @Test
        fun `updates sql query in state`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged("SELECT * FROM users"))

            viewModel.uiState.value.sqlQuery shouldBe "SELECT * FROM users"
        }
    }

    @Nested
    inner class `ExecuteQuery event` {

        @Test
        fun `executes query via repository`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }

                viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged("SELECT * FROM users"))
                viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery)

                val state = awaitUntil { it.queryExecutionResult != null && !it.isQueryExecuting }
                state.queryExecutionResult shouldBe sampleQueryResult
                cancelAndIgnoreRemainingEvents()
            }

            verify { repository.executeQuery("app.db", "SELECT * FROM users") }
        }

        @Test
        fun `sets error for empty query`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }

                viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged(""))
                viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery)

                val state = awaitUntil { it.queryExecutionResult != null }
                state.queryExecutionResult?.isSuccess shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `sets error for whitespace-only query`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }

                viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged("   "))
                viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery)

                val state = awaitUntil { it.queryExecutionResult != null }
                state.queryExecutionResult?.isSuccess shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `does nothing when no database selected`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged("SELECT 1"))
            viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery)

            verify(exactly = 0) { repository.executeQuery(any(), any()) }
        }

        @Test
        fun `adds successful query to history`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }

                viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged("SELECT * FROM users"))
                viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery)

                val state = awaitUntil { it.queryHistory.isNotEmpty() }
                state.queryHistory shouldHaveSize 1
                state.queryHistory.first() shouldBe "SELECT * FROM users"
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `does not add duplicate query to history`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

            // Wait for init
            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }
                cancelAndIgnoreRemainingEvents()
            }

            // First query
            viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged("SELECT * FROM users"))
            viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery)
            viewModel.uiState.test {
                awaitUntil { it.queryHistory.isNotEmpty() && !it.isQueryExecuting }
                cancelAndIgnoreRemainingEvents()
            }

            // Second duplicate query
            viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery)
            viewModel.uiState.test {
                awaitUntil { !it.isQueryExecuting }
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.uiState.value.queryHistory shouldHaveSize 1
        }

        @Test
        fun `handles query execution error`() = runTest {
            every { repository.executeQuery(any(), any()) } throws IllegalStateException("DB error")

            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }

                viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged("BAD QUERY"))
                viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery)

                val state = awaitUntil { it.queryExecutionResult != null && !it.isQueryExecuting }
                state.queryExecutionResult?.isSuccess shouldBe false
                state.queryExecutionResult?.error shouldBe "DB error"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `ClearQuery event` {

        @Test
        fun `clears sql query and execution result`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }

                viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged("SELECT 1"))
                viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery)
                awaitUntil { it.queryExecutionResult != null && !it.isQueryExecuting }

                viewModel.sendEvent(DatabaseViewEvent.ClearQuery)

                val state = awaitUntil { it.sqlQuery.isEmpty() && it.queryExecutionResult == null }
                state.sqlQuery shouldBe ""
                state.queryExecutionResult shouldBe null
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `QuerySelectedFromHistory event` {

        @Test
        fun `populates sql query from history`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.QuerySelectedFromHistory("SELECT * FROM users"))

            viewModel.uiState.value.sqlQuery shouldBe "SELECT * FROM users"
        }
    }

    @Nested
    inner class `PrefilledQueryRequested event` {

        @Test
        fun `generates select query`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.PrefilledQueryRequested("users", "select"))

            viewModel.uiState.value.sqlQuery shouldContain "SELECT * FROM `users` LIMIT 10"
        }

        @Test
        fun `generates count query`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.PrefilledQueryRequested("users", "count"))

            viewModel.uiState.value.sqlQuery shouldContain "SELECT COUNT(*) FROM `users`"
        }

        @Test
        fun `generates schema query`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.PrefilledQueryRequested("users", "schema"))

            viewModel.uiState.value.sqlQuery shouldContain "PRAGMA table_info('users')"
        }

        @Test
        fun `generates empty string for unknown query type`() = runTest {
            viewModel.sendEvent(DatabaseViewEvent.PrefilledQueryRequested("users", "unknown"))

            viewModel.uiState.value.sqlQuery shouldBe ""
        }
    }

    @Nested
    inner class `error handling` {

        @Test
        fun `handles database loading error`() = runTest {
            every { repository.getDatabases() } throws IllegalStateException("Access denied")

            val vm = DatabaseViewModel(repository, application)

            vm.uiState.test {
                val state = awaitUntil { it.databasesError != null }
                state.databasesError shouldBe "Access denied"
                state.isDatabasesLoading shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `handles table loading error`() = runTest {
            every { repository.getTables(any()) } throws IllegalStateException("Table error")

            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))

            viewModel.uiState.test {
                val state = awaitUntil { it.tablesError != null }
                state.tablesError shouldBe "Table error"
                state.isTablesLoading shouldBe false
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `handles table data loading error`() = runTest {
            every { repository.queryTable(any(), any(), any(), any()) } throws IllegalStateException("Data error")

            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected("app.db"))
            viewModel.sendEvent(DatabaseViewEvent.TableSelected("users"))

            viewModel.uiState.test {
                val state = awaitUntil { it.queryResult != null && !it.isDataLoading }
                state.queryResult?.isSuccess shouldBe false
                state.queryResult?.error shouldBe "Data error"
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    inner class `LoadDatabases event` {

        @Test
        fun `reloads databases from repository`() = runTest {
            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.sendEvent(DatabaseViewEvent.LoadDatabases)

            viewModel.uiState.test {
                awaitUntil { !it.isDatabasesLoading }
                cancelAndIgnoreRemainingEvents()
            }

            verify(atLeast = 2) { repository.getDatabases() }
        }
    }
}
