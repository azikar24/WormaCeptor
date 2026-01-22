/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.database

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.feature.database.data.DatabaseDataSource
import com.azikar24.wormaceptor.feature.database.data.DatabaseRepositoryImpl
import com.azikar24.wormaceptor.feature.database.ui.DatabaseListScreen
import com.azikar24.wormaceptor.feature.database.ui.QueryScreen
import com.azikar24.wormaceptor.feature.database.ui.TableDataScreen
import com.azikar24.wormaceptor.feature.database.ui.TableListScreen
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

    // Collect state
    val databases by viewModel.databases.collectAsState()
    val databaseSearchQuery by viewModel.databaseSearchQuery.collectAsState()
    val isDatabasesLoading by viewModel.isDatabasesLoading.collectAsState()
    val databasesError by viewModel.databasesError.collectAsState()

    val selectedDatabaseName by viewModel.selectedDatabaseName.collectAsState()
    val tables by viewModel.tables.collectAsState()
    val tableSearchQuery by viewModel.tableSearchQuery.collectAsState()
    val isTablesLoading by viewModel.isTablesLoading.collectAsState()
    val tablesError by viewModel.tablesError.collectAsState()

    val selectedTableName by viewModel.selectedTableName.collectAsState()
    val tableSchema by viewModel.tableSchema.collectAsState()
    val queryResult by viewModel.queryResult.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val isDataLoading by viewModel.isDataLoading.collectAsState()
    val showSchema by viewModel.showSchema.collectAsState()

    val sqlQuery by viewModel.sqlQuery.collectAsState()
    val queryExecutionResult by viewModel.queryExecutionResult.collectAsState()
    val queryHistory by viewModel.queryHistory.collectAsState()
    val isQueryExecuting by viewModel.isQueryExecuting.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "databases",
        modifier = modifier,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300),
            ) + fadeOut(animationSpec = tween(300))
        },
    ) {
        composable("databases") {
            DatabaseListScreen(
                databases = databases,
                searchQuery = databaseSearchQuery,
                isLoading = isDatabasesLoading,
                error = databasesError,
                onSearchQueryChanged = viewModel::onDatabaseSearchQueryChanged,
                onDatabaseClick = { db ->
                    viewModel.selectDatabase(db.name)
                    navController.navigate("tables")
                },
                onRefresh = viewModel::loadDatabases,
                onBack = { onNavigateBack?.invoke() },
            )
        }

        composable("tables") {
            TableListScreen(
                databaseName = selectedDatabaseName ?: "",
                tables = tables,
                searchQuery = tableSearchQuery,
                isLoading = isTablesLoading,
                error = tablesError,
                onSearchQueryChanged = viewModel::onTableSearchQueryChanged,
                onTableClick = { table ->
                    viewModel.selectTable(table.name)
                    navController.navigate("data")
                },
                onQueryClick = {
                    navController.navigate("query")
                },
                onBack = {
                    viewModel.clearDatabaseSelection()
                    navController.popBackStack()
                },
            )
        }

        composable("data") {
            TableDataScreen(
                tableName = selectedTableName ?: "",
                queryResult = queryResult,
                schema = tableSchema,
                showSchema = showSchema,
                currentPage = currentPage,
                isLoading = isDataLoading,
                onToggleSchema = viewModel::toggleSchema,
                onPreviousPage = viewModel::previousPage,
                onNextPage = viewModel::nextPage,
                onBack = {
                    viewModel.clearTableSelection()
                    navController.popBackStack()
                },
            )
        }

        composable("query") {
            QueryScreen(
                databaseName = selectedDatabaseName ?: "",
                sqlQuery = sqlQuery,
                queryResult = queryExecutionResult,
                queryHistory = queryHistory,
                isExecuting = isQueryExecuting,
                onQueryChanged = viewModel::onSqlQueryChanged,
                onExecuteQuery = viewModel::executeQuery,
                onClearQuery = viewModel::clearQuery,
                onSelectFromHistory = viewModel::selectQueryFromHistory,
                onBack = {
                    viewModel.clearQuery()
                    navController.popBackStack()
                },
            )
        }
    }
}
