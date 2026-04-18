package com.azikar24.wormaceptor.feature.viewer.ui.components.pdf

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun PdfThumbnailStrip(
    pages: List<Bitmap>,
    currentPage: Int,
    listState: LazyListState,
    onPageSelect: (Int) -> Unit,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = darkColors.background.copy(alpha = WormaCeptorTokens.Alpha.PROMINENT),
        shape = RoundedCornerShape(
            topStart = WormaCeptorTokens.Radius.lg,
            topEnd = WormaCeptorTokens.Radius.lg,
        ),
    ) {
        LazyRow(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.md),
        ) {
            itemsIndexed(pages) { index, bitmap ->
                ThumbnailItem(
                    bitmap = bitmap,
                    pageNumber = index + 1,
                    isSelected = index == currentPage,
                    onClick = { onPageSelect(index) },
                )
            }
        }
    }
}

@Composable
private fun ThumbnailItem(
    bitmap: Bitmap,
    pageNumber: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) WormaCeptorTokens.semantic().accent else Color.Transparent,
        animationSpec = tween(WormaCeptorTokens.Animation.FAST),
        label = "border",
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() },
    ) {
        Box(
            modifier = Modifier
                .width(thumbnailWidth)
                .aspectRatio(ThumbnailAspectRatio)
                .clip(WormaCeptorTokens.Shapes.button)
                .border(
                    width = WormaCeptorTokens.BorderWidth.thick,
                    color = borderColor,
                    shape = WormaCeptorTokens.Shapes.button,
                ),
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.viewer_pdf_page_description, pageNumber),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

        Text(
            text = "$pageNumber",
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) {
                WormaCeptorTokens.semantic().accent
            } else {
                darkColors.textPrimary.copy(alpha = WormaCeptorTokens.Alpha.HEAVY)
            },
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

private val thumbnailWidth = 60.dp
private const val ThumbnailAspectRatio = 0.75f

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun PdfThumbnailStripPreview() {
    val pages = List(5) {
        createBitmap(200, 280).apply {
            eraseColor(android.graphics.Color.WHITE)
        }
    }
    WormaCeptorTheme {
        PdfThumbnailStrip(
            pages = pages,
            currentPage = 2,
            listState = LazyListState(),
            onPageSelect = {},
        )
    }
}
