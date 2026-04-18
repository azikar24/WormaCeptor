package com.azikar24.wormaceptor.feature.fps.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.feature.fps.R
import com.azikar24.wormaceptor.feature.fps.ui.util.FpsStatus
import com.azikar24.wormaceptor.feature.fps.ui.util.classifyFps
import com.azikar24.wormaceptor.feature.fps.ui.util.fpsBackgroundColor
import com.azikar24.wormaceptor.feature.fps.ui.util.fpsStatusColor
import kotlin.math.roundToInt

@Composable
internal fun CurrentFpsCard(
    fpsInfo: FpsInfo,
    isMonitoring: Boolean,
    modifier: Modifier = Modifier,
) {
    val status = classifyFps(fpsInfo.currentFps)

    val fpsColor by animateColorAsState(
        targetValue = fpsStatusColor(status),
        animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        label = "fps_color",
    )

    val backgroundColor by animateColorAsState(
        targetValue = fpsBackgroundColor(status),
        animationSpec = tween(WormaCeptorTokens.Animation.PAGE),
        label = "fps_background",
    )

    WormaCeptorCard(
        modifier = modifier,
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.cardExtraLarge,
        backgroundColor = backgroundColor,
        borderColor = if (status != FpsStatus.Idle) {
            fpsColor.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)
        } else {
            null
        },
    ) {
        FpsCardContent(
            fpsInfo = fpsInfo,
            status = status,
            isMonitoring = isMonitoring,
            fpsColor = fpsColor,
        )
    }
}

@Composable
private fun FpsCardContent(
    fpsInfo: FpsInfo,
    status: FpsStatus,
    isMonitoring: Boolean,
    fpsColor: Color,
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
            color = WormaCeptorTokens.semantic().textSecondary,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        Text(
            text = if (status != FpsStatus.Idle || isMonitoring) {
                fpsInfo.currentFps.roundToInt().toString()
            } else {
                "--"
            },
            style = WormaCeptorTokens.Typography.displayNumber,
            color = fpsColor,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

        Text(
            text = when (status) {
                FpsStatus.Excellent -> stringResource(R.string.fps_status_excellent)
                FpsStatus.Moderate -> stringResource(R.string.fps_status_moderate)
                FpsStatus.Poor -> stringResource(R.string.fps_status_poor)
                FpsStatus.Idle -> stringResource(R.string.fps_status_not_monitoring)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (status != FpsStatus.Idle) fpsColor else WormaCeptorTokens.semantic().textSecondary,
        )
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

@Preview(name = "CurrentFpsCard - Idle")
@Composable
private fun CurrentFpsCardIdlePreview() {
    WormaCeptorTheme {
        CurrentFpsCard(
            fpsInfo = FpsInfo(
                currentFps = 0f,
                averageFps = 0f,
                minFps = 0f,
                maxFps = 0f,
                droppedFrames = 0,
                jankFrames = 0,
                timestamp = System.currentTimeMillis(),
            ),
            isMonitoring = false,
        )
    }
}
