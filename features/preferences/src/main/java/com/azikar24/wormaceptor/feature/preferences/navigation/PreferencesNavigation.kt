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
import com.azikar24.wormaceptor.feature.preferences.PreferencesViewModelFactory
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesDataSource
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesRepositoryImpl
import com.azikar24.wormaceptor.feature.preferences.ui.PreferenceDetailScreen
import com.azikar24.wormaceptor.feature.preferences.ui.PreferencesListScreen
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewEvent
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewModel

/**
 * Adds the Preferences Inspector navigation graph to the [NavGraphBuilder].
 * Scopes the [PreferencesViewModel] to the graph so it is shared across screens.
 */
fun NavGraphBuilder.preferencesGraph(
    navController: NavHostController,
    context: Context,
    onNavigateBack: () -> Unit,
) {
    navigation(
        startDestination = WormaCeptorNavKeys.PreferencesList.route,
        route = WormaCeptorNavKeys.Preferences.route,
    ) {
        composable(WormaCeptorNavKeys.PreferencesList.route) { backStackEntry ->
            PreferencesListDestination(backStackEntry, navController, context, onNavigateBack)
        }
        composable(WormaCeptorNavKeys.PreferencesDetail.route) { backStackEntry ->
            PreferencesDetailDestination(backStackEntry, navController, context)
        }
    }
}

@Composable
private fun graphScopedViewModel(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
): PreferencesViewModel {
    val graphEntry = remember(backStackEntry) {
        navController.getBackStackEntry(WormaCeptorNavKeys.Preferences.route)
    }
    val repository = remember(context) { PreferencesRepositoryImpl(PreferencesDataSource(context.applicationContext)) }
    val factory = remember(repository) { PreferencesViewModelFactory(repository) }
    return viewModel(viewModelStoreOwner = graphEntry, factory = factory)
}

@Composable
private fun PreferencesListDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
    onNavigateBack: () -> Unit,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, context)

    BaseScreen(viewModel) { state, onEvent ->
        PreferencesListScreen(
            state = state,
            onEvent = onEvent,
            onFileClick = { file ->
                onEvent(PreferencesViewEvent.List.Selected(file.name))
                navController.navigate(WormaCeptorNavKeys.PreferencesDetail.route)
            },
            onNavigateBack = onNavigateBack,
        )
    }
}

@Composable
private fun PreferencesDetailDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, context)

    BaseScreen(viewModel) { state, onEvent ->
        PreferenceDetailScreen(
            state = state,
            onEvent = onEvent,
            onBack = {
                onEvent(PreferencesViewEvent.List.SelectionCleared)
                navController.popBackStack()
            },
        )
    }
}
