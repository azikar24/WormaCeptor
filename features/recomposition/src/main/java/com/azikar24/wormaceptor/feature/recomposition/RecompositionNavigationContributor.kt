package com.azikar24.wormaceptor.feature.recomposition

import android.content.Context
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.recomposition.ui.RecompositionSummaryScreen
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionViewModel
import com.google.auto.service.AutoService

@AutoService(FeatureNavigationContributor::class)
class RecompositionNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.Recomposition.route) {
            val viewModel: RecompositionViewModel = viewModel()
            RecompositionSummaryScreen(
                viewModel = viewModel,
                onBack = onBack,
            )
        }
    }
}
