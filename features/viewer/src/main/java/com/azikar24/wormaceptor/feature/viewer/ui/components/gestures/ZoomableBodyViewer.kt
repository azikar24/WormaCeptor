package com.azikar24.wormaceptor.feature.viewer.ui.components.gestures

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import kotlinx.coroutines.launch

private const val MIN_ZOOM = 0.8f
private const val MAX_ZOOM = 5f
private const val DOUBLE_TAP_ZOOM = 2f

/**
 * A fullscreen dialog viewer for body text with pinch-to-zoom and pan capabilities.
 *
 * @param text The text to display
 * @param annotatedText Optional annotated text for syntax highlighting
 * @param onDismiss Callback when the viewer is dismissed
 * @param onTextLayout Optional callback for text layout result
 */
@Composable
fun FullscreenZoomableBodyViewer(
    text: String,
    annotatedText: AnnotatedString? = null,
    onDismiss: () -> Unit,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().background(Color.Red),
            color = MaterialTheme.colorScheme.surface
        ) {
            ZoomableBodyContent(
                text = text,
                annotatedText = annotatedText,
                onTextLayout = onTextLayout,
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun ZoomableBodyContent(
    text: String,
    annotatedText: AnnotatedString?,
    onTextLayout: ((TextLayoutResult) -> Unit)?,
    onDismiss: () -> Unit
) {
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(MIN_ZOOM, MAX_ZOOM)

        // Haptic feedback at zoom boundaries
        if ((newScale == MIN_ZOOM || newScale == MAX_ZOOM) && scale != newScale) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }

        scale = newScale

        // Only allow panning when zoomed in
        if (scale > 1f) {
            val maxOffsetX = (scale - 1f) * 500f
            val maxOffsetY = (scale - 1f) * 2000f

            offset = Offset(
                x = (offset.x + panChange.x).coerceIn(-maxOffsetX, maxOffsetX),
                y = (offset.y + panChange.y).coerceIn(-maxOffsetY, maxOffsetY)
            )
        } else {
            offset = Offset.Zero
        }
    }

    fun toggleZoom() {
        scope.launch {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            if (scale < 1.5f) {
                scale = DOUBLE_TAP_ZOOM
            } else {
                scale = 1f
                offset = Offset.Zero
            }
        }
    }

    fun resetZoom() {
        scope.launch {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            scale = 1f
            offset = Offset.Zero
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content with zoom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { toggleZoom() }
                    )
                }
                .transformable(state = transformableState)
        ) {
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .verticalScroll(scrollState)
                    .padding(WormaCeptorDesignSystem.Spacing.lg)
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
        }

        // Top bar with close button
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            tonalElevation = WormaCeptorDesignSystem.Elevation.sm
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WormaCeptorDesignSystem.Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Response Body",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = WormaCeptorDesignSystem.Spacing.sm)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
        }

        // Zoom controls
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            shape = WormaCeptorDesignSystem.Shapes.chip,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            shadowElevation = WormaCeptorDesignSystem.Elevation.md
        ) {
            Row(
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
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
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom out"
                    )
                }

                // Zoom level indicator
                Surface(
                    shape = WormaCeptorDesignSystem.Shapes.chip,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(horizontal = WormaCeptorDesignSystem.Spacing.xs)
                ) {
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.md,
                            vertical = WormaCeptorDesignSystem.Spacing.xs
                        )
                    )
                }

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
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom in"
                    )
                }

                // Reset button (only show when zoomed)
                AnimatedVisibility(
                    visible = scale != 1f || offset != Offset.Zero,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        onClick = { resetZoom() },
                        shape = WormaCeptorDesignSystem.Shapes.chip,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.md,
                                vertical = WormaCeptorDesignSystem.Spacing.sm
                            )
                        )
                    }
                }
            }
        }

        // Hint text for double-tap
        AnimatedVisibility(
            visible = scale == 1f,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(WormaCeptorDesignSystem.Spacing.lg)
                .padding(bottom = 80.dp)
        ) {
            Text(
                text = "Double-tap to zoom",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * A button to open the fullscreen zoomable viewer
 */
@Composable
fun ZoomBodyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs
            ),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Fullscreen,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Zoom",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
