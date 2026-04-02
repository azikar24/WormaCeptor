package com.azikar24.wormaceptor.feature.preferences.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.feature.preferences.PreferencesFeature
import com.azikar24.wormaceptor.feature.preferences.ui.PreferenceDetailScreen
import com.azikar24.wormaceptor.feature.preferences.ui.PreferencesListScreen
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditSheet
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
    val repository = remember { PreferencesFeature.createRepository(context) }
    val factory = remember { PreferencesFeature.createViewModelFactory(repository) }
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
            files = state.preferenceFiles,
            searchQuery = state.fileSearchQuery,
            onSearchQueryChanged = { onEvent(PreferencesViewEvent.FileSearchQueryChanged(it)) },
            onFileClick = { file ->
                onEvent(PreferencesViewEvent.SelectFile(file.name))
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

    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<PreferenceItem?>(null) }

    BaseScreen(viewModel) { state, onEvent ->
        val fileName = state.selectedFileName ?: return@BaseScreen

        PreferenceDetailScreen(
            fileName = fileName,
            items = state.preferenceItems,
            totalCount = state.totalItemCount,
            searchQuery = state.itemSearchQuery,
            typeFilter = state.typeFilter,
            availableTypes = state.availableTypes,
            onSearchQueryChanged = { onEvent(PreferencesViewEvent.ItemSearchQueryChanged(it)) },
            onTypeFilterChanged = { onEvent(PreferencesViewEvent.SetTypeFilter(it)) },
            onBack = {
                onEvent(PreferencesViewEvent.ClearFileSelection)
                navController.popBackStack()
            },
            onEditItem = { item ->
                editingItem = item
                showEditDialog = true
            },
            onDeleteItem = { key -> onEvent(PreferencesViewEvent.DeletePreference(key)) },
            onClearAll = { onEvent(PreferencesViewEvent.ClearCurrentFile) },
            onCreateItem = {
                editingItem = null
                showEditDialog = true
            },
        )

        if (showEditDialog) {
            PreferencesEditSheetDialog(
                editingItem = editingItem,
                onEvent = onEvent,
                onDismiss = {
                    showEditDialog = false
                    editingItem = null
                },
            )
        }
    }
}

@Composable
private fun PreferencesEditSheetDialog(
    editingItem: PreferenceItem?,
    onEvent: (PreferencesViewEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    PreferenceEditSheet(
        item = editingItem,
        onDismiss = onDismiss,
        onSave = { key, value ->
            if (editingItem != null) {
                onEvent(PreferencesViewEvent.SetPreference(key, value))
            } else {
                onEvent(PreferencesViewEvent.CreatePreference(key, value))
            }
            onDismiss()
        },
        onDelete = if (editingItem != null) {
            { key: String ->
                onEvent(PreferencesViewEvent.DeletePreference(key))
                onDismiss()
            }
        } else {
            null
        },
    )
}
