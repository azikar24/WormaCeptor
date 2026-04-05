package com.azikar24.wormaceptor.feature.viewer.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.PdfMetadata
import com.azikar24.wormaceptor.feature.viewer.R
import java.io.File

@Composable
internal fun PdfPreviewSuccessContent(
    thumbnail: Bitmap,
    metadata: PdfMetadata,
    pdfData: ByteArray,
    tempFile: File?,
    onFullscreen: () -> Unit,
    onDownload: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(PdfPreviewDefaults.ThumbnailAspectRatio)
                .clip(
                    RoundedCornerShape(
                        topStart = WormaCeptorTokens.Radius.md,
                        topEnd = WormaCeptorTokens.Radius.md,
                    ),
                ),
        ) {
            Image(
                bitmap = thumbnail.asImageBitmap(),
                contentDescription = stringResource(R.string.viewer_pdf_preview),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PdfPreviewDefaults.ScrimHeight)
                    .align(Alignment.BottomCenter)
                    .background(
                        WormaCeptorTokens.semantic(darkTheme = true).background.copy(
                            alpha = WormaCeptorTokens.Alpha.MEDIUM,
                        ),
                    ),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(WormaCeptorTokens.Spacing.md),
                shape = WormaCeptorTokens.Shapes.chip,
                color = MaterialTheme.colorScheme.surface.copy(alpha = WormaCeptorTokens.Alpha.OPAQUE),
                shadowElevation = WormaCeptorTokens.Elevation.sm,
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.md,
                        vertical = WormaCeptorTokens.Spacing.sm,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = stringResource(R.string.viewer_pdf_page_count),
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = pluralStringResource(
                            R.plurals.viewer_pdf_page_count_label,
                            metadata.pageCount,
                            metadata.pageCount,
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = CircleShape,
                color = WormaCeptorTokens.semantic(darkTheme = true).background.copy(
                    alpha = WormaCeptorTokens.Alpha.INTENSE,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = stringResource(R.string.viewer_pdf_open_fullscreen),
                    modifier = Modifier
                        .padding(WormaCeptorTokens.Spacing.md)
                        .size(PdfPreviewDefaults.FullscreenIconSize),
                    tint = WormaCeptorTokens.semantic(darkTheme = false).background,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            Text(
                text = metadata.title ?: stringResource(R.string.viewer_pdf_document),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
            ) {
                PdfMetadataChip(
                    icon = Icons.Default.Description,
                    text = formatBytes(metadata.fileSize),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                metadata.version.takeIf { it.isNotEmpty() }?.let { version ->
                    PdfMetadataChip(
                        icon = null,
                        text = stringResource(R.string.viewer_pdf_version, version),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
                metadata.author?.let { author ->
                    PdfMetadataChip(
                        icon = Icons.Default.Person,
                        text = author,
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorButton(
                    text = stringResource(R.string.viewer_pdf_open),
                    onClick = onFullscreen,
                    modifier = Modifier.weight(1f),
                    variant = ButtonVariant.Primary,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = null,
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                        )
                    },
                )

                WormaCeptorButton(
                    text = stringResource(R.string.viewer_pdf_download),
                    onClick = onDownload,
                    variant = ButtonVariant.Outlined,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                        )
                    },
                )

                WormaCeptorButton(
                    text = stringResource(R.string.viewer_pdf_share),
                    onClick = { sharePdf(context, pdfData, tempFile)?.let { onShowMessage(it) } },
                    variant = ButtonVariant.Outlined,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                        )
                    },
                )
            }
        }
    }
}

@Composable
internal fun PdfMetadataChip(
    icon: ImageVector?,
    text: String,
    tint: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
    ) {
        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = text,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                tint = tint.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PdfMetadataChipPreview() {
    WormaCeptorTheme {
        PdfMetadataChip(
            icon = Icons.Default.Description,
            text = "2.1 MB",
            tint = MaterialTheme.colorScheme.tertiary,
        )
    }
}
