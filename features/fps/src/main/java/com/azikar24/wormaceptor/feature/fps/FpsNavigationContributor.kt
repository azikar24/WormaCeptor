package com.azikar24.wormaceptor.feature.fps

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.fps.ui.FpsScreen
import com.azikar24.wormaceptor.feature.fps.vm.FpsViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class FpsNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.Fps.route) {
            val engine: FpsMonitorEngine = koinInject()
            val performanceOverlayEngine: PerformanceOverlayEngine = koinInject()
            LaunchedEffect(Unit) {
                performanceOverlayEngine.enableMetricForMonitorScreen(fps = true)
            }
            val factory = remember { FpsViewModelFactory(engine) }
            val viewModel: FpsViewModel = viewModel(factory = factory)
            BaseScreen(viewModel) { state, onEvent ->
                FpsScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}
