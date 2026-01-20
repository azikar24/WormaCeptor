/*
 * Copyright AziKar24 2025.
 * Swipe Refresh Wrapper for WormaCeptor
 *
 * A custom pull-to-refresh container that uses WormaCeptor's design system.
 * Provides visual feedback matching the app's aesthetic while integrating
 * with Material3's PullToRefreshBox under the hood.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.gesture

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * A pull-to-refresh wrapper that uses WormaCeptor's custom indicator.
 * Wraps Material3's PullToRefreshBox with custom visual feedback.
 *
 * @param isRefreshing Whether the refresh operation is in progress
 * @param onRefresh Callback when refresh is triggered
 * @param modifier Modifier for the container
 * @param enabled Whether pull-to-refresh is enabled
 * @param content The scrollable content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeRefreshWrapper(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val state = rememberPullToRefreshState()

    // Calculate refresh state from pull progress
    val refreshState by remember(state.distanceFraction, isRefreshing) {
        derivedStateOf {
            when {
                isRefreshing -> RefreshState.Refreshing
                state.distanceFraction >= 1f -> RefreshState.ThresholdReached
                state.distanceFraction > 0f -> RefreshState.Pulling
                else -> RefreshState.Idle
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        state = state,
        indicator = {
            // Custom indicator positioned at top center
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                PullToRefreshIndicator(
                    state = refreshState,
                    pullProgress = state.distanceFraction,
                )
            }
        },
    ) {
        content()
    }
}

/**
 * Alternative implementation using pure Compose gestures for more control.
 * This version provides finer control over the pull animation.
 */
@Composable
fun CustomSwipeRefreshWrapper(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    threshold: Float = 120f,
    content: @Composable () -> Unit,
) {
    rememberCoroutineScope()
    var pullDistance by remember { mutableFloatStateOf(0f) }
    val animatedPullDistance = remember { Animatable(0f) }

    // Sync pull distance with animated value
    LaunchedEffect(animatedPullDistance.value) {
        pullDistance = animatedPullDistance.value
    }

    // Reset pull distance when refresh completes
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && pullDistance > 0f) {
            animatedPullDistance.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }

    // Calculate refresh state
    val refreshState by remember(pullDistance, isRefreshing) {
        derivedStateOf {
            when {
                isRefreshing -> RefreshState.Refreshing
                pullDistance >= threshold -> RefreshState.ThresholdReached
                pullDistance > 0f -> RefreshState.Pulling
                else -> RefreshState.Idle
            }
        }
    }

    val pullProgress = (pullDistance / threshold).coerceIn(0f, 1.5f)

    Box(modifier = modifier.fillMaxSize()) {
        // Main content with offset when pulling
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, pullDistance.roundToInt()) },
        ) {
            content()
        }

        // Custom indicator at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, (pullDistance * 0.5f).roundToInt() - 48) }
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            PullToRefreshIndicator(
                state = refreshState,
                pullProgress = pullProgress,
            )
        }
    }
}

/**
 * Simple wrapper that just adds the refresh state management.
 * Uses Material3's default indicator with custom styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleSwipeRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        content()
    }
}
