package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import kotlinx.collections.immutable.ImmutableMap

/**
 * Modern filter bottom sheet content with distribution bars
 * Inspired by MetricsCard visual style
 */
@Composable
fun FilterBottomSheetContent(
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    filterMethod: String?,
    filterStatusRange: IntRange?,
    onMethodFilterChanged: (String?) -> Unit,
    onStatusFilterChanged: (IntRange?) -> Unit,
    onClearFilters: () -> Unit,
    onApply: () -> Unit,
    filteredCount: Int,
    totalCount: Int,
    methodCounts: ImmutableMap<String, Int>,
    statusCounts: ImmutableMap<IntRange, Int>,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val filtersActive = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        // Compact Header
        FilterHeader(
            filteredCount = filteredCount,
            totalCount = totalCount,
            filtersActive = filtersActive,
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        )

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Search Field
            MinimalSearchField(
                value = searchQuery,
                onValueChange = onSearchChanged,
                onSearch = { focusManager.clearFocus() },
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

            // HTTP Methods Section
            FilterSectionHeader(
                icon = Icons.Outlined.Code,
                title = stringResource(R.string.viewer_filter_http_method),
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            MethodFilterBars(
                methodCounts = methodCounts,
                selectedMethod = filterMethod,
                onMethodSelected = onMethodFilterChanged,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

            // Status Codes Section
            FilterSectionHeader(
                icon = Icons.Outlined.DataUsage,
                title = stringResource(R.string.viewer_filter_status_code),
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            StatusFilterBars(
                statusCounts = statusCounts,
                selectedRange = filterStatusRange,
                onStatusSelected = onStatusFilterChanged,
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
        }

        // Action Buttons - Fixed at bottom
        FilterActionButtons(
            filtersActive = filtersActive,
            onClearAll = {
                onClearFilters()
                onSearchChanged("")
            },
            onApply = {
                focusManager.clearFocus()
                onApply()
            },
        )
    }
}

@Composable
private fun FilterHeader(filteredCount: Int, totalCount: Int, filtersActive: Boolean) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.md,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = stringResource(R.string.viewer_filter_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (filtersActive) {
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                Text(
                    text = stringResource(R.string.viewer_filter_results_count, filteredCount, totalCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // Result count badge with LiveRegion for accessibility announcements
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
            color = if (filtersActive) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = context.getString(
                    R.string.viewer_filter_results_description,
                    filteredCount,
                    totalCount,
                )
            },
        ) {
            Text(
                text = filteredCount.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (filtersActive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.lg,
                    vertical = WormaCeptorDesignSystem.Spacing.sm,
                ),
            )
        }
    }
}

@Composable
private fun MinimalSearchField(value: String, onValueChange: (String) -> Unit, onSearch: () -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md)),
        placeholder = {
            Text(
                text = stringResource(R.string.viewer_filter_search_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.viewer_filter_search),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp),
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.viewer_filter_clear),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

@Composable
private fun FilterSectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MethodFilterBars(
    methodCounts: ImmutableMap<String, Int>,
    selectedMethod: String?,
    onMethodSelected: (String?) -> Unit,
) {
    val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

    Column(
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        methods.chunked(2).forEach { rowMethods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                rowMethods.forEach { method ->
                    val count = methodCounts[method] ?: 0
                    val color = methodColor(method)
                    val isSelected = selectedMethod == method

                    GridFilterCard(
                        label = method,
                        count = count,
                        color = color,
                        isSelected = isSelected,
                        onClick = {
                            onMethodSelected(if (isSelected) null else method)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                // Fill empty space if odd number of items
                if (rowMethods.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatusFilterBars(
    statusCounts: ImmutableMap<IntRange, Int>,
    selectedRange: IntRange?,
    onStatusSelected: (IntRange?) -> Unit,
) {
    val statusFilters = listOf(
        Triple("2xx", 200..299, WormaCeptorColors.StatusGreen),
        Triple("3xx", 300..399, WormaCeptorColors.StatusBlue),
        Triple("4xx", 400..499, WormaCeptorColors.StatusAmber),
        Triple("5xx", 500..599, WormaCeptorColors.StatusRed),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        statusFilters.chunked(2).forEach { rowFilters ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                rowFilters.forEach { (label, range, color) ->
                    val count = statusCounts[range] ?: 0
                    val isSelected = selectedRange == range
                    val sublabelText = when (label) {
                        "2xx" -> stringResource(R.string.viewer_filter_success)
                        "3xx" -> stringResource(R.string.viewer_filter_redirect)
                        "4xx" -> stringResource(R.string.viewer_filter_client_error)
                        "5xx" -> stringResource(R.string.viewer_filter_server_error)
                        else -> null
                    }

                    GridFilterCard(
                        label = label,
                        sublabel = sublabelText,
                        count = count,
                        color = color,
                        isSelected = isSelected,
                        onClick = {
                            onStatusSelected(if (isSelected) null else range)
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun GridFilterCard(
    label: String,
    count: Int,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sublabel: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale_animation",
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            color.copy(alpha = WormaCeptorDesignSystem.Alpha.light)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "bg_animation",
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            color.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f)
        },
        animationSpec = tween(durationMillis = 200),
        label = "border_animation",
    )

    val context = LocalContext.current
    val stateDesc = when {
        count == 0 -> context.getString(R.string.viewer_filter_no_items)
        isSelected -> context.getString(R.string.viewer_filter_selected, count)
        else -> context.getString(R.string.viewer_filter_items_count, count)
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
            .background(backgroundColor)
            .border(
                width = if (isSelected) WormaCeptorDesignSystem.BorderWidth.regular else WormaCeptorDesignSystem.BorderWidth.thin,
                color = borderColor,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                enabled = count > 0,
            )
            .semantics {
                role = Role.Button
                selected = isSelected
                stateDescription = stateDesc
                val actionText = if (count > 0) {
                    context.getString(
                        if (isSelected) R.string.viewer_filter_action_deselect else R.string.viewer_filter_action_select,
                    )
                } else {
                    context.getString(R.string.viewer_filter_disabled)
                }
                contentDescription = "$label filter. $stateDesc. $actionText"
            }
            .padding(WormaCeptorDesignSystem.Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left side: dot, label, sublabel
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                modifier = Modifier.weight(1f),
            ) {
                // Color indicator dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (count > 0) color else color.copy(alpha = 0.3f)),
                )

                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (count > 0) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        },
                    )
                    if (sublabel != null) {
                        Text(
                            text = sublabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (count > 0) 0.6f else 0.3f,
                            ),
                        )
                    }
                }
            }

            // Right side: count and checkmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (count > 0) color else color.copy(alpha = 0.3f),
                )

                // Selection indicator
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.viewer_filter_selected_indicator),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(10.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterActionButtons(filtersActive: Boolean, onClearAll: () -> Unit, onApply: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = WormaCeptorDesignSystem.Elevation.sm,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            OutlinedButton(
                onClick = onClearAll,
                modifier = Modifier.weight(1f),
                enabled = filtersActive,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Text(
                    text = stringResource(R.string.viewer_filter_clear_all),
                    fontWeight = FontWeight.Medium,
                )
            }

            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
            ) {
                Text(
                    text = stringResource(R.string.viewer_filter_done),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

private fun methodColor(method: String): Color = when (method.uppercase()) {
    "GET" -> WormaCeptorColors.StatusGreen
    "POST" -> WormaCeptorColors.StatusBlue
    "PUT" -> WormaCeptorColors.StatusAmber
    "DELETE" -> WormaCeptorColors.StatusRed
    "PATCH" -> Color(0xFF9C27B0)
    else -> WormaCeptorColors.StatusGrey
}
