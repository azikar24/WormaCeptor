package com.azikar24.wormaceptor.feature.mockrules

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.google.auto.service.AutoService

@AutoService(FeatureNavigationContributor::class)
class MockRulesNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.navigation(
            startDestination = WormaCeptorNavKeys.MockRules.route,
            route = "mockrules_graph",
        ) {
            composable(WormaCeptorNavKeys.MockRules.route) { backStackEntry ->
                MockRulesListDestination(
                    backStackEntry = backStackEntry,
                    navController = navController,
                    onBack = onBack,
                )
            }
            composable(WormaCeptorNavKeys.MockRuleEditor.route) { backStackEntry ->
                val ruleId = backStackEntry.arguments?.getString("ruleId")
                MockRuleEditorDestination(
                    ruleId = ruleId,
                    backStackEntry = backStackEntry,
                    navController = navController,
                )
            }
        }
    }
}
