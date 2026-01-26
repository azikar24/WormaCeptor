/*
 * Copyright AziKar24 2025.
 * Zoomable Text Container for WormaCeptor
 *
 * Provides pinch-to-zoom and pan functionality for large response bodies.
 * Features:
 * - Pinch-to-zoom with smooth spring animations
 * - Pan/drag when zoomed in with boundary constraints
 * - Double-tap to toggle between 1x and 2x zoom
 * - Floating zoom controls
 * - Zoom level persistence across recompositions
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import kotlinx.coroutines.launch

/**
 * Minimum and maximum zoom levels
 */
private const val MIN_ZOOM = 0.5f
private const val MAX_ZOOM = 3f
private const val DEFAULT_ZOOM = 1f
private const val DOUBLE_TAP_ZOOM = 2f

/**
 * A container that wraps text content with zoom and pan capabilities.
 * Perfect for viewing large JSON/XML responses in the transaction detail screen.
 *
 * @param text The text to display (plain string)
 * @param modifier Modifier for the container
 * @param initialZoom Starting zoom level (default 1f)
 * @param showZoomControls Whether to show floating zoom controls
 * @param onZoomChange Callback when zoom level changes
 * @param content Optional custom content instead of default text rendering
 */
@Composable
fun ZoomableTextContainer(
    text: String,
    modifier: Modifier = Modifier,
    initialZoom: Float = DEFAULT_ZOOM,
    showZoomControls: Boolean = true,
    onZoomChange: ((Float) -> Unit)? = null,
    content: (@Composable (scale: Float, offset: Offset) -> Unit)? = null,
) {
    ZoomableContainer(
        modifier = modifier,
        initialZoom = initialZoom,
        showZoomControls = showZoomControls,
        onZoomChange = onZoomChange,
    ) { scale, offset ->
        if (content != null) {
            content(scale, offset)
        } else {
            DefaultTextContent(
                text = text,
                scale = scale,
                offset = offset,
            )
        }
    }
}

/**
 * Variant that accepts AnnotatedString for syntax-highlighted content
 */
@Composable
fun ZoomableTextContainer(
    annotatedText: AnnotatedString,
    modifier: Modifier = Modifier,
    initialZoom: Float = DEFAULT_ZOOM,
    showZoomControls: Boolean = true,
    onZoomChange: ((Float) -> Unit)? = null,
) {
    ZoomableContainer(
        modifier = modifier,
        initialZoom = initialZoom,
        showZoomControls = showZoomControls,
        onZoomChange = onZoomChange,
    ) { scale, offset ->
        DefaultAnnotatedTextContent(
            text = annotatedText,
            scale = scale,
            offset = offset,
        )
    }
}

/**
 * Core zoomable container with gesture handling
 */
@Composable
fun ZoomableContainer(
    modifier: Modifier = Modifier,
    initialZoom: Float = DEFAULT_ZOOM,
    showZoomControls: Boolean = true,
    onZoomChange: ((Float) -> Unit)? = null,
    content: @Composable (scale: Float, offset: Offset) -> Unit,
) {
    val scope = rememberCoroutineScope()

    // Animated zoom and offset values
    val animatedScale = remember { Animatable(initialZoom) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    // Current state
    var scale by remember { mutableFloatStateOf(initialZoom) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isZoomed by remember { mutableStateOf(initialZoom > 1.05f) }

    // UI state
    var showControls by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Sync animated values with state
    LaunchedEffect(animatedScale.value) {
        scale = animatedScale.value
        isZoomed = scale > 1.05f
        onZoomChange?.invoke(scale)
    }

    LaunchedEffect(animatedOffset.value) {
        offset = animatedOffset.value
    }

    // Auto-hide controls after inactivity with cancellation support
    LaunchedEffect(lastInteractionTime) {
        try {
            kotlinx.coroutines.delay(3000)
            if (System.currentTimeMillis() - lastInteractionTime >= 3000) {
                showControls = false
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Coroutine was cancelled, ignore
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                // Double-tap detection
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                        lastInteractionTime = System.currentTimeMillis()
                    },
                    onDoubleTap = { tapOffset ->
                        scope.launch {
                            lastInteractionTime = System.currentTimeMillis()
                            if (scale > 1.1f) {
                                // Zoom out to default
                                animatedScale.animateTo(
                                    targetValue = DEFAULT_ZOOM,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                )
                                animatedOffset.animateTo(
                                    targetValue = Offset.Zero,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                )
                            } else {
                                // Zoom in to 2x at tap location
                                animatedScale.animateTo(
                                    targetValue = DOUBLE_TAP_ZOOM,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                )
                            }
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                // Pinch-to-zoom and pan
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()

                        lastInteractionTime = System.currentTimeMillis()
                        showControls = true

                        // Apply zoom with constraints
                        val newScale = (scale * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)
                        scope.launch {
                            animatedScale.snapTo(newScale)
                        }

                        // Apply pan when zoomed (with boundary constraints)
                        if (scale > 1f) {
                            val newOffset = calculateConstrainedOffset(
                                currentOffset = offset,
                                panChange = panChange,
                                scale = scale,
                                containerSize = size,
                            )
                            scope.launch {
                                animatedOffset.snapTo(newOffset)
                            }
                        }

                        // Consume events to prevent scroll conflicts
                        event.changes.forEach { change ->
                            if (change.positionChanged()) {
                                change.consume()
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // Gesture ended - apply constraints
                    scope.launch {
                        // Snap back if zoomed below minimum
                        if (scale < DEFAULT_ZOOM) {
                            animatedScale.animateTo(
                                targetValue = DEFAULT_ZOOM,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            )
                            animatedOffset.animateTo(
                                targetValue = Offset.Zero,
                                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                            )
                        }
                    }
                }
            },
    ) {
        // Zoomable content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
        ) {
            content(scale, offset)
        }

        // Floating zoom controls
        if (showZoomControls) {
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                ZoomControls(
                    currentZoom = scale,
                    onZoomIn = {
                        lastInteractionTime = System.currentTimeMillis()
                        scope.launch {
                            animatedScale.animateTo(
                                (scale * 1.5f).coerceAtMost(MAX_ZOOM),
                                spring(stiffness = Spring.StiffnessMedium),
                            )
                        }
                    },
                    onZoomOut = {
                        lastInteractionTime = System.currentTimeMillis()
                        scope.launch {
                            animatedScale.animateTo(
                                (scale / 1.5f).coerceAtLeast(MIN_ZOOM),
                                spring(stiffness = Spring.StiffnessMedium),
                            )
                            if (animatedScale.value <= DEFAULT_ZOOM) {
                                animatedOffset.animateTo(Offset.Zero)
                            }
                        }
                    },
                    onReset = {
                        lastInteractionTime = System.currentTimeMillis()
                        scope.launch {
                            animatedScale.animateTo(
                                DEFAULT_ZOOM,
                                spring(stiffness = Spring.StiffnessMedium),
                            )
                            animatedOffset.animateTo(
                                Offset.Zero,
                                spring(stiffness = Spring.StiffnessMedium),
                            )
                        }
                    },
                    minZoom = MIN_ZOOM,
                    maxZoom = MAX_ZOOM,
                )
            }
        }

        // Zoom level indicator badge (always visible when zoomed)
        AnimatedVisibility(
            visible = isZoomed,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(WormaCeptorDesignSystem.Spacing.md),
        ) {
            ZoomLevelBadge(zoom = scale)
        }
    }
}

/**
 * Default text rendering for plain strings
 */
@Composable
private fun DefaultTextContent(text: String, scale: Float, offset: Offset) {
    val scrollState = rememberScrollState()

    SelectionContainer {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(WormaCeptorDesignSystem.Spacing.md),
        )
    }
}

/**
 * Default text rendering for annotated strings (syntax highlighted)
 */
@Composable
private fun DefaultAnnotatedTextContent(text: AnnotatedString, scale: Float, offset: Offset) {
    val scrollState = rememberScrollState()

    SelectionContainer {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(WormaCeptorDesignSystem.Spacing.md),
        )
    }
}

/**
 * Small badge showing current zoom level
 */
@Composable
private fun ZoomLevelBadge(zoom: Float) {
    androidx.compose.material3.Surface(
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.md),
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        ),
    ) {
        Text(
            text = "${String.format("%.1f", zoom)}x",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
        )
    }
}

/**
 * Calculates constrained offset to keep content within viewable bounds
 */
private fun calculateConstrainedOffset(
    currentOffset: Offset,
    panChange: Offset,
    scale: Float,
    containerSize: androidx.compose.ui.unit.IntSize,
): Offset {
    // Calculate max allowed offset based on zoom level
    val maxX = (containerSize.width * (scale - 1) / 2).coerceAtLeast(0f)
    val maxY = (containerSize.height * (scale - 1) / 2).coerceAtLeast(0f)

    return Offset(
        x = (currentOffset.x + panChange.x * scale).coerceIn(-maxX, maxX),
        y = (currentOffset.y + panChange.y * scale).coerceIn(-maxY, maxY),
    )
}

/**
 * Simplified zoomable wrapper for any composable content
 */
@Composable
fun ZoomableContent(modifier: Modifier = Modifier, initialZoom: Float = DEFAULT_ZOOM, content: @Composable () -> Unit) {
    ZoomableContainer(
        modifier = modifier,
        initialZoom = initialZoom,
        showZoomControls = true,
        onZoomChange = null,
    ) { _, _ ->
        content()
    }
}

// Note: Uses Material3's built-in surfaceColorAtElevation() extension
