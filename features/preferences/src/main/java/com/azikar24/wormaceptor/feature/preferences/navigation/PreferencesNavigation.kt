package com.azikar24.wormaceptor.feature.preferences.navigation

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
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.PreferenceSource
import com.azikar24.wormaceptor.feature.preferences.PreferencesViewModelFactory
import com.azikar24.wormaceptor.feature.preferences.data.DataStoreDataSource
import com.azikar24.wormaceptor.feature.preferences.data.DataStoreRepositoryImpl
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesDataSource
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesRepositoryImpl
import com.azikar24.wormaceptor.feature.preferences.navigator.PreferencesNavigator
import com.azikar24.wormaceptor.feature.preferences.ui.PreferenceDetailScreen
import com.azikar24.wormaceptor.feature.preferences.ui.PreferencesListScreen
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewEvent
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewModel
import java.io.File

private data class StorageGraph(
    val source: PreferenceSource,
    val graphRoute: String,
    val listRoute: String,
    val detailRoute: String,
)

private val SharedPreferencesGraph = StorageGraph(
    source = PreferenceSource.SHARED_PREFERENCES,
    graphRoute = WormaCeptorNavKeys.Preferences.route,
    listRoute = WormaCeptorNavKeys.PreferencesList.route,
    detailRoute = WormaCeptorNavKeys.PreferencesDetail.route,
)

private val DataStoreGraph = StorageGraph(
    source = PreferenceSource.DATASTORE,
    graphRoute = WormaCeptorNavKeys.DataStore.route,
    listRoute = WormaCeptorNavKeys.DataStoreList.route,
    detailRoute = WormaCeptorNavKeys.DataStoreDetail.route,
)

/**
 * Adds the SharedPreferences Inspector navigation graph to the [NavGraphBuilder].
 * Scopes the [PreferencesViewModel] to the graph so it is shared across screens.
 */
fun NavGraphBuilder.preferencesGraph(
    navController: NavHostController,
    context: Context,
    onNavigateBack: () -> Unit,
) = storageGraph(SharedPreferencesGraph, navController, context, onNavigateBack)

/**
 * Adds the read-only Preferences DataStore Inspector navigation graph to the [NavGraphBuilder].
 * Reuses the preferences screens with a DataStore-backed repository.
 */
fun NavGraphBuilder.dataStoreGraph(
    navController: NavHostController,
    context: Context,
    onNavigateBack: () -> Unit,
) = storageGraph(DataStoreGraph, navController, context, onNavigateBack)

private fun NavGraphBuilder.storageGraph(
    graph: StorageGraph,
    navController: NavHostController,
    context: Context,
    onNavigateBack: () -> Unit,
) {
    navigation(startDestination = graph.listRoute, route = graph.graphRoute) {
        composable(graph.listRoute) { backStackEntry ->
            val viewModel = graphScopedViewModel(graph, backStackEntry, navController, context)
            BaseScreen(viewModel) { state, onEvent ->
                PreferencesListScreen(
                    state = state,
                    onEvent = onEvent,
                    onFileClick = { file -> onEvent(PreferencesViewEvent.List.Selected(file.name)) },
                    onNavigateBack = onNavigateBack,
                    source = graph.source,
                )
            }
        }
        composable(graph.detailRoute) { backStackEntry ->
            val viewModel = graphScopedViewModel(graph, backStackEntry, navController, context)
            BaseScreen(viewModel) { state, onEvent ->
                PreferenceDetailScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = { onEvent(PreferencesViewEvent.List.BackPressed) },
                )
            }
        }
    }
}

@Composable
private fun graphScopedViewModel(
    graph: StorageGraph,
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
): PreferencesViewModel {
    val graphEntry = remember(backStackEntry) { navController.getBackStackEntry(graph.graphRoute) }
    val repository = remember(context) { createRepository(graph.source, context.applicationContext) }
    val navigator = remember(navController) {
        object : PreferencesNavigator {
            override fun navigateToDetail() = navController.navigate(graph.detailRoute)

            override fun navigateBack() {
                navController.popBackStack()
            }
        }
    }
    val factory = remember(repository, navigator) { PreferencesViewModelFactory(repository, navigator) }
    return viewModel(viewModelStoreOwner = graphEntry, factory = factory)
}

private fun createRepository(
    source: PreferenceSource,
    appContext: Context,
): PreferencesRepository = when (source) {
    PreferenceSource.SHARED_PREFERENCES -> PreferencesRepositoryImpl(PreferencesDataSource(appContext))
    PreferenceSource.DATASTORE -> DataStoreRepositoryImpl(
        DataStoreDataSource { File(appContext.filesDir, "datastore") },
    )
}
