package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorFilledTonalIconButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun FullscreenTopControlBar(
    onClose: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    currentZoom: Float,
) {
    val darkColors = WormaCeptorTokens.semantic(darkTheme = true)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = darkColors.background.copy(alpha = WormaCeptorTokens.Alpha.STRONG),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md)
                .windowInsetsPadding(WindowInsets.statusBars),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WormaCeptorFilledTonalIconButton(
                onClick = onClose,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(
                        alpha = WormaCeptorTokens.Alpha.SOFT,
                    ),
                    contentColor = WormaCeptorTokens.Colors.Viewer.onOverlay,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.viewer_image_close),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WormaCeptorFilledTonalIconButton(
                    onClick = onZoomOut,
                    enabled = currentZoom > 1f,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(
                            alpha = WormaCeptorTokens.Alpha.SOFT,
                        ),
                        contentColor = WormaCeptorTokens.Colors.Viewer.onOverlay,
                        disabledContainerColor = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(
                            alpha = WormaCeptorTokens.Alpha.HINT,
                        ),
                        disabledContentColor = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(
                            alpha = WormaCeptorTokens.Alpha.MODERATE,
                        ),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = stringResource(R.string.viewer_image_zoom_out),
                    )
                }

                WormaCeptorFilledTonalIconButton(
                    onClick = onZoomIn,
                    enabled = currentZoom < FullscreenImageDefaults.MAX_ZOOM,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(
                            alpha = WormaCeptorTokens.Alpha.SOFT,
                        ),
                        contentColor = WormaCeptorTokens.Colors.Viewer.onOverlay,
                        disabledContainerColor = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(
                            alpha = WormaCeptorTokens.Alpha.HINT,
                        ),
                        disabledContentColor = WormaCeptorTokens.Colors.Viewer.onOverlay.copy(
                            alpha = WormaCeptorTokens.Alpha.MODERATE,
                        ),
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = stringResource(R.string.viewer_image_zoom_in),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FullscreenTopControlBarPreview() {
    WormaCeptorTheme {
        FullscreenTopControlBar(
            onClose = {},
            onZoomIn = {},
            onZoomOut = {},
            currentZoom = 1f,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FullscreenTopControlBarZoomedPreview() {
    WormaCeptorTheme {
        FullscreenTopControlBar(
            onClose = {},
            onZoomIn = {},
            onZoomOut = {},
            currentZoom = 2.5f,
        )
    }
}
