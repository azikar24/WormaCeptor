package com.azikar24.wormaceptor.feature.location.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.location.R
import org.osmdroid.util.GeoPoint

/**
 * Map content with legend, map view, and footer hints.
 * Designed to be placed inside a collapsible container.
 *
 * @param realLocation The real device location
 * @param mockLocation The mock location
 * @param isMockActive Whether mock location is currently active
 * @param onMapTap Callback when the map is tapped
 * @param modifier Modifier for the composable
 */
@Composable
fun LocationMapCard(
    realLocation: GeoPoint?,
    mockLocation: GeoPoint?,
    isMockActive: Boolean,
    onMapTap: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Legend row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LegendItem(
                color = WormaCeptorTokens.Colors.Location.builtInPreset,
                label = stringResource(R.string.location_real_location),
            )
            LegendItem(
                color = WormaCeptorTokens.Colors.Location.enabled,
                label = stringResource(R.string.location_mock_location),
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        // Map view
        LocationMapView(
            realLocation = realLocation,
            mockLocation = mockLocation,
            isMockActive = isMockActive,
            onMapTap = onMapTap,
            modifier = Modifier
                .fillMaxWidth()
                .height(WormaCeptorTokens.ComponentSize.chartHeight),
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        // Footer with tap hint and distance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Tap hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.location_tap_to_set),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Distance indicator (only show when both locations are available)
            AnimatedVisibility(
                visible = realLocation != null && mockLocation != null,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                if (realLocation != null && mockLocation != null) {
                    val distance = calculateDistance(realLocation, mockLocation)
                    Surface(
                        shape = WormaCeptorTokens.Shapes.chip,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ) {
                        Text(
                            text = stringResource(R.string.location_distance, formatDistance(distance)),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorTokens.Spacing.sm,
                                vertical = WormaCeptorTokens.Spacing.xxs,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: androidx.compose.ui.graphics.Color,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(WormaCeptorTokens.IconSize.xxs)
                .clip(CircleShape)
                .background(color),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
