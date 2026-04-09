package com.azikar24.wormaceptor.feature.memory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.feature.memory.ui.MemoryScreen
import com.azikar24.wormaceptor.feature.memory.vm.MemoryViewModel
import org.koin.compose.koinInject

object MemoryFeature {

    fun createViewModelFactory(engine: MemoryMonitorEngine): MemoryViewModelFactory {
        return MemoryViewModelFactory(engine)
    }
}

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

@Composable
fun MemoryMonitor(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: MemoryMonitorEngine = koinInject()
    val performanceOverlayEngine: PerformanceOverlayEngine = koinInject()
    LaunchedEffect(Unit) {
        performanceOverlayEngine.enableMetricForMonitorScreen(memory = true)
    }
    val factory = remember { MemoryFeature.createViewModelFactory(engine) }
    val viewModel: MemoryViewModel = viewModel(factory = factory)

    BaseScreen(viewModel) { state, onEvent ->
        MemoryScreen(
            state = state,
            onEvent = onEvent,
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}
