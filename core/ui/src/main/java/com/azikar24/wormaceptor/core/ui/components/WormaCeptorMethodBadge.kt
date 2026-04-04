package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Badge displaying an HTTP method name with color-coded styling.
 * Uses [WormaCeptorTokens.Colors.HttpMethod.forMethod] for consistent method coloring.
 *
 * @param method HTTP method string (GET, POST, PUT, etc.)
 * @param modifier Modifier for the root composable
 */
@Composable
fun WormaCeptorMethodBadge(
    method: String,
    modifier: Modifier = Modifier,
) {
    val color = WormaCeptorTokens.Colors.HttpMethod.forMethod(method)
    Surface(
        modifier = modifier,
        color = color.copy(alpha = WormaCeptorTokens.Alpha.SOFT),
        contentColor = color,
        shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
    ) {
        Text(
            text = method.uppercase(),
            style = WormaCeptorTokens.Typography.codeSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.xs,
                vertical = WormaCeptorTokens.Spacing.xxs,
            ),
        )
    }
}

// region Previews

@Preview(name = "MethodBadge")
@Composable
private fun MethodBadgePreview() {
    WormaCeptorTheme {
        Surface {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorMethodBadge(method = "GET")
                WormaCeptorMethodBadge(method = "POST")
                WormaCeptorMethodBadge(method = "PUT")
                WormaCeptorMethodBadge(method = "DELETE")
            }
        }
    }
}

// endregion
