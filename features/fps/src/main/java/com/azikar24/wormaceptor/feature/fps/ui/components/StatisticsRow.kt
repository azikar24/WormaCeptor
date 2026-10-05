package com.azikar24.wormaceptor.feature.fps.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.status.WormaCeptorStatusDot
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.scaled
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.feature.fps.R
import com.azikar24.wormaceptor.feature.fps.ui.util.classifyFps
import com.azikar24.wormaceptor.feature.fps.ui.util.fpsIndicatorColor
import kotlin.math.roundToInt

@Composable
internal fun StatisticsRow(
    fpsInfo: FpsInfo,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        FpsStatCard(fps = fpsInfo.minFps, label = stringResource(R.string.fps_min), modifier = Modifier.weight(1f))
        FpsStatCard(fps = fpsInfo.averageFps, label = stringResource(R.string.fps_avg), modifier = Modifier.weight(1f))
        FpsStatCard(fps = fpsInfo.maxFps, label = stringResource(R.string.fps_max), modifier = Modifier.weight(1f))
    }
}

/** Value stays in text colors for contrast; the threshold color is carried by the dot. */
@Composable
private fun FpsStatCard(
    fps: Float,
    label: String,
    modifier: Modifier = Modifier,
) {
    val hasValue = fps > 0
    WormaCeptorCard(
        modifier = modifier,
        shape = WormaCeptorTokens.Shapes.cardLarge,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md.scaled()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (hasValue) fps.roundToInt().toString() else "--",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (hasValue) {
                    WormaCeptorTokens.semantic().textPrimary
                } else {
                    WormaCeptorTokens.semantic().textSecondary
                },
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
            ) {
                if (hasValue) {
                    WormaCeptorStatusDot(color = fpsIndicatorColor(classifyFps(fps)))
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = WormaCeptorTokens.semantic().textSecondary,
                )
            }
        }
    }
}

@Preview(name = "StatisticsRow - Light")
@Composable
private fun StatisticsRowPreview() {
    WormaCeptorTheme {
        StatisticsRow(
            fpsInfo = FpsInfo(
                currentFps = 58f,
                averageFps = 52f,
                minFps = 12f,
                maxFps = 60f,
                droppedFrames = 4,
                jankFrames = 1,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }
}

@Preview(name = "StatisticsRow - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatisticsRowDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        StatisticsRow(
            fpsInfo = FpsInfo(
                currentFps = 0f,
                averageFps = 0f,
                minFps = 0f,
                maxFps = 0f,
                droppedFrames = 0,
                jankFrames = 0,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }
}
