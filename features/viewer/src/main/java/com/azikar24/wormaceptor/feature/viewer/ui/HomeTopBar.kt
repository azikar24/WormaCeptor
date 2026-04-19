package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.BulkActionBar
import com.azikar24.wormaceptor.feature.viewer.vm.CrashListViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewState
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch
import java.util.UUID

private object HomeTabs {
    const val TRANSACTIONS = 0
    const val CRASHES = 1
    const val TOOLS = 2
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    homeState: HomeViewState,
    transactionState: TransactionListViewState,
    onHomeEvent: (HomeViewEvent) -> Unit,
    onTransactionEvent: (TransactionListViewEvent) -> Unit,
    onCrashEvent: (CrashListViewEvent) -> Unit,
    pagerState: PagerState,
    titles: List<String>,
) {
    val scope = rememberCoroutineScope()
    val isSelectionMode =
        transactionState.selectedIds.isNotEmpty() && pagerState.currentPage == HomeTabs.TRANSACTIONS

    // Dismiss overflow menu when tab changes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            onHomeEvent(HomeViewEvent.OverflowMenuVisibilityChanged(false))
        }
    }

    Column {
        // Crossfade between BulkActionBar and TopAppBar to avoid height shift
        Crossfade(targetState = isSelectionMode, label = "TopBarCrossfade") { inSelectionMode ->
            if (inSelectionMode) {
                BulkActionBar(
                    selectedCount = transactionState.selectedIds.size,
                    totalCount = transactionState.transactions.size,
                    onShare = { onTransactionEvent(TransactionListViewEvent.ShareSelectedTransactions) },
                    onDelete = {
                        onTransactionEvent(
                            TransactionListViewEvent.DeleteSelectedDialogVisibilityChanged(true),
                        )
                    },
                    onExport = { onTransactionEvent(TransactionListViewEvent.ExportSelectedTransactions) },
                    onExportAsHar = {
                        onTransactionEvent(TransactionListViewEvent.ExportSelectedTransactionsAsHar)
                    },
                    onSelectAll = { onTransactionEvent(TransactionListViewEvent.SelectAllClicked) },
                    onDeselectAll = { onTransactionEvent(TransactionListViewEvent.SelectionCleared) },
                    onCancel = { onTransactionEvent(TransactionListViewEvent.SelectionCleared) },
                )
            } else {
                WormaCeptorTopBar(
                    title = stringResource(R.string.viewer_home_title),
                    onBack = { onHomeEvent(HomeViewEvent.BackPressed) },
                    backContentDescription = stringResource(R.string.viewer_home_back),
                    actions = {
                        // Overflow Menu - only show on Transactions and Crashes tabs
                        if (pagerState.currentPage < HomeTabs.TOOLS) {
                            WormaCeptorIconButton(
                                onClick = {
                                    onHomeEvent(HomeViewEvent.OverflowMenuVisibilityChanged(true))
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
                                expanded = homeState.showOverflowMenu,
                                onDismissRequest = {
                                    onHomeEvent(
                                        HomeViewEvent.OverflowMenuVisibilityChanged(false),
                                    )
                                },
                                shape = WormaCeptorTokens.Shapes.cardLarge,
                            ) {
                                when (pagerState.currentPage) {
                                    HomeTabs.TRANSACTIONS -> TransactionsOverflowMenu(
                                        onHomeEvent = onHomeEvent,
                                        onTransactionEvent = onTransactionEvent,
                                    )
                                    HomeTabs.CRASHES -> CrashesOverflowMenu(
                                        onHomeEvent = onHomeEvent,
                                        onCrashEvent = onCrashEvent,
                                    )
                                }
                            }
                        }

                        // Search toggle - only show on Tools tab
                        if (pagerState.currentPage == HomeTabs.TOOLS) {
                            WormaCeptorIconButton(
                                onClick = {
                                    onHomeEvent(
                                        HomeViewEvent.ToolsSearchActiveChanged(
                                            !homeState.toolsSearchActive,
                                        ),
                                    )
                                },
                            ) {
                                Icon(
                                    imageVector = if (homeState.toolsSearchActive) {
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

        PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        if (isSelectionMode) {
                            onTransactionEvent(TransactionListViewEvent.SelectionCleared)
                        }
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(title) },
                )
            }
        }
    }
}

@Composable
private fun TransactionsOverflowMenu(
    onHomeEvent: (HomeViewEvent) -> Unit,
    onTransactionEvent: (TransactionListViewEvent) -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_export_transactions)) },
        leadingIcon = { Icon(Icons.Default.Share, null) },
        onClick = {
            onHomeEvent(HomeViewEvent.OverflowMenuVisibilityChanged(false))
            onTransactionEvent(TransactionListViewEvent.ExportAllTransactions)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_export_transactions_as_har)) },
        leadingIcon = { Icon(Icons.Default.Share, null) },
        onClick = {
            onHomeEvent(HomeViewEvent.OverflowMenuVisibilityChanged(false))
            onTransactionEvent(TransactionListViewEvent.ExportAllTransactionsAsHar)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_clear_all_transactions)) },
        leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
        onClick = {
            onHomeEvent(HomeViewEvent.OverflowMenuVisibilityChanged(false))
            onTransactionEvent(
                TransactionListViewEvent.ClearTransactionsDialogVisibilityChanged(true),
            )
        },
    )
}

@Composable
private fun CrashesOverflowMenu(
    onHomeEvent: (HomeViewEvent) -> Unit,
    onCrashEvent: (CrashListViewEvent) -> Unit,
) {
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_export_crashes)) },
        leadingIcon = { Icon(Icons.Default.Share, null) },
        onClick = {
            onHomeEvent(HomeViewEvent.OverflowMenuVisibilityChanged(false))
            onCrashEvent(CrashListViewEvent.ExportCrashesClicked)
        },
    )
    DropdownMenuItem(
        text = { Text(stringResource(R.string.viewer_home_clear_all_crashes)) },
        leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
        onClick = {
            onHomeEvent(HomeViewEvent.OverflowMenuVisibilityChanged(false))
            onCrashEvent(CrashListViewEvent.ClearCrashesDialogVisibilityChanged(true))
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun HomeTopBarPreview() {
    val titles = remember { listOf("Transactions", "Crashes", "Tools") }
    WormaCeptorTheme {
        HomeTopBar(
            homeState = HomeViewState(),
            transactionState = TransactionListViewState(
                transactions = persistentListOf(
                    TransactionSummary(
                        id = UUID.randomUUID(),
                        method = "GET",
                        host = "api.example.com",
                        path = "/users",
                        code = 200,
                        tookMs = 120L,
                        hasRequestBody = false,
                        hasResponseBody = true,
                        status = TransactionStatus.COMPLETED,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            ),
            onHomeEvent = {},
            onTransactionEvent = {},
            onCrashEvent = {},
            pagerState = rememberPagerState(pageCount = { titles.size }),
            titles = titles,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun HomeTopBarSelectionModePreview() {
    val titles = remember { listOf("Transactions", "Crashes", "Tools") }
    WormaCeptorTheme {
        HomeTopBar(
            homeState = HomeViewState(),
            transactionState = TransactionListViewState(
                selectedIds = setOf(UUID.randomUUID(), UUID.randomUUID()),
                transactions = persistentListOf(
                    TransactionSummary(
                        id = UUID.randomUUID(),
                        method = "GET",
                        host = "api.example.com",
                        path = "/users",
                        code = 200,
                        tookMs = 120L,
                        hasRequestBody = false,
                        hasResponseBody = true,
                        status = TransactionStatus.COMPLETED,
                        timestamp = System.currentTimeMillis(),
                    ),
                    TransactionSummary(
                        id = UUID.randomUUID(),
                        method = "POST",
                        host = "api.example.com",
                        path = "/auth/login",
                        code = 401,
                        tookMs = 250L,
                        hasRequestBody = true,
                        hasResponseBody = true,
                        status = TransactionStatus.COMPLETED,
                        timestamp = System.currentTimeMillis(),
                    ),
                ),
            ),
            onHomeEvent = {},
            onTransactionEvent = {},
            onCrashEvent = {},
            pagerState = rememberPagerState(pageCount = { titles.size }),
            titles = titles,
        )
    }
}
