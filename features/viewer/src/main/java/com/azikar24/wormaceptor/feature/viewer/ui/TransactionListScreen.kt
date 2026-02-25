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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.rememberHapticOnce
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.SelectableTransactionItem
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

// ============================================================================
// SELECTABLE VERSION WITH MULTI-SELECT AND QUICK FILTERS
// ============================================================================

/**
 * TransactionListScreen with multi-select and context menu support.
 *
 * @param transactions List of transactions to display
 * @param onItemClick Callback when a transaction is clicked
 * @param hasActiveFilters Whether filters are currently active
 * @param onClearFilters Callback to clear filters
 * @param isRefreshing Whether the list is currently refreshing
 * @param onRefresh Callback triggered on pull-to-refresh
 * @param selectedIds Set of currently selected transaction IDs
 * @param isSelectionMode Whether multi-select mode is active
 * @param onSelectionToggle Callback when a selection is toggled
 * @param onLongClick Callback when an item is long-clicked (enters selection mode)
 * @param onCopyUrl Callback to copy transaction URL
 * @param onShare Callback to share transaction
 * @param onDelete Callback to delete transaction
 * @param onCopyAsCurl Callback to copy transaction as cURL
 * @param modifier Modifier for the screen
 * @param header Optional header composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectableTransactionListScreen(
    transactions: ImmutableList<TransactionSummary>,
    onItemClick: (TransactionSummary) -> Unit,
    modifier: Modifier = Modifier,
    isInitialLoading: Boolean = false,
    hasActiveFilters: Boolean = false,
    onClearFilters: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    selectedIds: Set<UUID> = emptySet(),
    isSelectionMode: Boolean = false,
    onSelectionToggle: (UUID) -> Unit = {},
    onLongClick: (UUID) -> Unit = {},
    onCopyUrl: (TransactionSummary) -> Unit = {},
    onShare: (TransactionSummary) -> Unit = {},
    onDelete: (TransactionSummary) -> Unit = {},
    onCopyAsCurl: (TransactionSummary) -> Unit = {},
    header: (@Composable () -> Unit)? = null,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val haptic = rememberHapticOnce()

    // Trigger haptic feedback when pull threshold is reached
    LaunchedEffect(pullToRefreshState.distanceFraction) {
        if (pullToRefreshState.distanceFraction >= 1f && !haptic.isTriggered) {
            haptic.triggerHaptic()
        } else if (pullToRefreshState.distanceFraction < 1f) {
            haptic.resetHaptic()
        }
    }

    // Reset haptic state when refreshing ends
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            haptic.resetHaptic()
        }
    }

    val listContent: @Composable () -> Unit = {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WormaCeptorDesignSystem.Spacing.xs,
                bottom = WormaCeptorDesignSystem.Spacing.xs + navigationBarPadding,
            ),
        ) {
            // Optional header (e.g., MetricsCard)
            if (header != null) {
                item(key = "header") {
                    header()
                }
            }

            // Transaction items
            items(transactions, key = { it.id }) { transaction ->
                SelectableTransactionItem(
                    transaction = transaction,
                    isSelected = transaction.id in selectedIds,
                    isSelectionMode = isSelectionMode,
                    onClick = {
                        if (isSelectionMode) {
                            onSelectionToggle(transaction.id)
                        } else {
                            onItemClick(transaction)
                        }
                    },
                    onLongClick = {
                        onLongClick(transaction.id)
                    },
                    onCopyUrl = { onCopyUrl(transaction) },
                    onShare = { onShare(transaction) },
                    onDelete = { onDelete(transaction) },
                    onCopyAsCurl = { onCopyAsCurl(transaction) },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    if (transactions.isEmpty()) {
        if (isInitialLoading) {
            // Show loading state while data is being fetched
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (onRefresh != null) {
            // Empty state with pull-to-refresh
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = modifier.fillMaxSize(),
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isRefreshing,
                        state = pullToRefreshState,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
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
        } else {
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
                modifier = modifier.fillMaxSize(),
            )
        }
    } else {
        // List with pull-to-refresh
        if (onRefresh != null) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = modifier.fillMaxSize(),
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isRefreshing,
                        state = pullToRefreshState,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                listContent()
            }
        } else {
            Box(modifier = modifier.fillMaxSize()) {
                listContent()
            }
        }
    }
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
