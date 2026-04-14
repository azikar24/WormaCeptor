package com.azikar24.wormaceptor.core.ui.components.badge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Tonal status/category badge with caller-provided container and content colors.
 * Thin specialization of [WormaCeptorBadge] in its Tonal variant.
 */
@Composable
fun WormaCeptorStatusBadge(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    WormaCeptorBadge(
        text = text,
        variant = BadgeVariant.Tonal(containerColor = containerColor, contentColor = contentColor),
        modifier = modifier,
    )
}

// region Previews

@Preview(name = "StatusBadge")
@Composable
private fun StatusBadgePreview() {
    WormaCeptorTheme {
        Surface {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorStatusBadge(
                    text = "C",
                    containerColor = WormaCeptorTokens.Colors.Status.red.copy(
                        alpha = WormaCeptorTokens.Alpha.LIGHT,
                    ),
                    contentColor = WormaCeptorTokens.Colors.Status.red,
                )
                WormaCeptorStatusBadge(
                    text = "DISK",
                    containerColor = WormaCeptorTokens.Colors.Category.simulation.copy(
                        alpha = WormaCeptorTokens.Alpha.LIGHT,
                    ),
                    contentColor = WormaCeptorTokens.Colors.Category.simulation,
                )
                WormaCeptorStatusBadge(
                    text = "SO",
                    containerColor = WormaCeptorTokens.Colors.HttpMethod.get.copy(
                        alpha = WormaCeptorTokens.Alpha.LIGHT,
                    ),
                    contentColor = WormaCeptorTokens.Colors.HttpMethod.get,
                )
            }
        }
    }
}

// endregion
