package com.azikar24.wormaceptor.feature.memory.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.metric.WormaCeptorChartLegendItem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import com.azikar24.wormaceptor.feature.memory.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun MemoryChartCard(
    history: ImmutableList<MemoryInfo>,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.memory_over_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )

            MemoryChartContent(history = history)

            MemoryChartLegend()
        }
    }
}

@Composable
private fun MemoryChartContent(history: ImmutableList<MemoryInfo>) {
    if (history.isEmpty()) {
        MemoryChartEmptyState()
    } else {
        val latest = history.last()
        val chartDescription = stringResource(
            R.string.memory_chart_description,
            latest.usedMemory / ChartDimensions.BytesPerMb,
            latest.totalMemory / ChartDimensions.BytesPerMb,
        )
        MemoryLineChart(
            history = history,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .semantics { contentDescription = chartDescription },
        )
    }
}

@Composable
private fun MemoryChartEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(WormaCeptorTokens.Radius.sm))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.memory_no_data),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MemoryChartLegend() {
    val mem = WormaCeptorTokens.Colors.Memory
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
    ) {
        WormaCeptorChartLegendItem(
            label = stringResource(R.string.memory_heap_used),
            color = mem.heapUsed,
        )
        WormaCeptorChartLegendItem(
            label = stringResource(R.string.memory_native),
            color = mem.nativeHeap,
        )
    }
}

@Composable
private fun MemoryLineChart(
    history: ImmutableList<MemoryInfo>,
    modifier: Modifier = Modifier,
) {
    if (history.isEmpty()) return

    val mem = WormaCeptorTokens.Colors.Memory
    val maxMemory = history.maxOf { maxOf(it.usedMemory, it.nativeHeapAllocated) }
        .coerceAtLeast(1L)

    val gridColor = MaterialTheme.colorScheme.outline.copy(
        alpha = WormaCeptorTokens.Alpha.MODERATE,
    )

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(WormaCeptorTokens.Radius.sm))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val padding = WormaCeptorTokens.Spacing.lg.toPx()
        val dimensions = ChartDimensions(
            padding = padding,
            chartWidth = size.width - padding * 2,
            chartHeight = size.height - padding * 2,
            pointCount = history.size,
        )

        drawMemoryGridLines(gridColor, dimensions)

        if (history.size < 2) return@Canvas

        drawLinePath(history, maxMemory, mem.heapUsed, dimensions) { it.usedMemory }
        drawLinePath(history, maxMemory, mem.nativeHeap, dimensions) { it.nativeHeapAllocated }
        drawAreaFill(history, maxMemory, mem.heapUsed, dimensions) { it.usedMemory }
    }
}

@Preview(name = "MemoryChartCard")
@Composable
private fun MemoryChartCardPreview() {
    WormaCeptorTheme {
        MemoryChartCard(
            history = persistentListOf(
                MemoryInfo(
                    timestamp = 1L,
                    usedMemory = 30_000_000L,
                    freeMemory = 34_000_000L,
                    totalMemory = 64_000_000L,
                    maxMemory = 128_000_000L,
                    heapUsagePercent = 23f,
                    nativeHeapSize = 32_000_000L,
                    nativeHeapAllocated = 10_000_000L,
                ),
                MemoryInfo(
                    timestamp = 2L,
                    usedMemory = 42_000_000L,
                    freeMemory = 22_000_000L,
                    totalMemory = 64_000_000L,
                    maxMemory = 128_000_000L,
                    heapUsagePercent = 33f,
                    nativeHeapSize = 32_000_000L,
                    nativeHeapAllocated = 14_000_000L,
                ),
                MemoryInfo(
                    timestamp = 3L,
                    usedMemory = 55_000_000L,
                    freeMemory = 9_000_000L,
                    totalMemory = 64_000_000L,
                    maxMemory = 128_000_000L,
                    heapUsagePercent = 43f,
                    nativeHeapSize = 32_000_000L,
                    nativeHeapAllocated = 18_000_000L,
                ),
                MemoryInfo(
                    timestamp = 4L,
                    usedMemory = 38_000_000L,
                    freeMemory = 26_000_000L,
                    totalMemory = 64_000_000L,
                    maxMemory = 128_000_000L,
                    heapUsagePercent = 30f,
                    nativeHeapSize = 32_000_000L,
                    nativeHeapAllocated = 12_000_000L,
                ),
                MemoryInfo(
                    timestamp = 5L,
                    usedMemory = 50_000_000L,
                    freeMemory = 14_000_000L,
                    totalMemory = 64_000_000L,
                    maxMemory = 128_000_000L,
                    heapUsagePercent = 39f,
                    nativeHeapSize = 32_000_000L,
                    nativeHeapAllocated = 16_000_000L,
                ),
            ),
        )
    }
}

@Preview(name = "MemoryChartCard")
@Composable
private fun MemoryChartCardDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        MemoryChartCard(history = persistentListOf())
    }
}
