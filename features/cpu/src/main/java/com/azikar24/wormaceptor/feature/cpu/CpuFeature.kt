package com.azikar24.wormaceptor.feature.cpu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.feature.cpu.ui.CpuScreen
import com.azikar24.wormaceptor.feature.cpu.vm.CpuViewModel
import org.koin.compose.koinInject

/**
 * Entry point for the CPU Monitoring feature.
 * Provides factory methods and composable entry point.
 */
object CpuFeature {

    /**
     * Creates a CpuViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(engine: CpuMonitorEngine): CpuViewModelFactory {
        return CpuViewModelFactory(engine)
    }
}

/**
 * Factory for creating CpuViewModel instances.
 */
class CpuViewModelFactory(
    private val engine: CpuMonitorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CpuViewModel::class.java)) {
            return CpuViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the CPU Monitoring feature.
 * Displays real-time CPU usage with charts and controls.
 */
@Composable
fun CpuMonitor(modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine: CpuMonitorEngine = koinInject()
    val performanceOverlayEngine: PerformanceOverlayEngine = koinInject()
    LaunchedEffect(Unit) {
        performanceOverlayEngine.enableMetricForMonitorScreen(cpu = true)
    }
    val factory = remember { CpuFeature.createViewModelFactory(engine) }
    val viewModel: CpuViewModel = viewModel(factory = factory)

    // Collect state
    val currentCpu by viewModel.currentCpu.collectAsState()
    val cpuHistory by viewModel.cpuHistory.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()
    val isCpuWarning by viewModel.isCpuWarning.collectAsState()

    CpuScreen(
        currentCpu = currentCpu,
        cpuHistory = cpuHistory,
        isMonitoring = isMonitoring,
        isCpuWarning = isCpuWarning,
        onStartMonitoring = viewModel::startMonitoring,
        onStopMonitoring = viewModel::stopMonitoring,
        onClearHistory = viewModel::clearHistory,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
