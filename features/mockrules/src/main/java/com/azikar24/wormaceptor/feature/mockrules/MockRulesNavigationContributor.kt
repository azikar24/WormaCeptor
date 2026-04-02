package com.azikar24.wormaceptor.feature.mockrules

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.google.auto.service.AutoService

/** Registers Mock Rules navigation routes with the main NavHost. */
@AutoService(FeatureNavigationContributor::class)
class MockRulesNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.MockRules.route) {
            MockRulesList(
                onNavigateBack = onBack,
                onNavigateToEditor = { ruleId ->
                    if (ruleId != null) {
                        navController.navigate(WormaCeptorNavKeys.MockRuleEditor.createRoute(ruleId))
                    } else {
                        navController.navigate(WormaCeptorNavKeys.MockRuleEditor.createNewRoute())
                    }
                },
            )
        }

        builder.composable(WormaCeptorNavKeys.MockRuleEditor.route) { backStackEntry ->
            val ruleId = backStackEntry.arguments?.getString("ruleId")
            MockRuleEditor(
                ruleId = ruleId,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
