package com.azikar24.wormaceptor.feature.memory.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import com.azikar24.wormaceptor.feature.memory.R
import java.text.DecimalFormat

@Composable
internal fun HeapUsageCard(
    currentMemory: MemoryInfo,
    modifier: Modifier = Modifier,
) {
    val status = WormaCeptorTokens.Colors.Status
    val statusColor = when {
        currentMemory.heapUsagePercent >= MemoryMonitorEngine.HEAP_WARNING_THRESHOLD -> status.red
        currentMemory.heapUsagePercent >= MemoryMonitorEngine.HEAP_CAUTION_THRESHOLD -> status.amber
        else -> status.green
    }
    val isWarning = currentMemory.heapUsagePercent >= MemoryMonitorEngine.HEAP_WARNING_THRESHOLD
    val formatter = remember { DecimalFormat("#,##0.0") }

    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            HeapHeader(
                statusColor = statusColor,
                isWarning = isWarning,
            )

            HeapProgressSection(
                currentMemory = currentMemory,
                statusColor = statusColor,
                formatter = formatter,
            )

            HeapDetailsRow(currentMemory = currentMemory)
        }
    }
}

@Composable
private fun HeapHeader(
    statusColor: Color,
    isWarning: Boolean,
) {
    val status = WormaCeptorTokens.Colors.Status
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.Memory,
                contentDescription = stringResource(R.string.memory_title),
                tint = statusColor,
                modifier = Modifier.size(WormaCeptorTokens.Spacing.xl),
            )
            Text(
                text = stringResource(R.string.memory_java_heap),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = WormaCeptorTokens.semantic().textPrimary,
            )
        }

        if (isWarning) {
            HeapWarningBadge(warningColor = status.red)
        }
    }
}

@Composable
private fun HeapWarningBadge(warningColor: Color) {
    Surface(
        shape = WormaCeptorTokens.Shapes.button,
        color = warningColor.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xs,
            ),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = stringResource(R.string.memory_warning_high),
                tint = warningColor,
                modifier = Modifier.size(WormaCeptorTokens.Spacing.lg),
            )
            Text(
                text = stringResource(R.string.memory_high),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = warningColor,
            )
        }
    }
}

@Composable
private fun HeapProgressSection(
    currentMemory: MemoryInfo,
    statusColor: Color,
    formatter: DecimalFormat,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
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
                color = WormaCeptorTokens.semantic().textSecondary,
                fontFamily = FontFamily.Monospace,
            )
        }

        LinearProgressIndicator(
            progress = {
                (currentMemory.heapUsagePercent / 100f).coerceIn(0f, 1f)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(WormaCeptorTokens.Spacing.sm)
                .clip(WormaCeptorTokens.Shapes.chip),
            color = statusColor,
            trackColor = WormaCeptorTokens.semantic().surfaceVariant,
        )
    }
}

@Composable
private fun HeapDetailsRow(currentMemory: MemoryInfo) {
    val mem = WormaCeptorTokens.Colors.Memory
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        MemoryStatItem(
            label = stringResource(R.string.memory_used),
            value = formatBytes(currentMemory.usedMemory),
            color = mem.heapUsed,
        )
        MemoryStatItem(
            label = stringResource(R.string.memory_free),
            value = formatBytes(currentMemory.freeMemory),
            color = mem.heapFree,
        )
        MemoryStatItem(
            label = stringResource(R.string.memory_total),
            value = formatBytes(currentMemory.totalMemory),
            color = mem.heapTotal,
        )
        MemoryStatItem(
            label = stringResource(R.string.memory_max),
            value = formatBytes(currentMemory.maxMemory),
            color = WormaCeptorTokens.semantic().textSecondary,
        )
    }
}

@Composable
private fun MemoryStatItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WormaCeptorTokens.semantic().textSecondary,
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

private class HeapUsagePreviewProvider : PreviewParameterProvider<MemoryInfo> {
    private val normal = MemoryInfo(
        timestamp = System.currentTimeMillis(),
        usedMemory = 45 * 1024 * 1024L,
        freeMemory = 19 * 1024 * 1024L,
        totalMemory = 64 * 1024 * 1024L,
        maxMemory = 128 * 1024 * 1024L,
        heapUsagePercent = 35f,
        nativeHeapSize = 32 * 1024 * 1024L,
        nativeHeapAllocated = 20 * 1024 * 1024L,
    )

    override val values: Sequence<MemoryInfo> = sequenceOf(
        normal,
        normal.copy(
            heapUsagePercent = 65f,
            usedMemory = 83 * 1024 * 1024L,
            freeMemory = 45 * 1024 * 1024L,
        ),
        normal.copy(
            heapUsagePercent = 88f,
            usedMemory = 113 * 1024 * 1024L,
            freeMemory = 15 * 1024 * 1024L,
        ),
    )
}

@Preview
@Composable
private fun HeapUsageCardPreview(@PreviewParameter(HeapUsagePreviewProvider::class) memoryInfo: MemoryInfo) {
    WormaCeptorTheme {
        HeapUsageCard(currentMemory = memoryInfo)
    }
}
