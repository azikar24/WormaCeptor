package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

/**
 * Unified divider component for WormaCeptor.
 *
 * Replaces raw [HorizontalDivider] calls with consistent, semantic variants.
 *
 * @param modifier Modifier for the divider
 * @param style Divider style variant
 */
@Composable
fun WormaCeptorDivider(modifier: Modifier = Modifier, style: DividerStyle = DividerStyle.Standard) {
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    when (style) {
        DividerStyle.Standard -> HorizontalDivider(modifier = modifier)
        DividerStyle.Subtle -> HorizontalDivider(
            modifier = modifier,
            color = outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        )
        DividerStyle.Section -> HorizontalDivider(
            modifier = modifier,
            color = outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.bold),
        )
        DividerStyle.Thick -> HorizontalDivider(
            modifier = modifier,
            thickness = 2.dp,
        )
    }
}

/**
 * Divider style variants for consistent visual separation across WormaCeptor.
 */
enum class DividerStyle {
    /**
     * Standard list item divider using the theme's outline color.
     * Best for: list separators, menu dividers, generic content breaks.
     */
    Standard,

    /**
     * Subtle divider with reduced opacity for lightweight separation.
     * Best for: nested content, form field separators, secondary groupings.
     */
    Subtle,

    /**
     * Prominent section divider with stronger visibility.
     * Best for: major section breaks, tab content dividers, header/content separation.
     */
    Section,

    /**
     * Thick divider (2dp) for structural emphasis.
     * Best for: table header separators, data grid boundaries.
     */
    Thick,
}
