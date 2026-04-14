package com.azikar24.wormaceptor.core.ui.components.chip

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.modifier.wormaceptorFocusRing
import com.azikar24.wormaceptor.core.ui.modifier.wormaceptorPressScale
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.scaled

/**
 * Lightweight chip primitive for filters, tags, and categorical selections.
 * Holds no internal state -- `selected` and `onClick` are both hoisted so the
 * same component covers filter, input, and assist use cases without forking.
 *
 * @param label Chip text.
 * @param modifier Modifier for the root surface.
 * @param selected Whether the chip is in the selected state.
 * @param onClick Tap callback. Null makes the chip static.
 * @param leadingIcon Optional leading icon (e.g., filter glyph).
 * @param onDismiss Optional trailing close icon callback; when set, a close
 *                  affordance is shown at the end of the chip.
 * @param enabled Whether the chip responds to interactions.
 * @param accentColor Optional accent color that overrides the default primary color for the
 *                    selected state (container at [WormaCeptorTokens.Alpha.MEDIUM], label/icon at
 *                    full opacity). Useful for per-item categorical coloring such as severity or
 *                    log-level chips. Null falls back to [MaterialTheme.colorScheme.primary].
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
fun WormaCeptorChip(
    label: String,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null,
    onDismiss: (() -> Unit)? = null,
    enabled: Boolean = true,
    accentColor: Color? = null,
) {
    val baseColor = accentColor ?: MaterialTheme.colorScheme.primary
    val containerAlpha = if (accentColor != null) WormaCeptorTokens.Alpha.MEDIUM else WormaCeptorTokens.Alpha.LIGHT
    val container = if (selected) {
        baseColor.copy(alpha = containerAlpha)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE)
    }
    val content = if (selected) {
        baseColor
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = if (selected) {
        baseColor.copy(alpha = WormaCeptorTokens.Alpha.STRONG)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)
    }
    val interactionSource = remember { MutableInteractionSource() }
    val interactionModifier = if (onClick != null && enabled) {
        Modifier
            .wormaceptorPressScale(interactionSource)
            .wormaceptorFocusRing(interactionSource, WormaCeptorTokens.Shapes.pill)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
            )
    } else {
        Modifier
    }

    Surface(
        modifier = modifier.then(interactionModifier),
        shape = WormaCeptorTokens.Shapes.pill,
        color = container,
        contentColor = content.copy(
            alpha = if (enabled) WormaCeptorTokens.Alpha.OPAQUE else WormaCeptorTokens.Alpha.MODERATE,
        ),
        border = BorderStroke(WormaCeptorTokens.BorderWidth.regular, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.md.scaled(),
                vertical = WormaCeptorTokens.Spacing.xs.scaled(),
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs.scaled()),
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
            )
            if (onDismiss != null) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove $label",
                    modifier = Modifier
                        .size(WormaCeptorTokens.IconSize.sm)
                        .clickable(enabled = enabled, onClick = onDismiss),
                    tint = content,
                )
            }
        }
    }
}

// region Previews

@Preview(name = "Chip - Light")
@Composable
private fun ChipLightPreview() {
    WormaCeptorTheme {
        Surface(color = Color.Transparent) {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorChip(label = "All", selected = true, onClick = {})
                WormaCeptorChip(label = "GET", onClick = {})
                WormaCeptorChip(label = "POST", onClick = {}, onDismiss = {})
            }
        }
    }
}

@Preview(name = "Chip - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChipDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface(color = Color.Transparent) {
            Row(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorChip(label = "All", selected = true, onClick = {})
                WormaCeptorChip(label = "GET", onClick = {})
                WormaCeptorChip(label = "POST", onClick = {}, onDismiss = {})
            }
        }
    }
}

// endregion
