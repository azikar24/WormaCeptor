package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Badge displaying an HTTP method name with color-coded styling.
 * Uses [WormaCeptorColors.HttpMethod.forMethod] for consistent method coloring.
 *
 * @param method HTTP method string (GET, POST, PUT, etc.)
 * @param modifier Modifier for the root composable
 */
@Composable
fun WormaCeptorMethodBadge(
    method: String,
    modifier: Modifier = Modifier,
) {
    val color = WormaCeptorColors.HttpMethod.forMethod(method)
    Surface(
        modifier = modifier,
        color = color.copy(alpha = WormaCeptorDesignSystem.Alpha.soft),
        contentColor = color,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
    ) {
        Text(
            text = method.uppercase(),
            style = WormaCeptorDesignSystem.Typography.codeSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.xs,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
        )
    }
}
