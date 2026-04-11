package com.azikar24.wormaceptor.feature.cpu

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.cpu.ui.CpuScreen
import com.azikar24.wormaceptor.feature.cpu.vm.CpuViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class CpuNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.Cpu.route) {
            val engine: CpuMonitorEngine = koinInject()
            val performanceOverlayEngine: PerformanceOverlayEngine = koinInject()
            LaunchedEffect(Unit) {
                performanceOverlayEngine.enableMetricForMonitorScreen(cpu = true)
            }
            val factory = remember { CpuViewModelFactory(engine) }
            val viewModel: CpuViewModel = viewModel(factory = factory)
            BaseScreen(viewModel) { state, onEvent ->
                CpuScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}
