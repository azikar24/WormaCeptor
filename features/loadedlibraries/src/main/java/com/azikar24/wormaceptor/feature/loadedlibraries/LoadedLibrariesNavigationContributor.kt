package com.azikar24.wormaceptor.feature.loadedlibraries

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.google.auto.service.AutoService

/** Registers [LoadedLibraries] navigation routes with the main NavHost. */
@AutoService(FeatureNavigationContributor::class)
class LoadedLibrariesNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.LoadedLibraries.route) {
            LoadedLibrariesInspector(onNavigateBack = onBack)
        }
    }
}
