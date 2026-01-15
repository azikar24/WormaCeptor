/*
 * Copyright AziKar24 2025.
 * Gesture Navigation UI Components for WormaCeptor
 *
 * Provides visual feedback and controls for:
 * - Pull-to-refresh indicator
 * - Swipe navigation between transactions
 * - Transaction position indicator
 * - Zoom controls
 * - Swipe-back gesture feedback
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

// =============================================================================
// PULL-TO-REFRESH INDICATOR
// =============================================================================

/**
 * State representation for pull-to-refresh gesture
 */
enum class RefreshState {
    Idle,
    Pulling,
    ThresholdReached,
    Refreshing,
    Complete
}

/**
 * Custom pull-to-refresh indicator that follows the WormaCeptor design system.
 * Features:
 * - Smooth arc progress animation
 * - Color transition at threshold
 * - Bouncy spring animation for refresh state
 * - Subtle shadow and glow effects
 */
@Composable
fun PullToRefreshIndicator(
    state: RefreshState,
    pullProgress: Float, // 0f to 1f+
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = pullProgress.coerceIn(0f, 1.5f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pull_progress"
    )

    val scale by animateFloatAsState(
        targetValue = when (state) {
            RefreshState.Idle -> 0f
            RefreshState.Pulling -> 0.8f + (animatedProgress * 0.2f).coerceAtMost(0.2f)
            RefreshState.ThresholdReached -> 1.1f
            RefreshState.Refreshing -> 1f
            RefreshState.Complete -> 0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicator_scale"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val successColor = MaterialTheme.colorScheme.tertiary

    // Color transitions based on state
    val indicatorColor by animateFloatAsState(
        targetValue = when (state) {
            RefreshState.ThresholdReached, RefreshState.Refreshing -> 1f
            RefreshState.Complete -> 0.5f
            else -> 0f
        },
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
        label = "color_transition"
    )

    val currentColor = lerp(primaryColor, successColor, indicatorColor)

    // Rotation animation for refreshing state
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    AnimatedVisibility(
        visible = state != RefreshState.Idle && state != RefreshState.Complete,
        enter = fadeIn() + scaleIn(initialScale = 0.5f),
        exit = fadeOut() + scaleOut(targetScale = 0.5f),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
                .shadow(
                    elevation = WormaCeptorDesignSystem.Elevation.md,
                    shape = CircleShape
                )
                .background(surfaceColor, CircleShape)
                .border(
                    width = WormaCeptorDesignSystem.BorderWidth.thin,
                    color = currentColor.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Progress arc
            Canvas(
                modifier = Modifier
                    .size(36.dp)
                    .rotate(if (state == RefreshState.Refreshing) rotation else 0f)
            ) {
                val strokeWidth = 3.dp.toPx()
                val sweepAngle = when (state) {
                    RefreshState.Pulling -> animatedProgress * 270f
                    RefreshState.ThresholdReached -> 270f
                    RefreshState.Refreshing -> 270f
                    else -> 0f
                }

                // Background track
                drawArc(
                    color = currentColor.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress arc
                drawArc(
                    color = currentColor,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Center icon for refreshing state
            if (state == RefreshState.Refreshing) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refreshing",
                    tint = currentColor,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(rotation)
                )
            }
        }
    }
}

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
    modifier: Modifier = Modifier
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
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)
        ) {
            // Previous button - minimum 48dp touch target for accessibility
            IconButton(
                onClick = onPrevious,
                enabled = canGoPrevious,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous transaction",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Position text
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)
            ) {
                // Animated current number
                AnimatedNumber(number = currentIndex + 1)

                Text(
                    text = "of",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "$totalCount",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Next button - minimum 48dp touch target for accessibility
            IconButton(
                onClick = onNext,
                enabled = canGoNext,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next transaction",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Compact variant of position indicator for smaller spaces
 */
@Composable
fun CompactPositionIndicator(
    currentIndex: Int,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm).copy(alpha = 0.9f)
    ) {
        Text(
            text = "${currentIndex + 1} / $totalCount",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs
            )
        )
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
                    stiffness = Spring.StiffnessMedium
                )
            )
            previousNumber = number
        }
    }

    Text(
        text = "${animatedValue.value.roundToInt()}",
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.primary
    )
}

// =============================================================================
// SWIPE NAVIGATION VISUAL FEEDBACK
// =============================================================================

/**
 * Edge peek indicator shown when swiping to reveal next/previous content
 */
@Composable
fun SwipeEdgePeek(
    direction: SwipeDirection,
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = (progress * 2f).coerceIn(0f, 1f),
        animationSpec = tween(100),
        label = "peek_alpha"
    )

    val offset by animateFloatAsState(
        targetValue = progress * 60f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "peek_offset"
    )

    val isLeft = direction == SwipeDirection.Left

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
            .alpha(alpha)
            .offset {
                IntOffset(
                    x = if (isLeft) -offset.roundToInt() else offset.roundToInt(),
                    y = 0
                )
            }
    ) {
        // Gradient shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isLeft) {
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        }
                    )
                )
        )

        // Arrow indicator
        Icon(
            imageVector = if (isLeft) {
                Icons.AutoMirrored.Filled.ArrowBack
            } else {
                Icons.AutoMirrored.Filled.ArrowForward
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha * 0.8f),
            modifier = Modifier
                .align(if (isLeft) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = WormaCeptorDesignSystem.Spacing.md)
                .size(24.dp)
        )
    }
}

enum class SwipeDirection {
    Left,
    Right
}

/**
 * Visual indicator showing swipe navigation is available
 * Shows subtle arrows on edges that pulse to indicate gesture availability
 */
@Composable
fun SwipeNavigationHint(
    canSwipeLeft: Boolean,
    canSwipeRight: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hint_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Left hint
        AnimatedVisibility(
            visible = canSwipeLeft,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterStart)
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
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill)
                    )
            )
        }

        // Right hint
        AnimatedVisibility(
            visible = canSwipeRight,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
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
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill)
                    )
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
    modifier: Modifier = Modifier
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
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)
        ) {
            // Zoom in button
            IconButton(
                onClick = onZoomIn,
                enabled = canZoomIn,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom in",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom level indicator
            ZoomLevelIndicator(
                zoom = currentZoom,
                minZoom = minZoom,
                maxZoom = maxZoom
            )

            // Zoom out button
            IconButton(
                onClick = onZoomOut,
                enabled = canZoomOut,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Zoom out",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Reset button (shown when zoomed)
            AnimatedVisibility(
                visible = isZoomed,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset zoom",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Horizontal variant of zoom controls for toolbar placement
 */
@Composable
fun HorizontalZoomControls(
    currentZoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    minZoom: Float = 0.5f,
    maxZoom: Float = 3f,
    modifier: Modifier = Modifier
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
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)
        ) {
            // Zoom out button - 40dp for compact layout but still accessible
            IconButton(
                onClick = onZoomOut,
                enabled = canZoomOut,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = "Zoom out",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Zoom level text
            Text(
                text = "${(currentZoom * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isZoomed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(48.dp)
            )

            // Zoom in button - 40dp for compact layout but still accessible
            IconButton(
                onClick = onZoomIn,
                enabled = canZoomIn,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Zoom in",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Reset button - 40dp for compact layout
            AnimatedVisibility(
                visible = isZoomed,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Reset zoom",
                        modifier = Modifier.size(18.dp)
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
private fun ZoomLevelIndicator(
    zoom: Float,
    minZoom: Float,
    maxZoom: Float
) {
    val normalizedZoom = ((zoom - minZoom) / (maxZoom - minZoom)).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs)
    ) {
        // Zoom percentage
        Text(
            text = "${(zoom * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        // Progress bar
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalizedZoom)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill)
                    )
            )
        }
    }
}

// =============================================================================
// SWIPE-BACK VISUAL FEEDBACK
// =============================================================================

/**
 * Container that provides swipe-back gesture with visual feedback.
 * Features:
 * - Edge shadow that intensifies with drag
 * - Page sliding animation
 * - Velocity-based snap decision
 * - Spring animation for cancel/confirm
 */
@Composable
fun SwipeBackContainer(
    onBack: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffset = remember { Animatable(0f) }
    val threshold = with(density) { 100.dp.toPx() }
    val maxOffset = with(density) { 300.dp.toPx() }

    // Sync animated value
    LaunchedEffect(animatedOffset.value) {
        offsetX = animatedOffset.value
    }

    val progress = (offsetX / threshold).coerceIn(0f, 1.5f)
    val shadowAlpha = (progress * 0.4f).coerceIn(0f, 0.4f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectHorizontalDragGestures(
                    onDragStart = { },
                    onDragEnd = {
                        scope.launch {
                            if (offsetX > threshold) {
                                // Swipe confirmed - animate out and trigger back
                                animatedOffset.animateTo(
                                    targetValue = maxOffset,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioNoBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                                onBack()
                            } else {
                                // Cancel - spring back
                                animatedOffset.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            animatedOffset.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium)
                            )
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        val newOffset = (offsetX + dragAmount).coerceIn(0f, maxOffset)
                        scope.launch {
                            animatedOffset.snapTo(newOffset)
                        }
                    }
                )
            }
    ) {
        // Edge shadow overlay
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(24.dp)
                .align(Alignment.CenterStart)
                .alpha(shadowAlpha)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Content with offset
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .graphicsLayer {
                    // Subtle scale reduction as user drags
                    val scaleValue = 1f - (progress * 0.02f).coerceAtMost(0.02f)
                    scaleX = scaleValue
                    scaleY = scaleValue
                }
                .drawBehind {
                    // Left edge shadow on content
                    if (offsetX > 0) {
                        drawRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = shadowAlpha),
                                    Color.Transparent
                                ),
                                startX = -20.dp.toPx(),
                                endX = 0f
                            )
                        )
                    }
                }
        ) {
            content()
        }

        // Back arrow indicator
        AnimatedVisibility(
            visible = progress > 0.2f,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = WormaCeptorDesignSystem.Spacing.md)
        ) {
            SwipeBackArrowIndicator(progress = progress)
        }
    }
}

/**
 * Arrow indicator that appears during swipe-back gesture
 */
@Composable
private fun SwipeBackArrowIndicator(progress: Float) {
    val scale by animateFloatAsState(
        targetValue = if (progress >= 1f) 1.2f else 0.8f + (progress * 0.2f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "arrow_scale"
    )

    val color = if (progress >= 1f) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = WormaCeptorDesignSystem.Elevation.sm,
        modifier = Modifier.scale(scale)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Swipe back",
            tint = color,
            modifier = Modifier
                .padding(WormaCeptorDesignSystem.Spacing.sm)
                .size(20.dp)
        )
    }
}

/**
 * Edge shadow that appears on the left side during swipe-back
 */
@Composable
fun SwipeBackEdgeShadow(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = (progress * 0.6f).coerceIn(0f, 0.6f),
        animationSpec = tween(50),
        label = "shadow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(32.dp)
            .alpha(alpha)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )
    )
}

// =============================================================================
// UTILITY FUNCTIONS
// =============================================================================

/**
 * Linear interpolation between two colors
 */
private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = start.red + (stop.red - start.red) * fraction,
        green = start.green + (stop.green - start.green) * fraction,
        blue = start.blue + (stop.blue - start.blue) * fraction,
        alpha = start.alpha + (stop.alpha - start.alpha) * fraction
    )
}

/**
 * Rounds float to specified decimal places
 */
private fun Float.roundTo(decimals: Int): Float {
    var multiplier = 1f
    repeat(decimals) { multiplier *= 10 }
    return kotlin.math.round(this * multiplier) / multiplier
}
