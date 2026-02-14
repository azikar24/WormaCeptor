package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.components.rememberHapticOnce
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem.Alpha
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem.CornerRadius
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem.Spacing
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import kotlinx.collections.immutable.ImmutableList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * CrashListScreen with pull-to-refresh support.
 *
 * @param crashes List of crashes to display
 * @param onCrashClick Callback when a crash is clicked
 * @param isRefreshing Whether the list is currently refreshing
 * @param onRefresh Callback triggered on pull-to-refresh
 * @param modifier Modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashListScreen(
    crashes: ImmutableList<Crash>,
    onCrashClick: (Crash) -> Unit,
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val haptic = rememberHapticOnce()

    // Trigger haptic feedback when pull threshold is reached
    LaunchedEffect(pullToRefreshState.distanceFraction) {
        if (pullToRefreshState.distanceFraction >= 1f && !haptic.isTriggered) {
            haptic.triggerHaptic()
        } else if (pullToRefreshState.distanceFraction < 1f) {
            haptic.resetHaptic()
        }
    }

    // Reset haptic state when refreshing ends
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            haptic.resetHaptic()
        }
    }

    if (crashes.isEmpty()) {
        // Empty state with pull-to-refresh
        if (onRefresh != null) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = modifier.fillMaxSize(),
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isRefreshing,
                        state = pullToRefreshState,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        color = MaterialTheme.colorScheme.error,
                    )
                },
            ) {
                EnhancedEmptyState(
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            EnhancedEmptyState(
                modifier = modifier,
            )
        }
    } else {
        // List with pull-to-refresh
        if (onRefresh != null) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = modifier.fillMaxSize(),
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isRefreshing,
                        state = pullToRefreshState,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        color = MaterialTheme.colorScheme.error,
                    )
                },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.md,
                        top = Spacing.md,
                        end = Spacing.md,
                        bottom = Spacing.md + navigationBarPadding,
                    ),
                ) {
                    items(crashes, key = { it.id }) { crash ->
                        EnhancedCrashItem(
                            crash = crash,
                            onClick = { onCrashClick(crash) },
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.md,
                    top = Spacing.md,
                    end = Spacing.md,
                    bottom = Spacing.md + navigationBarPadding,
                ),
            ) {
                items(crashes, key = { it.id }) { crash ->
                    EnhancedCrashItem(
                        crash = crash,
                        onClick = { onCrashClick(crash) },
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                }
            }
        }
    }
}

@Composable
fun EnhancedCrashItem(crash: Crash, onClick: () -> Unit) {
    val location = remember(crash.stackTrace) { CrashUtils.extractCrashLocation(crash.stackTrace) }
    val relativeTime = remember(crash.timestamp) { formatRelativeTime(crash.timestamp) }
    val isSevere = remember(crash.exceptionType) { isSevereException(crash.exceptionType) }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        label = "crash_item_alpha",
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(WormaCeptorDesignSystem.Shapes.card)
            .clickable(onClick = onClick)
            .alpha(alpha),
        shape = WormaCeptorDesignSystem.Shapes.card,
        color = WormaCeptorColors.StatusRed.copy(alpha = Alpha.subtle),
        tonalElevation = WormaCeptorDesignSystem.Elevation.xs,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalAlignment = Alignment.Top,
        ) {
            // Icon badge
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.xs),
                color = WormaCeptorColors.StatusRed.copy(alpha = Alpha.light),
                contentColor = WormaCeptorColors.StatusRed,
                modifier = Modifier.size(Spacing.xxl),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = if (isSevere) Icons.Default.BugReport else Icons.Default.Warning,
                        contentDescription = if (isSevere) {
                            stringResource(
                                R.string.viewer_crash_list_critical_crash,
                            )
                        } else {
                            stringResource(R.string.viewer_crash_list_warning)
                        },
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                // Exception type - prominent
                Text(
                    text = crash.exceptionType,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = (-0.2).sp,
                )

                Spacer(modifier = Modifier.height(Spacing.xs))

                // Error message
                val message = crash.message
                if (message != null && message.isNotBlank()) {
                    Text(
                        text = message,
                        style = WormaCeptorDesignSystem.Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = Alpha.prominent),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(Spacing.sm))
                }

                // Stack trace location in monospace
                if (location != null) {
                    Surface(
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.xs),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = Alpha.strong),
                    ) {
                        Text(
                            text = location,
                            style = WormaCeptorDesignSystem.Typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.xs))
                }

                // Relative timestamp with better typography
                Text(
                    text = relativeTime,
                    style = WormaCeptorDesignSystem.Typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.heavy),
                )
            }
        }
    }
}

@Composable
@Deprecated("Use EnhancedCrashItem instead")
fun CrashItem(crash: Crash, onClick: () -> Unit) {
    EnhancedCrashItem(crash = crash, onClick = onClick)
}

@Composable
private fun EnhancedEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon
        Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(CornerRadius.lg),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = Alpha.moderate),
            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.emptyState),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = stringResource(R.string.viewer_crash_list_no_crashes),
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xl),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.intense),
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))

        // Title
        Text(
            text = stringResource(R.string.viewer_crash_list_no_crashes_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        // Description
        Text(
            text = stringResource(R.string.viewer_crash_list_no_crashes_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = Alpha.heavy),
        )
    }
}

@Composable
@Deprecated("Use EnhancedEmptyState instead")
private fun EmptyState(message: String, modifier: Modifier = Modifier) {
    EnhancedEmptyState(modifier = modifier)
}

// Helper functions
private fun formatRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> {
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
            "$minutes min ago"
        }
        diff < TimeUnit.DAYS.toMillis(1) -> {
            val hours = TimeUnit.MILLISECONDS.toHours(diff)
            "$hours hr ago"
        }
        diff < TimeUnit.DAYS.toMillis(7) -> {
            val days = TimeUnit.MILLISECONDS.toDays(diff)
            "$days day${if (days > 1) "s" else ""} ago"
        }
        else -> {
            SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(timestamp))
        }
    }
}

private fun isSevereException(exceptionType: String): Boolean {
    val severeTypes = listOf(
        "NullPointerException",
        "OutOfMemoryError",
        "StackOverflowError",
        "SecurityException",
        "IllegalStateException",
        "AssertionError",
    )
    return severeTypes.any { exceptionType.contains(it, ignoreCase = true) }
}
