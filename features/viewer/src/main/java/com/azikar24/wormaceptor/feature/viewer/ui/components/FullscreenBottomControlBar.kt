package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.ImageMetadata
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun FullscreenBottomControlBar(
    metadata: ImageMetadata?,
    onDownload: () -> Unit,
    onShare: () -> Unit,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = WormaCeptorTokens.Spacing.xxl),
        color = darkColors.background.copy(alpha = WormaCeptorTokens.Alpha.INTENSE),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
        ) {
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
                    Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))
                    MetadataChip(
                        icon = Icons.Outlined.Memory,
                        text = formatBytes(meta.fileSize),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))
                    Surface(
                        shape = WormaCeptorTokens.Shapes.chip,
                        color = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(alpha = WormaCeptorTokens.Alpha.SOFT),
                    ) {
                        Text(
                            text = meta.format,
                            style = MaterialTheme.typography.labelMedium,
                            color = WormaCeptorTokens.Colors.Viewer.onOverlay,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorTokens.Spacing.sm,
                                vertical = WormaCeptorTokens.Spacing.xs,
                            ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    WormaCeptorTokens.Spacing.md,
                    Alignment.CenterHorizontally,
                ),
            ) {
                ActionChip(
                    icon = Icons.Default.Download,
                    label = stringResource(R.string.viewer_image_save_to_gallery),
                    onClick = onDownload,
                )
                ActionChip(
                    icon = Icons.Default.Share,
                    label = stringResource(R.string.viewer_image_share),
                    onClick = onShare,
                )
            }
        }
    }
}

@Composable
private fun MetadataChip(
    icon: ImageVector,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
        )
    }
}

@Composable
private fun ActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = WormaCeptorTokens.Shapes.pill,
        color = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(alpha = WormaCeptorTokens.Alpha.SOFT),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.lg,
                vertical = WormaCeptorTokens.Spacing.md,
            ),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = WormaCeptorTokens.Colors.Viewer.onOverlay,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = WormaCeptorTokens.Colors.Viewer.onOverlay,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FullscreenBottomControlBarPreview() {
    WormaCeptorTheme {
        FullscreenBottomControlBar(
            metadata = ImageMetadata(
                width = 1920,
                height = 1080,
                format = "PNG",
                fileSize = 2_048_000L,
            ),
            onDownload = {},
            onShare = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FullscreenBottomControlBarNoMetadataPreview() {
    WormaCeptorTheme {
        FullscreenBottomControlBar(
            metadata = null,
            onDownload = {},
            onShare = {},
        )
    }
}
