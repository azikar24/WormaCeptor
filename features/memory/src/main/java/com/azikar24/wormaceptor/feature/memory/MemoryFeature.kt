/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.memory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.feature.memory.ui.MemoryScreen
import com.azikar24.wormaceptor.feature.memory.vm.MemoryViewModel

/**
 * Entry point for the Memory Monitoring feature.
 * Provides factory methods and composable entry point.
 */
object MemoryFeature {

    /**
     * Creates a MemoryMonitorEngine instance.
     * Use this in your dependency injection setup or as a singleton.
     */
    fun createEngine(
        intervalMs: Long = MemoryMonitorEngine.DEFAULT_INTERVAL_MS,
        historySize: Int = MemoryMonitorEngine.DEFAULT_HISTORY_SIZE,
    ): MemoryMonitorEngine {
        return MemoryMonitorEngine(
            intervalMs = intervalMs,
            historySize = historySize,
        )
    }

    /**
     * Creates a MemoryViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(engine: MemoryMonitorEngine): MemoryViewModelFactory {
        return MemoryViewModelFactory(engine)
    }
}

/**
 * Factory for creating MemoryViewModel instances.
 */
class MemoryViewModelFactory(
    private val engine: MemoryMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MemoryViewModel::class.java)) {
            return MemoryViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Memory Monitoring feature.
 * Displays real-time memory usage with charts and controls.
 *
 * @param engine Pre-created engine instance (required - must be created at Activity/Application level for state persistence)
 * @param modifier Modifier for the root layout
 * @param onNavigateBack Optional callback for back navigation
 */
@Composable
fun MemoryMonitor(engine: MemoryMonitorEngine, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val factory = remember { MemoryFeature.createViewModelFactory(engine) }
    val viewModel: MemoryViewModel = viewModel(factory = factory)

    // Collect state
    val currentMemory by viewModel.currentMemory.collectAsState()
    val memoryHistory by viewModel.memoryHistory.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val isHeapWarning by viewModel.isHeapWarning.collectAsState()

    MemoryScreen(
        currentMemory = currentMemory,
        memoryHistory = memoryHistory,
        isMonitoring = isMonitoring,
        isHeapWarning = isHeapWarning,
        onStartMonitoring = viewModel::startMonitoring,
        onStopMonitoring = viewModel::stopMonitoring,
        onForceGc = viewModel::forceGc,
        onClearHistory = viewModel::clearHistory,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
