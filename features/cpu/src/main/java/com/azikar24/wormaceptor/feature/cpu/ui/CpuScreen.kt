package com.azikar24.wormaceptor.feature.cpu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorWarningBadge
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.monitoring.WormaCeptorMonitoringStatusBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.feature.cpu.R
import com.azikar24.wormaceptor.feature.cpu.vm.CpuViewEvent
import com.azikar24.wormaceptor.feature.cpu.vm.CpuViewState
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CpuTopAppBar(
    state: CpuViewState,
    onEvent: (CpuViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    onClearHistory: () -> Unit,
) {
    WormaCeptorTopBar(
        title = stringResource(R.string.cpu_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.cpu_back),
        titleTrailing = {
            AnimatedVisibility(
                visible = state.isCpuWarning,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                WormaCeptorWarningBadge(
                    contentDescription = stringResource(R.string.cpu_warning),
                )
            }
        },
        actions = {
            WormaCeptorPlayPauseButton(
                isActive = state.isMonitoring,
                onToggle = {
                    if (state.isMonitoring) {
                        onEvent(CpuViewEvent.StopMonitoring)
                    } else {
                        onEvent(CpuViewEvent.StartMonitoring)
                    }
                },
            )
            WormaCeptorIconButton(onClick = onClearHistory) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cpu_clear_history),
                )
            }
        },
    )
}

@Composable
private fun CpuScreenContent(
    state: CpuViewState,
    paddingValues: PaddingValues,
    scrollState: ScrollState,
) {
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
        WormaCeptorMonitoringStatusBar(
            isMonitoring = state.isMonitoring,
            sampleCount = state.cpuHistory.size,
        )
        CpuUsageGaugeCard(
            currentCpu = state.currentCpu,
            isWarning = state.isCpuWarning,
        )
        PerCoreUsageCard(
            currentCpu = state.currentCpu,
        )
        CpuChartCard(
            history = state.cpuHistory,
        )
        SystemInfoCard(
            currentCpu = state.currentCpu,
            formattedUptime = state.formattedUptime,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CpuScreen(
    state: CpuViewState,
    onEvent: (CpuViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            CpuTopAppBar(
                state = state,
                onEvent = onEvent,
                onBack = onBack,
                onClearHistory = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEvent(CpuViewEvent.ClearHistory)
                },
            )
        },
    ) { paddingValues ->
        CpuScreenContent(
            state = state,
            paddingValues = paddingValues,
            scrollState = scrollState,
        )
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun CpuScreenPreview() {
    WormaCeptorTheme {
        CpuScreen(
            state = CpuViewState(
                currentCpu = CpuInfo(
                    timestamp = System.currentTimeMillis(),
                    overallUsagePercent = 15.2f,
                    perCoreUsage = listOf(32.5f, 67.8f, 12.3f, 55.0f),
                    coreCount = 8,
                    cpuFrequencyMHz = 2400L,
                    cpuTemperature = 42.5f,
                    uptime = 3_600_000L,
                ),
                cpuHistory = persistentListOf(
                    CpuInfo(
                        timestamp = 1L,
                        overallUsagePercent = 30f,
                        perCoreUsage = listOf(25f, 35f, 20f, 40f),
                        coreCount = 4,
                        cpuFrequencyMHz = 2400L,
                        cpuTemperature = 40f,
                    ),
                    CpuInfo(
                        timestamp = 2L,
                        overallUsagePercent = 45.2f,
                        perCoreUsage = listOf(32.5f, 67.8f, 12.3f, 55.0f),
                        coreCount = 4,
                        cpuFrequencyMHz = 2400L,
                        cpuTemperature = 42.5f,
                    ),
                ),
                isMonitoring = true,
                isCpuWarning = false,
                formattedUptime = "1h 0m 0s",
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
