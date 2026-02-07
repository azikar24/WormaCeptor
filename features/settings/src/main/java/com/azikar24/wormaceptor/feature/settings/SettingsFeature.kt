package com.azikar24.wormaceptor.feature.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.domain.contracts.FeatureConfigRepository
import com.azikar24.wormaceptor.feature.settings.data.FeatureConfigRepositoryImpl
import com.azikar24.wormaceptor.feature.settings.data.SettingsDataStore
import com.azikar24.wormaceptor.feature.settings.ui.FeatureToggleScreen
import com.azikar24.wormaceptor.feature.settings.vm.SettingsViewModel

/**
 * Entry point for the Feature Toggles settings feature.
 * Provides factory methods and composable access.
 */
object SettingsFeature {

    /**
     * Creates a FeatureConfigRepository instance for the given context.
     * Use this in your dependency injection setup.
     */
    fun createRepository(context: Context): FeatureConfigRepository {
        val dataStore = SettingsDataStore(context.applicationContext)
        return FeatureConfigRepositoryImpl(dataStore)
    }

    /**
     * Creates a SettingsViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(repository: FeatureConfigRepository): SettingsViewModelFactory {
        return SettingsViewModelFactory(repository)
    }
}

/**
 * Factory for creating SettingsViewModel instances.
 */
class SettingsViewModelFactory(
    private val repository: FeatureConfigRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Feature Toggles screen.
 */
@Composable
fun FeatureToggles(context: Context, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val repository = remember { SettingsFeature.createRepository(context) }
    val factory = remember { SettingsFeature.createViewModelFactory(repository) }
    val viewModel: SettingsViewModel = viewModel(factory = factory)

    FeatureToggleScreen(
        viewModel = viewModel,
        onBack = { onNavigateBack?.invoke() },
        modifier = modifier,
    )
}
