package com.azikar24.wormaceptor.feature.preferences.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.feature.preferences.PreferencesFeature
import com.azikar24.wormaceptor.feature.preferences.ui.PreferenceDetailScreen
import com.azikar24.wormaceptor.feature.preferences.ui.PreferencesListScreen
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditSheet
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

    val files by viewModel.preferenceFiles.collectAsState()
    val fileSearchQuery by viewModel.fileSearchQuery.collectAsState()

    PreferencesListScreen(
        files = files,
        searchQuery = fileSearchQuery,
        onSearchQueryChanged = viewModel::onFileSearchQueryChanged,
        onFileClick = { file ->
            viewModel.selectFile(file.name)
            navController.navigate(WormaCeptorNavKeys.PreferencesDetail.route)
        },
        onNavigateBack = onNavigateBack,
    )
}

@Composable
private fun PreferencesDetailDestination(
    backStackEntry: NavBackStackEntry,
    navController: NavHostController,
    context: Context,
) {
    val viewModel = graphScopedViewModel(backStackEntry, navController, context)

    val items by viewModel.preferenceItems.collectAsState()
    val itemSearchQuery by viewModel.itemSearchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val availableTypes by viewModel.availableTypes.collectAsState()
    val totalItemCount by viewModel.totalItemCount.collectAsState()
    val selectedFileName by viewModel.selectedFileName.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<PreferenceItem?>(null) }

    val fileName = selectedFileName ?: return

    PreferenceDetailScreen(
        fileName = fileName,
        items = items,
        totalCount = totalItemCount,
        searchQuery = itemSearchQuery,
        typeFilter = typeFilter,
        availableTypes = availableTypes,
        onSearchQueryChanged = viewModel::onItemSearchQueryChanged,
        onTypeFilterChanged = viewModel::setTypeFilter,
        onBack = {
            viewModel.clearFileSelection()
            navController.popBackStack()
        },
        onEditItem = { item ->
            editingItem = item
            showEditDialog = true
        },
        onDeleteItem = viewModel::deletePreference,
        onClearAll = viewModel::clearCurrentFile,
        onCreateItem = {
            editingItem = null
            showEditDialog = true
        },
    )

    if (showEditDialog) {
        PreferencesEditSheetDialog(
            editingItem = editingItem,
            viewModel = viewModel,
            onDismiss = {
                showEditDialog = false
                editingItem = null
            },
        )
    }
}

@Composable
private fun PreferencesEditSheetDialog(
    editingItem: PreferenceItem?,
    viewModel: PreferencesViewModel,
    onDismiss: () -> Unit,
) {
    PreferenceEditSheet(
        item = editingItem,
        onDismiss = onDismiss,
        onSave = { key, value ->
            if (editingItem != null) {
                viewModel.setPreference(key, value)
            } else {
                viewModel.createPreference(key, value)
            }
            onDismiss()
        },
        onDelete = if (editingItem != null) {
            { key: String ->
                viewModel.deletePreference(key)
                onDismiss()
            }
        } else {
            null
        },
    )
}
