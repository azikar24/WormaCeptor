package com.azikar24.wormaceptor.feature.recomposition.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.recomposition.R

private const val ElevatedThreshold = 2f
private const val ExcessiveThreshold = 5f
private const val CriticalThreshold = 10f

@Composable
internal fun rateColor(ratePerSecond: Float): Color {
    val recomp = WormaCeptorTokens.Colors.Recomposition
    return when {
        ratePerSecond <= ElevatedThreshold -> recomp.normal
        ratePerSecond <= ExcessiveThreshold -> recomp.elevated
        ratePerSecond <= CriticalThreshold -> recomp.excessive
        else -> recomp.critical
    }
}

@Composable
internal fun rateLabel(ratePerSecond: Float): String = when {
    ratePerSecond <= ElevatedThreshold -> stringResource(R.string.recomposition_status_normal)
    ratePerSecond <= ExcessiveThreshold -> stringResource(R.string.recomposition_status_elevated)
    ratePerSecond <= CriticalThreshold -> stringResource(R.string.recomposition_status_excessive)
    else -> stringResource(R.string.recomposition_status_critical)
}
