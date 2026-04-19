package com.azikar24.wormaceptor.feature.websocket

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.feature.websocket.navigation.webSocketGraph
import com.google.auto.service.AutoService

/** Registers [WebSocket] navigation routes with the main NavHost. */
@AutoService(FeatureNavigationContributor::class)
class WebSocketNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.webSocketGraph(
            navController = navController,
            onNavigateBack = onBack,
        )
    }
}
