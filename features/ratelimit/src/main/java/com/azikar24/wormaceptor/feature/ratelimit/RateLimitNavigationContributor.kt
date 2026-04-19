package com.azikar24.wormaceptor.feature.ratelimit

import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.RateLimitEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.ratelimit.ui.RateLimitScreen
import com.azikar24.wormaceptor.feature.ratelimit.vm.RateLimitViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class RateLimitNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.RateLimit.route) {
            val engine: RateLimitEngine = koinInject()
            val factory = remember(engine) { RateLimitViewModelFactory(engine) }
            val viewModel: RateLimitViewModel = viewModel(factory = factory)
            BaseScreen(viewModel) { state, onEvent ->
                RateLimitScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}
