package com.azikar24.wormaceptor.feature.dependenciesinspector

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.DependenciesInspectorScreen
import com.azikar24.wormaceptor.feature.dependenciesinspector.vm.DependenciesInspectorViewModel
import org.koin.compose.koinInject

/** Entry point for the dependencies inspector feature. */
object DependenciesInspectorFeature {

    /** Creates a [DependenciesInspectorViewModelFactory] bound to the given [engine]. */
    fun createViewModelFactory(engine: DependenciesInspectorEngine): DependenciesInspectorViewModelFactory {
        return DependenciesInspectorViewModelFactory(engine)
    }
}

/** Main composable for the dependencies inspector feature. */
@Composable
fun DependenciesInspector(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: DependenciesInspectorEngine = koinInject()
    val factory = remember { DependenciesInspectorFeature.createViewModelFactory(engine) }
    val viewModel: DependenciesInspectorViewModel = viewModel(factory = factory)

    BaseScreen(viewModel) { state, onEvent ->
        DependenciesInspectorScreen(
            state = state,
            onEvent = onEvent,
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}
