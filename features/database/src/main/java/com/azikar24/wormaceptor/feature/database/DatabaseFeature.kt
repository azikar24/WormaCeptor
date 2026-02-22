package com.azikar24.wormaceptor.feature.database

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavTransitions
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.feature.database.data.DatabaseDataSource
import com.azikar24.wormaceptor.feature.database.data.DatabaseRepositoryImpl
import com.azikar24.wormaceptor.feature.database.ui.DatabaseListScreen
import com.azikar24.wormaceptor.feature.database.ui.QueryScreen
import com.azikar24.wormaceptor.feature.database.ui.TableDataScreen
import com.azikar24.wormaceptor.feature.database.ui.TableListScreen
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewEvent
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewModel

/**
 * Entry point for the SQLite Database Browser feature.
 * Provides factory methods and composable navigation host.
 */
object DatabaseFeature {

    /**
     * Creates a DatabaseRepository instance for the given context.
     * Use this in your dependency injection setup.
     */
    fun createRepository(context: Context): DatabaseRepository {
        val dataSource = DatabaseDataSource(context.applicationContext)
        return DatabaseRepositoryImpl(dataSource)
    }

    /**
     * Creates a DatabaseViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(repository: DatabaseRepository): DatabaseViewModelFactory {
        return DatabaseViewModelFactory(repository)
    }
}

/**
 * Factory for creating DatabaseViewModel instances.
 */
class DatabaseViewModelFactory(
    private val repository: DatabaseRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
            return DatabaseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Database Browser feature.
 * Handles navigation between database list, table list, and table data screens.
 */
@Composable
fun DatabaseBrowser(context: Context, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val repository = remember { DatabaseFeature.createRepository(context) }
    val factory = remember { DatabaseFeature.createViewModelFactory(repository) }
    val viewModel: DatabaseViewModel = viewModel(factory = factory)
    val navController = rememberNavController()

    val state by viewModel.uiState.collectAsState()
    val databases by viewModel.databases.collectAsState()
    val tables by viewModel.tables.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "databases",
        modifier = modifier,
        enterTransition = WormaCeptorNavTransitions.enterTransition,
        exitTransition = WormaCeptorNavTransitions.exitTransition,
        popEnterTransition = WormaCeptorNavTransitions.popEnterTransition,
        popExitTransition = WormaCeptorNavTransitions.popExitTransition,
    ) {
        composable("databases") {
            DatabaseListScreen(
                databases = databases,
                searchQuery = state.databaseSearchQuery,
                isLoading = state.isDatabasesLoading,
                error = state.databasesError,
                onSearchQueryChanged = { viewModel.sendEvent(DatabaseViewEvent.DatabaseSearchQueryChanged(it)) },
                onDatabaseClick = { db ->
                    viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected(db.name))
                    navController.navigate("tables")
                },
                onRefresh = { viewModel.sendEvent(DatabaseViewEvent.LoadDatabases) },
                onBack = { onNavigateBack?.invoke() },
            )
        }

        composable("tables") {
            TableListScreen(
                databaseName = state.selectedDatabaseName ?: "",
                tables = tables,
                searchQuery = state.tableSearchQuery,
                isLoading = state.isTablesLoading,
                error = state.tablesError,
                onSearchQueryChanged = { viewModel.sendEvent(DatabaseViewEvent.TableSearchQueryChanged(it)) },
                onTableClick = { table ->
                    viewModel.sendEvent(DatabaseViewEvent.TableSelected(table.name))
                    navController.navigate("data")
                },
                onQueryClick = {
                    navController.navigate("query")
                },
                onBack = {
                    viewModel.sendEvent(DatabaseViewEvent.DatabaseSelectionCleared)
                    navController.popBackStack()
                },
            )
        }

        composable("data") {
            TableDataScreen(
                tableName = state.selectedTableName ?: "",
                queryResult = state.queryResult,
                schema = state.tableSchema,
                showSchema = state.showSchema,
                currentPage = state.currentPage,
                isLoading = state.isDataLoading,
                onToggleSchema = { viewModel.sendEvent(DatabaseViewEvent.ToggleSchema) },
                onPreviousPage = { viewModel.sendEvent(DatabaseViewEvent.PreviousPage) },
                onNextPage = { viewModel.sendEvent(DatabaseViewEvent.NextPage) },
                onBack = {
                    viewModel.sendEvent(DatabaseViewEvent.TableSelectionCleared)
                    navController.popBackStack()
                },
            )
        }

        composable("query") {
            QueryScreen(
                databaseName = state.selectedDatabaseName ?: "",
                sqlQuery = state.sqlQuery,
                queryResult = state.queryExecutionResult,
                queryHistory = state.queryHistory,
                isExecuting = state.isQueryExecuting,
                onQueryChanged = { viewModel.sendEvent(DatabaseViewEvent.SqlQueryChanged(it)) },
                onExecuteQuery = { viewModel.sendEvent(DatabaseViewEvent.ExecuteQuery) },
                onClearQuery = { viewModel.sendEvent(DatabaseViewEvent.ClearQuery) },
                onSelectFromHistory = { viewModel.sendEvent(DatabaseViewEvent.QuerySelectedFromHistory(it)) },
                onBack = {
                    viewModel.sendEvent(DatabaseViewEvent.ClearQuery)
                    navController.popBackStack()
                },
            )
        }
    }
}
