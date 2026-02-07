/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SignalWifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.feature.viewer.ui.util.formatBytes

// ============================================================================
// SKELETON LOADING COMPONENTS
// ============================================================================

/**
 * Shimmer effect brush for skeleton loading animations.
 * Creates a subtle, professional shimmer that moves across the component.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.intense),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
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
 * Skeleton placeholder for a single transaction item.
 * Matches the exact layout of TransactionItem for seamless transition.
 */
@Composable
fun TransactionItemSkeleton(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberShimmerBrush()

    WormaCeptorContainer(
        style = ContainerStyle.Outlined,
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.hint),
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
    ) {
        Row(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status indicator skeleton
            Box(
                modifier = Modifier
                    .width(WormaCeptorDesignSystem.BorderWidth.thick)
                    .height(48.dp)
                    .background(shimmerBrush, RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs)),
            )

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    // Method badge skeleton
                    SkeletonBox(
                        width = 36.dp,
                        height = 16.dp,
                        brush = shimmerBrush,
                    )
                    // Path skeleton
                    SkeletonBox(
                        modifier = Modifier.weight(1f),
                        height = 18.dp,
                        brush = shimmerBrush,
                    )
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

                // Host chip skeleton
                SkeletonBox(
                    width = 120.dp,
                    height = 20.dp,
                    brush = shimmerBrush,
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
                )
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(horizontalAlignment = Alignment.End) {
                // Status code skeleton
                SkeletonBox(
                    width = 40.dp,
                    height = 24.dp,
                    brush = shimmerBrush,
                )
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                // Duration skeleton
                SkeletonBox(
                    width = 32.dp,
                    height = 14.dp,
                    brush = shimmerBrush,
                )
            }
        }
    }
}

/**
 * Generic skeleton box with shimmer effect.
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

/**
 * Multiple skeleton placeholders for initial loading state.
 */
@Composable
fun TransactionListSkeleton(itemCount: Int = 5, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        repeat(itemCount) { index ->
            // Staggered fade-in animation for each skeleton
            val animatedAlpha by animateFloatAsState(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = WormaCeptorDesignSystem.AnimationDuration.normal,
                    delayMillis = index * 50,
                    easing = FastOutSlowInEasing,
                ),
                label = "skeletonAlpha$index",
            )

            TransactionItemSkeleton(
                modifier = Modifier.graphicsLayer { alpha = animatedAlpha },
            )
        }
    }
}

// ============================================================================
// LOADING MORE INDICATOR
// ============================================================================

/**
 * Loading indicator shown at the bottom of the list when fetching more items.
 * Features a subtle pulsing animation and contextual messaging.
 */
@Composable
fun LoadingMoreIndicator(modifier: Modifier = Modifier, message: String? = null) {
    val displayMessage = message ?: stringResource(R.string.viewer_loading_more)
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
        // Animated dots loading indicator
        LoadingDots()

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

        Text(
            text = displayMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = pulse),
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Animated three-dot loading indicator.
 */
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
// ERROR STATE
// ============================================================================

/**
 * Error state with retry functionality.
 * Supports different error types with appropriate icons and messaging.
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    errorType: ErrorType = ErrorType.GENERIC,
    isRetrying: Boolean = false,
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
                    WormaCeptorColors.StatusRed.asSubtleBackground(),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = errorType.icon,
                contentDescription = stringResource(R.string.viewer_loading_error_description, errorType.title),
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xl),
                tint = WormaCeptorColors.StatusRed,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
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
                        contentDescription = stringResource(R.string.viewer_loading_retry),
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text(
                    text = if (isRetrying) {
                        stringResource(
                            R.string.viewer_loading_retrying,
                        )
                    } else {
                        stringResource(R.string.viewer_loading_retry)
                    },
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
fun InlineErrorRetry(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(WormaCeptorDesignSystem.Spacing.md)
            .background(
                WormaCeptorColors.StatusRed.asSubtleBackground(),
                RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
            .padding(WormaCeptorDesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.ErrorOutline,
            contentDescription = stringResource(R.string.viewer_loading_error),
            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
            tint = WormaCeptorColors.StatusRed,
        )

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = WormaCeptorColors.StatusRed,
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
                text = stringResource(R.string.viewer_loading_retry),
                fontWeight = FontWeight.SemiBold,
                color = WormaCeptorColors.StatusRed,
            )
        }
    }
}

enum class ErrorType(
    val icon: ImageVector,
    val titleResId: Int,
) {
    GENERIC(Icons.Outlined.ErrorOutline, R.string.viewer_loading_error_title),
    NETWORK(Icons.Outlined.SignalWifiOff, R.string.viewer_loading_error_network),
    NOT_FOUND(Icons.Outlined.Search, R.string.viewer_loading_error_not_found),
    ;

    val title: String
        @Composable
        get() = stringResource(titleResId)
}

// ============================================================================
// EMPTY STATE
// ============================================================================

/**
 * Enhanced empty state with visual polish.
 * Shows different content based on whether filters are active.
 */
@Composable
fun EnhancedEmptyState(
    modifier: Modifier = Modifier,
    hasActiveFilters: Boolean = false,
    hasSearchQuery: Boolean = false,
    onClearFilters: () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "emptyState")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(WormaCeptorDesignSystem.Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Visual illustration
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.2f),
                    CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha * 0.3f),
                        CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Network icon representation using shapes
                EmptyStateIcon(hasActiveFilters = hasActiveFilters || hasSearchQuery)
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxl))

        Text(
            text = when {
                hasSearchQuery -> stringResource(R.string.viewer_empty_no_matches)
                hasActiveFilters -> stringResource(R.string.viewer_empty_no_matches)
                else -> stringResource(R.string.viewer_empty_no_transactions)
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        Text(
            text = when {
                hasSearchQuery -> stringResource(R.string.viewer_empty_search_hint)
                hasActiveFilters -> stringResource(R.string.viewer_empty_filter_hint)
                else -> stringResource(R.string.viewer_empty_transactions_hint)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
            textAlign = TextAlign.Center,
        )

        if (hasActiveFilters || hasSearchQuery) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

            OutlinedButton(
                onClick = onClearFilters,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                    width = WormaCeptorDesignSystem.BorderWidth.regular,
                ),
            ) {
                Text(
                    text = if (hasSearchQuery) {
                        stringResource(
                            R.string.viewer_empty_clear_search,
                        )
                    } else {
                        stringResource(R.string.viewer_empty_clear_filters)
                    },
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                )
            }
        }
    }
}

@Composable
private fun EmptyStateIcon(hasActiveFilters: Boolean) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.size(40.dp)) {
        if (hasActiveFilters) {
            // Search icon representation
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.viewer_filter_search),
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xxl),
                tint = primaryColor,
            )
        } else {
            // Network-like icon using circles and lines
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(4.dp)
                            .background(primaryColor, RoundedCornerShape(2.dp)),
                    )
                }
            }
        }
    }
}

// ============================================================================
// LOAD MORE BUTTON FOR BODY CONTENT
// ============================================================================

/**
 * Load more button for paginated body content.
 * Shows remaining bytes and loading state.
 */
@Composable
fun LoadMoreBodyButton(remainingBytes: Long, isLoading: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "loadMoreScale",
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isLoading,
            ) { onClick() },
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = WormaCeptorDesignSystem.Alpha.strong),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                CompactLoadingSpinner(
                    size = 16.dp,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text(
                    text = "Loading more content...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                // Expand icon
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
                            CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text(
                    text = "Load more",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                Text(
                    text = "(${formatBytes(remainingBytes)} remaining)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.heavy,
                    ),
                )
            }
        }
    }
}

/**
 * Progress indicator for body loading showing how much has been loaded.
 */
@Composable
fun BodyLoadingProgress(loadedBytes: Long, totalBytes: Long, modifier: Modifier = Modifier) {
    val progress = if (totalBytes > 0) loadedBytes.toFloat() / totalBytes else 0f

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatBytes(loadedBytes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
            )
            Text(
                text = formatBytes(totalBytes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

// ============================================================================
// SCROLL TO TOP FAB
// ============================================================================

/**
 * Floating action button to scroll to top of list.
 * Appears with animation when user scrolls down.
 */
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
                contentDescription = stringResource(R.string.viewer_loading_scroll_to_top),
                modifier = Modifier
                    .size(WormaCeptorDesignSystem.IconSize.lg)
                    .rotate(rotation),
            )
        }
    }
}

/**
 * Extended FAB variant with text label.
 */
@Composable
fun ScrollToTopExtendedFab(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showItemCount: Int? = null,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        ) + fadeOut(),
    ) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            icon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.viewer_loading_scroll_to_top),
                )
            },
            text = {
                Text(
                    text = if (showItemCount != null) "Back to top ($showItemCount)" else "Back to top",
                    fontWeight = FontWeight.Medium,
                )
            },
        )
    }
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Modifier extension for applying graphics layer with alpha.
 */
private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit): Modifier {
    return this.then(Modifier.graphicsLayer(block))
}

// ============================================================================
// DETAIL SCREEN LOADING SKELETON
// ============================================================================

/**
 * Skeleton placeholder for transaction detail screen.
 * Matches the overview tab structure for seamless loading transition.
 */
@Composable
fun TransactionDetailSkeleton(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(WormaCeptorDesignSystem.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
    ) {
        // Timing card skeleton
        DetailCardSkeleton(
            shimmerBrush = shimmerBrush,
            rowCount = 6,
        )

        // Security card skeleton
        DetailCardSkeleton(
            shimmerBrush = shimmerBrush,
            rowCount = 2,
        )

        // Data transfer card skeleton
        DetailCardSkeleton(
            shimmerBrush = shimmerBrush,
            rowCount = 3,
        )
    }
}

/**
 * Skeleton for a detail card section.
 */
@Composable
private fun DetailCardSkeleton(shimmerBrush: Brush, rowCount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm),
        ),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
        ),
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            // Header row with icon and title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                SkeletonBox(
                    width = 20.dp,
                    height = 20.dp,
                    brush = shimmerBrush,
                    shape = CircleShape,
                )
                SkeletonBox(
                    width = 100.dp,
                    height = 20.dp,
                    brush = shimmerBrush,
                )
            }

            // Content rows
            repeat(rowCount) { index ->
                DetailRowSkeleton(
                    shimmerBrush = shimmerBrush,
                    labelWidth = when (index % 3) {
                        0 -> 60.dp
                        1 -> 80.dp
                        else -> 70.dp
                    },
                    valueWidth = when (index % 4) {
                        0 -> 150.dp
                        1 -> 200.dp
                        2 -> 120.dp
                        else -> 180.dp
                    },
                )
            }
        }
    }
}

/**
 * Skeleton for a single detail row (label: value pair).
 */
@Composable
private fun DetailRowSkeleton(shimmerBrush: Brush, labelWidth: Dp, valueWidth: Dp) {
    Row(
        modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SkeletonBox(
            width = labelWidth,
            height = 14.dp,
            brush = shimmerBrush,
        )
        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
        SkeletonBox(
            width = valueWidth,
            height = 14.dp,
            brush = shimmerBrush,
        )
    }
}

/**
 * Skeleton for request/response tab content with headers and body.
 */
@Composable
fun RequestResponseTabSkeleton(modifier: Modifier = Modifier) {
    val shimmerBrush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(WormaCeptorDesignSystem.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xl),
    ) {
        // Headers section
        CollapsibleSectionSkeleton(
            shimmerBrush = shimmerBrush,
            title = stringResource(R.string.viewer_skeleton_headers),
            contentRowCount = 5,
        )

        // Body section
        CollapsibleSectionSkeleton(
            shimmerBrush = shimmerBrush,
            title = stringResource(R.string.viewer_skeleton_body),
            isBodySection = true,
        )
    }
}

/**
 * Skeleton for collapsible section (Headers/Body).
 */
@Composable
private fun CollapsibleSectionSkeleton(
    shimmerBrush: Brush,
    title: String,
    modifier: Modifier = Modifier,
    contentRowCount: Int = 0,
    isBodySection: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                SkeletonBox(
                    width = 18.dp,
                    height = 18.dp,
                    brush = shimmerBrush,
                )
                SkeletonBox(
                    width = 80.dp,
                    height = 16.dp,
                    brush = shimmerBrush,
                )
            }

            if (isBodySection) {
                Row(horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
                    // Content type chip skeleton
                    SkeletonBox(
                        width = 60.dp,
                        height = 24.dp,
                        brush = shimmerBrush,
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                    )
                    // Pretty/Raw toggle skeleton
                    SkeletonBox(
                        width = 80.dp,
                        height = 24.dp,
                        brush = shimmerBrush,
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                    )
                }
            }

            SkeletonBox(
                width = 32.dp,
                height = 32.dp,
                brush = shimmerBrush,
                shape = CircleShape,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

        // Content
        if (isBodySection) {
            // Body code block skeleton
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
                        WormaCeptorDesignSystem.Shapes.chip,
                    )
                    .padding(WormaCeptorDesignSystem.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
            ) {
                repeat(8) { index ->
                    val widthFraction = when (index % 4) {
                        0 -> 0.95f
                        1 -> 0.6f
                        2 -> 0.8f
                        else -> 0.5f
                    }
                    SkeletonBox(
                        modifier = Modifier.fillMaxWidth(widthFraction),
                        height = 14.dp,
                        brush = shimmerBrush,
                    )
                }
            }
        } else {
            // Headers rows
            Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)) {
                repeat(contentRowCount) { index ->
                    DetailRowSkeleton(
                        shimmerBrush = shimmerBrush,
                        labelWidth = when (index % 3) {
                            0 -> 100.dp
                            1 -> 80.dp
                            else -> 120.dp
                        },
                        valueWidth = when (index % 4) {
                            0 -> 180.dp
                            1 -> 220.dp
                            2 -> 150.dp
                            else -> 200.dp
                        },
                    )
                }
            }
        }
    }
}
