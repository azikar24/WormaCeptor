/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.fps.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.FpsInfo
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "FPS Monitor",
                            fontWeight = FontWeight.SemiBold,
                        )
                        MonitoringIndicator(isMonitoring = isMonitoring)
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                actions = {
                    // Reset button
                    IconButton(onClick = { viewModel.resetStats() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset statistics",
                        )
                    }

                    // Play/Pause toggle
                    IconButton(onClick = { viewModel.toggleMonitoring() }) {
                        Icon(
                            imageVector = if (isMonitoring) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isMonitoring) "Pause monitoring" else "Start monitoring",
                            tint = if (isMonitoring) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Current FPS Display
            CurrentFpsCard(
                fpsInfo = currentInfo,
                isMonitoring = isMonitoring,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )

            // Statistics Cards
            StatisticsRow(
                fpsInfo = currentInfo,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )

            // Dropped Frames Card
            DroppedFramesCard(
                droppedFrames = currentInfo.droppedFrames,
                jankFrames = currentInfo.jankFrames,
                colors = colors,
                modifier = Modifier.fillMaxWidth(),
            )

            // FPS Chart
            FpsChartCard(
                history = history,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )

            // Empty state when not monitoring
            if (!isMonitoring && history.isEmpty()) {
                EmptyState(
                    onStartMonitoring = { viewModel.startMonitoring() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                )
            }
        }
    }
}

@Composable
private fun MonitoringIndicator(isMonitoring: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(
                if (isMonitoring) {
                    MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                } else {
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                },
            ),
    )
}

@Composable
private fun CurrentFpsCard(fpsInfo: FpsInfo, isMonitoring: Boolean, colors: FpsColors, modifier: Modifier = Modifier) {
    val fpsColor by animateColorAsState(
        targetValue = if (fpsInfo.currentFps > 0) {
            colors.forFps(fpsInfo.currentFps)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(300),
        label = "fps_color",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (fpsInfo.currentFps > 0) {
            colors.backgroundForFps(fpsInfo.currentFps)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        animationSpec = tween(300),
        label = "fps_background",
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Current FPS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(4.dp))

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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            label = "Min",
            value = if (fpsInfo.minFps > 0) fpsInfo.minFps.roundToInt().toString() else "--",
            color = if (fpsInfo.minFps > 0) {
                colors.forFps(
                    fpsInfo.minFps,
                )
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )

        StatCard(
            label = "Avg",
            value = if (fpsInfo.averageFps > 0) fpsInfo.averageFps.roundToInt().toString() else "--",
            color = if (fpsInfo.averageFps > 0) {
                colors.forFps(
                    fpsInfo.averageFps,
                )
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )

        StatCard(
            label = "Max",
            value = if (fpsInfo.maxFps > 0) fpsInfo.maxFps.roundToInt().toString() else "--",
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
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                ),
                color = color,
            )
        }
    }
}

@Composable
private fun DroppedFramesCard(droppedFrames: Int, jankFrames: Int, colors: FpsColors, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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

                Spacer(modifier = Modifier.height(4.dp))

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            )

            // Jank frames
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                            contentDescription = "Jank detected",
                            modifier = Modifier.size(16.dp),
                            tint = colors.jankIndicator,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun FpsChartCard(history: ImmutableList<FpsInfo>, colors: FpsColors, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
        ) {
            Text(
                text = "FPS Over Time",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No data yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                }
            } else {
                FpsChart(
                    data = history,
                    colors = colors,
                    modifier = Modifier.fillMaxSize(),
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
    val goodThresholdColor = colors.good.copy(alpha = 0.3f)
    val warningThresholdColor = colors.warning.copy(alpha = 0.3f)

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
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ready to monitor",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap play to start measuring frame rate",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            onClick = onStartMonitoring,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
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
