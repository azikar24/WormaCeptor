package com.azikar24.wormaceptor.core.ui.components.card

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
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
 * Thin delegate to [WormaCeptorCard] with an outlined style, exposing a
 * [BoxScope] content slot for callers that need absolute positioning.
 *
 * New code should prefer [WormaCeptorCard] directly. This wrapper exists to
 * keep the ~10 existing callers working unchanged while consolidating all
 * card-like rendering on a single primitive.
 */
@Composable
fun WormaCeptorContainer(
    modifier: Modifier = Modifier,
    shape: Shape = WormaCeptorTokens.Shapes.card,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    WormaCeptorCard(
        modifier = modifier,
        style = CardStyle.Outlined,
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
    ) {
        Box(content = content)
    }
}

/**
 * Clickable variant of [WormaCeptorContainer]. Delegates to [WormaCeptorCard]
 * so press-scale, focus ring, and ripple are inherited for free.
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
    WormaCeptorCard(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        style = CardStyle.Outlined,
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
    ) {
        Box(content = content)
    }
}

@Preview(name = "Container - Light")
@Composable
private fun WormaCeptorContainerPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorContainer(modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg)) {
                Text(
                    text = "Outlined container",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WormaCeptorTokens.Spacing.md),
                )
            }
        }
    }
}
