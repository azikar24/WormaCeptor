/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.touchvisualization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.TouchVisualizationEngine
import com.azikar24.wormaceptor.feature.touchvisualization.ui.TouchVisualizationScreen
import com.azikar24.wormaceptor.feature.touchvisualization.vm.TouchVisualizationViewModel

/**
 * Entry point for the Touch Visualization feature.
 * Provides factory methods and composable access.
 */
object TouchVisualizationFeature {

    /**
     * Creates a TouchVisualizationEngine instance.
     * Use this in your dependency injection setup or to share across components.
     *
     * @param context Application context required for WindowManager access
     */
    fun createEngine(context: Context): TouchVisualizationEngine {
        return TouchVisualizationEngine(context.applicationContext)
    }

    /**
     * Creates a TouchVisualizationViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(engine: TouchVisualizationEngine): TouchVisualizationViewModelFactory {
        return TouchVisualizationViewModelFactory(engine)
    }
}

/**
 * Factory for creating TouchVisualizationViewModel instances.
 */
class TouchVisualizationViewModelFactory(
    private val engine: TouchVisualizationEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TouchVisualizationViewModel::class.java)) {
            return TouchVisualizationViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Touch Visualization feature.
 *
 * @param modifier Modifier for the root composable
 * @param onNavigateBack Optional callback for back navigation
 * @param engine Pre-created engine instance (required - must be created with application context)
 */
@Composable
fun TouchVisualization(
    engine: TouchVisualizationEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val factory = remember { TouchVisualizationFeature.createViewModelFactory(engine) }
    val viewModel: TouchVisualizationViewModel = viewModel(factory = factory)

    TouchVisualizationScreen(
        viewModel = viewModel,
        onBack = { onNavigateBack?.invoke() },
        modifier = modifier,
    )
}
