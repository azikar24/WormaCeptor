package com.azikar24.wormaceptor.feature.preferences.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue
import com.azikar24.wormaceptor.feature.preferences.R
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceEditSheet
import com.azikar24.wormaceptor.feature.preferences.ui.components.PreferenceItemCard
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewEvent
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewState
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceDetailScreen(
    state: PreferencesViewState,
    onEvent: (PreferencesViewEvent) -> Unit,
    onBack: () -> Unit,
) {
    val fileName = state.selectedFileName
    if (fileName == null) {
        WormaCeptorEmptyState(
            title = stringResource(R.string.preferences_empty_no_preferences),
            modifier = Modifier.fillMaxSize(),
            icon = Icons.Default.Key,
        )
        return
    }

    val typeColors = WormaCeptorTokens.Colors.Preferences.typeScheme()
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            PreferenceDetailTopBar(
                fileName = fileName,
                totalItemCount = state.totalItemCount,
                searchActive = searchActive,
                onToggleSearch = {
                    searchActive = !searchActive
                    if (!searchActive) onEvent(PreferencesViewEvent.Detail.SearchQueryChanged(""))
                },
                onClearAll = { onEvent(PreferencesViewEvent.Detail.ClearConfirmShown) },
                onBack = onBack,
            )
        },
        floatingActionButton = {
            WormaCeptorFAB(
                onClick = { onEvent(PreferencesViewEvent.Detail.EditSheetOpened(null)) },
                contentDescription = stringResource(R.string.preferences_add_preference),
            )
        },
    ) { padding ->
        PreferenceDetailBody(
            state = state,
            onEvent = onEvent,
            typeColors = typeColors,
            searchActive = searchActive,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        )
    }

    PreferenceDetailDialogs(
        state = state,
        onEvent = onEvent,
        fileName = fileName,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferenceDetailTopBar(
    fileName: String,
    totalItemCount: Int,
    searchActive: Boolean,
    onToggleSearch: () -> Unit,
    onClearAll: () -> Unit,
    onBack: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }

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
                val itemCountLabel = if (totalItemCount == 1) {
                    stringResource(R.string.preferences_item_count_singular)
                } else {
                    stringResource(R.string.preferences_item_count_plural)
                }
                Text(
                    text = "$totalItemCount $itemCountLabel",
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
            IconButton(onClick = onToggleSearch) {
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
                        onClearAll()
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
}

@Composable
private fun PreferenceDetailBody(
    state: PreferencesViewState,
    onEvent: (PreferencesViewEvent) -> Unit,
    typeColors: ToolColors.Preferences.TypeScheme,
    searchActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = searchActive,
            enter = WormaCeptorTokens.Animations.expandFadeIn,
            exit = WormaCeptorTokens.Animations.shrinkFadeOut,
        ) {
            WormaCeptorSearchBar(
                query = state.itemSearchQuery,
                onQueryChange = { onEvent(PreferencesViewEvent.Detail.SearchQueryChanged(it)) },
                placeholder = stringResource(R.string.preferences_search_keys_values_placeholder),
                modifier = Modifier.padding(
                    start = WormaCeptorTokens.Spacing.md,
                    end = WormaCeptorTokens.Spacing.md,
                    top = WormaCeptorTokens.Spacing.sm,
                ),
            )
        }

        PreferenceDetailFilterChips(
            state = state,
            typeColors = typeColors,
            onEvent = onEvent,
        )

        if (state.preferenceItems.isEmpty()) {
            WormaCeptorEmptyState(
                title = if (state.itemSearchQuery.isNotBlank() || state.typeFilter != null) {
                    stringResource(R.string.preferences_empty_no_matches)
                } else {
                    stringResource(R.string.preferences_empty_no_preferences)
                },
                modifier = Modifier.fillMaxSize(),
                subtitle = if (state.itemSearchQuery.isNotBlank() || state.typeFilter != null) {
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
                    start = WormaCeptorTokens.Spacing.md,
                    end = WormaCeptorTokens.Spacing.md,
                    top = WormaCeptorTokens.Spacing.sm,
                    bottom = WormaCeptorTokens.Spacing.xs +
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                items(state.preferenceItems, key = { it.key }) { item ->
                    PreferenceItemCard(
                        item = item,
                        typeColors = typeColors,
                        onClick = {
                            onEvent(PreferencesViewEvent.Detail.EditSheetOpened(item))
                        },
                        onLongClick = {
                            onEvent(PreferencesViewEvent.Detail.DeleteConfirmShown(item.key))
                        },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PreferenceDetailFilterChips(
    state: PreferencesViewState,
    typeColors: ToolColors.Preferences.TypeScheme,
    onEvent: (PreferencesViewEvent) -> Unit,
) {
    if (state.availableTypes.isEmpty()) return

    WormaCeptorFlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = WormaCeptorTokens.Spacing.md)
            .padding(bottom = WormaCeptorTokens.Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
    ) {
        WormaCeptorChip(
            label = stringResource(R.string.preferences_filter_all),
            selected = state.typeFilter == null,
            onClick = { onEvent(PreferencesViewEvent.Detail.TypeFilterChanged(null)) },
        )
        state.availableTypes.forEach { type ->
            val typeColor = typeColors.forTypeName(type)
            WormaCeptorChip(
                label = type,
                selected = state.typeFilter == type,
                onClick = { onEvent(PreferencesViewEvent.Detail.TypeFilterChanged(type)) },
                accentColor = typeColor,
            )
        }
    }
}

@Composable
private fun PreferenceDetailDialogs(
    state: PreferencesViewState,
    onEvent: (PreferencesViewEvent) -> Unit,
    fileName: String,
) {
    val haptic = LocalHapticFeedback.current

    if (state.showClearConfirmDialog) {
        WormaCeptorAlertDialog(
            title = stringResource(R.string.preferences_dialog_clear_title),
            message = stringResource(R.string.preferences_dialog_clear_message, fileName),
            confirmLabel = stringResource(R.string.preferences_dialog_clear_confirm),
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEvent(PreferencesViewEvent.Detail.FileCleared)
                onEvent(PreferencesViewEvent.Detail.ClearConfirmDismissed)
            },
            dismissLabel = stringResource(R.string.preferences_dialog_cancel),
            onDismiss = { onEvent(PreferencesViewEvent.Detail.ClearConfirmDismissed) },
            destructive = true,
        )
    }

    state.showDeleteConfirmKey?.let { key ->
        WormaCeptorAlertDialog(
            title = stringResource(R.string.preferences_dialog_delete_title),
            message = stringResource(R.string.preferences_dialog_delete_message, key),
            confirmLabel = stringResource(R.string.preferences_dialog_delete_confirm),
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEvent(PreferencesViewEvent.Detail.PreferenceDeleted(key))
                onEvent(PreferencesViewEvent.Detail.DeleteConfirmDismissed)
            },
            dismissLabel = stringResource(R.string.preferences_dialog_cancel),
            onDismiss = { onEvent(PreferencesViewEvent.Detail.DeleteConfirmDismissed) },
            destructive = true,
        )
    }

    if (state.showEditSheet) {
        PreferenceEditSheet(
            item = state.editingItem,
            onDismiss = { onEvent(PreferencesViewEvent.Detail.EditSheetDismissed) },
            onSave = { key, value ->
                if (state.editingItem != null) {
                    onEvent(PreferencesViewEvent.Detail.PreferenceSet(key, value))
                } else {
                    onEvent(PreferencesViewEvent.Detail.PreferenceCreated(key, value))
                }
                onEvent(PreferencesViewEvent.Detail.EditSheetDismissed)
            },
            onDelete = if (state.editingItem != null) {
                { key: String ->
                    onEvent(PreferencesViewEvent.Detail.PreferenceDeleted(key))
                    onEvent(PreferencesViewEvent.Detail.EditSheetDismissed)
                }
            } else {
                null
            },
        )
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun PreferenceDetailScreenPreview() {
    WormaCeptorTheme {
        PreferenceDetailScreen(
            state = PreferencesViewState(
                selectedFileName = "app_preferences",
                preferenceItems = persistentListOf(
                    PreferenceItem(key = "dark_mode", value = PreferenceValue.BooleanValue(true)),
                    PreferenceItem(key = "username", value = PreferenceValue.StringValue("john_doe")),
                    PreferenceItem(key = "launch_count", value = PreferenceValue.IntValue(42)),
                ),
                totalItemCount = 3,
                availableTypes = persistentListOf("Boolean", "String", "Int"),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
