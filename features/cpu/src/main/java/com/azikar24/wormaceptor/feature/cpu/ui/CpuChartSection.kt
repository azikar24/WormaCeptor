package com.azikar24.wormaceptor.feature.cpu.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.metric.WormaCeptorChartLegendItem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CpuInfo
import com.azikar24.wormaceptor.feature.cpu.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.roundToInt

private const val GridLineCount = 4
private const val WarningThresholdY = 0.5f
private const val CriticalThresholdY = 0.2f
private const val PercentDivisor = 100f
private val ChartHeight = 200.dp

@Composable
internal fun CpuChartCard(
    history: ImmutableList<CpuInfo>,
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
                text = stringResource(R.string.cpu_usage_over_time),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() },
            )

            if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ChartHeight)
                        .clip(WormaCeptorTokens.Shapes.card)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.cpu_no_data),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                val cpuPct = history.lastOrNull()?.overallUsagePercent?.roundToInt() ?: 0
                val chartDescription = stringResource(R.string.cpu_chart_content_description, cpuPct)
                CpuLineChart(
                    history = history,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ChartHeight)
                        .semantics {
                            contentDescription = chartDescription
                        },
                )
            }

            WormaCeptorChartLegendItem(
                label = stringResource(R.string.cpu_usage_label),
                color = WormaCeptorTokens.Colors.Cpu.usage,
            )
        }
    }
}

@Composable
private fun CpuLineChart(
    history: ImmutableList<CpuInfo>,
    modifier: Modifier = Modifier,
) {
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)

    Canvas(
        modifier = modifier
            .clip(WormaCeptorTokens.Shapes.card)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val padding = WormaCeptorTokens.Spacing.lg.toPx()
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        drawGridLines(gridColor, padding, chartHeight, size.width)
        drawThresholdLines(padding, chartHeight, size.width)

        if (history.size < 2) return@Canvas

        val points = computeChartPoints(history, padding, chartWidth, chartHeight)
        drawCpuUsageLine(points)
        drawCpuAreaFill(points, padding, chartWidth, chartHeight)
    }
}

private fun DrawScope.drawGridLines(
    gridColor: Color,
    padding: Float,
    chartHeight: Float,
    width: Float,
) {
    for (i in 0..GridLineCount) {
        val y = padding + chartHeight / GridLineCount * i
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = WormaCeptorTokens.BorderWidth.regular.toPx(),
        )
    }
}

private fun DrawScope.drawThresholdLines(
    padding: Float,
    chartHeight: Float,
    width: Float,
) {
    val warningY = padding + chartHeight * WarningThresholdY
    drawLine(
        color = WormaCeptorTokens.Colors.Status.amber.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
        start = Offset(padding, warningY),
        end = Offset(width - padding, warningY),
        strokeWidth = WormaCeptorTokens.BorderWidth.regular.toPx(),
    )

    val criticalY = padding + chartHeight * CriticalThresholdY
    drawLine(
        color = WormaCeptorTokens.Colors.Status.red.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
        start = Offset(padding, criticalY),
        end = Offset(width - padding, criticalY),
        strokeWidth = WormaCeptorTokens.BorderWidth.regular.toPx(),
    )
}

private fun computeChartPoints(
    history: ImmutableList<CpuInfo>,
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
): List<Offset> = history.mapIndexed { index, info ->
    Offset(
        x = padding + chartWidth / (history.size - 1) * index,
        y = padding + chartHeight - info.overallUsagePercent / PercentDivisor * chartHeight,
    )
}

private fun DrawScope.drawCpuUsageLine(points: List<Offset>) {
    val cpuPath = Path()
    points.forEachIndexed { index, point ->
        if (index == 0) cpuPath.moveTo(point.x, point.y) else cpuPath.lineTo(point.x, point.y)
    }
    drawPath(
        path = cpuPath,
        color = WormaCeptorTokens.Colors.Cpu.usage,
        style = Stroke(
            width = WormaCeptorTokens.BorderWidth.thick.toPx(),
            cap = StrokeCap.Round,
        ),
    )
}

private fun DrawScope.drawCpuAreaFill(
    points: List<Offset>,
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
) {
    val baseline = padding + chartHeight
    val areaPath = Path().apply {
        moveTo(points.first().x, baseline)
        points.forEach { lineTo(it.x, it.y) }
        lineTo(padding + chartWidth, baseline)
        close()
    }
    drawPath(
        path = areaPath,
        color = WormaCeptorTokens.Colors.Cpu.usage.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
    )
}

@Preview(showBackground = true)
@Composable
private fun CpuChartCardEmptyPreview() {
    WormaCeptorTheme {
        CpuChartCard(
            history = persistentListOf(),
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
        )
    }
}
