/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.preferences

import android.content.Context
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesDataSource
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesRepositoryImpl
import com.azikar24.wormaceptor.feature.preferences.ui.PreferenceDetailScreen
import com.azikar24.wormaceptor.feature.preferences.ui.PreferencesListScreen
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditDialog
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewModel

/**
 * Entry point for the SharedPreferences Inspector feature.
 * Provides a factory method and composable navigation host.
 */
object PreferencesFeature {

    /**
     * Creates a PreferencesRepository instance for the given context.
     * Use this in your dependency injection setup.
     */
    fun createRepository(context: Context): PreferencesRepository {
        val dataSource = PreferencesDataSource(context.applicationContext)
        return PreferencesRepositoryImpl(dataSource)
    }

    /**
     * Creates a PreferencesViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(repository: PreferencesRepository): PreferencesViewModelFactory {
        return PreferencesViewModelFactory(repository)
    }
}

/**
 * Factory for creating PreferencesViewModel instances.
 */
class PreferencesViewModelFactory(
    private val repository: PreferencesRepository,
) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {
            return PreferencesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Preferences Inspector feature.
 * Handles navigation between list and detail screens.
 */
@Composable
fun PreferencesInspector(context: Context, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val repository = remember { PreferencesFeature.createRepository(context) }
    val factory = remember { PreferencesFeature.createViewModelFactory(repository) }
    val viewModel: PreferencesViewModel = viewModel(factory = factory)

    val navController = rememberNavController()

    PreferencesNavHost(
        navController = navController,
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
private fun PreferencesNavHost(
    navController: NavHostController,
    viewModel: PreferencesViewModel,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val files by viewModel.preferenceFiles.collectAsState()
    val fileSearchQuery by viewModel.fileSearchQuery.collectAsState()
    val items by viewModel.preferenceItems.collectAsState()
    val itemSearchQuery by viewModel.itemSearchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val availableTypes by viewModel.availableTypes.collectAsState()
    val totalItemCount by viewModel.totalItemCount.collectAsState()
    val selectedFileName by viewModel.selectedFileName.collectAsState()

    var showEditDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<PreferenceItem?>(null) }

    NavHost(
        navController = navController,
        startDestination = "list",
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
        composable("list") {
            PreferencesListScreen(
                files = files,
                searchQuery = fileSearchQuery,
                onSearchQueryChanged = viewModel::onFileSearchQueryChanged,
                onFileClick = { file ->
                    viewModel.selectFile(file.name)
                    navController.navigate("detail")
                },
                onNavigateBack = onNavigateBack,
            )
        }

        composable("detail") {
            val fileName = selectedFileName ?: return@composable

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
        }
    }

    // Edit/Create dialog
    if (showEditDialog) {
        PreferenceEditDialog(
            item = editingItem,
            onDismiss = {
                showEditDialog = false
                editingItem = null
            },
            onSave = { key, value ->
                if (editingItem != null) {
                    viewModel.setPreference(key, value)
                } else {
                    viewModel.createPreference(key, value)
                }
                showEditDialog = false
                editingItem = null
            },
        )
    }
}
