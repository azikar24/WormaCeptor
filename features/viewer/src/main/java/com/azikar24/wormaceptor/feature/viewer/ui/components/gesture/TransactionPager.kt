/*
 * Copyright AziKar24 2025.
 * Transaction Pager for WormaCeptor
 *
 * Provides horizontal swipe navigation between transactions with:
 * - Smooth paging animations
 * - Edge peek previews
 * - Position indicators
 * - Haptic feedback on page changes
 * - Preloading of adjacent pages
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.gesture

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import kotlin.math.absoluteValue

/**
 * A pager component for navigating between transactions with swipe gestures.
 * Includes visual feedback, position indicator, and edge peek effects.
 *
 * @param transactionIds List of all transaction IDs
 * @param initialTransactionId The transaction to show initially
 * @param onTransactionChanged Callback when the visible transaction changes
 * @param modifier Modifier for the container
 * @param content Content composable for each transaction page
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionPager(
    transactionIds: List<UUID>,
    initialTransactionId: UUID,
    onTransactionChanged: (UUID) -> Unit,
    modifier: Modifier = Modifier,
    showPositionIndicator: Boolean = true,
    content: @Composable (transactionId: UUID, pageIndex: Int) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    // Find initial page index
    val initialPage = remember(initialTransactionId, transactionIds) {
        transactionIds.indexOf(initialTransactionId).coerceAtLeast(0)
    }

    // Pager state
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { transactionIds.size },
    )

    // Track previous page for haptic feedback
    var previousPage by remember { mutableIntStateOf(initialPage) }

    // Notify when page changes and trigger haptic feedback
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page != previousPage) {
                    // Haptic feedback on page change
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    previousPage = page

                    // Notify parent
                    transactionIds.getOrNull(page)?.let { transactionId ->
                        onTransactionChanged(transactionId)
                    }
                }
            }
    }

    // Calculate swipe progress for edge effects
    val swipeOffset by remember {
        derivedStateOf {
            pagerState.currentPageOffsetFraction
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Main pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1, // Preload adjacent pages
            pageSpacing = 0.dp,
            contentPadding = PaddingValues(horizontal = 0.dp),
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                pagerSnapDistance = PagerSnapDistance.atMost(1),
                snapAnimationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ),
            key = { page -> transactionIds.getOrNull(page) ?: page },
        ) { page ->
            val pageOffset = (pagerState.currentPage - page + pagerState.currentPageOffsetFraction)

            // Page content with transition effects
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Subtle parallax and scale effect
                        val scale = 1f - (pageOffset.absoluteValue * 0.05f).coerceAtMost(0.05f)
                        scaleX = scale
                        scaleY = scale

                        // Fade edges slightly
                        alpha = 1f - (pageOffset.absoluteValue * 0.3f).coerceAtMost(0.3f)

                        // Subtle rotation for 3D effect
                        rotationY = pageOffset * -5f
                    },
            ) {
                transactionIds.getOrNull(page)?.let { transactionId ->
                    content(transactionId, page)
                }
            }
        }

        // Edge shadows during swipe
        EdgeShadows(
            swipeOffset = swipeOffset,
            canSwipeLeft = pagerState.currentPage > 0,
            canSwipeRight = pagerState.currentPage < transactionIds.size - 1,
        )

        // Position indicator
        if (showPositionIndicator && transactionIds.size > 1) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = WormaCeptorDesignSystem.Spacing.xl),
            ) {
                TransactionPositionIndicator(
                    currentIndex = pagerState.currentPage,
                    totalCount = transactionIds.size,
                    onPrevious = {
                        scope.launch {
                            if (pagerState.currentPage > 0) {
                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                            }
                        }
                    },
                    onNext = {
                        scope.launch {
                            if (pagerState.currentPage < transactionIds.size - 1) {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                )
            }
        }

        // Swipe navigation hints (shown briefly on first load)
        SwipeNavigationHint(
            canSwipeLeft = pagerState.currentPage > 0,
            canSwipeRight = pagerState.currentPage < transactionIds.size - 1,
        )
    }
}

/**
 * Edge shadows that appear during swipe gestures
 */
@Composable
private fun BoxScope.EdgeShadows(swipeOffset: Float, canSwipeLeft: Boolean, canSwipeRight: Boolean) {
    val leftShadowAlpha = if (canSwipeLeft && swipeOffset > 0) {
        (swipeOffset * 0.5f).coerceIn(0f, 0.3f)
    } else {
        0f
    }

    val rightShadowAlpha = if (canSwipeRight && swipeOffset < 0) {
        (abs(swipeOffset) * 0.5f).coerceIn(0f, 0.3f)
    } else {
        0f
    }

    // Left edge shadow
    if (leftShadowAlpha > 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(leftShadowAlpha)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0f),
                        ),
                        startX = 0f,
                        endX = 100f,
                    ),
                ),
        )
    }

    // Right edge shadow
    if (rightShadowAlpha > 0) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(rightShadowAlpha)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0f),
                            MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f),
                        ),
                    ),
                ),
        )
    }
}

/**
 * Simplified pager that just handles the horizontal swipe
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> SimplePager(
    items: List<T>,
    initialIndex: Int = 0,
    onPageChanged: (Int, T) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (item: T, index: Int) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, items.lastIndex.coerceAtLeast(0)),
        pageCount = { items.size },
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                items.getOrNull(page)?.let { item ->
                    onPageChanged(page, item)
                }
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        beyondViewportPageCount = 1,
    ) { page ->
        items.getOrNull(page)?.let { item ->
            content(item, page)
        }
    }
}

/**
 * Page indicator dots for compact representation
 */
@Composable
fun PageIndicatorDots(totalPages: Int, currentPage: Int, modifier: Modifier = Modifier) {
    if (totalPages <= 1) return

    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
            WormaCeptorDesignSystem.Spacing.xs,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Show max 7 dots, with ellipsis behavior for longer lists
        val displayRange = when {
            totalPages <= 7 -> 0 until totalPages
            currentPage < 4 -> 0 until 7
            currentPage > totalPages - 5 -> (totalPages - 7) until totalPages
            else -> (currentPage - 3) until (currentPage + 4)
        }

        displayRange.forEach { index ->
            val isSelected = index == currentPage
            val dotSize = if (isSelected) 8.dp else 6.dp
            val alpha = when {
                isSelected -> 1f
                abs(index - currentPage) == 1 -> 0.7f
                abs(index - currentPage) == 2 -> 0.4f
                else -> 0.2f
            }

            Box(
                modifier = Modifier
                    .size(dotSize)
                    .alpha(alpha)
                    .background(
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        shape = androidx.compose.foundation.shape.CircleShape,
                    ),
            )
        }
    }
}
