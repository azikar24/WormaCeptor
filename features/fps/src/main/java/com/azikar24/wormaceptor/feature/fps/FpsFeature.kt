package com.azikar24.wormaceptor.feature.fps

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.feature.fps.ui.FpsScreen
import com.azikar24.wormaceptor.feature.fps.vm.FpsViewModel
import org.koin.compose.koinInject

/**
 * Entry point for the FPS Monitor feature.
 * Provides factory methods and composable access.
 */
object FpsFeature {

    /**
     * Creates an FpsViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(engine: FpsMonitorEngine): FpsViewModelFactory {
        return FpsViewModelFactory(engine)
    }
}

/**
 * Factory for creating FpsViewModel instances.
 */
class FpsViewModelFactory(
    private val engine: FpsMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FpsViewModel::class.java)) {
            return FpsViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the FPS Monitor feature.
 */
@Composable
fun FpsMonitor(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: FpsMonitorEngine = koinInject()
    val performanceOverlayEngine: PerformanceOverlayEngine = koinInject()
    LaunchedEffect(Unit) {
        performanceOverlayEngine.enableMetricForMonitorScreen(fps = true)
    }
    val factory = remember { FpsFeature.createViewModelFactory(engine) }
    val viewModel: FpsViewModel = viewModel(factory = factory)

    FpsScreen(
        viewModel = viewModel,
        onBack = { onNavigateBack?.invoke() },
        modifier = modifier,
    )
}
