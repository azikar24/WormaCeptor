@file:Suppress("MagicNumber")

package com.azikar24.wormaceptor.core.engine.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayState
import com.azikar24.wormaceptor.core.engine.R
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/** Fraction of screen height used for the dismiss zone (bottom portion). */
private const val DismissZoneFraction = 1f - PerformanceOverlayState.DISMISS_ZONE_THRESHOLD

/**
 * YouTube PiP-style dismiss zone indicator.
 *
 * Renders a circular close button centered within the dismiss zone at the bottom of the screen.
 * Appears when the user starts dragging the performance overlay. The circle grows and turns
 * red when the overlay enters the dismiss zone.
 *
 * @param isDragging Whether the user is currently dragging the overlay
 * @param isInDismissZone Whether the overlay is in the dismiss zone
 * @param modifier Optional modifier
 */
@Composable
fun DismissZoneContent(
    isDragging: Boolean,
    isInDismissZone: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn(tween(WormaCeptorTokens.Animation.MEDIUM)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(WormaCeptorTokens.Animation.MEDIUM),
            ),
            exit = fadeOut(tween(WormaCeptorTokens.Animation.FAST)) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(WormaCeptorTokens.Animation.FAST),
            ),
        ) {
            // Container fills the dismiss zone area (bottom 15% of screen)
            // so the circle is centered exactly where dismissal triggers
            Box(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(DismissZoneFraction),
                contentAlignment = Alignment.Center,
            ) {
                DismissCircle(isInDismissZone)
            }
        }
    }
}

@Composable
private fun DismissCircle(isActive: Boolean) {
    val circleSize by animateDpAsState(
        targetValue = if (isActive) WormaCeptorTokens.TouchTarget.large else WormaCeptorTokens.TouchTarget.comfortable,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "dismissCircleSize",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) {
            WormaCeptorTokens.Colors.DismissZone.error
        } else {
            WormaCeptorTokens.Colors.DismissZone.surface
        },
        animationSpec = tween(WormaCeptorTokens.Animation.FAST),
        label = "dismissCircleColor",
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (isActive) WormaCeptorTokens.Alpha.OPAQUE else WormaCeptorTokens.Alpha.BOLD,
        animationSpec = tween(WormaCeptorTokens.Animation.FAST),
        label = "dismissIconAlpha",
    )

    Box(
        modifier = Modifier
            .shadow(elevation = WormaCeptorTokens.Elevation.xl, shape = CircleShape)
            .size(circleSize)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.overlay_dismiss_remove),
            tint = Color.White,
            modifier = Modifier
                .size(WormaCeptorTokens.IconSize.md)
                .alpha(iconAlpha),
        )
    }
}
