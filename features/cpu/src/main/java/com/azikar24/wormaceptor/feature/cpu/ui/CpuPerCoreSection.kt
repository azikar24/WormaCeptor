package com.azikar24.wormaceptor.feature.cpu.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.domain.entities.CpuMeasurementSource
import com.azikar24.wormaceptor.feature.cpu.R

private const val CpuCriticalThreshold = 80f
private const val CpuWarningThreshold = 50f
private const val CpuPercentDivisor = 100f
private val CoreLabelWidth = 56.dp
private val PercentageLabelWidth = 36.dp

@Composable
internal fun PerCoreUsageCard(
    currentCpu: CpuInfo,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.cardExtraLarge,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.cpu_per_core_usage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (currentCpu.perCoreUsage.isEmpty()) {
                Text(
                    text = if (currentCpu.measurementSource == CpuMeasurementSource.PROCESS) {
                        stringResource(R.string.cpu_per_core_unavailable_process)
                    } else {
                        stringResource(R.string.cpu_no_core_data)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                currentCpu.perCoreUsage.forEachIndexed { index, usage ->
                    CoreUsageBar(
                        coreIndex = index,
                        usage = usage,
                        color = WormaCeptorTokens.Colors.Cpu.forCore(index),
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
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = usage / CpuPercentDivisor,
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.PAGE),
        label = "core_progress_$coreIndex",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.cpu_core_label, coreIndex),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(CoreLabelWidth),
        )

        LinearProgressIndicator(
            progress = { animatedProgress.coerceIn(0f, 1f) },
            modifier = Modifier
                .weight(1f)
                .height(WormaCeptorTokens.Spacing.sm)
                .clip(WormaCeptorTokens.Shapes.chip),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Text(
            text = "${usage.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = when {
                usage >= CpuCriticalThreshold -> WormaCeptorTokens.Colors.Status.red
                usage >= CpuWarningThreshold -> WormaCeptorTokens.Colors.Status.amber
                else -> WormaCeptorTokens.Colors.Status.green
            },
            modifier = Modifier.width(PercentageLabelWidth),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PerCoreUsageCardPreview() {
    WormaCeptorTheme {
        PerCoreUsageCard(
            currentCpu = CpuInfo(
                timestamp = System.currentTimeMillis(),
                overallUsagePercent = 42f,
                perCoreUsage = listOf(25f, 58f, 82f, 14f, 67f, 91f, 33f, 46f),
                coreCount = 8,
                cpuFrequencyMHz = 2400L,
                cpuTemperature = 44f,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PerCoreUsageCardEmptyPreview() {
    WormaCeptorTheme {
        PerCoreUsageCard(
            currentCpu = CpuInfo.empty(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PerCoreUsageCardProcessSourcePreview() {
    WormaCeptorTheme {
        PerCoreUsageCard(
            currentCpu = CpuInfo(
                timestamp = System.currentTimeMillis(),
                overallUsagePercent = 30f,
                perCoreUsage = emptyList(),
                coreCount = 4,
                cpuFrequencyMHz = 2400L,
                cpuTemperature = null,
                measurementSource = CpuMeasurementSource.PROCESS,
            ),
        )
    }
}
