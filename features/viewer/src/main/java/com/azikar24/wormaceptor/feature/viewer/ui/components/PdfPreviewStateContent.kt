package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.PdfMetadata
import com.azikar24.wormaceptor.feature.viewer.R
import java.io.File

@Composable
internal fun PdfPreviewLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PdfPreviewDefaults.LoadingHeight),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(PdfPreviewDefaults.StateIconSize),
                strokeWidth = WormaCeptorTokens.BorderWidth.bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.viewer_pdf_rendering),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun PdfPreviewErrorContent(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PdfPreviewDefaults.ErrorHeight)
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = stringResource(R.string.viewer_pdf_error_loading),
                modifier = Modifier.size(PdfPreviewDefaults.StateIconSize),
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = stringResource(R.string.viewer_pdf_load_failed),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    }
}

@Composable
internal fun PdfPreviewPasswordContent(
    metadata: PdfMetadata,
    pdfData: ByteArray,
    tempFile: File?,
    onDownload: () -> Unit,
    onShowMessage: (String) -> Unit,
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WormaCeptorTokens.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
            modifier = Modifier.size(PdfPreviewDefaults.LockContainerSize),
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = stringResource(R.string.viewer_pdf_password_protected),
                    modifier = Modifier.size(PdfPreviewDefaults.LockIconSize),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
        ) {
            Text(
                text = stringResource(R.string.viewer_pdf_password_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.viewer_pdf_password_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PdfMetadataChip(
            icon = Icons.Default.Description,
            text = formatBytes(metadata.fileSize),
            tint = MaterialTheme.colorScheme.tertiary,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
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

@Preview(showBackground = true)
@Composable
private fun PdfPreviewLoadingContentPreview() {
    WormaCeptorTheme {
        PdfPreviewLoadingContent()
    }
}

@Preview(showBackground = true)
@Composable
private fun PdfPreviewErrorContentPreview() {
    WormaCeptorTheme {
        PdfPreviewErrorContent(message = "Unable to parse PDF structure")
    }
}

@Preview(showBackground = true)
@Composable
private fun PdfPreviewPasswordContentPreview() {
    WormaCeptorTheme {
        PdfPreviewPasswordContent(
            metadata = PdfMetadata(
                pageCount = 0,
                fileSize = 1_024_000L,
                version = "1.7",
                isEncrypted = true,
            ),
            pdfData = byteArrayOf(),
            tempFile = null,
            onDownload = {},
            onShowMessage = {},
        )
    }
}
