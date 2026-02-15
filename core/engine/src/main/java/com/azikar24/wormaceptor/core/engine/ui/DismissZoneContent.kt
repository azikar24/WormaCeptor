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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayState
import com.azikar24.wormaceptor.core.engine.R
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem

private val DismissCircleSize = WormaCeptorDesignSystem.TouchTarget.comfortable
private val DismissCircleActiveSize = WormaCeptorDesignSystem.TouchTarget.large
private val DismissIconSize = 22.dp
private val DismissCircleElevation = WormaCeptorDesignSystem.Elevation.xl

private const val EnterDurationMs = 200
private const val ExitDurationMs = 150
private const val ColorTransitionMs = 150

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
fun DismissZoneContent(isDragging: Boolean, isInDismissZone: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = isDragging,
            enter = fadeIn(tween(EnterDurationMs)) + slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(EnterDurationMs),
            ),
            exit = fadeOut(tween(ExitDurationMs)) + slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(ExitDurationMs),
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
        targetValue = if (isActive) DismissCircleActiveSize else DismissCircleSize,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "dismissCircleSize",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isActive) {
            WormaCeptorDesignSystem.ThemeColors.Error
        } else {
            WormaCeptorDesignSystem.ThemeColors.DarkSurfaceVariant
        },
        animationSpec = tween(ColorTransitionMs),
        label = "dismissCircleColor",
    )

    val iconAlpha by animateFloatAsState(
        targetValue = if (isActive) WormaCeptorDesignSystem.Alpha.opaque else WormaCeptorDesignSystem.Alpha.bold,
        animationSpec = tween(ColorTransitionMs),
        label = "dismissIconAlpha",
    )

    Box(
        modifier = Modifier
            .shadow(elevation = DismissCircleElevation, shape = CircleShape)
            .size(circleSize)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.overlay_dismiss_remove),
            tint = WormaCeptorDesignSystem.ThemeColors.LightBackground,
            modifier = Modifier
                .size(DismissIconSize)
                .alpha(iconAlpha),
        )
    }
}
