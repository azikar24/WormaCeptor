package com.azikar24.wormaceptor.feature.viewer.ui.components.gestures

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculateCentroidSize
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import kotlinx.coroutines.launch

private const val MIN_ZOOM = 0.8f
private const val MAX_ZOOM = 4f
private const val DOUBLE_TAP_ZOOM = 2f

/**
 * ZoomableTextContainer - A container that allows pinch-to-zoom and pan gestures on text content.
 *
 * Features:
 * - Pinch-to-zoom with smooth animations
 * - Double-tap to toggle between 1x and 2x zoom
 * - Pan gesture when zoomed in
 * - Zoom level indicator
 * - Reset zoom button
 * - Haptic feedback on zoom boundaries
 * - Maintains 60fps animations
 *
 * @param text The text content to display
 * @param annotatedText Optional AnnotatedString for syntax highlighted text
 * @param modifier Modifier for the container
 * @param initialScale Initial zoom scale (default: 1f)
 * @param onTextLayout Callback for text layout result
 */
@Composable
fun ZoomableTextContainer(
    text: String,
    annotatedText: AnnotatedString? = null,
    modifier: Modifier = Modifier,
    initialScale: Float = 1f,
    showZoomControls: Boolean = true,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    var scale by remember { mutableFloatStateOf(initialScale) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastHapticScale by remember { mutableFloatStateOf(1f) }

    val animatedScale = remember { Animatable(initialScale) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    // Transformable state for pinch-to-zoom
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)

        // Haptic feedback at zoom boundaries
        if ((newScale == MIN_ZOOM || newScale == MAX_ZOOM) && lastHapticScale != newScale) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            lastHapticScale = newScale
        } else if (newScale != MIN_ZOOM && newScale != MAX_ZOOM) {
            lastHapticScale = newScale
        }

        scale = newScale

        // Only allow panning when zoomed in
        if (scale > 1f) {
            // Calculate max offset based on zoom level
            val maxOffsetX = (scale - 1f) * 500f
            val maxOffsetY = (scale - 1f) * 1000f

            offset = Offset(
                x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
            )
        } else {
            // Reset offset when at 1x or lower
            offset = Offset.Zero
        }
    }

    // Double-tap to toggle zoom
    fun toggleZoom() {
        scope.launch {
            val targetScale = if (scale < 1.5f) DOUBLE_TAP_ZOOM else 1f
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

            // Animate scale
            animatedScale.snapTo(scale)
            animatedScale.animateTo(
                targetValue = targetScale,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            scale = targetScale

            // Reset offset when zooming out
            if (targetScale == 1f) {
                animatedOffset.snapTo(offset)
                animatedOffset.animateTo(
                    targetValue = Offset.Zero,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                offset = Offset.Zero
            }
        }
    }

    // Reset zoom function
    fun resetZoom() {
        scope.launch {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            animatedScale.snapTo(scale)
            animatedScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            scale = 1f

            animatedOffset.snapTo(offset)
            animatedOffset.animateTo(
                targetValue = Offset.Zero,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            offset = Offset.Zero
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { toggleZoom() }
                    )
                }
                .transformable(state = transformableState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            SelectionContainer {
                Text(
                    text = annotatedText ?: AnnotatedString(text),
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    onTextLayout = { onTextLayout?.invoke(it) }
                )
            }
        }

        // Zoom controls and indicator
        if (showZoomControls) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(WormaCeptorDesignSystem.Spacing.md),
                shape = WormaCeptorDesignSystem.Shapes.chip,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
                shadowElevation = WormaCeptorDesignSystem.Elevation.sm
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                        WormaCeptorDesignSystem.Spacing.xs
                    )
                ) {
                    // Zoom out button
                    FilledIconButton(
                        onClick = {
                            scope.launch {
                                val newScale = (scale - 0.5f).coerceIn(MIN_ZOOM, MAX_ZOOM)
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                scale = newScale
                                if (newScale <= 1f) {
                                    offset = Offset.Zero
                                }
                            }
                        },
                        enabled = scale > MIN_ZOOM,
                        modifier = Modifier.padding(0.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomOut,
                            contentDescription = "Zoom out",
                            modifier = Modifier.padding(4.dp)
                        )
                    }

                    // Zoom level indicator
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = WormaCeptorDesignSystem.Spacing.xs)
                    )

                    // Zoom in button
                    FilledIconButton(
                        onClick = {
                            scope.launch {
                                val newScale = (scale + 0.5f).coerceIn(MIN_ZOOM, MAX_ZOOM)
                                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                scale = newScale
                            }
                        },
                        enabled = scale < MAX_ZOOM,
                        modifier = Modifier.padding(0.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ZoomIn,
                            contentDescription = "Zoom in",
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            // Reset button (only show when zoomed)
            if (scale != 1f || offset != Offset.Zero) {
                Surface(
                    onClick = { resetZoom() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .padding(WormaCeptorDesignSystem.Spacing.md),
                    shape = WormaCeptorDesignSystem.Shapes.chip,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = WormaCeptorDesignSystem.Elevation.sm
                ) {
                    Text(
                        text = "Reset",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.md,
                            vertical = WormaCeptorDesignSystem.Spacing.sm
                        )
                    )
                }
            }
        }
    }
}

/**
 * A simplified zoomable box that can contain any content.
 * Useful for wrapping existing composables with zoom functionality.
 */
@Composable
fun ZoomableBox(
    modifier: Modifier = Modifier,
    minScale: Float = MIN_ZOOM,
    maxScale: Float = MAX_ZOOM,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(minScale, maxScale)

        if (scale > 1f) {
            val maxOffsetX = (scale - 1f) * 500f
            val maxOffsetY = (scale - 1f) * 1000f

            offset = Offset(
                x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
            )
        } else {
            offset = Offset.Zero
        }
    }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            val targetScale = if (scale < 1.5f) DOUBLE_TAP_ZOOM else 1f
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            scale = targetScale
                            if (targetScale == 1f) {
                                offset = Offset.Zero
                            }
                        }
                    }
                )
            }
            .transformable(state = transformableState)
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            ),
        content = content
    )
}
