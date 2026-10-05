package com.azikar24.wormaceptor.feature.fps.ui.util

import androidx.compose.runtime.Composable
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.fps.ui.components.FpsChartConstants

internal fun classifyFps(currentFps: Float): FpsStatus = when {
    currentFps >= FpsChartConstants.GOOD_THRESHOLD -> FpsStatus.Excellent
    currentFps >= FpsChartConstants.WARNING_THRESHOLD -> FpsStatus.Moderate
    currentFps > 0 -> FpsStatus.Poor
    else -> FpsStatus.Idle
}

@Composable
internal fun fpsStatusColor(status: FpsStatus) = when (status) {
    FpsStatus.Excellent -> WormaCeptorTokens.Colors.Status.green
    FpsStatus.Moderate -> WormaCeptorTokens.Colors.Status.amber
    FpsStatus.Poor -> WormaCeptorTokens.Colors.Status.red
    FpsStatus.Idle -> WormaCeptorTokens.semantic().textSecondary
}

/** Semantic tokens, not raw status colors: these keep >= 3:1 non-text contrast on white. */
@Composable
internal fun fpsIndicatorColor(status: FpsStatus) = when (status) {
    FpsStatus.Excellent -> WormaCeptorTokens.semantic().success
    FpsStatus.Moderate -> WormaCeptorTokens.semantic().warning
    FpsStatus.Poor -> WormaCeptorTokens.semantic().error
    FpsStatus.Idle -> WormaCeptorTokens.semantic().textTertiary
}

@Composable
internal fun fpsBackgroundColor(status: FpsStatus) = when (status) {
    FpsStatus.Excellent -> WormaCeptorTokens.Colors.Status.green.copy(
        alpha = WormaCeptorTokens.Alpha.LIGHT,
    )
    FpsStatus.Moderate -> WormaCeptorTokens.Colors.Status.amber.copy(
        alpha = WormaCeptorTokens.Alpha.LIGHT,
    )
    FpsStatus.Poor -> WormaCeptorTokens.Colors.Status.red.copy(
        alpha = WormaCeptorTokens.Alpha.LIGHT,
    )
    FpsStatus.Idle -> WormaCeptorTokens.semantic().surfaceVariant.copy(
        alpha = WormaCeptorTokens.Alpha.MODERATE,
    )
}
