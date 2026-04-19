package com.azikar24.wormaceptor.feature.preferences

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.feature.preferences.navigation.preferencesGraph
import com.google.auto.service.AutoService

/** Registers [Preferences] navigation routes with the main NavHost. */
@AutoService(FeatureNavigationContributor::class)
class PreferencesNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.preferencesGraph(
            navController = navController,
            context = context,
            onNavigateBack = onBack,
        )
    }
}
