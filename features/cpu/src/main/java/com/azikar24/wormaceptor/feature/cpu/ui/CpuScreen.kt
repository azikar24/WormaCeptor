package com.azikar24.wormaceptor.feature.cpu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMonitoringStatusBar
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorWarningBadge
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.feature.cpu.R
import com.azikar24.wormaceptor.feature.cpu.ui.theme.cpuColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

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
    currentCpu: CpuInfo,
    cpuHistory: ImmutableList<CpuInfo>,
    isMonitoring: Boolean,
    isCpuWarning: Boolean,
    formattedUptime: String,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    onClearHistory: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = cpuColors()
    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Text(
                            text = stringResource(R.string.cpu_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                        // Warning badge
                        AnimatedVisibility(
                            visible = isCpuWarning,
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
                        isActive = isMonitoring,
                        onToggle = { if (isMonitoring) onStopMonitoring() else onStartMonitoring() },
                    )

                    // Clear history
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
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(
                    start = WormaCeptorDesignSystem.Spacing.lg,
                    top = WormaCeptorDesignSystem.Spacing.lg,
                    end = WormaCeptorDesignSystem.Spacing.lg,
                    bottom = WormaCeptorDesignSystem.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Status bar
            WormaCeptorMonitoringStatusBar(
                isMonitoring = isMonitoring,
                sampleCount = cpuHistory.size,
            )

            // CPU usage gauge card
            CpuUsageGaugeCard(
                currentCpu = currentCpu,
                isWarning = isCpuWarning,
                colors = colors,
            )

            // Per-core usage card
            PerCoreUsageCard(
                currentCpu = currentCpu,
                colors = colors,
            )

            // CPU usage over time chart
            CpuChartCard(
                history = cpuHistory,
                colors = colors,
            )

            // System info card (frequency, temperature)
            SystemInfoCard(
                currentCpu = currentCpu,
                formattedUptime = formattedUptime,
                colors = colors,
            )
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun CpuScreenPreview() {
    WormaCeptorTheme {
        CpuScreen(
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
            onStartMonitoring = {},
            onStopMonitoring = {},
            onClearHistory = {},
            onBack = {},
        )
    }
}
