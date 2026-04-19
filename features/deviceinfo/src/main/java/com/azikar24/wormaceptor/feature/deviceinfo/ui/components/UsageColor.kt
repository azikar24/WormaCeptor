package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

private const val CriticalUsageThreshold = 90f
private const val WarningUsageThreshold = 50f

@Composable
internal fun usageColor(percentage: Float): Color {
    return when {
        percentage >= CriticalUsageThreshold -> WormaCeptorTokens.Colors.Status.red
        percentage >= WarningUsageThreshold -> WormaCeptorTokens.Colors.Status.amber
        else -> WormaCeptorTokens.Colors.Status.green
    }
}
