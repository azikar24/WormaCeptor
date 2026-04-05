package com.azikar24.wormaceptor.feature.memory.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import com.azikar24.wormaceptor.feature.memory.R
import java.text.DecimalFormat

@Composable
internal fun HeapUsageCard(
    currentMemory: MemoryInfo,
    isWarning: Boolean,
    modifier: Modifier = Modifier,
) {
    val mem = WormaCeptorTokens.Colors.Memory
    val status = WormaCeptorTokens.Colors.Status
    val statusColor = when {
        currentMemory.heapUsagePercent >= 80f -> status.red
        currentMemory.heapUsagePercent >= 60f -> status.amber
        else -> status.green
    }
    val formatter = remember { DecimalFormat("#,##0.0") }

    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
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
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Warning indicator
                if (isWarning) {
                    Surface(
                        shape = RoundedCornerShape(WormaCeptorTokens.Radius.sm),
                        color = status.red.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
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
                                tint = status.red,
                                modifier = Modifier.size(WormaCeptorTokens.Spacing.lg),
                            )
                            Text(
                                text = stringResource(R.string.memory_high),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = status.red,
                            )
                        }
                    }
                }
            }

            // Progress bar
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace,
                    )
                }

                LinearProgressIndicator(
                    progress = { (currentMemory.heapUsagePercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(WormaCeptorTokens.Spacing.sm)
                        .clip(RoundedCornerShape(WormaCeptorTokens.Radius.xs)),
                    color = statusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

private val previewMemoryInfo = MemoryInfo(
    timestamp = System.currentTimeMillis(),
    usedMemory = 45 * 1024 * 1024L,
    freeMemory = 19 * 1024 * 1024L,
    totalMemory = 64 * 1024 * 1024L,
    maxMemory = 128 * 1024 * 1024L,
    heapUsagePercent = 35f,
    nativeHeapSize = 32 * 1024 * 1024L,
    nativeHeapAllocated = 20 * 1024 * 1024L,
)

@Preview(name = "HeapUsageCard - Light")
@Composable
private fun HeapUsageCardPreview() {
    WormaCeptorTheme {
        HeapUsageCard(
            currentMemory = previewMemoryInfo,
            isWarning = false,
        )
    }
}
