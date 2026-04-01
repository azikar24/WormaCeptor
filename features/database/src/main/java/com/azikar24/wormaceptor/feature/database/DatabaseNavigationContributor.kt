package com.azikar24.wormaceptor.feature.database

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.feature.database.navigation.databaseGraph

/** Registers [Database] navigation routes with the main NavHost. */
class DatabaseNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.databaseGraph(
            navController = navController,
            context = context,
            onNavigateBack = onBack,
        )
    }
}
