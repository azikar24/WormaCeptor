/*
 * Gesture Navigation UI Components for WormaCeptor
 *
 * Provides visual feedback and controls for:
 * - Transaction position indicator
 * - Zoom controls
 * - Swipe navigation hints
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.R
import kotlin.math.roundToInt

// =============================================================================
// TRANSACTION POSITION INDICATOR
// =============================================================================

/**
 * Displays current position in transaction list (e.g., "3 of 15")
 * with navigation affordances and smooth animations.
 */
@Composable
fun TransactionPositionIndicator(
    currentIndex: Int,
    totalCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canGoPrevious = currentIndex > 0
    val canGoNext = currentIndex < totalCount - 1

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.md),
        shadowElevation = WormaCeptorDesignSystem.Elevation.sm,
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        ) {
            // Previous button - minimum 48dp touch target for accessibility
            IconButton(
                onClick = onPrevious,
                enabled = canGoPrevious,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.medium,
                    ),
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.viewer_gesture_previous),
                    modifier = Modifier.size(24.dp),
                )
            }

            // Position text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
            ) {
                // Animated current number
                AnimatedNumber(number = currentIndex + 1)

                Text(
                    text = "of",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Text(
                    text = "$totalCount",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            // Next button - minimum 48dp touch target for accessibility
            IconButton(
                onClick = onNext,
                enabled = canGoNext,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.medium,
                    ),
                ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.viewer_gesture_next),
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

/**
 * Animated number that slides when value changes
 */
@Composable
private fun AnimatedNumber(number: Int) {
    var previousNumber by remember { mutableStateOf(number) }
    val animatedValue = remember { Animatable(number.toFloat()) }

    LaunchedEffect(number) {
        if (number != previousNumber) {
            animatedValue.animateTo(
                targetValue = number.toFloat(),
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
            previousNumber = number
        }
    }

    Text(
        text = "${animatedValue.value.roundToInt()}",
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
        ),
        color = MaterialTheme.colorScheme.primary,
    )
}

// =============================================================================
// SWIPE NAVIGATION VISUAL FEEDBACK
// =============================================================================

/**
 * Visual indicator showing swipe navigation is available.
 * Shows subtle bars on edges that pulse to indicate gesture availability.
 */
@Composable
fun SwipeNavigationHint(canSwipeLeft: Boolean, canSwipeRight: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "hint_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Left hint
        AnimatedVisibility(
            visible = canSwipeLeft,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .alpha(pulseAlpha)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary,
                                Color.Transparent,
                            ),
                        ),
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
                    ),
            )
        }

        // Right hint
        AnimatedVisibility(
            visible = canSwipeRight,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .alpha(pulseAlpha)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary,
                                Color.Transparent,
                            ),
                        ),
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
                    ),
            )
        }
    }
}

// =============================================================================
// ZOOM CONTROLS
// =============================================================================

/**
 * Floating zoom controls with reset, zoom in/out, and level indicator.
 * Designed for use with zoomable content like response bodies.
 */
@Composable
fun ZoomControls(
    currentZoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    minZoom: Float = 0.5f,
    maxZoom: Float = 3f,
    modifier: Modifier = Modifier,
) {
    val canZoomIn = currentZoom < maxZoom
    val canZoomOut = currentZoom > minZoom
    val isZoomed = currentZoom != 1f

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.md),
        shadowElevation = WormaCeptorDesignSystem.Elevation.sm,
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        ) {
            // Zoom in button
            IconButton(
                onClick = onZoomIn,
                enabled = canZoomIn,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.medium,
                    ),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = stringResource(R.string.viewer_image_zoom_in),
                    modifier = Modifier.size(20.dp),
                )
            }

            // Zoom level indicator
            ZoomLevelIndicator(
                zoom = currentZoom,
                minZoom = minZoom,
                maxZoom = maxZoom,
            )

            // Zoom out button
            IconButton(
                onClick = onZoomOut,
                enabled = canZoomOut,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.medium,
                    ),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = stringResource(R.string.viewer_image_zoom_out),
                    modifier = Modifier.size(20.dp),
                )
            }

            // Reset button (shown when zoomed)
            AnimatedVisibility(
                visible = isZoomed,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = stringResource(R.string.viewer_gesture_reset_zoom),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * Visual zoom level indicator with progress bar
 */
@Composable
private fun ZoomLevelIndicator(zoom: Float, minZoom: Float, maxZoom: Float) {
    val normalizedZoom = ((zoom - minZoom) / (maxZoom - minZoom)).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
    ) {
        // Zoom percentage
        Text(
            text = "${(zoom * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Progress bar
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalizedZoom)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
                    ),
            )
        }
    }
}
