/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewborders

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.ToolOverlayEngine
import com.azikar24.wormaceptor.core.engine.ViewBordersEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.feature.viewborders.data.ViewBordersDataStore
import com.azikar24.wormaceptor.feature.viewborders.ui.ViewBordersScreen
import com.azikar24.wormaceptor.feature.viewborders.vm.ViewBordersViewModel

/**
 * Entry point for the View Borders feature.
 *
 * This feature provides a visual debugging tool that draws colored borders
 * around views to help visualize the layout hierarchy, similar to browser
 * developer tools.
 *
 * Usage:
 * ```kotlin
 * // As a composable in navigation
 * ViewBorders(
 *     activity = activity,
 *     onNavigateBack = { navController.popBackStack() }
 * )
 *
 * // Or programmatically control the engine
 * val engine = ViewBordersFeature.createEngine()
 * engine.enable(activity)
 * engine.updateConfig(config)
 * ```
 */
object ViewBordersFeature {

    /**
     * Creates a ViewBordersEngine instance.
     * Use this for programmatic control of view borders outside of the settings UI.
     *
     * The engine can be shared across components if needed.
     *
     * @return A new ViewBordersEngine instance
     */
    fun createEngine(): ViewBordersEngine {
        return ViewBordersEngine()
    }

    /**
     * Creates a ViewBordersDataStore instance for the given context.
     * Use this for accessing persisted configuration.
     *
     * @param context Application context
     * @return A new ViewBordersDataStore instance
     */
    fun createDataStore(context: Context): ViewBordersDataStore {
        return ViewBordersDataStore(context.applicationContext)
    }

    /**
     * Creates a ViewBordersViewModel factory for use with viewModel().
     *
     * @param dataStore DataStore for persistence
     * @param engine Engine for rendering the overlay
     * @param toolOverlayEngine Engine for the floating toolbar
     * @return A ViewModelProvider.Factory for creating ViewBordersViewModel
     */
    fun createViewModelFactory(
        dataStore: ViewBordersDataStore,
        engine: ViewBordersEngine,
        toolOverlayEngine: ToolOverlayEngine,
    ): ViewBordersViewModelFactory {
        return ViewBordersViewModelFactory(dataStore, engine, toolOverlayEngine)
    }
}

/**
 * Factory for creating ViewBordersViewModel instances.
 */
class ViewBordersViewModelFactory(
    private val dataStore: ViewBordersDataStore,
    private val engine: ViewBordersEngine,
    private val toolOverlayEngine: ToolOverlayEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewBordersViewModel::class.java)) {
            return ViewBordersViewModel(dataStore, engine, toolOverlayEngine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the View Borders feature.
 *
 * Provides a settings UI for configuring the view borders overlay,
 * including enable/disable, border width, colors, and dimension display.
 *
 * @param activity The activity to attach the overlay to
 * @param modifier Modifier for the root composable
 * @param onNavigateBack Optional callback for back navigation
 * @param engine Optional pre-created engine instance. If null, uses Koin singleton.
 */
@Composable
fun ViewBorders(
    activity: Activity,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    engine: ViewBordersEngine? = null,
) {
    val context = activity.applicationContext
    val bordersEngine = remember { engine ?: WormaCeptorKoin.getKoin().get<ViewBordersEngine>() }
    val toolOverlayEngine = remember { WormaCeptorKoin.getKoin().get<ToolOverlayEngine>() }
    val dataStore = remember { ViewBordersFeature.createDataStore(context) }
    val factory = remember { ViewBordersFeature.createViewModelFactory(dataStore, bordersEngine, toolOverlayEngine) }
    val viewModel: ViewBordersViewModel = viewModel(factory = factory)

    ViewBordersScreen(
        viewModel = viewModel,
        activity = activity,
        onBack = { onNavigateBack?.invoke() },
        modifier = modifier,
    )
}
