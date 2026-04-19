package com.azikar24.wormaceptor.feature.dependenciesinspector

import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.DependenciesInspectorScreen
import com.azikar24.wormaceptor.feature.dependenciesinspector.vm.DependenciesInspectorViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class DependenciesInspectorNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.Dependencies.route) {
            val engine: DependenciesInspectorEngine = koinInject()
            val factory = remember { DependenciesInspectorViewModelFactory(engine) }
            val viewModel: DependenciesInspectorViewModel = viewModel(factory = factory)
            BaseScreen(viewModel) { state, onEvent ->
                DependenciesInspectorScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}
