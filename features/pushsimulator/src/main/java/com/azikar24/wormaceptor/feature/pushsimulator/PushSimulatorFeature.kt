package com.azikar24.wormaceptor.feature.pushsimulator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.feature.pushsimulator.ui.PushSimulatorScreen
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewModel
import org.koin.compose.koinInject

/**
 * Entry point for the Push Notification Simulator feature.
 * Provides factory methods and composable access.
 */
object PushSimulatorFeature {

    /**
     * Creates a PushSimulatorViewModel factory.
     */
    fun createViewModelFactory(
        repository: PushSimulatorRepository,
        engine: PushSimulatorEngine,
    ): PushSimulatorViewModelFactory {
        return PushSimulatorViewModelFactory(repository, engine)
    }
}

/**
 * Factory for creating PushSimulatorViewModel instances.
 */
class PushSimulatorViewModelFactory(
    private val repository: PushSimulatorRepository,
    private val engine: PushSimulatorEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PushSimulatorViewModel::class.java)) {
            return PushSimulatorViewModel(repository, engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Push Notification Simulator screen.
 */
@Composable
fun PushSimulator(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: PushSimulatorEngine = koinInject()
    val repository: PushSimulatorRepository = koinInject()
    val factory = remember { PushSimulatorFeature.createViewModelFactory(repository, engine) }
    val viewModel: PushSimulatorViewModel = viewModel(factory = factory)

    PushSimulatorScreen(
        viewModel = viewModel,
        onBack = { onNavigateBack?.invoke() },
        modifier = modifier,
    )
}
