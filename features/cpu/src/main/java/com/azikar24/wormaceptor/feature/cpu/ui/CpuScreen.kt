package com.azikar24.wormaceptor.feature.cpu.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorChartLegendItem
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMonitoringStatusBar
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorWarningBadge
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.domain.entities.CpuMeasurementSource
import com.azikar24.wormaceptor.feature.cpu.R
import com.azikar24.wormaceptor.feature.cpu.ui.theme.CpuColors
import com.azikar24.wormaceptor.feature.cpu.ui.theme.cpuColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.text.DecimalFormat
import java.util.Locale

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
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    onClearHistory: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = cpuColors()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Text(
                            text = "CPU Monitor",
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
                .padding(WormaCeptorDesignSystem.Spacing.lg),
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
                colors = colors,
            )
        }
    }
}

@Composable
private fun CpuUsageGaugeCard(
    currentCpu: CpuInfo,
    isWarning: Boolean,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    val statusColor = colors.statusColorForUsage(currentCpu.overallUsagePercent)
    val formatter = DecimalFormat("#,##0.0")

    // Animated sweep angle for the gauge
    val animatedProgress by animateFloatAsState(
        targetValue = currentCpu.overallUsagePercent / 100f,
        animationSpec = tween(durationMillis = WormaCeptorDesignSystem.AnimationDuration.verySlow),
        label = "gauge_progress",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = stringResource(R.string.cpu_title),
                        tint = statusColor,
                        modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
                    )
                    Column {
                        Text(
                            text = "Overall CPU Usage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.labelPrimary,
                        )
                        Text(
                            text = when (currentCpu.measurementSource) {
                                CpuMeasurementSource.SYSTEM -> stringResource(R.string.cpu_measurement_system)
                                CpuMeasurementSource.PROCESS -> stringResource(R.string.cpu_measurement_process)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.labelSecondary,
                        )
                    }
                }

                // Warning indicator
                if (isWarning) {
                    Surface(
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                        color = colors.critical.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                vertical = WormaCeptorDesignSystem.Spacing.xs,
                            ),
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = stringResource(R.string.cpu_warning_high),
                                tint = colors.critical,
                                modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.lg),
                            )
                            Text(
                                text = "HIGH",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.critical,
                            )
                        }
                    }
                }
            }

            // Circular gauge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                val cpuPercentage = (animatedProgress * 100).toInt()
                CpuGauge(
                    progress = animatedProgress,
                    statusColor = statusColor,
                    colors = colors,
                    modifier = Modifier
                        .size(160.dp)
                        .semantics {
                            contentDescription = "CPU usage: $cpuPercentage%"
                        },
                )

                // Center text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "${formatter.format(currentCpu.overallUsagePercent)}%",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                    )
                    Text(
                        text = "${currentCpu.coreCount} cores",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.labelSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun CpuGauge(
    progress: Float,
    statusColor: Color,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = WormaCeptorDesignSystem.Spacing.lg.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        // Draw background arc
        drawArc(
            color = colors.gaugeTrack,
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )

        // Draw progress arc
        drawArc(
            color = statusColor,
            startAngle = 135f,
            sweepAngle = 270f * progress.coerceIn(0f, 1f),
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun PerCoreUsageCard(
    currentCpu: CpuInfo,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(
                text = "Per-Core Usage",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
            )

            if (currentCpu.perCoreUsage.isEmpty()) {
                Text(
                    text = if (currentCpu.measurementSource == CpuMeasurementSource.PROCESS) {
                        stringResource(R.string.cpu_per_core_unavailable_process)
                    } else {
                        "No core data available"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.labelSecondary,
                )
            } else {
                currentCpu.perCoreUsage.forEachIndexed { index, usage ->
                    CoreUsageBar(
                        coreIndex = index,
                        usage = usage,
                        color = colors.colorForCore(index),
                        colors = colors,
                    )
                }
            }
        }
    }
}

@Composable
private fun CoreUsageBar(
    coreIndex: Int,
    usage: Float,
    color: Color,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = usage / 100f,
        animationSpec = tween(durationMillis = WormaCeptorDesignSystem.AnimationDuration.page),
        label = "core_progress_$coreIndex",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        // Core label
        Text(
            text = "Core $coreIndex",
            style = MaterialTheme.typography.labelMedium,
            color = colors.labelSecondary,
            modifier = Modifier.width(56.dp),
        )

        // Progress bar
        LinearProgressIndicator(
            progress = { animatedProgress.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(WormaCeptorDesignSystem.Spacing.sm)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs)),
            color = color,
            trackColor = colors.chartBackground,
        )

        // Percentage
        Text(
            text = "${usage.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = colors.statusColorForUsage(usage),
            modifier = Modifier.width(36.dp),
        )
    }
}

@Composable
private fun CpuChartCard(
    history: ImmutableList<CpuInfo>,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(
                text = "CPU Usage Over Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
                modifier = Modifier.semantics { heading() },
            )

            // Chart
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                        .background(colors.chartBackground),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.labelSecondary,
                    )
                }
            } else {
                val latestCpuPct = history.lastOrNull()?.overallUsagePercent?.toInt() ?: 0
                CpuLineChart(
                    history = history,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .semantics {
                            contentDescription = "CPU usage chart showing current: $latestCpuPct%"
                        },
                )
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                WormaCeptorChartLegendItem(
                    label = stringResource(R.string.cpu_usage_label),
                    color = colors.cpuUsage,
                )
            }
        }
    }
}

@Composable
private fun CpuLineChart(
    history: ImmutableList<CpuInfo>,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    if (history.isEmpty()) return

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
            .background(colors.chartBackground),
    ) {
        val width = size.width
        val height = size.height
        val padding = WormaCeptorDesignSystem.Spacing.lg.toPx()
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Draw grid lines
        val gridLineCount = 4
        for (i in 0..gridLineCount) {
            val y = padding + chartHeight / gridLineCount * i
            drawLine(
                color = colors.gridLines,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        // Draw threshold lines
        // 50% threshold (warning)
        val warningY = padding + chartHeight * 0.5f
        drawLine(
            color = colors.warning.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
            start = Offset(padding, warningY),
            end = Offset(width - padding, warningY),
            strokeWidth = 1.dp.toPx(),
        )

        // 80% threshold (critical)
        val criticalY = padding + chartHeight * 0.2f
        drawLine(
            color = colors.critical.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
            start = Offset(padding, criticalY),
            end = Offset(width - padding, criticalY),
            strokeWidth = 1.dp.toPx(),
        )

        if (history.size < 2) return@Canvas

        // Draw CPU usage line
        val cpuPath = Path()
        history.forEachIndexed { index, info ->
            val x = padding + chartWidth / (history.size - 1) * index
            val y = padding + chartHeight - info.overallUsagePercent / 100f * chartHeight

            if (index == 0) {
                cpuPath.moveTo(x, y)
            } else {
                cpuPath.lineTo(x, y)
            }
        }
        drawPath(
            path = cpuPath,
            color = colors.cpuUsage,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )

        // Draw area fill
        val areaPath = Path()
        history.forEachIndexed { index, info ->
            val x = padding + chartWidth / (history.size - 1) * index
            val y = padding + chartHeight - info.overallUsagePercent / 100f * chartHeight

            if (index == 0) {
                areaPath.moveTo(x, padding + chartHeight)
                areaPath.lineTo(x, y)
            } else {
                areaPath.lineTo(x, y)
            }
        }
        areaPath.lineTo(padding + chartWidth, padding + chartHeight)
        areaPath.close()

        drawPath(
            path = areaPath,
            color = colors.cpuUsage.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        )
    }
}

@Composable
private fun SystemInfoCard(
    currentCpu: CpuInfo,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(
                text = "System Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
                modifier = Modifier.semantics { heading() },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                // CPU Frequency
                SystemInfoItem(
                    icon = Icons.Default.Speed,
                    label = stringResource(R.string.cpu_frequency_label),
                    value = if (currentCpu.cpuFrequencyMHz > 0) {
                        "${currentCpu.cpuFrequencyMHz} MHz"
                    } else {
                        "N/A"
                    },
                    iconTint = colors.cpuUsage,
                    colors = colors,
                )

                // Temperature
                val cpuTemp = currentCpu.cpuTemperature
                SystemInfoItem(
                    icon = Icons.Default.Thermostat,
                    label = stringResource(R.string.cpu_temperature_label),
                    value = cpuTemp?.let {
                        String.format(Locale.US, "%.1f C", it)
                    } ?: "N/A",
                    iconTint = when {
                        cpuTemp != null && cpuTemp > 70f -> colors.critical
                        cpuTemp != null && cpuTemp > 50f -> colors.warning
                        else -> colors.normal
                    },
                    colors = colors,
                )

                // Core count
                SystemInfoItem(
                    icon = Icons.Default.Memory,
                    label = stringResource(R.string.cpu_cores_label),
                    value = "${currentCpu.coreCount}",
                    iconTint = colors.cpuUsage,
                    colors = colors,
                )
            }

            // Uptime
            if (currentCpu.uptime > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "Uptime: ${formatUptime(currentCpu.uptime)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.labelSecondary,
                        fontFamily = FontFamily.Monospace,
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    iconTint: Color,
    colors: CpuColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.labelSecondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = colors.labelPrimary,
        )
    }
}

/**
 * Formats uptime in milliseconds to a human-readable string.
 */
private fun formatUptime(uptimeMs: Long): String {
    val seconds = uptimeMs / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ${hours % 24}h ${minutes % 60}m"
        hours > 0 -> "${hours}h ${minutes % 60}m ${seconds % 60}s"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
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
            onStartMonitoring = {},
            onStopMonitoring = {},
            onClearHistory = {},
            onBack = {},
        )
    }
}
