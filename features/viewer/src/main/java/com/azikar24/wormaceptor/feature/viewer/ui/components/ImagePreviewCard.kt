package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.os.Build
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.contracts.ImageMetadataExtractor
import com.azikar24.wormaceptor.domain.entities.ImageMetadata
import com.azikar24.wormaceptor.feature.viewer.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.get

/**
 * Checks if the given content type string indicates an image.
 */
fun isImageContentType(contentType: String?): Boolean {
    if (contentType == null) return false
    val normalized = contentType.lowercase().trim()
    return normalized.startsWith("image/")
}

/**
 * Checks if raw bytes likely represent an image by examining magic bytes.
 * Delegates to [ImageMetadataExtractor] via Koin.
 */
fun isImageData(data: ByteArray?): Boolean {
    if (data == null || data.size < 4) return false
    return try {
        val extractor: ImageMetadataExtractor = get(ImageMetadataExtractor::class.java)
        extractor.isImageData(data)
    } catch (_: Exception) {
        false
    }
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
    onFullscreen: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val gifImageLoader = rememberGifImageLoader()
    var metadata by remember { mutableStateOf<ImageMetadata?>(null) }
    var isLoadingMetadata by remember { mutableStateOf(true) }
    var imageLoadState by remember { mutableStateOf<AsyncImagePainter.State?>(null) }

    // Extract metadata via Koin-injected extractor
    LaunchedEffect(imageData) {
        isLoadingMetadata = true
        metadata = withContext(Dispatchers.Default) {
            try {
                val extractor: ImageMetadataExtractor = get(ImageMetadataExtractor::class.java)
                val extracted = extractor.extractMetadata(imageData)
                if (extracted.width > 0 && extracted.height > 0) extracted else null
            } catch (_: Exception) {
                null
            }
        }
        isLoadingMetadata = false
    }

    val surfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor,
        ),
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.MEDIUM),
        ),
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
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
                            color = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = WormaCeptorDesignSystem.Alpha.INTENSE,
                            ),
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
                val aspectRatio = metadata?.let { it.width.toFloat() / it.height.toFloat() } ?: (16f / 9f)
                val constrainedAspectRatio = aspectRatio.coerceIn(0.5f, 2.5f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = WormaCeptorDesignSystem.Alpha.MODERATE,
                            ),
                        )
                        .clickable(onClick = onFullscreen)
                        .aspectRatio(constrainedAspectRatio, matchHeightConstraintsFirst = false),
                    contentAlignment = Alignment.Center,
                ) {
                    // Background for transparency
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = WormaCeptorDesignSystem.Alpha.MEDIUM,
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
                                .background(
                                    MaterialTheme.colorScheme.surface.copy(alpha = WormaCeptorDesignSystem.Alpha.HEAVY),
                                ),
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
                                .background(
                                    MaterialTheme.colorScheme.errorContainer.copy(
                                        alpha = WormaCeptorDesignSystem.Alpha.MODERATE,
                                    ),
                                ),
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
                            color = MaterialTheme.colorScheme.surface.copy(
                                alpha = WormaCeptorDesignSystem.Alpha.PROMINENT,
                            ),
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
                    WormaCeptorDivider(style = DividerStyle.Subtle)

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
                                value = formatBytes(meta.fileSize),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                            MetadataItem(
                                icon = Icons.Outlined.ColorLens,
                                label = stringResource(R.string.viewer_image_color),
                                value = meta.colorSpace ?: if (meta.hasAlpha) "RGBA" else "RGB",
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
private fun MetadataItem(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
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
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.BOLD)
        },
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            if (isPrimary) {
                MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE)
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
