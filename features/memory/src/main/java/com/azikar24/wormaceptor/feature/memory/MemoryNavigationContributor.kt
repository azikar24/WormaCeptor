package com.azikar24.wormaceptor.feature.memory

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.google.auto.service.AutoService

/** Registers [Memory] navigation routes with the main NavHost. */
@AutoService(FeatureNavigationContributor::class)
class MemoryNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.Memory.route) {
            MemoryMonitor(onNavigateBack = onBack)
        }
    }
}
