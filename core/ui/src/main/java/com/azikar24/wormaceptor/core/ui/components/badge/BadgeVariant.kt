package com.azikar24.wormaceptor.core.ui.components.badge

import androidx.compose.ui.graphics.Color
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/**
 * Visual style for [WormaCeptorBadge].
 *
 * `Filled` -- solid container with contrasting content (emphasized).
 * `Soft`   -- low-alpha container tint with strong content color (readable yet subtle).
 * `Tonal`  -- caller-provided container + content colors (fully custom).
 */
sealed interface BadgeVariant {
    /** Solid fill using [color] as container with a readable content tint. */
    data class Filled(val color: Color) : BadgeVariant

    /** Soft tint using [color] at [WormaCeptorTokens.Alpha.SOFT] with [color] as content. */
    data class Soft(val color: Color) : BadgeVariant

    /** Explicit container + content colors, for palette-driven callers. */
    data class Tonal(
        /** Surface (background) color of the badge. */
        val containerColor: Color,
        /** Text (content) color of the badge. */
        val contentColor: Color,
    ) : BadgeVariant
}
