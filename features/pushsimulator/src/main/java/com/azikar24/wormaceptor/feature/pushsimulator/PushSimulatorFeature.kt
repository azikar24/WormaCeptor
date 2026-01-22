/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.pushsimulator

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.feature.pushsimulator.data.PushSimulatorDataSource
import com.azikar24.wormaceptor.feature.pushsimulator.data.PushSimulatorRepositoryImpl
import com.azikar24.wormaceptor.feature.pushsimulator.ui.PushSimulatorScreen
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewModel

/**
 * Entry point for the Push Notification Simulator feature.
 * Provides factory methods and composable access.
 */
object PushSimulatorFeature {

    /**
     * Creates a PushSimulatorEngine for the given context.
     */
    fun createEngine(context: Context): PushSimulatorEngine {
        return PushSimulatorEngine(context.applicationContext)
    }

    /**
     * Creates a PushSimulatorRepository instance for the given context.
     * Use this in your dependency injection setup.
     */
    fun createRepository(context: Context): PushSimulatorRepository {
        val dataSource = PushSimulatorDataSource(context.applicationContext)
        val engine = createEngine(context)
        return PushSimulatorRepositoryImpl(dataSource, engine)
    }

    /**
     * Creates a PushSimulatorViewModel factory.
     */
    fun createViewModelFactory(
        repository: PushSimulatorRepository,
        engine: PushSimulatorEngine,
    ): PushSimulatorViewModelFactory {
        return PushSimulatorViewModelFactory(repository, engine)
    }

    /**
     * Creates a PushSimulatorDataSource for template initialization.
     */
    fun createDataSource(context: Context): PushSimulatorDataSource {
        return PushSimulatorDataSource(context.applicationContext)
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
fun PushSimulator(context: Context, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine = remember { PushSimulatorFeature.createEngine(context) }
    val dataSource = remember { PushSimulatorFeature.createDataSource(context) }
    val repository = remember {
        PushSimulatorRepositoryImpl(dataSource, engine)
    }
    val factory = remember { PushSimulatorFeature.createViewModelFactory(repository, engine) }
    val viewModel: PushSimulatorViewModel = viewModel(factory = factory)

    // Initialize preset templates on first launch
    LaunchedEffect(Unit) {
        dataSource.initializePresets()
    }

    PushSimulatorScreen(
        viewModel = viewModel,
        onBack = { onNavigateBack?.invoke() },
        modifier = modifier,
    )
}
