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
import com.azikar24.wormaceptor.feature.threadviolation.vm.ThreadViolationViewModel
import org.koin.compose.koinInject

object ThreadViolationFeature {
    fun createViewModelFactory(engine: ThreadViolationEngine): ThreadViolationViewModelFactory {
        return ThreadViolationViewModelFactory(engine)
    }
}

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
            state = state,
            onEvent = onEvent,
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}
