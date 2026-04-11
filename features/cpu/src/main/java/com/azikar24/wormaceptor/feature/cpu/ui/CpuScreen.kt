package com.azikar24.wormaceptor.feature.cpu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMonitoringStatusBar
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorWarningBadge
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
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.cpu_title),
                    fontWeight = FontWeight.SemiBold,
                )
                AnimatedVisibility(
                    visible = state.isCpuWarning,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    WormaCeptorWarningBadge(
                        contentDescription = stringResource(R.string.cpu_warning),
                    )
                }
            }
        },
        navigationIcon = {
            onBack?.let { back ->
                IconButton(onClick = back) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cpu_back),
                    )
                }
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
            IconButton(onClick = onClearHistory) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cpu_clear_history),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
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

/**
 * Main screen for CPU Monitoring.
 *
 * Features:
 * - Overall CPU usage as animated percentage arc/gauge
 * - Per-core usage bars (horizontal bars for each core)
 * - CPU frequency display
 * - Line chart showing CPU usage over time
 * - Color coding: green < 50%, yellow 50-80%, red > 80%
 * - Temperature display (if available)
 */
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
