package com.azikar24.wormaceptor.feature.threadviolation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.feature.threadviolation.ui.ThreadViolationScreen
import com.azikar24.wormaceptor.feature.threadviolation.vm.ThreadViolationViewModel

/**
 * Entry point for the Thread Violation Detection feature.
 */
object ThreadViolationFeature {

    /**
     * Creates a ThreadViolationEngine instance.
     */
    fun createEngine(historySize: Int = ThreadViolationEngine.DEFAULT_HISTORY_SIZE): ThreadViolationEngine {
        return ThreadViolationEngine(historySize)
    }

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
    engine: ThreadViolationEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val factory = remember(engine) { ThreadViolationFeature.createViewModelFactory(engine) }
    val viewModel: ThreadViolationViewModel = viewModel(factory = factory)

    val violations by viewModel.filteredViolations.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val selectedViolation by viewModel.selectedViolation.collectAsState()

    ThreadViolationScreen(
        violations = violations,
        stats = stats,
        isMonitoring = isMonitoring,
        selectedType = selectedType,
        selectedViolation = selectedViolation,
        onToggleMonitoring = viewModel::toggleMonitoring,
        onTypeSelected = viewModel::setSelectedType,
        onViolationSelected = viewModel::selectViolation,
        onDismissDetail = viewModel::dismissDetail,
        onClearViolations = viewModel::clearViolations,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
