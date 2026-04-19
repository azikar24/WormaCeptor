package com.azikar24.wormaceptor.core.ui.components.badge

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * HTTP method badge (GET, POST, PUT, ...). Specialization of [WormaCeptorBadge]
 * that maps the method name to its brand color via
 * [WormaCeptorTokens.Colors.HttpMethod.forMethod] and renders it in the Soft variant
 * with the code typography scale.
 */
@Composable
fun WormaCeptorMethodBadge(
    method: String,
    modifier: Modifier = Modifier,
) {
    WormaCeptorBadge(
        text = method.uppercase(),
        variant = BadgeVariant.Soft(WormaCeptorTokens.Colors.HttpMethod.forMethod(method)),
        modifier = modifier,
        textStyle = WormaCeptorTokens.Typography.codeSmall,
        horizontalPadding = WormaCeptorTokens.Spacing.xs,
        verticalPadding = WormaCeptorTokens.Spacing.xxs,
    )
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
