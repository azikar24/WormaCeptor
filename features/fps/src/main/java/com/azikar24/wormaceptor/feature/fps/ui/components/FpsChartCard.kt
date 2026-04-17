package com.azikar24.wormaceptor.feature.fps.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.metric.WormaCeptorChartLegendItem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.feature.fps.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.max

@Composable
internal fun FpsChartCard(
    history: ImmutableList<FpsInfo>,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier,
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.cardLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.fps_over_time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics { heading() },
            )

            FpsChartContent(
                history = history,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
            ) {
                WormaCeptorChartLegendItem(
                    label = stringResource(R.string.fps_legend_good),
                    color = WormaCeptorTokens.Colors.Fps.good,
                )
                WormaCeptorChartLegendItem(
                    label = stringResource(R.string.fps_legend_warning),
                    color = WormaCeptorTokens.Colors.Fps.warning,
                )
            }
        }
    }
}

@Composable
private fun FpsChartContent(
    history: ImmutableList<FpsInfo>,
    modifier: Modifier = Modifier,
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.fps_no_data),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = WormaCeptorTokens.Alpha.BOLD,
                ),
            )
        }
    } else {
        val latestFps = history.lastOrNull()?.currentFps?.toInt() ?: 0
        val chartDescription = stringResource(R.string.fps_chart_content_description, latestFps)
        FpsChart(
            data = history,
            modifier = modifier.semantics {
                contentDescription = chartDescription
            },
        )
    }
}

@Composable
private fun FpsChart(
    data: ImmutableList<FpsInfo>,
    modifier: Modifier = Modifier,
) {
    val lineColor = WormaCeptorTokens.Colors.Fps.chartLine()
    val fillColor = WormaCeptorTokens.Colors.Fps.chartLine().copy(
        alpha = WormaCeptorTokens.Alpha.MEDIUM,
    )
    val gridColor = MaterialTheme.colorScheme.outline.copy(
        alpha = WormaCeptorTokens.Alpha.MODERATE,
    )
    val goodThresholdColor = WormaCeptorTokens.Colors.Fps.good.copy(
        alpha = WormaCeptorTokens.Alpha.MODERATE,
    )
    val warningThresholdColor = WormaCeptorTokens.Colors.Fps.warning.copy(
        alpha = WormaCeptorTokens.Alpha.MODERATE,
    )

    val maxFps = remember(data) {
        max(data.maxOf { it.currentFps }, FpsChartConstants.CHART_CEILING)
    }

    Canvas(modifier = modifier) {
        val chartWidth = size.width - FpsChartConstants.CHART_PADDING * 2
        val chartHeight = size.height - FpsChartConstants.CHART_PADDING * 2

        drawFpsGridLines(
            maxFps = maxFps,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            gridColor = gridColor,
            goodThresholdColor = goodThresholdColor,
            warningThresholdColor = warningThresholdColor,
        )

        if (data.size < 2) return@Canvas

        drawFpsDataPaths(
            data = data,
            maxFps = maxFps,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            lineColor = lineColor,
            fillColor = fillColor,
        )
    }
}

@Suppress("LongParameterList")
private fun DrawScope.drawFpsGridLines(
    maxFps: Float,
    chartWidth: Float,
    chartHeight: Float,
    gridColor: Color,
    goodThresholdColor: Color,
    warningThresholdColor: Color,
) {
    val gridLines = listOf(0f, FpsChartConstants.WARNING_THRESHOLD, FpsChartConstants.GOOD_THRESHOLD, maxFps)
    gridLines.forEach { fps ->
        val y = FpsChartConstants.CHART_PADDING + chartHeight * (1 - fps / maxFps)
        when (fps) {
            FpsChartConstants.GOOD_THRESHOLD -> drawLine(
                color = goodThresholdColor,
                start = Offset(FpsChartConstants.CHART_PADDING, y),
                end = Offset(FpsChartConstants.CHART_PADDING + chartWidth, y),
                strokeWidth = FpsChartConstants.THRESHOLD_STROKE_WIDTH,
            )
            FpsChartConstants.WARNING_THRESHOLD -> drawLine(
                color = warningThresholdColor,
                start = Offset(FpsChartConstants.CHART_PADDING, y),
                end = Offset(FpsChartConstants.CHART_PADDING + chartWidth, y),
                strokeWidth = FpsChartConstants.THRESHOLD_STROKE_WIDTH,
            )
            else -> drawLine(
                color = gridColor,
                start = Offset(FpsChartConstants.CHART_PADDING, y),
                end = Offset(FpsChartConstants.CHART_PADDING + chartWidth, y),
                strokeWidth = FpsChartConstants.GRID_STROKE_WIDTH,
            )
        }
    }
}

@Suppress("LongParameterList")
private fun DrawScope.drawFpsDataPaths(
    data: ImmutableList<FpsInfo>,
    maxFps: Float,
    chartWidth: Float,
    chartHeight: Float,
    lineColor: Color,
    fillColor: Color,
) {
    val linePath = Path()
    val fillPath = Path()
    val xStep = chartWidth / (data.size - 1).coerceAtLeast(1)

    data.forEachIndexed { index, info ->
        val x = FpsChartConstants.CHART_PADDING + index * xStep
        val y = FpsChartConstants.CHART_PADDING + chartHeight * (1 - info.currentFps / maxFps)

        if (index == 0) {
            linePath.moveTo(x, y)
            fillPath.moveTo(x, FpsChartConstants.CHART_PADDING + chartHeight)
            fillPath.lineTo(x, y)
        } else {
            linePath.lineTo(x, y)
            fillPath.lineTo(x, y)
        }
    }

    fillPath.lineTo(
        FpsChartConstants.CHART_PADDING + (data.size - 1) * xStep,
        FpsChartConstants.CHART_PADDING + chartHeight,
    )
    fillPath.close()

    drawPath(path = fillPath, color = fillColor)
    drawPath(
        path = linePath,
        color = lineColor,
        style = Stroke(width = FpsChartConstants.LINE_STROKE_WIDTH),
    )
}

@Suppress("MagicNumber")
private val previewFpsHistory = persistentListOf(
    FpsInfo(58f, 58f, 58f, 58f, 0, 0, 1L),
    FpsInfo(55f, 56f, 55f, 58f, 1, 0, 2L),
    FpsInfo(42f, 51f, 42f, 58f, 3, 1, 3L),
    FpsInfo(30f, 46f, 30f, 58f, 5, 2, 4L),
    FpsInfo(50f, 47f, 30f, 58f, 5, 2, 5L),
    FpsInfo(60f, 49f, 30f, 60f, 5, 2, 6L),
)

@Preview(name = "FpsChartCard - Light")
@Composable
private fun FpsChartCardPreview() {
    WormaCeptorTheme {
        FpsChartCard(
            history = previewFpsHistory,
            modifier = Modifier.height(200.dp),
        )
    }
}

@Preview(name = "FpsChartCard - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FpsChartCardDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        FpsChartCard(
            history = persistentListOf(),
            modifier = Modifier.height(200.dp),
        )
    }
}
