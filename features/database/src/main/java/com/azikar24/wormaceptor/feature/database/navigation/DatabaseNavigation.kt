package com.azikar24.wormaceptor.feature.database.navigation

import android.app.Application
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.database.DatabaseViewModelFactory
import com.azikar24.wormaceptor.feature.database.data.DatabaseDataSource
import com.azikar24.wormaceptor.feature.database.data.DatabaseRepositoryImpl
import com.azikar24.wormaceptor.feature.database.navigator.DatabaseNavigator
import com.azikar24.wormaceptor.feature.database.ui.DatabaseListScreen
import com.azikar24.wormaceptor.feature.database.ui.QueryScreen
import com.azikar24.wormaceptor.feature.database.ui.TableDataScreen
import com.azikar24.wormaceptor.feature.database.ui.TableListScreen
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewEvent
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewModel

fun NavGraphBuilder.databaseGraph(
    navController: NavHostController,
    context: Context,
    onNavigateBack: () -> Unit,
) {
    val application = requireNotNull(context.applicationContext as? Application) {
        "applicationContext must be an Application instance"
    }
    val repository by lazy { DatabaseRepositoryImpl(DatabaseDataSource(context.applicationContext)) }
    val navigator by lazy {
        object : DatabaseNavigator {
            override fun navigateToTables() = navController.navigate(WormaCeptorNavKeys.DatabaseTables.route)

            override fun navigateToTableData() = navController.navigate(WormaCeptorNavKeys.DatabaseTableData.route)

            override fun navigateToQuery() = navController.navigate(WormaCeptorNavKeys.DatabaseQuery.route)

            override fun navigateBack() {
                navController.popBackStack()
            }
        }
    }
    val factory by lazy { DatabaseViewModelFactory(repository, application, navigator) }

    navigation(
        startDestination = WormaCeptorNavKeys.DatabaseList.route,
        route = WormaCeptorNavKeys.Database.route,
    ) {
        composable(WormaCeptorNavKeys.DatabaseList.route) { backStackEntry ->
            DatabaseListDestination(backStackEntry, navController, factory, onNavigateBack)
        }
        composable(WormaCeptorNavKeys.DatabaseTables.route) { backStackEntry ->
            DatabaseTablesDestination(backStackEntry, navController, factory)
        }
        composable(WormaCeptorNavKeys.DatabaseTableData.route) { backStackEntry ->
            DatabaseTableDataDestination(backStackEntry, navController, factory)
        }
        composable(WormaCeptorNavKeys.DatabaseQuery.route) { backStackEntry ->
            DatabaseQueryDestination(backStackEntry, navController, factory)
        }
    }
}

@Composable
private fun graphScopedViewModel(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    factory: DatabaseViewModelFactory,
): DatabaseViewModel {
    val graphEntry = remember(backStackEntry) {
        navController.getBackStackEntry(WormaCeptorNavKeys.Database.route)
    }
    return viewModel(viewModelStoreOwner = graphEntry, factory = factory)
}

@Composable
private fun DatabaseListDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    factory: DatabaseViewModelFactory,
    onNavigateBack: () -> Unit,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, factory)

    BaseScreen(viewModel) { state, onEvent ->
        DatabaseListScreen(
            state = state,
            onEvent = onEvent,
            onDatabaseClick = { db -> onEvent(DatabaseViewEvent.List.Selected(db.name)) },
            onBack = onNavigateBack,
        )
    }
}

@Composable
private fun DatabaseTablesDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    factory: DatabaseViewModelFactory,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, factory)

    BaseScreen(viewModel) { state, onEvent ->
        TableListScreen(
            state = state,
            onEvent = onEvent,
            onTableClick = { table -> onEvent(DatabaseViewEvent.Tables.Selected(table.name)) },
            onQueryClick = { onEvent(DatabaseViewEvent.Tables.NavigateToQuery) },
            onBack = { onEvent(DatabaseViewEvent.Tables.BackPressed) },
        )
    }
}

@Composable
private fun DatabaseTableDataDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    factory: DatabaseViewModelFactory,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, factory)

    BaseScreen(viewModel) { state, onEvent ->
        TableDataScreen(
            state = state,
            onEvent = onEvent,
            onBack = { onEvent(DatabaseViewEvent.Data.BackPressed) },
        )
    }
}

@Composable
private fun DatabaseQueryDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    factory: DatabaseViewModelFactory,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, factory)

    BaseScreen(viewModel) { state, onEvent ->
        QueryScreen(
            state = state,
            onEvent = onEvent,
            onBack = { onEvent(DatabaseViewEvent.Query.BackPressed) },
        )
    }
}
