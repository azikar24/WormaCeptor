package com.azikar24.wormaceptor.feature.viewer.ui.components.pdf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Suppress("LongParameterList", "LongMethod")
@Composable
internal fun PdfBottomNavigationBar(
    currentPage: Int,
    totalPages: Int,
    showThumbnails: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onFirstPage: () -> Unit,
    onLastPage: () -> Unit,
    onToggleThumbnails: () -> Unit,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    val hasPrevious = currentPage > 0
    val hasNext = currentPage < totalPages - 1

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = WormaCeptorTokens.Spacing.xxl),
        color = darkColors.background.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.md,
                    vertical = WormaCeptorTokens.Spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            WormaCeptorIconButton(onClick = onFirstPage, enabled = hasPrevious) {
                Icon(
                    imageVector = Icons.Default.FirstPage,
                    contentDescription = stringResource(R.string.viewer_pdf_first_page),
                    tint = darkColors.textPrimary.copy(
                        alpha = if (hasPrevious) 1f else WormaCeptorTokens.Alpha.MODERATE,
                    ),
                )
            }

            WormaCeptorIconButton(onClick = onPreviousPage, enabled = hasPrevious) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = stringResource(R.string.viewer_pdf_previous_page),
                    tint = darkColors.textPrimary.copy(
                        alpha = if (hasPrevious) 1f else WormaCeptorTokens.Alpha.MODERATE,
                    ),
                )
            }

            Surface(
                shape = WormaCeptorTokens.Shapes.card,
                color = darkColors.textPrimary.copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
            ) {
                Text(
                    text = "${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = darkColors.textPrimary,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.lg,
                        vertical = WormaCeptorTokens.Spacing.sm,
                    ),
                )
            }

            WormaCeptorIconButton(onClick = onNextPage, enabled = hasNext) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(R.string.viewer_pdf_next_page),
                    tint = darkColors.textPrimary.copy(
                        alpha = if (hasNext) 1f else WormaCeptorTokens.Alpha.MODERATE,
                    ),
                )
            }

            WormaCeptorIconButton(onClick = onLastPage, enabled = hasNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.LastPage,
                    contentDescription = stringResource(R.string.viewer_pdf_last_page),
                    tint = darkColors.textPrimary.copy(
                        alpha = if (hasNext) 1f else WormaCeptorTokens.Alpha.MODERATE,
                    ),
                )
            }

            WormaCeptorIconButton(onClick = onToggleThumbnails) {
                Icon(
                    imageVector = if (showThumbnails) Icons.Default.GridOff else Icons.Default.GridOn,
                    contentDescription = if (showThumbnails) {
                        stringResource(R.string.viewer_pdf_hide_thumbnails)
                    } else {
                        stringResource(R.string.viewer_pdf_show_thumbnails)
                    },
                    tint = if (showThumbnails) WormaCeptorTokens.semantic().accent else darkColors.textPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun PdfBottomNavigationBarMiddlePagePreview() {
    WormaCeptorTheme {
        PdfBottomNavigationBar(
            currentPage = 4,
            totalPages = 10,
            showThumbnails = false,
            onPreviousPage = {},
            onNextPage = {},
            onFirstPage = {},
            onLastPage = {},
            onToggleThumbnails = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun PdfBottomNavigationBarFirstPagePreview() {
    WormaCeptorTheme {
        PdfBottomNavigationBar(
            currentPage = 0,
            totalPages = 10,
            showThumbnails = false,
            onPreviousPage = {},
            onNextPage = {},
            onFirstPage = {},
            onLastPage = {},
            onToggleThumbnails = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun PdfBottomNavigationBarLastPagePreview() {
    WormaCeptorTheme {
        PdfBottomNavigationBar(
            currentPage = 9,
            totalPages = 10,
            showThumbnails = true,
            onPreviousPage = {},
            onNextPage = {},
            onFirstPage = {},
            onLastPage = {},
            onToggleThumbnails = {},
        )
    }
}
