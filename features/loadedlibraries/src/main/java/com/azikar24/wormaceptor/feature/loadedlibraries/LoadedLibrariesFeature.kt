package com.azikar24.wormaceptor.feature.loadedlibraries

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.LoadedLibrariesScreen
import com.azikar24.wormaceptor.feature.loadedlibraries.vm.LoadedLibrariesViewModel
import org.koin.compose.koinInject

object LoadedLibrariesFeature {

    fun createViewModelFactory(engine: LoadedLibrariesEngine): LoadedLibrariesViewModelFactory {
        return LoadedLibrariesViewModelFactory(engine)
    }
}

@Composable
fun LoadedLibrariesInspector(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: LoadedLibrariesEngine = koinInject()
    val factory = remember { LoadedLibrariesFeature.createViewModelFactory(engine) }
    val viewModel: LoadedLibrariesViewModel = viewModel(factory = factory)

    BaseScreen(viewModel) { state, onEvent ->
        LoadedLibrariesScreen(
            state = state,
            onEvent = onEvent,
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}
