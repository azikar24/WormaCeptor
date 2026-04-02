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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.domain.entities.CpuMeasurementSource
import com.azikar24.wormaceptor.feature.cpu.R
import com.azikar24.wormaceptor.feature.cpu.ui.theme.CpuColors
import com.azikar24.wormaceptor.feature.cpu.ui.theme.cpuColors

@Composable
internal fun PerCoreUsageCard(
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
                text = stringResource(R.string.cpu_per_core_usage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.labelPrimary,
            )

            if (currentCpu.perCoreUsage.isEmpty()) {
                Text(
                    text = if (currentCpu.measurementSource == CpuMeasurementSource.PROCESS) {
                        stringResource(R.string.cpu_per_core_unavailable_process)
                    } else {
                        stringResource(R.string.cpu_no_core_data)
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
        animationSpec = tween(durationMillis = WormaCeptorDesignSystem.AnimationDuration.PAGE),
        label = "core_progress_$coreIndex",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        // Core label
        Text(
            text = stringResource(R.string.cpu_core_label, coreIndex),
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
            colors = cpuColors(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PerCoreUsageCardEmptyPreview() {
    WormaCeptorTheme {
        PerCoreUsageCard(
            currentCpu = CpuInfo.empty(),
            colors = cpuColors(),
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
            colors = cpuColors(),
        )
    }
}
