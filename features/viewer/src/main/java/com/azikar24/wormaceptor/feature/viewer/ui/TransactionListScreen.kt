package com.azikar24.wormaceptor.feature.viewer.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.SelectableTransactionItem
import kotlinx.collections.immutable.ImmutableList
import java.util.UUID

@Composable
private fun EmptyState(hasActiveFilters: Boolean, onClearFilters: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
            modifier = Modifier.size(64.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = stringResource(R.string.viewer_transaction_list_no_transactions),
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xl),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.intense,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        // Title
        Text(
            text = if (hasActiveFilters) {
                stringResource(
                    R.string.viewer_transaction_list_no_matches_title,
                )
            } else {
                stringResource(R.string.viewer_transaction_list_no_transactions_title)
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        // Description
        Text(
            text = if (hasActiveFilters) {
                stringResource(R.string.viewer_transaction_list_no_matches_description)
            } else {
                stringResource(R.string.viewer_transaction_list_no_transactions_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
        )

        if (hasActiveFilters) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
            Button(
                onClick = onClearFilters,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
            ) {
                Text(
                    text = stringResource(R.string.viewer_transaction_list_clear_filters),
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                )
            }
        }
    }
}

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
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val view = LocalView.current
    val pullToRefreshState = rememberPullToRefreshState()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    // Trigger haptic feedback when pull threshold is reached
    LaunchedEffect(pullToRefreshState.distanceFraction) {
        if (pullToRefreshState.distanceFraction >= 1f && !hasTriggeredHaptic) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            hasTriggeredHaptic = true
        } else if (pullToRefreshState.distanceFraction < 1f) {
            hasTriggeredHaptic = false
        }
    }

    // Reset haptic state when refreshing ends
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            hasTriggeredHaptic = false
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
        // Empty state with pull-to-refresh
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                EmptyState(
                    hasActiveFilters = hasActiveFilters,
                    onClearFilters = onClearFilters,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        } else {
            EmptyState(
                hasActiveFilters = hasActiveFilters,
                onClearFilters = onClearFilters,
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
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
