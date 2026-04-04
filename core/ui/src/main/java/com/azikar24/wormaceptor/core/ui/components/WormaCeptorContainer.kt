package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Unified container component for WormaCeptor.
 *
 * Provides consistent styling for containers throughout the app
 *
 * @param modifier Modifier for the container
 * @param shape Shape of the container (defaults to card shape)
 * @param backgroundColor Optional custom background color (overrides style default)
 * @param borderColor Optional custom border color (only applies to Outlined style)
 * @param content Content of the container
 */
@Composable
fun WormaCeptorContainer(
    modifier: Modifier = Modifier,
    shape: Shape = WormaCeptorTokens.Shapes.card,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorTokens.Alpha.SUBTLE,
        ),
        border = BorderStroke(
            width = WormaCeptorTokens.BorderWidth.regular,
            color = borderColor ?: MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorTokens.Alpha.MEDIUM,
            ),
        ),
    ) {
        Box(content = content)
    }
}

/**
 * Clickable variant of WormaCeptorContainer.
 *
 * @param onClick Callback when container is clicked
 * @param modifier Modifier for the container
 * @param shape Shape of the container
 * @param backgroundColor Optional custom background color
 * @param borderColor Optional custom border color
 * @param enabled Whether the container is clickable
 * @param content Content of the container
 */
@Composable
fun WormaCeptorContainer(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = WormaCeptorTokens.Shapes.card,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = backgroundColor ?: MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorTokens.Alpha.SUBTLE,
        ),
        border = BorderStroke(
            width = WormaCeptorTokens.BorderWidth.regular,
            color = borderColor ?: MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorTokens.Alpha.MEDIUM,
            ),
        ),
    ) {
        Box(content = content)
    }
}

@Preview(name = "Container - Light")
@Composable
private fun WormaCeptorContainerPreview() {
    WormaCeptorTheme {
        Surface {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorContainer {
                    Text(
                        text = "Outlined container",
                        modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                    )
                }
            }
        }
    }
}
