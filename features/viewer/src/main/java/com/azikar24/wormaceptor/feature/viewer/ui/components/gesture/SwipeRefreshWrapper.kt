/*
 * Copyright AziKar24 2025.
 * Swipe Refresh Wrapper for WormaCeptor
 *
 * A custom pull-to-refresh container that uses WormaCeptor's design system.
 * Provides visual feedback matching the app's aesthetic while integrating
 * with Material3's PullToRefreshBox under the hood.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.gesture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
