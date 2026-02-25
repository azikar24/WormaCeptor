package com.azikar24.wormaceptor.feature.logs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.feature.logs.ui.LogsScreen
import com.azikar24.wormaceptor.feature.logs.vm.LogsViewModel
import org.koin.compose.koinInject

object LogsFeature {
    fun createViewModelFactory(engine: LogCaptureEngine): LogsViewModelFactory = LogsViewModelFactory(engine)
}

class LogsViewModelFactory(
    private val engine: LogCaptureEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LogsViewModel::class.java)) {
            return LogsViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

@Composable
fun LogViewer(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: LogCaptureEngine = koinInject()
    val factory = remember { LogsFeature.createViewModelFactory(engine) }
    val logsViewModel: LogsViewModel = viewModel(factory = factory)
    LogsScreen(
        viewModel = logsViewModel,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
