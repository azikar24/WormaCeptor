package com.azikar24.wormaceptor.feature.viewer.ui

import android.app.Activity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.BulkActionBar
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewState
import kotlinx.coroutines.launch

/**
 * Top bar for [HomeScreen] containing the app bar (or bulk-action bar in selection mode)
 * and the tab row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    state: ViewerViewState,
    isSelectionMode: Boolean,
    transactionCount: Int,
    onEvent: (ViewerViewEvent) -> Unit,
    pagerState: PagerState,
    titles: List<String>,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column {
        // Crossfade between BulkActionBar and TopAppBar to avoid height shift
        Crossfade(targetState = isSelectionMode) { inSelectionMode ->
            if (inSelectionMode) {
                BulkActionBar(
                    selectedCount = state.selectedIds.size,
                    totalCount = transactionCount,
                    onShare = { onEvent(ViewerViewEvent.ShareSelectedTransactions) },
                    onDelete = { onEvent(ViewerViewEvent.DeleteSelectedDialogVisibilityChanged(true)) },
                    onExport = { onEvent(ViewerViewEvent.ExportSelectedTransactions) },
                    onExportAsHar = { onEvent(ViewerViewEvent.ExportSelectedTransactionsAsHar) },
                    onSelectAll = { onEvent(ViewerViewEvent.SelectAllClicked) },
                    onDeselectAll = { onEvent(ViewerViewEvent.SelectionCleared) },
                    onCancel = { onEvent(ViewerViewEvent.SelectionCleared) },
                )
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.viewer_home_title)) },
                    navigationIcon = {
                        IconButton(onClick = { (context as? Activity)?.finish() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.viewer_home_back),
                            )
                        }
                    },
                    actions = {
                        // Overflow Menu - only show on Transactions and Crashes tabs
                        if (pagerState.currentPage < 2) {
                            IconButton(
                                onClick = {
                                    onEvent(ViewerViewEvent.OverflowMenuVisibilityChanged(true))
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = stringResource(
                                        R.string.viewer_home_more_options,
                                    ),
                                )
                            }

                            DropdownMenu(
                                expanded = state.showOverflowMenu,
                                onDismissRequest = {
                                    onEvent(ViewerViewEvent.OverflowMenuVisibilityChanged(false))
                                },
                                shape = WormaCeptorDesignSystem.Shapes.cardLarge,
                            ) {
                                when (pagerState.currentPage) {
                                    0 -> TransactionsOverflowMenu(onEvent)
                                    1 -> CrashesOverflowMenu(onEvent)
                                }
                            }
                        }

                        // Search toggle - only show on Tools tab
                        if (pagerState.currentPage == 2) {
                            IconButton(
                                onClick = {
                                    onEvent(
                                        ViewerViewEvent.ToolsSearchActiveChanged(
                                            !state.toolsSearchActive,
                                        ),
                                    )
                                },
                            ) {
                                Icon(
                                    imageVector = if (state.toolsSearchActive) {
                                        Icons.Default.Close
                                    } else {
                                        Icons.Default.Search
                                    },
                                    contentDescription = stringResource(
                                        R.string.viewer_tools_search_placeholder,
                                    ),
                                )
                            }
                        }
                    },
                )
            }
        }

        TabRow(selectedTabIndex = pagerState.currentPage) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        if (isSelectionMode) onEvent(ViewerViewEvent.SelectionCleared)
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(title) },
                )
            }
        }
    }
}

@Composable
private fun TransactionsOverflowMenu(onEvent: (ViewerViewEvent) -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_export_transactions)) },
        leadingIcon = { Icon(Icons.Default.Share, null) },
        onClick = {
            onEvent(ViewerViewEvent.OverflowMenuVisibilityChanged(false))
            onEvent(ViewerViewEvent.ExportAllTransactions)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_export_transactions_as_har)) },
        leadingIcon = { Icon(Icons.Default.Share, null) },
        onClick = {
            onEvent(ViewerViewEvent.OverflowMenuVisibilityChanged(false))
            onEvent(ViewerViewEvent.ExportAllTransactionsAsHar)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_clear_all_transactions)) },
        leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
        onClick = {
            onEvent(ViewerViewEvent.OverflowMenuVisibilityChanged(false))
            onEvent(ViewerViewEvent.ClearTransactionsDialogVisibilityChanged(true))
        },
    )
}

@Composable
private fun CrashesOverflowMenu(onEvent: (ViewerViewEvent) -> Unit) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_export_crashes)) },
        leadingIcon = { Icon(Icons.Default.Share, null) },
        onClick = {
            onEvent(ViewerViewEvent.OverflowMenuVisibilityChanged(false))
            onEvent(ViewerViewEvent.ExportCrashesClicked)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_clear_all_crashes)) },
        leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
        onClick = {
            onEvent(ViewerViewEvent.OverflowMenuVisibilityChanged(false))
            onEvent(ViewerViewEvent.ClearCrashesDialogVisibilityChanged(true))
        },
    )
}
