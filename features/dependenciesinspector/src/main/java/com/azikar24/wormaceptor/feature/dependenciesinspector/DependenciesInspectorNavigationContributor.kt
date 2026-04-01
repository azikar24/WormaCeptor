package com.azikar24.wormaceptor.feature.dependenciesinspector

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys

/** Registers [DependenciesInspector] navigation routes with the main NavHost. */
class DependenciesInspectorNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.Dependencies.route) {
            DependenciesInspector(onNavigateBack = onBack)
        }
    }
}
