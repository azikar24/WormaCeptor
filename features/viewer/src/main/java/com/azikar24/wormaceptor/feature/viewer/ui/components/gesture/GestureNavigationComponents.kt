package com.azikar24.wormaceptor.feature.viewer.ui.components.gesture

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.R
import kotlin.math.roundToInt

// =============================================================================
// ZOOM CONTROLS
// =============================================================================

/**
 * Floating zoom controls with reset, zoom in/out, and level indicator.
 * Designed for use with zoomable content like response bodies.
 */
@Composable
fun ZoomControls(
    currentZoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
    minZoom: Float = 0.5f,
    maxZoom: Float = 3f,
) {
    val canZoomIn = currentZoom < maxZoom
    val canZoomOut = currentZoom > minZoom
    val isZoomed = currentZoom != 1f

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.md),
        shadowElevation = WormaCeptorDesignSystem.Elevation.sm,
        border = androidx.compose.foundation.BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        ),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        ) {
            // Zoom in button
            IconButton(
                onClick = onZoomIn,
                enabled = canZoomIn,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.medium,
                    ),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = stringResource(R.string.viewer_image_zoom_in),
                    modifier = Modifier.size(20.dp),
                )
            }

            // Zoom level indicator
            ZoomLevelIndicator(
                zoom = currentZoom,
                minZoom = minZoom,
                maxZoom = maxZoom,
            )

            // Zoom out button
            IconButton(
                onClick = onZoomOut,
                enabled = canZoomOut,
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.medium,
                    ),
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.ZoomOut,
                    contentDescription = stringResource(R.string.viewer_image_zoom_out),
                    modifier = Modifier.size(20.dp),
                )
            }

            // Reset button (shown when zoomed)
            AnimatedVisibility(
                visible = isZoomed,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(36.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.tertiary,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = stringResource(R.string.viewer_gesture_reset_zoom),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}

/**
 * Visual zoom level indicator with progress bar
 */
@Composable
private fun ZoomLevelIndicator(
    zoom: Float,
    minZoom: Float,
    maxZoom: Float,
) {
    val normalizedZoom = ((zoom - minZoom) / (maxZoom - minZoom)).coerceIn(0f, 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs),
    ) {
        // Zoom percentage
        Text(
            text = "${(zoom * 100).roundToInt()}%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Progress bar
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(normalizedZoom)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
                    ),
            )
        }
    }
}
