package com.azikar24.wormaceptor.feature.leakdetection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.feature.leakdetection.ui.LeakDetectionScreen
import com.azikar24.wormaceptor.feature.leakdetection.vm.LeakDetectionViewEvent
import com.azikar24.wormaceptor.feature.leakdetection.vm.LeakDetectionViewModel
import org.koin.compose.koinInject

/**
 * Entry point for the Memory Leak Detection feature.
 * Provides factory methods and composable entry point.
 *
 * IMPORTANT: This feature should only be used in debug builds.
 * Leak detection adds overhead and should not be included in production.
 */
object LeakDetectionFeature {
    /**
     * Creates a LeakDetectionViewModel factory for use with viewModel().
     *
     * @param engine The LeakDetectionEngine instance to use
     * @return A ViewModelProvider.Factory for creating LeakDetectionViewModel
     */
    fun createViewModelFactory(engine: LeakDetectionEngine): LeakDetectionViewModelFactory {
        return LeakDetectionViewModelFactory(engine)
    }
}

/**
 * Factory for creating LeakDetectionViewModel instances.
 */
class LeakDetectionViewModelFactory(
    private val engine: LeakDetectionEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeakDetectionViewModel::class.java)) {
            return LeakDetectionViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Memory Leak Detection feature.
 * Displays detected memory leaks with filtering and detail capabilities.
 */
@Composable
fun LeakDetector(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: LeakDetectionEngine = koinInject()
    val factory = remember(engine) { LeakDetectionFeature.createViewModelFactory(engine) }
    val viewModel: LeakDetectionViewModel = viewModel(factory = factory)

    BaseScreen(viewModel) { state, onEvent ->
        LeakDetectionScreen(
            leaks = state.filteredLeaks,
            summary = state.summary,
            isRunning = state.isRunning,
            selectedSeverity = state.selectedSeverity,
            selectedLeak = state.selectedLeak,
            onSeveritySelected = { onEvent(LeakDetectionViewEvent.SelectSeverity(it)) },
            onLeakSelected = { onEvent(LeakDetectionViewEvent.SelectLeak(it)) },
            onDismissDetail = { onEvent(LeakDetectionViewEvent.DismissDetail) },
            onTriggerCheck = { onEvent(LeakDetectionViewEvent.TriggerCheck) },
            onClearLeaks = { onEvent(LeakDetectionViewEvent.ClearLeaks) },
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}
