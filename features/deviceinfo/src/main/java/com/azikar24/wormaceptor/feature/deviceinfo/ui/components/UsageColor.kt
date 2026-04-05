package com.azikar24.wormaceptor.feature.deviceinfo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

@Composable
internal fun usageColor(percentage: Float): Color {
    return when {
        percentage >= 90f -> WormaCeptorTokens.Colors.Status.red
        percentage >= 50f -> WormaCeptorTokens.Colors.Status.amber
        else -> WormaCeptorTokens.Colors.Status.green
    }
}
