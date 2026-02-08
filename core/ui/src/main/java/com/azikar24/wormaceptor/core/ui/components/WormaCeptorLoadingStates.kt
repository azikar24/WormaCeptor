package com.azikar24.wormaceptor.core.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground

// ============================================================================
// SHIMMER ANIMATION
// ============================================================================

/**
 * Shimmer effect brush for skeleton loading animations.
 * Creates a subtle, professional shimmer that moves across the component.
 */
@Suppress("MagicNumber") // Animation values are self-documenting in context
@Composable
fun rememberShimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.intense),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.intense),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslate",
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation.value - 200f, 0f),
        end = Offset(translateAnimation.value, 0f),
    )
}

/**
 * Generic skeleton box with shimmer effect.
 * Reusable component for creating loading placeholders.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 16.dp,
    brush: Brush = rememberShimmerBrush(),
    shape: RoundedCornerShape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .height(height)
            .background(brush, shape),
    )
}

// ============================================================================
// LOADING INDICATORS
// ============================================================================

/**
 * Loading indicator shown at the bottom of the list when fetching more items.
 * Features a subtle pulsing animation and contextual messaging.
 */
@Suppress("MagicNumber") // Animation values are self-documenting in context
@Composable
fun LoadingMoreIndicator(modifier: Modifier = Modifier, message: String = "Loading more...") {
    val infiniteTransition = rememberInfiniteTransition(label = "loadingMore")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(WormaCeptorDesignSystem.Spacing.lg),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LoadingDots()

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = pulse),
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Animated three-dot loading indicator.
 */
@Suppress("MagicNumber") // Animation values are self-documenting in context
@Composable
private fun LoadingDots() {
    val dotCount = 3
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(dotCount) { index ->
            val delay = index * 150

            val animatedY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400,
                        delayMillis = delay,
                        easing = FastOutSlowInEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dotY$index",
            )

            val animatedAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400,
                        delayMillis = delay,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dotAlpha$index",
            )

            Box(
                modifier = Modifier
                    .size(6.dp)
                    .offset(y = animatedY.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = animatedAlpha),
                        CircleShape,
                    ),
            )
        }
    }
}

/**
 * Compact spinner for inline loading states.
 */
@Composable
fun CompactLoadingSpinner(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    strokeWidth: Dp = 2.dp,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        strokeWidth = strokeWidth,
        color = color,
    )
}

// ============================================================================
// ERROR STATES
// ============================================================================

/**
 * Error type for different error scenarios.
 */
enum class ErrorType(
    val icon: ImageVector,
    val title: String,
) {
    GENERIC(Icons.Outlined.ErrorOutline, "Something went wrong"),
    NETWORK(Icons.Outlined.SignalWifiOff, "Connection error"),
}

/**
 * Error state with retry functionality.
 * Supports different error types with appropriate icons and messaging.
 */
@Suppress("LongMethod", "MagicNumber") // Composable UI - acceptable length for visual layout
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    errorType: ErrorType = ErrorType.GENERIC,
    isRetrying: Boolean = false,
    errorColor: Color = MaterialTheme.colorScheme.error,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "retryScale",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(WormaCeptorDesignSystem.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Error icon with subtle background
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    errorColor.asSubtleBackground(),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = errorType.icon,
                contentDescription = "Error: ${errorType.title}",
                modifier = Modifier.size(32.dp),
                tint = errorColor,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        Text(
            text = errorType.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = WormaCeptorDesignSystem.Spacing.xl),
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

        // Retry button with press animation
        Surface(
            modifier = Modifier
                .scale(scale)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = !isRetrying,
                ) { onRetry() },
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.xl,
                    vertical = WormaCeptorDesignSystem.Spacing.md,
                ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isRetrying) {
                    CompactLoadingSpinner(
                        size = 18.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text(
                    text = if (isRetrying) "Retrying..." else "Try Again",
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

/**
 * Compact inline error for append loading failures.
 */
@Composable
fun InlineErrorRetry(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    errorColor: Color = MaterialTheme.colorScheme.error,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(WormaCeptorDesignSystem.Spacing.md)
            .background(
                errorColor.asSubtleBackground(),
                RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
            .padding(WormaCeptorDesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = "Error",
            modifier = Modifier.size(20.dp),
            tint = errorColor,
        )

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = errorColor,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

        TextButton(
            onClick = onRetry,
            contentPadding = PaddingValues(
                horizontal = WormaCeptorDesignSystem.Spacing.md,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
        ) {
            Text(
                text = "Retry",
                fontWeight = FontWeight.SemiBold,
                color = errorColor,
            )
        }
    }
}

// ============================================================================
// SCROLL TO TOP FAB
// ============================================================================

/**
 * Floating action button to scroll to top of list.
 * Appears with animation when user scrolls down.
 */
@Suppress("MagicNumber") // Animation values are self-documenting in context
@Composable
fun ScrollToTopFab(visible: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val rotation by animateFloatAsState(
        targetValue = if (visible) 0f else 180f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "fabRotation",
    )

    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        ) + fadeIn(),
        exit = scaleOut(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        ) + fadeOut(),
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Scroll to top",
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation),
            )
        }
    }
}
