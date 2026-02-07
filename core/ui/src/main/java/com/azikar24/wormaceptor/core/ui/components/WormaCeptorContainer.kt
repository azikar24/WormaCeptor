package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Container style variants for consistent UI across WormaCeptor.
 */
enum class ContainerStyle {
    /**
     * Filled container with no visible border.
     * Uses surfaceVariant background at medium alpha.
     * Best for: cards, tiles, content areas.
     */
    Filled,

    /**
     * Outlined container with subtle border and light background.
     * Uses outlineVariant border with subtle surfaceVariant fill.
     * Best for: list items, selectable cards, grouped content.
     */
    Outlined,
}

/**
 * Unified container component for WormaCeptor.
 *
 * Provides consistent styling for containers throughout the app with two variants:
 * - [ContainerStyle.Filled]: Solid background, no border (modern, clean look)
 * - [ContainerStyle.Outlined]: Subtle border with light fill (for list items, selectable content)
 *
 * @param modifier Modifier for the container
 * @param style Container style variant
 * @param shape Shape of the container (defaults to card shape)
 * @param backgroundColor Optional custom background color (overrides style default)
 * @param borderColor Optional custom border color (only applies to Outlined style)
 * @param content Content of the container
 */
@Composable
fun WormaCeptorContainer(
    modifier: Modifier = Modifier,
    style: ContainerStyle = ContainerStyle.Outlined,
    shape: Shape = WormaCeptorDesignSystem.Shapes.card,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val defaultBackgroundColor = when (style) {
        ContainerStyle.Filled -> MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorDesignSystem.Alpha.strong,
        )
        ContainerStyle.Outlined -> MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorDesignSystem.Alpha.subtle,
        )
    }

    val defaultBorderStroke = when (style) {
        ContainerStyle.Filled -> null
        ContainerStyle.Outlined -> BorderStroke(
            width = WormaCeptorDesignSystem.BorderWidth.regular,
            color = borderColor ?: MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorDesignSystem.Alpha.medium,
            ),
        )
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor ?: defaultBackgroundColor,
        border = defaultBorderStroke,
    ) {
        Box(content = content)
    }
}

/**
 * Clickable variant of WormaCeptorContainer.
 *
 * @param onClick Callback when container is clicked
 * @param modifier Modifier for the container
 * @param style Container style variant
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
    style: ContainerStyle = ContainerStyle.Outlined,
    shape: Shape = WormaCeptorDesignSystem.Shapes.card,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    enabled: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val defaultBackgroundColor = when (style) {
        ContainerStyle.Filled -> MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorDesignSystem.Alpha.strong,
        )
        ContainerStyle.Outlined -> MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorDesignSystem.Alpha.subtle,
        )
    }

    val defaultBorderStroke = when (style) {
        ContainerStyle.Filled -> null
        ContainerStyle.Outlined -> BorderStroke(
            width = WormaCeptorDesignSystem.BorderWidth.regular,
            color = borderColor ?: MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = WormaCeptorDesignSystem.Alpha.medium,
            ),
        )
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        color = backgroundColor ?: defaultBackgroundColor,
        border = defaultBorderStroke,
    ) {
        Box(content = content)
    }
}
