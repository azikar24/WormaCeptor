package com.azikar24.wormaceptor.feature.pushsimulator

import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.feature.pushsimulator.ui.PushSimulatorScreen
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class PushSimulatorNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.PushSimulator.route) {
            val engine: PushSimulatorEngine = koinInject()
            val repository: PushSimulatorRepository = koinInject()
            val factory = remember { PushSimulatorViewModelFactory(repository, engine) }
            val viewModel: PushSimulatorViewModel = viewModel(factory = factory)

            PushSimulatorScreen(
                viewModel = viewModel,
                onBack = onBack,
            )
        }
    }
}
