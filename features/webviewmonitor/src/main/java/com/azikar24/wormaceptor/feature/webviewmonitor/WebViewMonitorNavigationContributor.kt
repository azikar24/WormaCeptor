package com.azikar24.wormaceptor.feature.webviewmonitor

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.WebViewMonitor

/** Registers [WebViewMonitor] navigation routes with the main NavHost. */
class WebViewMonitorNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.WebViewMonitor.route) {
            WebViewMonitor(onNavigateBack = onBack)
        }
    }
}
