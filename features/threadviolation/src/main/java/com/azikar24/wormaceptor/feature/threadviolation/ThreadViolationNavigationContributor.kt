package com.azikar24.wormaceptor.feature.threadviolation

import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.threadviolation.ui.ThreadViolationScreen
import com.azikar24.wormaceptor.feature.threadviolation.vm.ThreadViolationViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class ThreadViolationNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.ThreadViolation.route) {
            val engine: ThreadViolationEngine = koinInject()
            val factory = remember(engine) { ThreadViolationViewModelFactory(engine) }
            val viewModel: ThreadViolationViewModel = viewModel(factory = factory)
            BaseScreen(viewModel) { state, onEvent ->
                ThreadViolationScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}
