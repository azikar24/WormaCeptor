package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Simple colored dot indicator for inline status display.
 *
 * @param color Dot fill color
 * @param modifier Modifier for the root composable
 * @param size Dot diameter (defaults to [WormaCeptorDesignSystem.Spacing.sm])
 */
@Composable
fun WormaCeptorStatusDot(color: Color, modifier: Modifier = Modifier, size: Dp = WormaCeptorDesignSystem.Spacing.sm) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color),
    )
}
