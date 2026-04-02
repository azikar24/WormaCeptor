package com.azikar24.wormaceptor.feature.ratelimit

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.RateLimitEngine
import com.azikar24.wormaceptor.feature.ratelimit.ui.RateLimitScreen
import com.azikar24.wormaceptor.feature.ratelimit.vm.RateLimitViewEvent
import com.azikar24.wormaceptor.feature.ratelimit.vm.RateLimitViewModel
import org.koin.compose.koinInject

/**
 * Main composable for the Rate Limiter feature.
 */
@Composable
fun RateLimiter(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: RateLimitEngine = koinInject()
    val factory = remember(engine) { RateLimitFeature.createViewModelFactory(engine) }
    val viewModel: RateLimitViewModel = viewModel(factory = factory)

    BaseScreen(viewModel) { state, onEvent ->
        RateLimitScreen(
            config = state.config,
            stats = state.stats,
            selectedPreset = state.selectedPreset,
            onEnableToggle = { onEvent(RateLimitViewEvent.ToggleEnabled) },
            onPresetSelected = { onEvent(RateLimitViewEvent.SelectPreset(it)) },
            onDownloadSpeedChanged = { onEvent(RateLimitViewEvent.SetDownloadSpeed(it)) },
            onUploadSpeedChanged = { onEvent(RateLimitViewEvent.SetUploadSpeed(it)) },
            onLatencyChanged = { onEvent(RateLimitViewEvent.SetLatency(it)) },
            onPacketLossChanged = { onEvent(RateLimitViewEvent.SetPacketLoss(it)) },
            onClearStats = { onEvent(RateLimitViewEvent.ClearStats) },
            onResetToDefaults = { onEvent(RateLimitViewEvent.ResetToDefaults) },
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
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
