package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

/**
 * A fullscreen image viewer with pinch-to-zoom, pan, and double-tap to zoom functionality.
 * Features:
 * - Pinch-to-zoom with smooth spring animations
 * - Pan/drag when zoomed in
 * - Double-tap to toggle between 1x and 2.5x zoom
 * - Swipe down to dismiss
 * - Dark overlay background
 * - Action buttons (download, share)
 * - Image metadata display
 */
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
    val context = LocalContext.current
    val gifImageLoader = rememberGifImageLoader()
    val scope = rememberCoroutineScope()

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isZoomed by remember { mutableStateOf(false) }

    // Animation for smooth zoom transitions
    val animatedScale = remember { Animatable(1f) }
    val animatedOffset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

    // Swipe to dismiss state
    var swipeOffset by remember { mutableFloatStateOf(0f) }
    val dismissThreshold = 200f

    // UI visibility
    var showControls by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }

    // Sync animated values with state
    LaunchedEffect(animatedScale.value) {
        scale = animatedScale.value
        isZoomed = scale > 1.1f
    }

    LaunchedEffect(animatedOffset.value) {
        offset = animatedOffset.value
    }

    // Background alpha based on swipe progress
    val backgroundAlpha by animateFloatAsState(
        targetValue = 1f - (abs(swipeOffset) / dismissThreshold).coerceIn(0f, 0.5f),
        animationSpec = tween(durationMillis = 100),
        label = "background_alpha",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backgroundAlpha * 0.95f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    },
                    onDoubleTap = { tapOffset ->
                        scope.launch {
                            if (scale > 1.1f) {
                                // Zoom out
                                animatedScale.animateTo(
                                    targetValue = 1f,
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
                                // Zoom in to tap location
                                val targetScale = 2.5f
                                animatedScale.animateTo(
                                    targetValue = targetScale,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium,
                                    ),
                                )
                            }
                        }
                    },
                )
            },
    ) {
        // Main image with gestures
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

                            // Apply zoom
                            val newScale = (scale * zoomChange).coerceIn(0.5f, 5f)

                            scope.launch {
                                animatedScale.snapTo(newScale)
                            }

                            // Apply pan when zoomed
                            if (scale > 1f) {
                                val newOffset = offset + panChange * scale
                                scope.launch {
                                    animatedOffset.snapTo(newOffset)
                                }
                            } else {
                                // Swipe to dismiss when not zoomed
                                if (abs(panChange.y) > abs(panChange.x) && event.changes.size == 1) {
                                    swipeOffset += panChange.y
                                }
                            }

                            event.changes.forEach { change ->
                                if (change.positionChanged()) {
                                    change.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        // Handle gesture end
                        if (scale < 1f) {
                            scope.launch {
                                animatedScale.animateTo(
                                    targetValue = 1f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                )
                            }
                        }

                        // Check for dismiss
                        if (abs(swipeOffset) > dismissThreshold && !isZoomed) {
                            onDismiss()
                        } else {
                            // Snap back
                            scope.launch {
                                val anim = Animatable(swipeOffset)
                                anim.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                                ) {
                                    swipeOffset = value
                                }
                            }
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
                contentDescription = "Fullscreen image",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                onState = { state ->
                    isLoading = state is AsyncImagePainter.State.Loading
                },
            )

            // Loading indicator
            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp),
                )
            }
        }

        // Top controls
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            TopControlBar(
                onClose = onDismiss,
                onZoomIn = {
                    scope.launch {
                        animatedScale.animateTo(
                            (scale * 1.5f).coerceAtMost(5f),
                            spring(stiffness = Spring.StiffnessMedium),
                        )
                    }
                },
                onZoomOut = {
                    scope.launch {
                        animatedScale.animateTo(
                            (scale / 1.5f).coerceAtLeast(1f),
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

        // Bottom controls with metadata
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            BottomControlBar(
                metadata = metadata,
                onDownload = onDownload,
                onShare = onShare,
            )
        }

        // Zoom indicator
        AnimatedVisibility(
            visible = isZoomed && showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 80.dp, end = WormaCeptorDesignSystem.Spacing.lg),
        ) {
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                color = Color.Black.copy(alpha = 0.6f),
            ) {
                Text(
                    text = "${String.format("%.1f", scale)}x",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xs,
                    ),
                )
            }
        }
    }
}

@Composable
private fun TopControlBar(onClose: () -> Unit, onZoomIn: () -> Unit, onZoomOut: () -> Unit, currentZoom: Float) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.4f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md)
                .windowInsetsPadding(WindowInsets.statusBars),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Close button
            FilledTonalIconButton(
                onClick = onClose,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                )
            }

            // Zoom controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalIconButton(
                    onClick = onZoomOut,
                    enabled = currentZoom > 1f,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom out",
                    )
                }

                FilledTonalIconButton(
                    onClick = onZoomIn,
                    enabled = currentZoom < 5f,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.15f),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.05f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom in",
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomControlBar(metadata: ImageMetadata?, onDownload: () -> Unit, onShare: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = WormaCeptorDesignSystem.Spacing.xxl),
        color = Color.Black.copy(alpha = 0.6f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Metadata row
            metadata?.let { meta ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MetadataChip(
                        icon = Icons.Outlined.AspectRatio,
                        text = "${meta.width} x ${meta.height}",
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))
                    MetadataChip(
                        icon = Icons.Outlined.Memory,
                        text = formatFileSize(meta.fileSize),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))
                    Surface(
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                        color = Color.White.copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = meta.format,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            color = Color.White,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                vertical = WormaCeptorDesignSystem.Spacing.xs,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    WormaCeptorDesignSystem.Spacing.md,
                    Alignment.CenterHorizontally,
                ),
            ) {
                ActionChip(
                    icon = Icons.Default.Download,
                    label = "Save to Gallery",
                    onClick = onDownload,
                )
                ActionChip(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = onShare,
                )
            }
        }
    }
}

@Composable
private fun MetadataChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.9f),
        )
    }
}

@Composable
private fun ActionChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
        color = Color.White.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.md,
            ),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Medium,
                ),
                color = Color.White,
            )
        }
    }
}

/**
 * Formats file size in human-readable format
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

/**
 * Saves image data to the device gallery
 */
fun saveImageToGallery(context: Context, imageData: ByteArray, format: String): Boolean {
    return try {
        val filename = "WormaCeptor_${System.currentTimeMillis()}.${format.lowercase()}"
        val mimeType = when (format.uppercase()) {
            "PNG" -> "image/png"
            "JPEG", "JPG" -> "image/jpeg"
            "GIF" -> "image/gif"
            "WEBP" -> "image/webp"
            "BMP" -> "image/bmp"
            else -> "image/png"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WormaCeptor")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues,
            )

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(imageData)
                }

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }

            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            true
        } else {
            // Legacy approach for older Android versions
            @Suppress("DEPRECATION")
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val wormaceptorDir = File(picturesDir, "WormaCeptor")
            if (!wormaceptorDir.exists()) {
                wormaceptorDir.mkdirs()
            }

            val file = File(wormaceptorDir, filename)
            FileOutputStream(file).use { it.write(imageData) }

            // Notify media scanner
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            intent.data = android.net.Uri.fromFile(file)
            context.sendBroadcast(intent)

            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            true
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
        false
    }
}

/**
 * Shares image data using Android's share sheet
 */
fun shareImage(context: Context, imageData: ByteArray, format: String) {
    try {
        val filename = "WormaCeptor_${System.currentTimeMillis()}.${format.lowercase()}"
        val mimeType = when (format.uppercase()) {
            "PNG" -> "image/png"
            "JPEG", "JPG" -> "image/jpeg"
            "GIF" -> "image/gif"
            "WEBP" -> "image/webp"
            "BMP" -> "image/bmp"
            else -> "image/png"
        }

        // Write to cache directory
        val file = File(context.cacheDir, filename)
        file.outputStream().use { it.write(imageData) }

        // Get URI via FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.wormaceptor.fileprovider",
            file,
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Image"))
    } catch (e: Exception) {
        Toast.makeText(context, "Failed to share image: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}
