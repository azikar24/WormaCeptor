/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.quickactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem

/**
 * Quick filter bar with horizontally scrolling filter chips.
 *
 * Design: Minimal chips with subtle backgrounds that become filled when selected.
 * Icons provide visual cues for each filter type.
 * Clear button appears when filters are active.
 */
@Composable
fun QuickFilterBar(
    activeFilters: Set<QuickFilter>,
    onFilterToggle: (QuickFilter) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = activeFilters.isNotEmpty()

    AnimatedVisibility(
        visible = true,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = WormaCeptorDesignSystem.Elevation.xs
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Filter chips scrollable area
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    contentPadding = PaddingValues(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                ) {
                    items(QuickFilter.entries.toList()) { filter ->
                        QuickFilterChip(
                            filter = filter,
                            isSelected = filter in activeFilters,
                            onClick = { onFilterToggle(filter) }
                        )
                    }
                }

                // Clear all button (only visible when filters active)
                AnimatedVisibility(
                    visible = hasActiveFilters,
                    enter = fadeIn(tween(150)),
                    exit = fadeOut(tween(100))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = WormaCeptorDesignSystem.Spacing.md)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .size(20.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        )
                        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                        ClearFiltersButton(onClick = onClearAll)
                    }
                }
            }
        }
    }
}

/**
 * Individual quick filter chip with icon and animation.
 */
@Composable
fun QuickFilterChip(
    filter: QuickFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "chipScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            filterColor(filter).copy(alpha = WormaCeptorDesignSystem.Alpha.light)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(200),
        label = "chipBg"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            filterColor(filter)
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(200),
        label = "chipContent"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            filterColor(filter).copy(alpha = 0.5f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(200),
        label = "chipBorder"
    )

    Surface(
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
        color = backgroundColor,
        modifier = modifier
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = WormaCeptorDesignSystem.BorderWidth.thin,
                        color = borderColor,
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill)
                    )
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.md,
                vertical = WormaCeptorDesignSystem.Spacing.sm
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)
        ) {
            // Leading icon (checkmark when selected, filter icon otherwise)
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            } else {
                Icon(
                    imageVector = filterIcon(filter),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor.copy(alpha = 0.7f)
                )
            }

            Text(
                text = filter.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

/**
 * Material3 FilterChip variant for consistency with system design.
 */
@Composable
fun MaterialQuickFilterChip(
    filter: QuickFilter,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = filterColor(filter)

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = filter.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            {
                Icon(
                    imageVector = filterIcon(filter),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
            selectedLabelColor = color,
            selectedLeadingIconColor = color
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = Color.Transparent,
            selectedBorderColor = color.copy(alpha = 0.4f),
            borderWidth = WormaCeptorDesignSystem.BorderWidth.thin,
            selectedBorderWidth = WormaCeptorDesignSystem.BorderWidth.regular,
            enabled = true,
            selected = isSelected
        ),
        modifier = modifier
    )
}

/**
 * Compact clear filters button.
 */
@Composable
private fun ClearFiltersButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "clearScale"
    )

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
        modifier = modifier
            .size(28.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.xs)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear all filters",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

/**
 * Expanded filter bar with descriptions and counts.
 */
@Composable
fun ExpandedQuickFilterBar(
    activeFilters: Set<QuickFilter>,
    filterCounts: Map<QuickFilter, Int>,
    onFilterToggle: (QuickFilter) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = WormaCeptorDesignSystem.Elevation.xs,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md)
        ) {
            // Header with active count and clear button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
                ) {
                    Text(
                        text = "Quick Filters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (activeFilters.isNotEmpty()) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = activeFilters.size.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(
                                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                    vertical = WormaCeptorDesignSystem.Spacing.xxs
                                )
                            )
                        }
                    }
                }

                if (activeFilters.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm))
                            .clickable(onClick = onClearAll)
                    ) {
                        Text(
                            text = "Clear all",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                vertical = WormaCeptorDesignSystem.Spacing.xs
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.md))

            // Filter chips grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
            ) {
                QuickFilter.entries.forEach { filter ->
                    ExpandedFilterChip(
                        filter = filter,
                        isSelected = filter in activeFilters,
                        count = filterCounts[filter] ?: 0,
                        onClick = { onFilterToggle(filter) }
                    )
                }
            }
        }
    }
}

/**
 * Expanded filter chip showing count.
 */
@Composable
private fun ExpandedFilterChip(
    filter: QuickFilter,
    isSelected: Boolean,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val color = filterColor(filter)

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "expandedChipScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            color.copy(alpha = WormaCeptorDesignSystem.Alpha.light)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        },
        animationSpec = tween(200),
        label = "expandedChipBg"
    )

    Surface(
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
        color = backgroundColor,
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                enabled = count > 0
            )
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.md,
                vertical = WormaCeptorDesignSystem.Spacing.sm
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.Check else filterIcon(filter),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) color else color.copy(alpha = if (count > 0) 0.7f else 0.3f)
            )

            Column {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (count > 0) {
                        if (isSelected) color else MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )

                Text(
                    text = "$count matches",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (count > 0) 0.6f else 0.3f
                    )
                )
            }
        }
    }
}

/**
 * Get the icon for each filter type.
 */
private fun filterIcon(filter: QuickFilter): ImageVector = when (filter) {
    QuickFilter.ERRORS -> Icons.Outlined.Error
    QuickFilter.SLOW -> Icons.Outlined.Schedule
    QuickFilter.LARGE -> Icons.Outlined.DataUsage
    QuickFilter.TODAY -> Icons.Outlined.Today
    QuickFilter.JSON -> Icons.Outlined.DataUsage
    QuickFilter.IMAGES -> Icons.Outlined.Image
}

/**
 * Get the accent color for each filter type.
 * Uses WormaCeptorColors for design system consistency.
 */
private fun filterColor(filter: QuickFilter): Color = when (filter) {
    QuickFilter.ERRORS -> WormaCeptorColors.StatusRed
    QuickFilter.SLOW -> WormaCeptorColors.StatusAmber
    QuickFilter.LARGE -> WormaCeptorColors.StatusBlue
    QuickFilter.TODAY -> WormaCeptorColors.StatusGreen
    QuickFilter.JSON -> WormaCeptorColors.ContentPurple
    QuickFilter.IMAGES -> WormaCeptorColors.ContentCyan
}
