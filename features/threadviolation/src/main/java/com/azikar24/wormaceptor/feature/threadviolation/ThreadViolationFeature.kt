package com.azikar24.wormaceptor.feature.threadviolation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.feature.threadviolation.ui.ThreadViolationScreen
import com.azikar24.wormaceptor.feature.threadviolation.vm.ThreadViolationViewEvent
import com.azikar24.wormaceptor.feature.threadviolation.vm.ThreadViolationViewModel
import org.koin.compose.koinInject

/**
 * Entry point for the Thread Violation Detection feature.
 */
object ThreadViolationFeature {
    /**
     * Creates a ThreadViolationViewModel factory.
     */
    fun createViewModelFactory(engine: ThreadViolationEngine): ThreadViolationViewModelFactory {
        return ThreadViolationViewModelFactory(engine)
    }
}

/**
 * Factory for creating ThreadViolationViewModel instances.
 */
class ThreadViolationViewModelFactory(
    private val engine: ThreadViolationEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThreadViolationViewModel::class.java)) {
            return ThreadViolationViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Thread Violation Detection feature.
 */
@Composable
fun ThreadViolationMonitor(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: ThreadViolationEngine = koinInject()
    val factory = remember(engine) { ThreadViolationFeature.createViewModelFactory(engine) }
    val viewModel: ThreadViolationViewModel = viewModel(factory = factory)

    BaseScreen(viewModel) { state, onEvent ->
        ThreadViolationScreen(
            violations = state.filteredViolations,
            stats = state.stats,
            isMonitoring = state.isMonitoring,
            selectedType = state.selectedType,
            selectedViolation = state.selectedViolation,
            onToggleMonitoring = { onEvent(ThreadViolationViewEvent.ToggleMonitoring) },
            onTypeSelected = { onEvent(ThreadViolationViewEvent.SelectType(it)) },
            onViolationSelected = { onEvent(ThreadViolationViewEvent.SelectViolation(it)) },
            onDismissDetail = { onEvent(ThreadViolationViewEvent.DismissDetail) },
            onClearViolations = { onEvent(ThreadViolationViewEvent.ClearViolations) },
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}
