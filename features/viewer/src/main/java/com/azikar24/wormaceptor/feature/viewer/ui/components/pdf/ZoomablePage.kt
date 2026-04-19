package com.azikar24.wormaceptor.feature.viewer.ui.components.pdf

import android.graphics.Bitmap
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.createBitmap
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Suppress("LongMethod")
@Composable
internal fun ZoomablePage(
    bitmap: Bitmap?,
    pageNumber: Int,
    onTap: () -> Unit,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onDoubleTap = { tapOffset ->
                        if (scale > 1.5f) {
                            scale = 1f
                            offset = Offset.Zero
                        } else {
                            val targetScale = 2.5f
                            scale = targetScale
                            offset = Offset(
                                x = (size.width / 2f - tapOffset.x) * (targetScale - 1),
                                y = (size.height / 2f - tapOffset.y) * (targetScale - 1),
                            )
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(MinScale, MaxScale)

                    if (scale > 1f) {
                        val maxX = size.width * (scale - 1) / 2
                        val maxY = size.height * (scale - 1) / 2
                        offset = Offset(
                            x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                            y = (offset.y + pan.y).coerceIn(-maxY, maxY),
                        )
                    } else {
                        offset = Offset.Zero
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.viewer_pdf_page_description, pageNumber),
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationX = offset.x
                        translationY = offset.y
                    },
                contentScale = ContentScale.Fit,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(darkColors.surface),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = darkColors.textPrimary.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.xl),
                )
            }
        }
    }
}

private const val MinScale = 1f
private const val MaxScale = 5f

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun ZoomablePageLoadingPreview() {
    WormaCeptorTheme {
        ZoomablePage(
            bitmap = null,
            pageNumber = 1,
            onTap = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun ZoomablePageWithContentPreview() {
    val bitmap = createBitmap(400, 600).apply {
        eraseColor(android.graphics.Color.WHITE)
    }
    WormaCeptorTheme {
        ZoomablePage(
            bitmap = bitmap,
            pageNumber = 3,
            onTap = {},
        )
    }
}
