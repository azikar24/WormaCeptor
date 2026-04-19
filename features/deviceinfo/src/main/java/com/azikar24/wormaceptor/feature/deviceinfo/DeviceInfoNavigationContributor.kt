package com.azikar24.wormaceptor.feature.deviceinfo

import android.content.Context
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.feature.deviceinfo.navigation.deviceInfoRoute
import com.google.auto.service.AutoService

/** Registers [DeviceInfo] navigation routes with the main NavHost. */
@AutoService(FeatureNavigationContributor::class)
class DeviceInfoNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.deviceInfoRoute(
            context = context,
            onNavigateBack = onBack,
        )
    }
}
