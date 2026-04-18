package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.SemanticColors
import com.azikar24.wormaceptor.domain.entities.ImageMetadata
import com.azikar24.wormaceptor.feature.viewer.R
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

private val ZoomIndicatorTopOffset = 80.dp

@Composable
fun FullscreenImageViewer(
    imageData: ByteArray,
    metadata: ImageMetadata?,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        FullscreenImageContent(
            imageData = imageData,
            metadata = metadata,
            onDismiss = onDismiss,
            onDownload = onDownload,
            onShare = onShare,
        )
    }
}

@Composable
private fun FullscreenImageContent(
    imageData: ByteArray,
    metadata: ImageMetadata?,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    val context = LocalContext.current
    val gifImageLoader = rememberGifImageLoader()
    val scope = rememberCoroutineScope()

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isZoomed by remember { mutableStateOf(false) }

    val animatedScale = remember { Animatable(1f) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    var swipeOffset by remember { mutableFloatStateOf(0f) }

    var showControls by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(animatedScale.value) {
        scale = animatedScale.value
        isZoomed = scale > FullscreenImageDefaults.ZOOM_THRESHOLD
    }

    LaunchedEffect(animatedOffset.value) {
        offset = animatedOffset.value
    }

    val backgroundAlpha by animateFloatAsState(
        targetValue = 1f - (abs(swipeOffset) / FullscreenImageDefaults.DISMISS_THRESHOLD).coerceIn(0f, 0.5f),
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.ULTRA_FAST),
        label = "background_alpha",
    )

    val toggleControlsLabel = stringResource(R.string.viewer_image_toggle_controls)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(darkColors.background.copy(alpha = backgroundAlpha * FullscreenImageDefaults.BACKGROUND_ALPHA))
            .semantics {
                onClick(label = toggleControlsLabel) { true }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { showControls = !showControls },
                    onDoubleTap = { _ ->
                        scope.launch {
                            if (scale > FullscreenImageDefaults.ZOOM_THRESHOLD) {
                                animatedScale.animateTo(1f, FullscreenImageDefaults.ZoomSpring)
                                animatedOffset.animateTo(Offset.Zero, FullscreenImageDefaults.OffsetSpring)
                            } else {
                                animatedScale.animateTo(
                                    FullscreenImageDefaults.DOUBLE_TAP_ZOOM,
                                    FullscreenImageDefaults.ZoomSpring,
                                )
                            }
                        }
                    },
                )
            },
    ) {
        ZoomableImageBox(
            imageData = imageData,
            scale = scale,
            offset = offset,
            swipeOffset = swipeOffset,
            isZoomed = isZoomed,
            isLoading = isLoading,
            gifImageLoader = gifImageLoader,
            onScaleChange = { scope.launch { animatedScale.snapTo(it) } },
            onOffsetChange = { scope.launch { animatedOffset.snapTo(it) } },
            onSwipeOffsetChange = { swipeOffset += it },
            onBounceBack = {
                scope.launch {
                    animatedScale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                }
            },
            onDismiss = onDismiss,
            onSwipeReset = {
                scope.launch {
                    val anim = Animatable(swipeOffset)
                    anim.animateTo(
                        targetValue = 0f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    ) { swipeOffset = value }
                }
            },
            onLoadingChanged = { isLoading = it },
        )

        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            FullscreenTopControlBar(
                onClose = onDismiss,
                onZoomIn = {
                    scope.launch {
                        animatedScale.animateTo(
                            (scale * FullscreenImageDefaults.ZOOM_STEP).coerceAtMost(FullscreenImageDefaults.MAX_ZOOM),
                            spring(stiffness = Spring.StiffnessMedium),
                        )
                    }
                },
                onZoomOut = {
                    scope.launch {
                        animatedScale.animateTo(
                            (scale / FullscreenImageDefaults.ZOOM_STEP).coerceAtLeast(1f),
                            spring(stiffness = Spring.StiffnessMedium),
                        )
                        if (animatedScale.value <= 1f) {
                            animatedOffset.animateTo(Offset.Zero)
                        }
                    }
                },
                currentZoom = scale,
            )
        }

        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            FullscreenBottomControlBar(
                metadata = metadata,
                onDownload = onDownload,
                onShare = onShare,
            )
        }

        AnimatedVisibility(
            visible = isZoomed && showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = ZoomIndicatorTopOffset,
                    end = WormaCeptorTokens.Spacing.lg,
                ),
        ) {
            ZoomIndicator(scale = scale, darkColors = darkColors)
        }
    }
}

@Composable
private fun ZoomableImageBox(
    imageData: ByteArray,
    scale: Float,
    offset: Offset,
    swipeOffset: Float,
    isZoomed: Boolean,
    isLoading: Boolean,
    gifImageLoader: coil.ImageLoader,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onSwipeOffsetChange: (Float) -> Unit,
    onBounceBack: () -> Unit,
    onDismiss: () -> Unit,
    onSwipeReset: () -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y + swipeOffset
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(pass = PointerEventPass.Initial)
                    do {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val zoomChange = event.calculateZoom()
                        val panChange = event.calculatePan()

                        onScaleChange(
                            (scale * zoomChange).coerceIn(
                                FullscreenImageDefaults.MIN_ZOOM,
                                FullscreenImageDefaults.MAX_ZOOM,
                            ),
                        )

                        if (scale > 1f) {
                            onOffsetChange(offset + panChange * scale)
                        } else {
                            if (abs(panChange.y) > abs(panChange.x) && event.changes.size == 1) {
                                onSwipeOffsetChange(panChange.y)
                            }
                        }

                        event.changes.forEach { change ->
                            if (change.positionChanged()) {
                                change.consume()
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (scale < 1f) {
                        onBounceBack()
                    }

                    if (abs(swipeOffset) > FullscreenImageDefaults.DISMISS_THRESHOLD && !isZoomed) {
                        onDismiss()
                    } else {
                        onSwipeReset()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageData)
                .crossfade(true)
                .build(),
            imageLoader = gifImageLoader,
            contentDescription = stringResource(R.string.viewer_image_preview),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
            onState = { state ->
                onLoadingChanged(state is AsyncImagePainter.State.Loading)
            },
        )

        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.xxxl),
            )
        }
    }
}

@Composable
internal fun ZoomIndicator(
    scale: Float,
    darkColors: SemanticColors,
) {
    Surface(
        shape = WormaCeptorTokens.Shapes.button,
        color = darkColors.background.copy(alpha = WormaCeptorTokens.Alpha.INTENSE),
    ) {
        Text(
            text = "${String.format(Locale.US, "%.1f", scale)}x",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xs,
            ),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun ZoomIndicatorPreview() {
    WormaCeptorTheme {
        ZoomIndicator(
            scale = 2.5f,
            darkColors = WormaCeptorTokens.semantic(darkTheme = true),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FullscreenImageViewerPreview() {
    WormaCeptorTheme {
        FullscreenImageViewer(
            imageData = byteArrayOf(),
            metadata = ImageMetadata(
                width = 1920,
                height = 1080,
                format = "PNG",
                fileSize = 2_048_000L,
            ),
            onDismiss = {},
            onDownload = {},
            onShare = {},
        )
    }
}
