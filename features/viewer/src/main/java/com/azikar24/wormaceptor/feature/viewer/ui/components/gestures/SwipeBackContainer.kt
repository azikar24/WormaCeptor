package com.azikar24.wormaceptor.feature.viewer.ui.components.gestures

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * SwipeBackContainer - A container that allows swipe-from-left-edge gesture to navigate back.
 *
 * Features:
 * - Swipe from left edge to trigger back navigation
 * - Visual feedback with shadow and arrow indicator
 * - Haptic feedback when threshold is crossed
 * - Smooth spring animation for settling
 * - Does not interfere with horizontal scrolling content
 *
 * @param onBack Callback invoked when the swipe-back gesture completes
 * @param enabled Whether the swipe-back gesture is enabled
 * @param edgeWidth Width of the edge zone that triggers the gesture (default: 24.dp)
 * @param thresholdFraction Fraction of screen width that triggers back (default: 0.35)
 * @param content The content to display inside the container
 */
@Composable
fun SwipeBackContainer(
    onBack: () -> Unit,
    enabled: Boolean = true,
    edgeWidth: Float = 48f,
    thresholdFraction: Float = 0.35f,
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val threshold = screenWidthPx * thresholdFraction
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }
    var dragStartedFromEdge by remember { mutableStateOf(false) }

    // Reset haptic state when drag ends
    LaunchedEffect(isDragging) {
        if (!isDragging) {
            hasTriggeredHaptic = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(
                if (enabled) {
                    Modifier.pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragStart = { startPosition ->
                                // Only start dragging if the gesture starts from the left edge
                                dragStartedFromEdge = startPosition.x <= edgeWidth
                                isDragging = dragStartedFromEdge
                            },
                            onDragEnd = {
                                isDragging = false
                                scope.launch {
                                    if (offsetX.value > threshold && dragStartedFromEdge) {
                                        // Animate out and call onBack
                                        offsetX.animateTo(
                                            targetValue = screenWidthPx,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            )
                                        )
                                        onBack()
                                    } else {
                                        // Spring back to original position
                                        offsetX.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                stiffness = Spring.StiffnessHigh
                                            )
                                        )
                                    }
                                    dragStartedFromEdge = false
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                scope.launch {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessHigh
                                        )
                                    )
                                }
                                dragStartedFromEdge = false
                            },
                            onHorizontalDrag = { _, dragAmount ->
                                if (dragStartedFromEdge) {
                                    scope.launch {
                                        val newValue = (offsetX.value + dragAmount).coerceIn(0f, screenWidthPx)
                                        offsetX.snapTo(newValue)

                                        // Trigger haptic feedback when threshold is crossed
                                        if (newValue > threshold && !hasTriggeredHaptic) {
                                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                            hasTriggeredHaptic = true
                                        } else if (newValue <= threshold && hasTriggeredHaptic) {
                                            // Reset when going back below threshold
                                            hasTriggeredHaptic = false
                                        }
                                    }
                                }
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        // Background scrim that shows when swiping
        val progress = (offsetX.value / screenWidthPx).coerceIn(0f, 1f)

        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f * (1f - progress)))
            )
        }

        // Arrow indicator on the left edge
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(48.dp)
                    .offset { IntOffset(0, 0) }
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = progress * 0.6f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.primary.copy(
                        alpha = (progress * 2f).coerceIn(0f, 1f)
                    ),
                    modifier = Modifier
                        .offset { IntOffset((16f * progress).roundToInt(), 0) }
                        .alpha((progress * 2f).coerceIn(0f, 1f))
                )
            }
        }

        // Main content with offset and shadow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .shadow(
                    elevation = (16.dp * progress),
                    ambientColor = Color.Black.copy(alpha = 0.5f),
                    spotColor = Color.Black.copy(alpha = 0.5f)
                )
                .background(MaterialTheme.colorScheme.surface),
            content = content
        )
    }
}
