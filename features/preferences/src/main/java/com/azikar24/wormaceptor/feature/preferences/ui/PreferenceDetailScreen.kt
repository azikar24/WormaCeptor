package com.azikar24.wormaceptor.feature.preferences.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.feature.preferences.R
import com.azikar24.wormaceptor.feature.preferences.ui.theme.PreferencesDesignSystem
import kotlinx.collections.immutable.ImmutableList

/**
 * Screen displaying key-value pairs for a specific SharedPreferences file.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Suppress("LongMethod", "LongParameterList")
@Composable
fun PreferenceDetailScreen(
    fileName: String,
    items: ImmutableList<PreferenceItem>,
    totalCount: Int,
    searchQuery: String,
    typeFilter: String?,
    availableTypes: ImmutableList<String>,
    onSearchQueryChanged: (String) -> Unit,
    onTypeFilterChanged: (String?) -> Unit,
    onBack: () -> Unit,
    onEditItem: (PreferenceItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    onClearAll: () -> Unit,
    onCreateItem: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf<String?>(null) }
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = fileName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        val itemCountLabel = if (totalCount == 1) {
                            stringResource(R.string.preferences_item_count_singular)
                        } else {
                            stringResource(R.string.preferences_item_count_plural)
                        }
                        Text(
                            text = "$totalCount $itemCountLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.preferences_back),
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            searchActive = !searchActive
                            if (!searchActive) onSearchQueryChanged("")
                        },
                    ) {
                        Icon(
                            imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = stringResource(
                                R.string.preferences_search_keys_values_placeholder,
                            ),
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.preferences_more_options),
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.preferences_menu_clear_all)) },
                            onClick = {
                                showMenu = false
                                showClearConfirmDialog = true
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteSweep, contentDescription = null)
                            },
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            WormaCeptorFAB(
                onClick = onCreateItem,
                contentDescription = stringResource(R.string.preferences_add_preference),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Animated search bar
            AnimatedVisibility(
                visible = searchActive,
                enter = expandVertically(
                    animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
                ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
                exit = shrinkVertically(
                    animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast),
                ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
            ) {
                WormaCeptorSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChanged,
                    placeholder = stringResource(R.string.preferences_search_keys_values_placeholder),
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.md,
                        vertical = WormaCeptorDesignSystem.Spacing.sm,
                    ),
                )
            }

            // Type filter chips - always visible when types available
            if (availableTypes.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = WormaCeptorDesignSystem.Spacing.md)
                        .padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                ) {
                    FilterChip(
                        selected = typeFilter == null,
                        onClick = { onTypeFilterChanged(null) },
                        label = { Text(stringResource(R.string.preferences_filter_all)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        ),
                    )
                    availableTypes.forEach { type ->
                        val typeColor = PreferencesDesignSystem.TypeColors.forTypeName(type)
                        FilterChip(
                            selected = typeFilter == type,
                            onClick = { onTypeFilterChanged(type) },
                            label = { Text(type) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = typeColor.copy(
                                    alpha = WormaCeptorDesignSystem.Alpha.medium,
                                ),
                                selectedLabelColor = typeColor,
                            ),
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                WormaCeptorEmptyState(
                    title = if (searchQuery.isNotBlank() || typeFilter != null) {
                        stringResource(R.string.preferences_empty_no_matches)
                    } else {
                        stringResource(R.string.preferences_empty_no_preferences)
                    },
                    modifier = Modifier.fillMaxSize(),
                    subtitle = if (searchQuery.isNotBlank() || typeFilter != null) {
                        stringResource(R.string.preferences_empty_try_adjusting_filters)
                    } else {
                        stringResource(R.string.preferences_empty_add_using_button)
                    },
                    icon = Icons.Default.Key,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = WormaCeptorDesignSystem.Spacing.md,
                        vertical = WormaCeptorDesignSystem.Spacing.xs,
                    ),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    items(items, key = { it.key }) { item ->
                        PreferenceItemContent(
                            item = item,
                            onClick = { onEditItem(item) },
                            onLongClick = { showDeleteConfirmDialog = item.key },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    // Clear all confirmation dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(stringResource(R.string.preferences_dialog_clear_title)) },
            text = {
                Text(
                    stringResource(R.string.preferences_dialog_clear_message, fileName),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAll()
                    },
                ) {
                    Text(
                        stringResource(R.string.preferences_dialog_clear_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(stringResource(R.string.preferences_dialog_cancel))
                }
            },
        )
    }

    // Delete confirmation dialog
    showDeleteConfirmDialog?.let { key ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text(stringResource(R.string.preferences_dialog_delete_title)) },
            text = { Text(stringResource(R.string.preferences_dialog_delete_message, key)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = null
                        onDeleteItem(key)
                    },
                ) {
                    Text(
                        stringResource(R.string.preferences_dialog_delete_confirm),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text(stringResource(R.string.preferences_dialog_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PreferenceItemContent(
    item: PreferenceItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val typeColor = PreferencesDesignSystem.TypeColors.forTypeName(item.value.typeName)

    WormaCeptorContainer(
        onClick = onClick,
        style = ContainerStyle.Outlined,
        backgroundColor = typeColor.asSubtleBackground(),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
    ) {
        PreferenceItemRow(item = item, typeColor = typeColor)
    }
}

@Composable
private fun PreferenceItemRow(item: PreferenceItem, typeColor: Color) {
    Row(
        modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xs),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = item.key,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

            Text(
                text = item.value.displayValue,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

        Surface(
            color = typeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
            contentColor = typeColor,
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
        ) {
            Text(
                text = item.value.typeName,
                style = WormaCeptorDesignSystem.Typography.overline,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                    vertical = WormaCeptorDesignSystem.Spacing.xxs,
                ),
            )
        }
    }
}
