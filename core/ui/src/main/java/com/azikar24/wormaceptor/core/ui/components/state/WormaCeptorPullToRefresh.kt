package com.azikar24.wormaceptor.core.ui.components.state

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Pull-to-refresh container with a circular indicator and one-shot haptic on threshold. */
@Composable
fun WormaCeptorPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = WormaCeptorTokens.semantic().surfaceVariant,
    indicatorColor: Color = WormaCeptorTokens.semantic().accent,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val thresholdPx = with(density) { WormaCeptorTokens.ComponentSize.pullRefreshThreshold.toPx() }
    val indicatorSizePx = with(density) { WormaCeptorTokens.ComponentSize.pullRefreshIndicator.toPx() }
    val pullOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = rememberHapticOnce()
    val progress = (pullOffset.value / thresholdPx).coerceIn(0f, MaxProgress)

    LaunchedEffect(progress) {
        if (progress >= 1f && !haptic.isTriggered) {
            haptic.triggerHaptic()
        } else if (progress < 1f) {
            haptic.resetHaptic()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            pullOffset.animateTo(thresholdPx, tween(WormaCeptorTokens.Animation.FAST))
        } else {
            pullOffset.animateTo(0f, tween(WormaCeptorTokens.Animation.PAGE))
            haptic.resetHaptic()
        }
    }

    val connection = remember(isRefreshing, thresholdPx) {
        pullConnection(
            isRefreshing = { isRefreshing },
            thresholdPx = thresholdPx,
            pullOffset = pullOffset,
            scope = scope,
            onRefresh = onRefresh,
        )
    }

    Box(modifier = modifier.nestedScroll(connection)) {
        content()
        PullIndicator(
            isRefreshing = isRefreshing,
            progress = progress,
            offsetYPx = { (pullOffset.value - indicatorSizePx).roundToInt() },
            containerColor = containerColor,
            indicatorColor = indicatorColor,
            modifier = Modifier.align(Alignment.TopCenter),
        )
    }
}

@Composable
private fun PullIndicator(
    isRefreshing: Boolean,
    progress: Float,
    offsetYPx: () -> Int,
    containerColor: Color,
    indicatorColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetYPx()) }
            .size(WormaCeptorTokens.ComponentSize.pullRefreshIndicator)
            .graphicsLayer { alpha = if (isRefreshing) 1f else progress.coerceAtMost(1f) }
            .background(color = containerColor, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (isRefreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
                color = indicatorColor,
                strokeWidth = WormaCeptorTokens.ComponentSize.pullRefreshStroke,
            )
        } else {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = indicatorColor,
                modifier = Modifier
                    .size(WormaCeptorTokens.IconSize.lg)
                    .graphicsLayer { rotationZ = progress * IconRotationDegrees },
            )
        }
    }
}

private fun pullConnection(
    isRefreshing: () -> Boolean,
    thresholdPx: Float,
    pullOffset: Animatable<Float, *>,
    scope: kotlinx.coroutines.CoroutineScope,
    onRefresh: () -> Unit,
): NestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (isRefreshing()) return Offset.Zero
        if (available.y >= 0f || pullOffset.value <= 0f) return Offset.Zero
        val delta = maxOf(available.y, -pullOffset.value)
        scope.launch { pullOffset.snapTo(pullOffset.value + delta) }
        return Offset(0f, delta)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        if (isRefreshing() || available.y <= 0f) return Offset.Zero
        val delta = available.y * DragDamping
        scope.launch { pullOffset.snapTo(pullOffset.value + delta) }
        return Offset(0f, available.y)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (isRefreshing()) return Velocity.Zero
        if (pullOffset.value >= thresholdPx) {
            onRefresh()
        } else if (pullOffset.value > 0f) {
            pullOffset.animateTo(0f, tween(WormaCeptorTokens.Animation.PAGE))
        }
        return Velocity.Zero
    }
}

private const val DragDamping = 0.5f
private const val MaxProgress = 1.5f
private const val IconRotationDegrees = 180f
