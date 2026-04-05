package com.azikar24.wormaceptor.feature.viewer.ui.components.pdf

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Suppress("LongParameterList")
@Composable
internal fun PdfTopControlBar(
    title: String,
    currentPage: Int,
    totalPages: Int,
    onClose: () -> Unit,
    onPageJump: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = darkColors.background.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.sm,
                    vertical = WormaCeptorTokens.Spacing.sm,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.viewer_pdf_close),
                    tint = darkColors.textPrimary,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = darkColors.textPrimary,
                    maxLines = 1,
                )
                Text(
                    text = stringResource(R.string.viewer_pdf_page_indicator, currentPage, totalPages),
                    style = MaterialTheme.typography.bodySmall,
                    color = darkColors.textPrimary.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
                    modifier = Modifier.clickable { onPageJump() },
                )
            }

            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = stringResource(R.string.viewer_pdf_download),
                    tint = darkColors.textPrimary,
                )
            }

            IconButton(onClick = onShare) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(R.string.viewer_pdf_share),
                    tint = darkColors.textPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun PdfTopControlBarPreview() {
    WormaCeptorTheme {
        PdfTopControlBar(
            title = "Annual Report 2025.pdf",
            currentPage = 3,
            totalPages = 12,
            onClose = {},
            onPageJump = {},
            onDownload = {},
            onShare = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun PdfTopControlBarSinglePagePreview() {
    WormaCeptorTheme {
        PdfTopControlBar(
            title = "Invoice",
            currentPage = 1,
            totalPages = 1,
            onClose = {},
            onPageJump = {},
            onDownload = {},
            onShare = {},
        )
    }
}
