package com.azikar24.wormaceptor.feature.fps.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMonitoringIndicator
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.feature.fps.R
import com.azikar24.wormaceptor.feature.fps.ui.theme.FpsColors
import com.azikar24.wormaceptor.feature.fps.ui.theme.fpsColors
import com.azikar24.wormaceptor.feature.fps.vm.FpsViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Main screen for FPS monitoring.
 *
 * Features:
 * - Large current FPS display with color-coded status
 * - Min/Max/Average statistics
 * - Dropped frame and jank counters
 * - Real-time FPS chart
 * - Play/Pause and reset controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FpsScreen(viewModel: FpsViewModel, modifier: Modifier = Modifier, onBack: (() -> Unit)? = null) {
    val currentInfo by viewModel.currentFpsInfo.collectAsState()
    val history by viewModel.fpsHistory.collectAsState()
    val isMonitoring by viewModel.isMonitoring.collectAsState()

    val colors = fpsColors()
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
                            text = "FPS Monitor",
                            fontWeight = FontWeight.SemiBold,
                        )
                        WormaCeptorMonitoringIndicator(isActive = isMonitoring)
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.fps_back),
                            )
                        }
                    }
                },
                actions = {
                    // Reset button
                    IconButton(onClick = { viewModel.resetStats() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.fps_reset_statistics),
                        )
                    }

                    WormaCeptorPlayPauseButton(
                        isActive = isMonitoring,
                        onToggle = { viewModel.toggleMonitoring() },
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        if (!isMonitoring && history.isEmpty()) {
            EmptyState(
                onStartMonitoring = { viewModel.startMonitoring() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(WormaCeptorDesignSystem.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                CurrentFpsCard(
                    fpsInfo = currentInfo,
                    isMonitoring = isMonitoring,
                    colors = colors,
                    modifier = Modifier.fillMaxWidth(),
                )

                StatisticsRow(
                    fpsInfo = currentInfo,
                    colors = colors,
                    modifier = Modifier.fillMaxWidth(),
                )

                DroppedFramesCard(
                    droppedFrames = currentInfo.droppedFrames,
                    jankFrames = currentInfo.jankFrames,
                    colors = colors,
                    modifier = Modifier.fillMaxWidth(),
                )

                FpsChartCard(
                    history = history,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            }
        }
    }
}

@Composable
private fun CurrentFpsCard(fpsInfo: FpsInfo, isMonitoring: Boolean, colors: FpsColors, modifier: Modifier = Modifier) {
    val fpsColor by animateColorAsState(
        targetValue = if (fpsInfo.currentFps > 0) {
            colors.forFps(fpsInfo.currentFps)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
        label = "fps_color",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (fpsInfo.currentFps > 0) {
            colors.backgroundForFps(fpsInfo.currentFps)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)
        },
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.page),
        label = "fps_background",
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
        color = backgroundColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Current FPS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            Text(
                text = if (fpsInfo.currentFps > 0 || isMonitoring) {
                    fpsInfo.currentFps.roundToInt().toString()
                } else {
                    "--"
                },
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = fpsColor,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

            Text(
                text = when {
                    fpsInfo.currentFps >= FpsColors.FPS_GOOD_THRESHOLD -> "Excellent"
                    fpsInfo.currentFps >= FpsColors.FPS_WARNING_THRESHOLD -> "Moderate"
                    fpsInfo.currentFps > 0 -> "Poor"
                    else -> "Not monitoring"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (fpsInfo.currentFps > 0) fpsColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatisticsRow(fpsInfo: FpsInfo, colors: FpsColors, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        WormaCeptorSummaryCard(
            count = if (fpsInfo.minFps > 0) fpsInfo.minFps.roundToInt().toString() else "--",
            label = stringResource(R.string.fps_min),
            color = if (fpsInfo.minFps > 0) {
                colors.forFps(
                    fpsInfo.minFps,
                )
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )

        WormaCeptorSummaryCard(
            count = if (fpsInfo.averageFps > 0) fpsInfo.averageFps.roundToInt().toString() else "--",
            label = stringResource(R.string.fps_avg),
            color = if (fpsInfo.averageFps > 0) {
                colors.forFps(
                    fpsInfo.averageFps,
                )
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )

        WormaCeptorSummaryCard(
            count = if (fpsInfo.maxFps > 0) fpsInfo.maxFps.roundToInt().toString() else "--",
            label = stringResource(R.string.fps_max),
            color = if (fpsInfo.maxFps > 0) {
                colors.forFps(
                    fpsInfo.maxFps,
                )
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DroppedFramesCard(droppedFrames: Int, jankFrames: Int, colors: FpsColors, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.bold),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Dropped frames
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Dropped Frames",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

                Text(
                    text = droppedFrames.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = if (droppedFrames > 0) colors.warning else MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "> 16.67ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.heavy,
                    ),
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(WormaCeptorDesignSystem.Spacing.xxxl)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)),
            )

            // Jank frames
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                ) {
                    Text(
                        text = "Jank Frames",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    AnimatedVisibility(
                        visible = jankFrames > 0,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.fps_jank_detected),
                            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                            tint = colors.jankIndicator,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

                Text(
                    text = jankFrames.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = if (jankFrames > 0) colors.critical else MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "> 32ms",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.heavy,
                    ),
                )
            }
        }
    }
}

@Composable
private fun FpsChartCard(history: ImmutableList<FpsInfo>, colors: FpsColors, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.bold),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Text(
                text = "FPS Over Time",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.bold,
                        ),
                    )
                }
            } else {
                val latestFps = history.lastOrNull()?.currentFps?.toInt() ?: 0
                FpsChart(
                    data = history,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = "FPS chart showing current: $latestFps fps"
                        },
                )
            }
        }
    }
}

@Composable
private fun FpsChart(data: ImmutableList<FpsInfo>, colors: FpsColors, modifier: Modifier = Modifier) {
    val lineColor = colors.chartLine
    val fillColor = colors.chartFill
    val gridColor = colors.chartGrid
    val goodThresholdColor = colors.good.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)
    val warningThresholdColor = colors.warning.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)

    // Calculate min/max for scaling
    val maxFps = remember(data) {
        max(data.maxOfOrNull { it.currentFps } ?: 60f, 65f)
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 8f

        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Draw horizontal grid lines and threshold indicators
        val gridLines = listOf(0f, 30f, 55f, maxFps)
        gridLines.forEach { fps ->
            val y = padding + chartHeight * (1 - fps / maxFps)

            // Draw threshold color bands
            when {
                fps == 55f -> {
                    drawLine(
                        color = goodThresholdColor,
                        start = Offset(padding, y),
                        end = Offset(padding + chartWidth, y),
                        strokeWidth = 2f,
                    )
                }
                fps == 30f -> {
                    drawLine(
                        color = warningThresholdColor,
                        start = Offset(padding, y),
                        end = Offset(padding + chartWidth, y),
                        strokeWidth = 2f,
                    )
                }
                else -> {
                    drawLine(
                        color = gridColor,
                        start = Offset(padding, y),
                        end = Offset(padding + chartWidth, y),
                        strokeWidth = 1f,
                    )
                }
            }
        }

        if (data.size < 2) return@Canvas

        // Create path for the line
        val linePath = Path()
        val fillPath = Path()

        val xStep = chartWidth / (data.size - 1).coerceAtLeast(1)

        data.forEachIndexed { index, info ->
            val x = padding + index * xStep
            val y = padding + chartHeight * (1 - info.currentFps / maxFps)

            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, padding + chartHeight)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        // Complete fill path
        fillPath.lineTo(padding + (data.size - 1) * xStep, padding + chartHeight)
        fillPath.close()

        // Draw fill
        drawPath(
            path = fillPath,
            color = fillColor,
        )

        // Draw line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 3f),
        )
    }
}

@Composable
private fun EmptyState(onStartMonitoring: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xl),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xxxl + WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Text(
                    text = "60",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.intense,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

        Text(
            text = "Ready to monitor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        Text(
            text = "Tap play to start measuring frame rate",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

        Surface(
            onClick = onStartMonitoring,
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = WormaCeptorDesignSystem.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.fps_start),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = "Start Monitoring",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}
