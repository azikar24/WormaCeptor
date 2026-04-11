package com.azikar24.wormaceptor.feature.leakdetection

import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.leakdetection.ui.LeakDetectionScreen
import com.azikar24.wormaceptor.feature.leakdetection.vm.LeakDetectionViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class LeakDetectionNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.LeakDetection.route) {
            val engine: LeakDetectionEngine = koinInject()
            val factory = remember(engine) { LeakDetectionViewModelFactory(engine) }
            val viewModel: LeakDetectionViewModel = viewModel(factory = factory)
            BaseScreen(viewModel) { state, onEvent ->
                LeakDetectionScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}
