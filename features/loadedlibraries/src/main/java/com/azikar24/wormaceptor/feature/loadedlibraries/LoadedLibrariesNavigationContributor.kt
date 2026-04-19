package com.azikar24.wormaceptor.feature.loadedlibraries

import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.LoadedLibrariesScreen
import com.azikar24.wormaceptor.feature.loadedlibraries.vm.LoadedLibrariesViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class LoadedLibrariesNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.LoadedLibraries.route) {
            val engine: LoadedLibrariesEngine = koinInject()
            val factory = remember { LoadedLibrariesViewModelFactory(engine) }
            val viewModel: LoadedLibrariesViewModel = viewModel(factory = factory)
            BaseScreen(viewModel) { state, onEvent ->
                LoadedLibrariesScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}
