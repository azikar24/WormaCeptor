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
import com.azikar24.wormaceptor.feature.leakdetection.vm.LeakDetectionViewModel
import org.koin.compose.koinInject

object LeakDetectionFeature {
    fun createViewModelFactory(engine: LeakDetectionEngine): LeakDetectionViewModelFactory {
        return LeakDetectionViewModelFactory(engine)
    }
}

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

@Composable
fun LeakDetectionMonitor(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: LeakDetectionEngine = koinInject()
    val factory = remember(engine) { LeakDetectionFeature.createViewModelFactory(engine) }
    val viewModel: LeakDetectionViewModel = viewModel(factory = factory)

    BaseScreen(viewModel) { state, onEvent ->
        LeakDetectionScreen(
            state = state,
            onEvent = onEvent,
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}
