package com.azikar24.wormaceptor.feature.securestorage

import android.content.Context
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.SecureStorageEngine
import com.azikar24.wormaceptor.core.ui.navigation.FeatureNavigationContributor
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.feature.securestorage.ui.SecureStorageScreen
import com.azikar24.wormaceptor.feature.securestorage.vm.SecureStorageViewModel
import com.google.auto.service.AutoService
import org.koin.compose.koinInject

@AutoService(FeatureNavigationContributor::class)
class SecureStorageNavigationContributor : FeatureNavigationContributor {
    override fun contribute(
        builder: NavGraphBuilder,
        navController: NavHostController,
        context: Context,
        onBack: () -> Unit,
    ) {
        builder.composable(WormaCeptorNavKeys.SecureStorage.route) {
            val engine: SecureStorageEngine = koinInject()
            val factory = remember { SecureStorageViewModelFactory(engine) }
            val viewModel: SecureStorageViewModel = viewModel(factory = factory)
            BaseScreen(viewModel) { state, onEvent ->
                SecureStorageScreen(
                    state = state,
                    onEvent = onEvent,
                    onBack = onBack,
                )
            }
        }
    }
}
