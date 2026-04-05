package com.azikar24.wormaceptor.feature.fps.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.feature.fps.R
import kotlin.math.roundToInt

@Composable
internal fun CurrentFpsCard(
    fpsInfo: FpsInfo,
    isMonitoring: Boolean,
    modifier: Modifier = Modifier,
) {
    val fpsColor by animateColorAsState(
        targetValue = if (fpsInfo.currentFps > 0) {
            when {
                fpsInfo.currentFps >= 55f -> WormaCeptorTokens.Colors.Status.green
                fpsInfo.currentFps >= 30f -> WormaCeptorTokens.Colors.Status.amber
                else -> WormaCeptorTokens.Colors.Status.red
            }
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        label = "fps_color",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (fpsInfo.currentFps > 0) {
            when {
                fpsInfo.currentFps >= 55f -> WormaCeptorTokens.Colors.Status.green.copy(
                    alpha = WormaCeptorTokens.Alpha.LIGHT,
                )
                fpsInfo.currentFps >= 30f -> WormaCeptorTokens.Colors.Status.amber.copy(
                    alpha = WormaCeptorTokens.Alpha.LIGHT,
                )
                else -> WormaCeptorTokens.Colors.Status.red.copy(alpha = WormaCeptorTokens.Alpha.LIGHT)
            }
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
        },
        animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        label = "fps_background",
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorTokens.Radius.xl),
        color = backgroundColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.fps_current),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

            Text(
                text = if (fpsInfo.currentFps > 0 || isMonitoring) {
                    fpsInfo.currentFps.roundToInt().toString()
                } else {
                    "--"
                },
                style = WormaCeptorTokens.Typography.displayNumber,
                color = fpsColor,
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

            Text(
                text = when {
                    fpsInfo.currentFps >= 55f -> stringResource(R.string.fps_status_excellent)
                    fpsInfo.currentFps >= 30f -> stringResource(
                        R.string.fps_status_moderate,
                    )
                    fpsInfo.currentFps > 0 -> stringResource(R.string.fps_status_poor)
                    else -> stringResource(R.string.fps_status_not_monitoring)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (fpsInfo.currentFps > 0) fpsColor else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "CurrentFpsCard - Light")
@Composable
private fun CurrentFpsCardPreview() {
    WormaCeptorTheme {
        CurrentFpsCard(
            fpsInfo = FpsInfo(
                currentFps = 58f,
                averageFps = 55f,
                minFps = 42f,
                maxFps = 60f,
                droppedFrames = 3,
                jankFrames = 1,
                timestamp = System.currentTimeMillis(),
            ),
            isMonitoring = true,
        )
    }
}

@Preview(name = "CurrentFpsCard - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CurrentFpsCardDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        CurrentFpsCard(
            fpsInfo = FpsInfo(
                currentFps = 24f,
                averageFps = 30f,
                minFps = 15f,
                maxFps = 45f,
                droppedFrames = 12,
                jankFrames = 5,
                timestamp = System.currentTimeMillis(),
            ),
            isMonitoring = true,
        )
    }
}
