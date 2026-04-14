package com.azikar24.wormaceptor.core.ui.modifier

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.rememberReduceMotion

private const val DefaultPressedScale = 0.98f
private const val UnpressedScale = 1f

/**
 * Press-feedback modifier -- scales the element slightly while pressed.
 * Honors [rememberReduceMotion] and becomes a no-op when the user has
 * disabled animations at the system level.
 *
 * Must be driven by an [InteractionSource] shared with the element's
 * clickable modifier so press state is observed rather than owned.
 */
@Composable
fun Modifier.wormaceptorPressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = DefaultPressedScale,
): Modifier {
    val reduceMotion = rememberReduceMotion()
    val isPressed by interactionSource.collectIsPressedAsState()
    val target = if (isPressed) pressedScale else UnpressedScale
    val scale by animateFloatAsState(
        targetValue = if (reduceMotion) UnpressedScale else target,
        animationSpec = tween(WormaCeptorTokens.Animation.ULTRA_FAST),
        label = "wormaceptor_press_scale",
    )
    return this.scale(scale)
}

/**
 * Focus-ring modifier -- draws a primary-tinted border around the element
 * while keyboard/assistive focus is on it. Invisible otherwise.
 *
 * Must be driven by an [InteractionSource] shared with the element's
 * focusable/clickable modifier.
 */
@Composable
fun Modifier.wormaceptorFocusRing(
    interactionSource: InteractionSource,
    shape: Shape,
): Modifier = composed {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val color = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }
    this.border(
        width = WormaCeptorTokens.FocusIndicator.width,
        color = color,
        shape = shape,
    )
}
