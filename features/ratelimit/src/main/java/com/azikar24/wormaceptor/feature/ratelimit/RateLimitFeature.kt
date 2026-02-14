package com.azikar24.wormaceptor.feature.ratelimit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.RateLimitEngine
import com.azikar24.wormaceptor.feature.ratelimit.ui.RateLimitScreen
import com.azikar24.wormaceptor.feature.ratelimit.vm.RateLimitViewModel

/**
 * Main composable with an externally provided engine.
 * Use this when you need to share the engine with your OkHttpClient.
 *
 * @param engine The shared RateLimitEngine instance
 * @param modifier Modifier for the root layout
 * @param onNavigateBack Optional callback for back navigation
 */
@Composable
fun RateLimiter(engine: RateLimitEngine, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val factory = remember(engine) { RateLimitFeature.createViewModelFactory(engine) }
    val viewModel: RateLimitViewModel = viewModel(factory = factory)

    // Collect state
    val config by viewModel.config.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()

    RateLimitScreen(
        config = config,
        stats = stats,
        selectedPreset = selectedPreset,
        onEnableToggle = viewModel::toggleEnabled,
        onPresetSelected = viewModel::selectPreset,
        onDownloadSpeedChanged = viewModel::setDownloadSpeed,
        onUploadSpeedChanged = viewModel::setUploadSpeed,
        onLatencyChanged = viewModel::setLatency,
        onPacketLossChanged = viewModel::setPacketLoss,
        onClearStats = viewModel::clearStats,
        onResetToDefaults = viewModel::resetToDefaults,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}

/**
 * Factory for creating RateLimitViewModel instances.
 */
class RateLimitViewModelFactory(
    private val engine: RateLimitEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RateLimitViewModel::class.java)) {
            return RateLimitViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Entry point for the Network Rate Limiting feature.
 * Provides factory methods and composable entry point.
 */
object RateLimitFeature {
    /**
     * Creates a RateLimitViewModel factory for use with viewModel().
     *
     * @param engine The RateLimitEngine instance to use
     * @return A ViewModelProvider.Factory for creating RateLimitViewModel
     */
    fun createViewModelFactory(engine: RateLimitEngine): RateLimitViewModelFactory {
        return RateLimitViewModelFactory(engine)
    }
}
