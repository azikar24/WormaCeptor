package com.azikar24.wormaceptor.core.ui.components.chip

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.R
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
    // Size must not depend on `enabled`, or toggling it makes rows of chips jump.
    val interactionModifier = if (onClick != null) {
        Modifier
            .minimumInteractiveComponentSize()
            .wormaceptorPressScale(interactionSource)
            .wormaceptorFocusRing(interactionSource, WormaCeptorTokens.Shapes.pill)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .semantics { this.selected = selected }
    } else {
        Modifier
    }

    // The dismiss target is a sibling overlay so its 48dp hit area isn't clipped by the pill.
    Box(
        modifier = modifier.then(interactionModifier),
        contentAlignment = Alignment.CenterStart,
    ) {
        ChipSurface(
            label = label,
            container = container,
            content = content,
            borderColor = borderColor,
            enabled = enabled,
            leadingIcon = leadingIcon,
            showDismiss = onDismiss != null,
        )
        if (onDismiss != null) {
            val dismissDescription = stringResource(R.string.chip_dismiss_description, label)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(WormaCeptorTokens.TouchTarget.comfortable)
                    .clickable(
                        interactionSource = null,
                        indication = ripple(bounded = false, radius = WormaCeptorTokens.TouchTarget.comfortable / 2),
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onDismiss,
                    )
                    .semantics { contentDescription = dismissDescription },
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun ChipSurface(
    label: String,
    container: Color,
    content: Color,
    borderColor: Color,
    enabled: Boolean,
    leadingIcon: ImageVector?,
    showDismiss: Boolean,
) {
    Surface(
        shape = WormaCeptorTokens.Shapes.pill,
        color = container,
        contentColor = content.copy(
            alpha = if (enabled) WormaCeptorTokens.Alpha.OPAQUE else WormaCeptorTokens.Alpha.MODERATE,
        ),
        border = BorderStroke(WormaCeptorTokens.BorderWidth.regular, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.lg.scaled(),
                vertical = WormaCeptorTokens.Spacing.sm.scaled(),
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
            if (showDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
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
