package com.azikar24.wormaceptor.feature.location.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.feature.location.R
import com.azikar24.wormaceptor.feature.location.ui.components.LocationMapCard
import org.osmdroid.util.GeoPoint

@Composable
internal fun PresetItem(
    preset: LocationPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val selectedAccent = WormaCeptorTokens.Colors.Location.enabled

    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        style = CardStyle.Outlined,
        backgroundColor = if (isSelected) selectedAccent.copy(alpha = TokenAlpha.SUBTLE) else null,
        borderColor = if (isSelected) selectedAccent else null,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PresetIcon(isBuiltIn = preset.isBuiltIn, name = preset.name)

            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                        Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.xs))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.location_selected),
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                            tint = selectedAccent,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxs))
                Text(
                    text = preset.location.formatCoordinates(),
                    style = MaterialTheme.typography.bodySmall,
                    color = WormaCeptorTokens.Colors.Location.coordinate,
                )
            }

            if (onDelete != null) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDelete()
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.location_delete_preset),
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetIcon(
    isBuiltIn: Boolean,
    name: String,
) {
    val accent = if (isBuiltIn) {
        WormaCeptorTokens.Colors.Location.builtInPreset
    } else {
        WormaCeptorTokens.Colors.Location.userPreset
    }
    Surface(
        shape = WormaCeptorTokens.Shapes.button,
        color = accent.copy(alpha = TokenAlpha.SUBTLE),
        modifier = Modifier.size(WormaCeptorTokens.TouchTarget.minimum),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = name,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                tint = accent,
            )
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
    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.cardLarge,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Toggle header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggle)
                    .padding(WormaCeptorTokens.Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = stringResource(R.string.location_map_preview),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                    )
                    Text(
                        text = stringResource(R.string.location_map_preview),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (isMockActive) {
                        Surface(
                            shape = WormaCeptorTokens.Shapes.chip,
                            color = WormaCeptorTokens.Colors.Location.enabled.copy(
                                alpha = WormaCeptorTokens.Alpha.SOFT,
                            ),
                        ) {
                            Text(
                                text = stringResource(R.string.location_map_live),
                                modifier = Modifier.padding(
                                    horizontal = WormaCeptorTokens.Spacing.sm,
                                    vertical = WormaCeptorTokens.Spacing.xxs,
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = WormaCeptorTokens.Colors.Location.enabled,
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
                            start = WormaCeptorTokens.Spacing.lg,
                            end = WormaCeptorTokens.Spacing.lg,
                            bottom = WormaCeptorTokens.Spacing.lg,
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
            .padding(vertical = WormaCeptorTokens.Spacing.xxl),
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
