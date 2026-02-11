package com.azikar24.wormaceptor.feature.location.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.feature.location.R
import com.azikar24.wormaceptor.feature.location.ui.components.LocationMapCard
import com.azikar24.wormaceptor.feature.location.ui.theme.LocationColors
import org.osmdroid.util.GeoPoint

@Composable
internal fun PresetItem(
    preset: LocationPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "presetItemScale",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
            .border(
                width = if (isSelected) WormaCeptorDesignSystem.BorderWidth.thick else WormaCeptorDesignSystem.BorderWidth.regular,
                color = if (isSelected) {
                    LocationColors.enabled
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)
                },
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
            .background(
                color = if (isSelected) {
                    LocationColors.enabled.asSubtleBackground()
                } else {
                    MaterialTheme.colorScheme.surface
                },
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(WormaCeptorDesignSystem.Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Location icon
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
            color = if (preset.isBuiltIn) {
                LocationColors.builtIn.asSubtleBackground()
            } else {
                LocationColors.userPreset.asSubtleBackground()
            },
            modifier = Modifier.size(WormaCeptorDesignSystem.TouchTarget.minimum),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = preset.name,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                    tint = if (preset.isBuiltIn) {
                        LocationColors.builtIn
                    } else {
                        LocationColors.userPreset
                    },
                )
            }
        }

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.location_selected),
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                        tint = LocationColors.enabled,
                    )
                }
            }
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
            Text(
                text = preset.location.formatCoordinates(),
                style = MaterialTheme.typography.bodySmall,
                color = LocationColors.coordinate,
            )
        }

        // Delete button for user presets
        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.location_delete_preset),
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
internal fun CollapsibleMapSection(
    isExpanded: Boolean,
    onToggle: () -> Unit,
    realLocation: GeoPoint?,
    mockLocation: GeoPoint?,
    isMockActive: Boolean,
    onMapTap: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
        ),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Toggle header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(WormaCeptorDesignSystem.Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = stringResource(R.string.location_map_preview),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                    )
                    Text(
                        text = stringResource(R.string.location_map_preview),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isMockActive) {
                        Surface(
                            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                            color = LocationColors.enabled.copy(alpha = WormaCeptorDesignSystem.Alpha.soft),
                        ) {
                            Text(
                                text = stringResource(R.string.location_map_live),
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = WormaCeptorDesignSystem.Spacing.xxs,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LocationColors.enabled,
                            )
                        }
                    }
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(
                        if (isExpanded) R.string.location_collapse else R.string.location_expand,
                    ),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Collapsible map content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = WormaCeptorDesignSystem.Spacing.lg,
                            end = WormaCeptorDesignSystem.Spacing.lg,
                            bottom = WormaCeptorDesignSystem.Spacing.lg,
                        ),
                ) {
                    LocationMapCard(
                        realLocation = realLocation,
                        mockLocation = mockLocation,
                        isMockActive = isMockActive,
                        onMapTap = onMapTap,
                    )
                }
            }
        }
    }
}

@Composable
internal fun EmptyPresetsState(hasSearchQuery: Boolean) {
    WormaCeptorEmptyState(
        title = stringResource(if (hasSearchQuery) R.string.location_no_matches else R.string.location_no_presets),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorDesignSystem.Spacing.xxl),
        subtitle = stringResource(
            if (hasSearchQuery) {
                R.string.location_try_different_search
            } else {
                R.string.location_save_for_quick_access
            },
        ),
        icon = Icons.Default.Place,
    )
}
