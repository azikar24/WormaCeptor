package com.azikar24.wormaceptor.feature.fps.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorSummaryCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.feature.fps.R
import com.azikar24.wormaceptor.feature.fps.ui.util.fpsColorForValue
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
        WormaCeptorSummaryCard(
            count = if (fpsInfo.minFps > 0) fpsInfo.minFps.roundToInt().toString() else "--",
            label = stringResource(R.string.fps_min),
            color = if (fpsInfo.minFps > 0) {
                fpsColorForValue(fpsInfo.minFps)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )

        WormaCeptorSummaryCard(
            count = if (fpsInfo.averageFps > 0) fpsInfo.averageFps.roundToInt().toString() else "--",
            label = stringResource(R.string.fps_avg),
            color = if (fpsInfo.averageFps > 0) {
                fpsColorForValue(fpsInfo.averageFps)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )

        WormaCeptorSummaryCard(
            count = if (fpsInfo.maxFps > 0) fpsInfo.maxFps.roundToInt().toString() else "--",
            label = stringResource(R.string.fps_max),
            color = if (fpsInfo.maxFps > 0) {
                fpsColorForValue(fpsInfo.maxFps)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )
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
                minFps = 35f,
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
