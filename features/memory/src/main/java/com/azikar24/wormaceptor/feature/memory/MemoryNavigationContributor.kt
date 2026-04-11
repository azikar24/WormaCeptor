package com.azikar24.wormaceptor.feature.memory

import android.content.Context
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.memory.ui.MemoryScreen
import com.azikar24.wormaceptor.feature.memory.vm.MemoryViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class MemoryNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.Memory.route) {
            val engine: MemoryMonitorEngine = koinInject()
            val performanceOverlayEngine: PerformanceOverlayEngine = koinInject()
            LaunchedEffect(Unit) {
                performanceOverlayEngine.enableMetricForMonitorScreen(memory = true)
            }
            val factory = remember { MemoryViewModelFactory(engine) }
            val viewModel: MemoryViewModel = viewModel(factory = factory)
            BaseScreen(viewModel) { state, onEvent ->
                MemoryScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}
