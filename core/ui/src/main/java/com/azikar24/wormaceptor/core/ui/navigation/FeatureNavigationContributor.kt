package com.azikar24.wormaceptor.core.ui.navigation

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

/** Contributes navigation destinations for a feature module to the main NavHost. */
interface FeatureNavigationContributor {
    /**
     * Registers one or more composable destinations inside [builder].
     *
     * @param builder the [NavGraphBuilder] that collects all destinations.
     * @param navController the host controller used for in-feature navigation.
     * @param context the current Android [Context].
     * @param onBack callback invoked when the user requests to navigate back.
     */
    fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    )
}
