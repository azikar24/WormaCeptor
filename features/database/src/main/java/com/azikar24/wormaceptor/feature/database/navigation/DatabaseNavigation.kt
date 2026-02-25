package com.azikar24.wormaceptor.feature.database.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.database.DatabaseFeature
import com.azikar24.wormaceptor.feature.database.ui.DatabaseListScreen
import com.azikar24.wormaceptor.feature.database.ui.QueryScreen
import com.azikar24.wormaceptor.feature.database.ui.TableDataScreen
import com.azikar24.wormaceptor.feature.database.ui.TableListScreen
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewEvent
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewModel

/**
 * Adds the Database Browser navigation graph to the [NavGraphBuilder].
 * Scopes the [DatabaseViewModel] to the graph so it is shared across screens.
 */
fun NavGraphBuilder.databaseGraph(
    navController: NavHostController,
    context: Context,
    onNavigateBack: () -> Unit,
) {
    navigation(
        startDestination = WormaCeptorNavKeys.DatabaseList.route,
        route = WormaCeptorNavKeys.Database.route,
    ) {
        composable(WormaCeptorNavKeys.DatabaseList.route) { backStackEntry ->
            DatabaseListDestination(backStackEntry, navController, context, onNavigateBack)
        }
        composable(WormaCeptorNavKeys.DatabaseTables.route) { backStackEntry ->
            DatabaseTablesDestination(backStackEntry, navController, context)
        }
        composable(WormaCeptorNavKeys.DatabaseTableData.route) { backStackEntry ->
            DatabaseTableDataDestination(backStackEntry, navController, context)
        }
        composable(WormaCeptorNavKeys.DatabaseQuery.route) { backStackEntry ->
            DatabaseQueryDestination(backStackEntry, navController, context)
        }
    }
}

@Composable
private fun graphScopedViewModel(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
): DatabaseViewModel {
    val graphEntry = remember(backStackEntry) {
        navController.getBackStackEntry(WormaCeptorNavKeys.Database.route)
    }
    val repository = remember { DatabaseFeature.createRepository(context) }
    val factory = remember { DatabaseFeature.createViewModelFactory(repository) }
    return viewModel(viewModelStoreOwner = graphEntry, factory = factory)
}

@Composable
private fun DatabaseListDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
    onNavigateBack: () -> Unit,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, context)
    val state by viewModel.uiState.collectAsState()
    val databases by viewModel.databases.collectAsState()

    DatabaseListScreen(
        databases = databases,
        searchQuery = state.databaseSearchQuery,
        isLoading = state.isDatabasesLoading,
        error = state.databasesError,
        onSearchQueryChanged = { viewModel.sendEvent(DatabaseViewEvent.DatabaseSearchQueryChanged(it)) },
        onDatabaseClick = { db ->
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelected(db.name))
            navController.navigate(WormaCeptorNavKeys.DatabaseTables.route)
        },
        onRefresh = { viewModel.sendEvent(DatabaseViewEvent.LoadDatabases) },
        onBack = onNavigateBack,
    )
}

@Composable
private fun DatabaseTablesDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, context)
    val state by viewModel.uiState.collectAsState()
    val tables by viewModel.tables.collectAsState()

    TableListScreen(
        databaseName = state.selectedDatabaseName ?: "",
        tables = tables,
        searchQuery = state.tableSearchQuery,
        isLoading = state.isTablesLoading,
        error = state.tablesError,
        onSearchQueryChanged = { viewModel.sendEvent(DatabaseViewEvent.TableSearchQueryChanged(it)) },
        onTableClick = { table ->
            viewModel.sendEvent(DatabaseViewEvent.TableSelected(table.name))
            navController.navigate(WormaCeptorNavKeys.DatabaseTableData.route)
        },
        onQueryClick = {
            navController.navigate(WormaCeptorNavKeys.DatabaseQuery.route)
        },
        onBack = {
            viewModel.sendEvent(DatabaseViewEvent.DatabaseSelectionCleared)
            navController.popBackStack()
        },
    )
}

@Composable
private fun DatabaseTableDataDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, context)
    val state by viewModel.uiState.collectAsState()

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

@Composable
private fun DatabaseQueryDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, context)
    val state by viewModel.uiState.collectAsState()

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
