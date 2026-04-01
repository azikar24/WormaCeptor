package com.azikar24.wormaceptor.feature.recomposition.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.feature.recomposition.R

@Immutable
data class RecompositionColors(
    val normal: Color,
    val elevated: Color,
    val excessive: Color,
    val critical: Color,
) {
    /** Returns a color for the given recomposition rate per second. */
    fun colorForRate(ratePerSecond: Float): Color = when {
        ratePerSecond <= 2f -> normal
        ratePerSecond <= 5f -> elevated
        ratePerSecond <= 10f -> excessive
        else -> critical
    }
}

@Composable
fun recompositionColors(): RecompositionColors = RecompositionColors(
    normal = WormaCeptorColors.StatusGreen,
    elevated = WormaCeptorColors.StatusAmber,
    excessive = WormaCeptorColors.StatusOrange,
    critical = WormaCeptorColors.StatusRed,
)

/** Returns a human-readable status label for the given recomposition rate. */
@Composable
fun statusLabelForRate(ratePerSecond: Float): String = when {
    ratePerSecond <= 2f -> stringResource(R.string.recomposition_status_normal)
    ratePerSecond <= 5f -> stringResource(R.string.recomposition_status_elevated)
    ratePerSecond <= 10f -> stringResource(R.string.recomposition_status_excessive)
    else -> stringResource(R.string.recomposition_status_critical)
}
