package com.azikar24.wormaceptor.feature.memory.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Memory
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import com.azikar24.wormaceptor.feature.memory.R
import com.azikar24.wormaceptor.feature.memory.ui.theme.memoryColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
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
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
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
                            WormaCeptorWarningBadge(
                                contentDescription = stringResource(R.string.memory_warning),
                            )
                        }
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.memory_back),
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
                            contentDescription = stringResource(R.string.memory_clear_history),
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
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
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
                        contentDescription = stringResource(R.string.memory_title),
                        tint = statusColor,
                        modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xl),
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
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
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
                                contentDescription = stringResource(R.string.memory_warning_high),
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

            // Progress bar
            Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
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
                        .height(WormaCeptorDesignSystem.Spacing.sm)
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs)),
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
                    label = stringResource(R.string.memory_used),
                    value = formatBytes(currentMemory.usedMemory),
                    color = colors.heapUsed,
                    colors = colors,
                )
                MemoryStatItem(
                    label = stringResource(R.string.memory_free),
                    value = formatBytes(currentMemory.freeMemory),
                    color = colors.heapFree,
                    colors = colors,
                )
                MemoryStatItem(
                    label = stringResource(R.string.memory_total),
                    value = formatBytes(currentMemory.totalMemory),
                    color = colors.heapTotal,
                    colors = colors,
                )
                MemoryStatItem(
                    label = stringResource(R.string.memory_max),
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
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(
                text = "Memory Over Time",
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
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm))
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
                val latestUsed = history.lastOrNull()?.usedMemory ?: 0L
                val totalMem = history.lastOrNull()?.totalMemory ?: 1L
                MemoryLineChart(
                    history = history,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .semantics {
                            contentDescription =
                                "Memory usage chart: ${latestUsed / 1_048_576}MB of ${totalMem / 1_048_576}MB"
                        },
                )
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                WormaCeptorChartLegendItem(
                    label = stringResource(R.string.memory_heap_used),
                    color = colors.heapUsed,
                )
                WormaCeptorChartLegendItem(
                    label = stringResource(R.string.memory_native),
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
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm))
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

        if (history.size < 2) return@Canvas

        // Draw heap usage line
        val heapPath = Path()
        history.forEachIndexed { index, info ->
            val x = padding + chartWidth / (history.size - 1) * index
            val y = padding + chartHeight - info.usedMemory.toFloat() / maxMemory * chartHeight

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
            val x = padding + chartWidth / (history.size - 1) * index
            val y = padding + chartHeight - info.nativeHeapAllocated.toFloat() / maxMemory * chartHeight

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
            val x = padding + chartWidth / (history.size - 1) * index
            val y = padding + chartHeight - info.usedMemory.toFloat() / maxMemory * chartHeight

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
            color = colors.heapUsed.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
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
        currentMemory.nativeHeapAllocated.toFloat() / currentMemory.nativeHeapSize.toFloat() * 100f
    } else {
        0f
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
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
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs)),
                color = colors.nativeHeap,
                trackColor = colors.chartBackground,
            )
        }
    }
}

@Composable
private fun ActionButtons(
    onForceGc: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Button(
            onClick = onForceGc,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.secondary,
            ),
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
        ) {
            Icon(
                imageVector = Icons.Default.CleaningServices,
                contentDescription = null,
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Text(
                text = "Force GC",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun MemoryScreenPreview() {
    WormaCeptorTheme {
        MemoryScreen(
            currentMemory = MemoryInfo(
                timestamp = System.currentTimeMillis(),
                usedMemory = 50 * 1_048_576L,
                freeMemory = 30 * 1_048_576L,
                totalMemory = 80 * 1_048_576L,
                maxMemory = 256 * 1_048_576L,
                heapUsagePercent = 62.5f,
                nativeHeapSize = 64 * 1_048_576L,
                nativeHeapAllocated = 40 * 1_048_576L,
            ),
            memoryHistory = persistentListOf(
                MemoryInfo(
                    timestamp = 1L,
                    usedMemory = 40 * 1_048_576L,
                    freeMemory = 40 * 1_048_576L,
                    totalMemory = 80 * 1_048_576L,
                    maxMemory = 256 * 1_048_576L,
                    heapUsagePercent = 50f,
                    nativeHeapSize = 64 * 1_048_576L,
                    nativeHeapAllocated = 35 * 1_048_576L,
                ),
                MemoryInfo(
                    timestamp = 2L,
                    usedMemory = 50 * 1_048_576L,
                    freeMemory = 30 * 1_048_576L,
                    totalMemory = 80 * 1_048_576L,
                    maxMemory = 256 * 1_048_576L,
                    heapUsagePercent = 62.5f,
                    nativeHeapSize = 64 * 1_048_576L,
                    nativeHeapAllocated = 40 * 1_048_576L,
                ),
            ),
            isMonitoring = true,
            isHeapWarning = false,
            onStartMonitoring = {},
            onStopMonitoring = {},
            onForceGc = {},
            onClearHistory = {},
            onBack = {},
        )
    }
}
