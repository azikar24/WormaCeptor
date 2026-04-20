package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorPullToRefresh
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.SelectableTransactionItem
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

/** Transaction list with multi-select, per-item context actions, and optional pull-to-refresh. */
@Composable
fun SelectableTransactionListScreen(
    transactions: ImmutableList<TransactionSummary>,
    onItemClick: (TransactionSummary) -> Unit,
    modifier: Modifier = Modifier,
    selectionState: TransactionSelectionState = TransactionSelectionState(),
    itemActions: TransactionItemActions = TransactionItemActions(),
    isInitialLoading: Boolean = false,
    hasActiveFilters: Boolean = false,
    onClearFilters: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
) {
    val loadable: @Composable () -> Unit = {
        WormaCeptorLoadableContent(
            isLoading = isInitialLoading,
            isEmpty = transactions.isEmpty(),
            loading = { WormaCeptorListSkeleton(modifier = Modifier.fillMaxSize()) },
            empty = {
                TransactionListEmptyState(
                    hasActiveFilters = hasActiveFilters,
                    onClearFilters = onClearFilters,
                )
            },
            content = {
                TransactionListContent(
                    transactions = transactions,
                    onItemClick = onItemClick,
                    selectionState = selectionState,
                    itemActions = itemActions,
                    header = header,
                )
            },
            modifier = Modifier.fillMaxSize(),
        )
    }

    if (onRefresh != null) {
        WormaCeptorPullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize(),
        ) {
            loadable()
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            loadable()
        }
    }
}

@Composable
private fun TransactionListContent(
    transactions: ImmutableList<TransactionSummary>,
    onItemClick: (TransactionSummary) -> Unit,
    selectionState: TransactionSelectionState,
    itemActions: TransactionItemActions,
    header: (@Composable () -> Unit)?,
) {
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = WormaCeptorTokens.Spacing.xs,
            bottom = WormaCeptorTokens.Spacing.xs + navigationBarPadding,
        ),
    ) {
        if (header != null) {
            item(key = "header") { header() }
        }
        items(transactions, key = { it.id }) { transaction ->
            SelectableTransactionItem(
                transaction = transaction,
                isSelected = transaction.id in selectionState.selectedIds,
                isSelectionMode = selectionState.isSelectionMode,
                onClick = {
                    if (selectionState.isSelectionMode) {
                        selectionState.onSelectionToggle(transaction.id)
                    } else {
                        onItemClick(transaction)
                    }
                },
                onLongClick = { selectionState.onLongClick(transaction.id) },
                onCopyUrl = { itemActions.onCopyUrl(transaction) },
                onShare = { itemActions.onShare(transaction) },
                onShareAsHar = { itemActions.onShareAsHar(transaction) },
                onDelete = { itemActions.onDelete(transaction) },
                onCopyAsCurl = { itemActions.onCopyAsCurl(transaction) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun TransactionListEmptyState(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
) {
    WormaCeptorEmptyState(
        title = stringResource(
            if (hasActiveFilters) {
                R.string.viewer_transaction_list_no_matches_title
            } else {
                R.string.viewer_transaction_list_no_transactions_title
            },
        ),
        subtitle = stringResource(
            if (hasActiveFilters) {
                R.string.viewer_transaction_list_no_matches_description
            } else {
                R.string.viewer_transaction_list_no_transactions_description
            },
        ),
        icon = Icons.Default.Wifi,
        actionLabel = if (hasActiveFilters) {
            stringResource(R.string.viewer_transaction_list_clear_filters)
        } else {
            null
        },
        onAction = if (hasActiveFilters) onClearFilters else null,
        modifier = Modifier.fillMaxSize(),
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectableTransactionListScreenPreview() {
    WormaCeptorTheme {
        SelectableTransactionListScreen(
            transactions = kotlinx.collections.immutable.persistentListOf(
                TransactionSummary(
                    id = UUID.randomUUID(),
                    method = "GET",
                    host = "api.example.com",
                    path = "/users/123",
                    code = 200,
                    tookMs = 142L,
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
                    tookMs = 310L,
                    hasRequestBody = true,
                    hasResponseBody = true,
                    status = TransactionStatus.COMPLETED,
                    timestamp = System.currentTimeMillis() - 60_000,
                ),
                TransactionSummary(
                    id = UUID.randomUUID(),
                    method = "PUT",
                    host = "api.example.com",
                    path = "/users/123/profile",
                    code = null,
                    tookMs = null,
                    hasRequestBody = true,
                    hasResponseBody = false,
                    status = TransactionStatus.ACTIVE,
                    timestamp = System.currentTimeMillis() - 5_000,
                ),
            ),
            onItemClick = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
