package com.azikar24.wormaceptor.feature.preferences.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
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

private val PreferenceRowHeight = 76.dp

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
    val itemCountLabel = if (totalItemCount == 1) {
        stringResource(R.string.preferences_item_count_singular)
    } else {
        stringResource(R.string.preferences_item_count_plural)
    }

    WormaCeptorTopBar(
        title = fileName,
        subtitle = "$totalItemCount $itemCountLabel",
        onBack = onBack,
        backContentDescription = stringResource(R.string.preferences_back),
        actions = {
            WormaCeptorIconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = stringResource(
                        R.string.preferences_search_keys_values_placeholder,
                    ),
                )
            }
            WormaCeptorIconButton(onClick = { showMenu = true }) {
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

        WormaCeptorLoadableContent(
            isLoading = state.isItemsLoading,
            isEmpty = state.preferenceItems.isEmpty(),
            loading = {
                WormaCeptorListSkeleton(
                    modifier = Modifier.fillMaxSize(),
                    rowHeight = PreferenceRowHeight,
                )
            },
            empty = {
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
            },
            content = {
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
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun PreferenceDetailFilterChips(
    state: PreferencesViewState,
    typeColors: ToolColors.Preferences.TypeScheme,
    onEvent: (PreferencesViewEvent) -> Unit,
) {
    if (state.availableTypes.isEmpty()) return

    WormaCeptorScrollableRow(
        modifier = Modifier
            .padding(
                vertical = WormaCeptorTokens.Spacing.sm,
                horizontal = WormaCeptorTokens.Spacing.md,
            ),
        contentPadding = PaddingValues(0.dp),
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
            editor = state.editor,
            onEvent = onEvent,
            showDelete = state.editingItem != null,
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
