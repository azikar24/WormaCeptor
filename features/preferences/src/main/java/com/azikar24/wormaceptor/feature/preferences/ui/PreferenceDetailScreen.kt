/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.preferences.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.feature.preferences.ui.theme.PreferencesDesignSystem
import com.azikar24.wormaceptor.feature.preferences.ui.theme.asSubtleBackground
import kotlinx.collections.immutable.ImmutableList

/**
 * Screen displaying key-value pairs for a specific SharedPreferences file.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    var showFilters by remember { mutableStateOf(false) }

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
                        Text(
                            text = "$totalCount ${if (totalCount == 1) "item" else "items"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (typeFilter != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Clear All") },
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
            FloatingActionButton(
                onClick = onCreateItem,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add preference")
            }
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search keys or values...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.md,
                        vertical = WormaCeptorDesignSystem.Spacing.sm,
                    ),
                singleLine = true,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )

            // Type filter chips
            AnimatedVisibility(
                visible = showFilters && availableTypes.isNotEmpty(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically(),
            ) {
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
                        label = { Text("All") },
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
                                selectedContainerColor = typeColor.copy(alpha = 0.2f),
                                selectedLabelColor = typeColor,
                            ),
                        )
                    }
                }
            }

            if (items.isEmpty()) {
                EmptyItemsState(
                    hasFilters = searchQuery.isNotBlank() || typeFilter != null,
                    modifier = Modifier.fillMaxSize(),
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
                        SwipeablePreferenceItem(
                            item = item,
                            onEdit = { onEditItem(item) },
                            onDelete = { showDeleteConfirmDialog = item.key },
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
            title = { Text("Clear All Preferences") },
            text = {
                Text(
                    "Are you sure you want to delete all preferences in $fileName? This action cannot be undone.",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAll()
                    },
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete confirmation dialog
    showDeleteConfirmDialog?.let { key ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Preference") },
            text = { Text("Are you sure you want to delete \"$key\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = null
                        onDeleteItem(key)
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeablePreferenceItem(
    item: PreferenceItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false // Don't dismiss, let the dialog handle it
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
                else -> Color.Transparent
            }
            val icon = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                else -> null
            }
            val alignment = when (direction) {
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.CenterStart
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                    .padding(horizontal = 20.dp),
                contentAlignment = alignment,
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = when (direction) {
                            SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                    )
                }
            }
        },
        content = {
            PreferenceItemContent(item = item, onClick = onEdit)
        },
    )
}

@Composable
private fun PreferenceItemContent(item: PreferenceItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val typeColor = PreferencesDesignSystem.TypeColors.forTypeName(item.value.typeName)

    WormaCeptorContainer(
        onClick = onClick,
        style = ContainerStyle.Outlined,
        backgroundColor = typeColor.asSubtleBackground(),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Type indicator
            Box(
                modifier = Modifier
                    .width(WormaCeptorDesignSystem.BorderWidth.thick)
                    .height(48.dp)
                    .background(
                        typeColor,
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                    ),
            )

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
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

            // Type badge
            Surface(
                color = typeColor.copy(alpha = 0.15f),
                contentColor = typeColor,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
            ) {
                Text(
                    text = item.value.typeName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                )
            }
        }
    }
}

@Composable
private fun EmptyItemsState(hasFilters: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        Text(
            text = if (hasFilters) "No matches found" else "No preferences",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        Text(
            text = if (hasFilters) {
                "Try adjusting your search or filters"
            } else {
                "Add preferences using the + button"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
