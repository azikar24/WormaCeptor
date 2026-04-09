package com.azikar24.wormaceptor.feature.cpu

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.feature.cpu.ui.CpuScreen
import com.azikar24.wormaceptor.feature.cpu.vm.CpuViewModel
import org.koin.compose.koinInject

/** Entry point for the CPU monitoring feature. */
object CpuFeature {

    /** Creates a [CpuViewModelFactory] bound to the given [engine]. */
    fun createViewModelFactory(engine: CpuMonitorEngine): CpuViewModelFactory {
        return CpuViewModelFactory(engine)
    }
}

/** Main composable for the CPU monitoring feature. */
@Composable
fun CpuMonitor(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: CpuMonitorEngine = koinInject()
    val performanceOverlayEngine: PerformanceOverlayEngine = koinInject()
    LaunchedEffect(Unit) {
        performanceOverlayEngine.enableMetricForMonitorScreen(cpu = true)
    }
    val factory = remember { CpuFeature.createViewModelFactory(engine) }
    val viewModel: CpuViewModel = viewModel(factory = factory)

    BaseScreen(viewModel) { state, onEvent ->
        CpuScreen(
            state = state,
            onEvent = onEvent,
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}
