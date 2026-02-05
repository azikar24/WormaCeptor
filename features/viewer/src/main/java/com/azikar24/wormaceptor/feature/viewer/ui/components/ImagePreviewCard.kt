package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.feature.viewer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Metadata extracted from an image
 */
data class ImageMetadata(
    val width: Int,
    val height: Int,
    val format: String,
    val fileSize: Long,
    val hasAlpha: Boolean = false,
    val colorDepth: String = "8-bit",
)

/**
 * Extracts image metadata from raw byte data
 */
suspend fun extractImageMetadata(data: ByteArray): ImageMetadata? = withContext(Dispatchers.Default) {
    try {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(data, 0, data.size, options)

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return@withContext null
        }

        val format = detectImageFormat(data)
        val hasAlpha = options.outMimeType?.contains("png") == true ||
            options.outMimeType?.contains("webp") == true

        ImageMetadata(
            width = options.outWidth,
            height = options.outHeight,
            format = format,
            fileSize = data.size.toLong(),
            hasAlpha = hasAlpha,
            colorDepth = "8-bit",
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Detects image format from magic bytes
 */
fun detectImageFormat(data: ByteArray): String {
    if (data.size < 8) return "Unknown"

    return when {
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        data[0] == 0x89.toByte() && data[1] == 0x50.toByte() &&
            data[2] == 0x4E.toByte() && data[3] == 0x47.toByte() -> "PNG"

        // JPEG: FF D8 FF
        data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() &&
            data[2] == 0xFF.toByte() -> "JPEG"

        // GIF: 47 49 46 38
        data[0] == 0x47.toByte() && data[1] == 0x49.toByte() &&
            data[2] == 0x46.toByte() && data[3] == 0x38.toByte() -> "GIF"

        // WebP: 52 49 46 46 ?? ?? ?? ?? 57 45 42 50
        data.size >= 12 && data[0] == 0x52.toByte() && data[1] == 0x49.toByte() &&
            data[2] == 0x46.toByte() && data[3] == 0x46.toByte() &&
            data[8] == 0x57.toByte() && data[9] == 0x45.toByte() &&
            data[10] == 0x42.toByte() && data[11] == 0x50.toByte() -> "WebP"

        // BMP: 42 4D
        data[0] == 0x42.toByte() && data[1] == 0x4D.toByte() -> "BMP"

        // ICO: 00 00 01 00
        data[0] == 0x00.toByte() && data[1] == 0x00.toByte() &&
            data[2] == 0x01.toByte() && data[3] == 0x00.toByte() -> "ICO"

        else -> "Unknown"
    }
}

/**
 * Checks if the given content type string indicates an image
 */
fun isImageContentType(contentType: String?): Boolean {
    if (contentType == null) return false
    val normalized = contentType.lowercase().trim()
    return normalized.startsWith("image/") ||
        normalized.contains("image/png") ||
        normalized.contains("image/jpeg") ||
        normalized.contains("image/jpg") ||
        normalized.contains("image/gif") ||
        normalized.contains("image/webp") ||
        normalized.contains("image/svg") ||
        normalized.contains("image/bmp")
}

/**
 * Checks if raw bytes likely represent an image by examining magic bytes
 */
fun isImageData(data: ByteArray?): Boolean {
    if (data == null || data.size < 4) return false
    val format = detectImageFormat(data)
    return format != "Unknown"
}

/**
 * Creates an ImageLoader with GIF support.
 * Uses ImageDecoderDecoder for API 28+ (more efficient) and GifDecoder for older APIs.
 */
@Composable
fun rememberGifImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }
}

/**
 * A polished card component for displaying image previews in HTTP response bodies.
 * Features:
 * - Thumbnail preview with aspect ratio preservation
 * - Tap to expand to fullscreen
 * - Image metadata display (dimensions, format, size)
 * - Download and share action buttons
 */
@Composable
fun ImagePreviewCard(
    imageData: ByteArray,
    contentType: String? = null,
    onFullscreen: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gifImageLoader = rememberGifImageLoader()
    var metadata by remember { mutableStateOf<ImageMetadata?>(null) }
    var isLoadingMetadata by remember { mutableStateOf(true) }
    var loadingProgress by remember { mutableFloatStateOf(0f) }
    var imageLoadState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }

    // Extract metadata on composition
    LaunchedEffect(imageData) {
        isLoadingMetadata = true
        metadata = extractImageMetadata(imageData)
        isLoadingMetadata = false
    }

    // Animate progress
    val animatedProgress by animateFloatAsState(
        targetValue = loadingProgress,
        animationSpec = tween(durationMillis = 300),
        label = "loading_progress",
    )

    val surfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
            MaterialTheme.colorScheme.primary.copy(alpha = 0.01f),
            Color.Transparent,
        ),
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor,
        ),
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        ),
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                // Header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.md),
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = stringResource(R.string.viewer_image),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.viewer_image_response),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.weight(1f))

                    // Format badge
                    metadata?.let { meta ->
                        Surface(
                            shape = WormaCeptorDesignSystem.Shapes.chip,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        ) {
                            Text(
                                text = meta.format,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(
                                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                    vertical = WormaCeptorDesignSystem.Spacing.xs,
                                ),
                            )
                        }
                    }
                }

                // Image Preview Container
                val aspectRatio = metadata?.let { it.width.toFloat() / it.height.toFloat() } ?: 16f / 9f
                val constrainedAspectRatio = aspectRatio.coerceIn(0.5f, 2.5f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .clickable(onClick = onFullscreen)
                        .aspectRatio(constrainedAspectRatio, matchHeightConstraintsFirst = false),
                    contentAlignment = Alignment.Center,
                ) {
                    // Background pattern for transparency
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                        MaterialTheme.colorScheme.surface,
                                    ),
                                ),
                            ),
                    )

                    // Actual image
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
                            imageLoadState = state
                            when (state) {
                                is AsyncImagePainter.State.Loading -> loadingProgress = 0.5f
                                is AsyncImagePainter.State.Success -> loadingProgress = 1f
                                is AsyncImagePainter.State.Error -> loadingProgress = 0f
                                else -> {}
                            }
                        },
                    )

                    // Loading overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = imageLoadState is AsyncImagePainter.State.Loading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(32.dp),
                                strokeWidth = 3.dp,
                            )
                        }
                    }

                    // Error overlay
                    androidx.compose.animation.AnimatedVisibility(
                        visible = imageLoadState is AsyncImagePainter.State.Error,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BrokenImage,
                                    contentDescription = stringResource(R.string.viewer_image_error),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(32.dp),
                                )
                                Text(
                                    text = stringResource(R.string.viewer_image_failed_to_load),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        }
                    }

                    // Fullscreen hint overlay
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shadowElevation = 2.dp,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = stringResource(R.string.viewer_image_fullscreen),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(WormaCeptorDesignSystem.Spacing.sm)
                                    .size(18.dp),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

                // Metadata Section
                if (!isLoadingMetadata && metadata != null) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                        thickness = 1.dp,
                    )

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

                    // Metadata grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        metadata?.let { meta ->
                            MetadataItem(
                                icon = Icons.Outlined.AspectRatio,
                                label = stringResource(R.string.viewer_image_dimensions),
                                value = "${meta.width} x ${meta.height}",
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            MetadataItem(
                                icon = Icons.Outlined.Memory,
                                label = stringResource(R.string.viewer_image_size),
                                value = formatFileSize(meta.fileSize),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                            MetadataItem(
                                icon = Icons.Outlined.ColorLens,
                                label = stringResource(R.string.viewer_image_color),
                                value = if (meta.hasAlpha) "RGBA" else "RGB",
                                tint = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    ActionButton(
                        icon = Icons.Default.Download,
                        label = stringResource(R.string.viewer_image_save),
                        onClick = onDownload,
                        modifier = Modifier.weight(1f),
                    )
                    ActionButton(
                        icon = Icons.Default.Share,
                        label = stringResource(R.string.viewer_image_share),
                        onClick = onShare,
                        modifier = Modifier.weight(1f),
                    )
                    ActionButton(
                        icon = Icons.Default.Fullscreen,
                        label = stringResource(R.string.viewer_image_expand),
                        onClick = onFullscreen,
                        modifier = Modifier.weight(1f),
                        isPrimary = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun MetadataItem(icon: ImageVector, label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = WormaCeptorDesignSystem.Shapes.button,
        color = if (isPrimary) {
            MaterialTheme.colorScheme.primary.asSubtleBackground()
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            if (isPrimary) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.md,
                    vertical = WormaCeptorDesignSystem.Spacing.sm,
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = if (isPrimary) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isPrimary) FontWeight.SemiBold else FontWeight.Medium,
                ),
                color = if (isPrimary) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
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
        bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}
