/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.memory.ui

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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import com.azikar24.wormaceptor.feature.memory.ui.theme.memoryColors
import kotlinx.collections.immutable.ImmutableList
import java.text.DecimalFormat

/**
 * Main screen for Memory Monitoring.
 *
 * Features:
 * - Current memory usage as percentage bar with color coding
 * - Numeric values (Used/Free/Total in MB)
 * - Line chart showing memory over time
 * - Force GC button
 * - Warning indicator when heap > 80%
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    currentMemory: MemoryInfo,
    memoryHistory: ImmutableList<MemoryInfo>,
    isMonitoring: Boolean,
    isHeapWarning: Boolean,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    onForceGc: () -> Unit,
    onClearHistory: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = memoryColors()
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
                            text = "Memory Monitor",
                            fontWeight = FontWeight.SemiBold,
                        )
                        // Warning badge
                        AnimatedVisibility(
                            visible = isHeapWarning,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            WarningBadge()
                        }
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
                    // Monitoring toggle
                    IconButton(onClick = {
                        if (isMonitoring) onStopMonitoring() else onStartMonitoring()
                    }) {
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

                    // Clear history
                    IconButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear history",
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
            // Status bar
            StatusBar(
                isMonitoring = isMonitoring,
                sampleCount = memoryHistory.size,
            )

            // Heap usage card
            HeapUsageCard(
                currentMemory = currentMemory,
                isWarning = isHeapWarning,
                colors = colors,
            )

            // Memory chart
            MemoryChartCard(
                history = memoryHistory,
                colors = colors,
            )

            // Native heap card
            NativeHeapCard(
                currentMemory = currentMemory,
                colors = colors,
            )

            // Actions
            ActionButtons(
                onForceGc = onForceGc,
            )
        }
    }
}

@Composable
private fun StatusBar(isMonitoring: Boolean, sampleCount: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Monitoring indicator
            val indicatorColor by animateColorAsState(
                targetValue = if (isMonitoring) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                animationSpec = tween(300),
                label = "indicator",
            )

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(indicatorColor),
            )

            Text(
                text = if (isMonitoring) "Monitoring" else "Paused",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Text(
            text = "$sampleCount samples",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun HeapUsageCard(
    currentMemory: MemoryInfo,
    isWarning: Boolean,
    colors: com.azikar24.wormaceptor.feature.memory.ui.theme.MemoryColors,
    modifier: Modifier = Modifier,
) {
    val statusColor = colors.statusColorForUsage(currentMemory.heapUsagePercent)
    val formatter = remember { DecimalFormat("#,##0.0") }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        text = "Java Heap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                    )
                }

                // Warning indicator
                if (isWarning) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.critical.copy(alpha = 0.15f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = colors.critical,
                                modifier = Modifier.size(16.dp),
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

            // Progress bar
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "${formatter.format(currentMemory.heapUsagePercent)}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                    )
                    Text(
                        text = "${formatBytes(currentMemory.usedMemory)} / ${formatBytes(currentMemory.maxMemory)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.labelSecondary,
                        fontFamily = FontFamily.Monospace,
                    )
                }

                LinearProgressIndicator(
                    progress = { (currentMemory.heapUsagePercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = colors.chartBackground,
                )
            }

            // Memory details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MemoryStatItem(
                    label = "Used",
                    value = formatBytes(currentMemory.usedMemory),
                    color = colors.heapUsed,
                    colors = colors,
                )
                MemoryStatItem(
                    label = "Free",
                    value = formatBytes(currentMemory.freeMemory),
                    color = colors.heapFree,
                    colors = colors,
                )
                MemoryStatItem(
                    label = "Total",
                    value = formatBytes(currentMemory.totalMemory),
                    color = colors.heapTotal,
                    colors = colors,
                )
                MemoryStatItem(
                    label = "Max",
                    value = formatBytes(currentMemory.maxMemory),
                    color = colors.labelSecondary,
                    colors = colors,
                )
            }
        }
    }
}

@Composable
private fun MemoryStatItem(
    label: String,
    value: String,
    color: Color,
    colors: com.azikar24.wormaceptor.feature.memory.ui.theme.MemoryColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
            color = color,
        )
    }
}

@Composable
private fun MemoryChartCard(
    history: ImmutableList<MemoryInfo>,
    colors: com.azikar24.wormaceptor.feature.memory.ui.theme.MemoryColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Memory Over Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
            )

            // Chart
            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
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
                MemoryLineChart(
                    history = history,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ChartLegendItem(
                    label = "Heap Used",
                    color = colors.heapUsed,
                )
                ChartLegendItem(
                    label = "Native",
                    color = colors.nativeHeap,
                )
            }
        }
    }
}

@Composable
private fun MemoryLineChart(
    history: ImmutableList<MemoryInfo>,
    colors: com.azikar24.wormaceptor.feature.memory.ui.theme.MemoryColors,
    modifier: Modifier = Modifier,
) {
    if (history.isEmpty()) return

    // Calculate max value for scaling
    val maxMemory = history.maxOfOrNull {
        maxOf(it.usedMemory, it.nativeHeapAllocated)
    } ?: 1L

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.chartBackground),
    ) {
        val width = size.width
        val height = size.height
        val padding = 16.dp.toPx()
        val chartWidth = width - padding * 2
        val chartHeight = height - padding * 2

        // Draw grid lines
        val gridLineCount = 4
        for (i in 0..gridLineCount) {
            val y = padding + (chartHeight / gridLineCount) * i
            drawLine(
                color = colors.gridLines,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1.dp.toPx(),
            )
        }

        if (history.size < 2) return@Canvas

        // Draw heap usage line
        val heapPath = Path()
        history.forEachIndexed { index, info ->
            val x = padding + (chartWidth / (history.size - 1)) * index
            val y = padding + chartHeight - (info.usedMemory.toFloat() / maxMemory * chartHeight)

            if (index == 0) {
                heapPath.moveTo(x, y)
            } else {
                heapPath.lineTo(x, y)
            }
        }
        drawPath(
            path = heapPath,
            color = colors.heapUsed,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )

        // Draw native heap line
        val nativePath = Path()
        history.forEachIndexed { index, info ->
            val x = padding + (chartWidth / (history.size - 1)) * index
            val y = padding + chartHeight - (info.nativeHeapAllocated.toFloat() / maxMemory * chartHeight)

            if (index == 0) {
                nativePath.moveTo(x, y)
            } else {
                nativePath.lineTo(x, y)
            }
        }
        drawPath(
            path = nativePath,
            color = colors.nativeHeap,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
            ),
        )

        // Draw area fill for heap usage
        val areaPath = Path()
        history.forEachIndexed { index, info ->
            val x = padding + (chartWidth / (history.size - 1)) * index
            val y = padding + chartHeight - (info.usedMemory.toFloat() / maxMemory * chartHeight)

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
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.heapUsed.copy(alpha = 0.3f),
                    colors.heapUsed.copy(alpha = 0.05f),
                ),
            ),
        )
    }
}

@Composable
private fun ChartLegendItem(label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NativeHeapCard(
    currentMemory: MemoryInfo,
    colors: com.azikar24.wormaceptor.feature.memory.ui.theme.MemoryColors,
    modifier: Modifier = Modifier,
) {
    val usagePercent = if (currentMemory.nativeHeapSize > 0) {
        (currentMemory.nativeHeapAllocated.toFloat() / currentMemory.nativeHeapSize.toFloat()) * 100f
    } else {
        0f
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Native Heap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${formatBytes(
                        currentMemory.nativeHeapAllocated,
                    )} / ${formatBytes(currentMemory.nativeHeapSize)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    color = colors.nativeHeap,
                )
                Text(
                    text = "${usagePercent.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.nativeHeap,
                )
            }

            LinearProgressIndicator(
                progress = { (usagePercent / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = colors.nativeHeap,
                trackColor = colors.chartBackground,
            )
        }
    }
}

@Composable
private fun ActionButtons(onForceGc: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Button(
            onClick = onForceGc,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.CleaningServices,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Force GC",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun WarningBadge(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "warning")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "warning_alpha",
    )

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.error.copy(alpha = alpha),
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Memory warning",
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier
                .padding(4.dp)
                .size(16.dp),
        )
    }
}

/**
 * Formats bytes into a human-readable string (KB, MB, GB).
 */
private fun formatBytes(bytes: Long): String {
    return when {
        bytes >= 1_073_741_824 -> String.format("%.1f GB", bytes / 1_073_741_824.0)
        bytes >= 1_048_576 -> String.format("%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format("%.1f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}
