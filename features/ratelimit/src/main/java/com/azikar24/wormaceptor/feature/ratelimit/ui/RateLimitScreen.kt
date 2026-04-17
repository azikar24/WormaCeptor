package com.azikar24.wormaceptor.feature.ratelimit.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.RateLimitConfig
import com.azikar24.wormaceptor.domain.entities.ThrottleStats
import com.azikar24.wormaceptor.feature.ratelimit.R
import com.azikar24.wormaceptor.feature.ratelimit.ui.components.ConfigurationCard
import com.azikar24.wormaceptor.feature.ratelimit.ui.components.EnableToggleCard
import com.azikar24.wormaceptor.feature.ratelimit.ui.components.NetworkPresetsCard
import com.azikar24.wormaceptor.feature.ratelimit.ui.components.StatisticsCard
import com.azikar24.wormaceptor.feature.ratelimit.vm.RateLimitViewEvent
import com.azikar24.wormaceptor.feature.ratelimit.vm.RateLimitViewState

@Suppress("LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateLimitScreen(
    state: RateLimitViewState,
    onEvent: (RateLimitViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = WormaCeptorTokens.Colors.RateLimit.scheme()
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            WormaCeptorTopBar(
                title = stringResource(R.string.ratelimit_title),
                onBack = onBack,
                backContentDescription = stringResource(R.string.ratelimit_back),
                actions = {
                    IconButton(onClick = { onEvent(RateLimitViewEvent.ResetToDefaults) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.ratelimit_reset_defaults),
                        )
                    }
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEvent(RateLimitViewEvent.ClearStats)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.ratelimit_clear_statistics),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(
                    start = WormaCeptorTokens.Spacing.lg,
                    top = WormaCeptorTokens.Spacing.lg,
                    end = WormaCeptorTokens.Spacing.lg,
                    bottom = WormaCeptorTokens.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
        ) {
            // Enable toggle card
            EnableToggleCard(
                enabled = state.config.enabled,
                onToggle = { onEvent(RateLimitViewEvent.ToggleEnabled) },
                colors = colors,
            )

            AnimatedVisibility(visible = state.config.enabled) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
                ) {
                    // Statistics
                    StatisticsCard(
                        stats = state.stats,
                        colors = colors,
                    )

                    // Network presets
                    NetworkPresetsCard(
                        selectedPreset = state.selectedPreset,
                        enabled = state.config.enabled,
                        onSelectPreset = { onEvent(RateLimitViewEvent.SelectPreset(it)) },
                        colors = colors,
                    )

                    // Custom configuration
                    ConfigurationCard(
                        config = state.config,
                        enabled = state.config.enabled,
                        onChangeDownloadSpeed = { onEvent(RateLimitViewEvent.SetDownloadSpeed(it)) },
                        onChangeUploadSpeed = { onEvent(RateLimitViewEvent.SetUploadSpeed(it)) },
                        onChangeLatency = { onEvent(RateLimitViewEvent.SetLatency(it)) },
                        onChangePacketLoss = { onEvent(RateLimitViewEvent.SetPacketLoss(it)) },
                        colors = colors,
                    )
                }
            }
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun RateLimitScreenPreview() {
    WormaCeptorTheme {
        RateLimitScreen(
            state = RateLimitViewState(
                config = RateLimitConfig(
                    enabled = true,
                    downloadSpeedKbps = 2000L,
                    uploadSpeedKbps = 500L,
                    latencyMs = 100L,
                    packetLossPercent = 1f,
                    preset = RateLimitConfig.NetworkPreset.GOOD_3G,
                ),
                stats = ThrottleStats(
                    requestsThrottled = 42,
                    totalDelayMs = 8500L,
                    packetsDropped = 3,
                    bytesThrottled = 1_048_576L,
                ),
                selectedPreset = RateLimitConfig.NetworkPreset.GOOD_3G,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
