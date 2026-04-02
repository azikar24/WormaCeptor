package com.azikar24.wormaceptor.feature.cpu.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorChartLegendItem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.feature.cpu.R
import com.azikar24.wormaceptor.feature.cpu.ui.theme.CpuColors
import com.azikar24.wormaceptor.feature.cpu.ui.theme.cpuColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun CpuChartCard(
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
                text = stringResource(R.string.cpu_usage_over_time),
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
                        text = stringResource(R.string.cpu_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.labelSecondary,
                    )
                }
            } else {
                val cpuPct = history.lastOrNull()?.overallUsagePercent?.toInt() ?: 0
                val chartDescription = stringResource(R.string.cpu_chart_content_description, cpuPct)
                CpuLineChart(
                    history = history,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .semantics {
                            contentDescription = chartDescription
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
            color = colors.warning.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE),
            start = Offset(padding, warningY),
            end = Offset(width - padding, warningY),
            strokeWidth = 1.dp.toPx(),
        )

        // 80% threshold (critical)
        val criticalY = padding + chartHeight * 0.2f
        drawLine(
            color = colors.critical.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE),
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
            color = colors.cpuUsage.copy(alpha = WormaCeptorDesignSystem.Alpha.LIGHT),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CpuChartCardEmptyPreview() {
    WormaCeptorTheme {
        CpuChartCard(
            history = persistentListOf(),
            colors = cpuColors(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CpuChartCardWithDataPreview() {
    WormaCeptorTheme {
        CpuChartCard(
            history = persistentListOf(
                CpuInfo(
                    timestamp = 1L,
                    overallUsagePercent = 20f,
                    perCoreUsage = listOf(15f, 25f, 18f, 22f),
                    coreCount = 4,
                    cpuFrequencyMHz = 2400L,
                    cpuTemperature = 38f,
                ),
                CpuInfo(
                    timestamp = 2L,
                    overallUsagePercent = 35f,
                    perCoreUsage = listOf(30f, 40f, 28f, 42f),
                    coreCount = 4,
                    cpuFrequencyMHz = 2400L,
                    cpuTemperature = 40f,
                ),
                CpuInfo(
                    timestamp = 3L,
                    overallUsagePercent = 55f,
                    perCoreUsage = listOf(50f, 60f, 48f, 62f),
                    coreCount = 4,
                    cpuFrequencyMHz = 2400L,
                    cpuTemperature = 44f,
                ),
                CpuInfo(
                    timestamp = 4L,
                    overallUsagePercent = 72f,
                    perCoreUsage = listOf(68f, 78f, 65f, 77f),
                    coreCount = 4,
                    cpuFrequencyMHz = 2400L,
                    cpuTemperature = 48f,
                ),
                CpuInfo(
                    timestamp = 5L,
                    overallUsagePercent = 45f,
                    perCoreUsage = listOf(40f, 50f, 38f, 52f),
                    coreCount = 4,
                    cpuFrequencyMHz = 2400L,
                    cpuTemperature = 42f,
                ),
            ),
            colors = cpuColors(),
        )
    }
}
