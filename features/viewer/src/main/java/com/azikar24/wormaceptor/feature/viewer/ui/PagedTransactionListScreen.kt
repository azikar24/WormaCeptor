package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.ErrorState
import com.azikar24.wormaceptor.core.ui.components.ErrorType
import com.azikar24.wormaceptor.core.ui.components.InlineErrorRetry
import com.azikar24.wormaceptor.core.ui.components.LoadingMoreIndicator
import com.azikar24.wormaceptor.core.ui.components.ScrollToTopFab
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMethodBadge
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.components.*
import kotlinx.coroutines.launch

/**
 * Paged transaction list screen using Paging 3 library.
 * Supports lazy loading, pull-to-refresh, and various loading states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagedTransactionListScreen(
    lazyPagingItems: LazyPagingItems<TransactionSummary>,
    onItemClick: (TransactionSummary) -> Unit,
    modifier: Modifier = Modifier,
    hasActiveFilters: Boolean = false,
    hasSearchQuery: Boolean = false,
    onClearFilters: () -> Unit = {},
    header: (@Composable () -> Unit)? = null,
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()

    // Track if user has scrolled down for FAB visibility
    val showScrollToTop by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 3
        }
    }

    // Track if refreshing
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { lazyPagingItems.refresh() },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize(),
        ) {
            when {
                // Initial loading state
                lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount == 0 -> {
                    TransactionListSkeleton(
                        itemCount = 6,
                        modifier = Modifier.padding(top = if (header != null) 72.dp else 0.dp),
                    )
                    // Still show header during loading
                    header?.let {
                        Column {
                            it()
                        }
                    }
                }

                // Error on initial load
                lazyPagingItems.loadState.refresh is LoadState.Error && lazyPagingItems.itemCount == 0 -> {
                    val error = (lazyPagingItems.loadState.refresh as LoadState.Error).error
                    Column {
                        header?.invoke()
                        ErrorState(
                            message = error.localizedMessage ?: "Failed to load transactions",
                            onRetry = { lazyPagingItems.refresh() },
                            errorType = determineErrorType(error),
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                // Empty state (after successful load with no results)
                lazyPagingItems.loadState.refresh is LoadState.NotLoading && lazyPagingItems.itemCount == 0 -> {
                    Column {
                        header?.invoke()
                        EnhancedEmptyState(
                            hasActiveFilters = hasActiveFilters,
                            hasSearchQuery = hasSearchQuery,
                            onClearFilters = onClearFilters,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }

                // Content loaded
                else -> {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.xs),
                    ) {
                        // Header
                        if (header != null) {
                            item(key = "header") {
                                header()
                            }
                        }

                        // Transaction items
                        items(
                            count = lazyPagingItems.itemCount,
                            key = { index -> lazyPagingItems.peek(index)?.id ?: "item_$index" },
                        ) { index ->
                            lazyPagingItems[index]?.let { transaction ->
                                PagedTransactionItem(
                                    transaction = transaction,
                                    onClick = { onItemClick(transaction) },
                                    modifier = Modifier.animateItem(),
                                )
                            } ?: run {
                                // Placeholder while item loads
                                TransactionItemSkeleton()
                            }
                        }

                        // Append loading state
                        when (lazyPagingItems.loadState.append) {
                            is LoadState.Loading -> {
                                item(key = "append_loading") {
                                    LoadingMoreIndicator()
                                }
                            }
                            is LoadState.Error -> {
                                val error = (lazyPagingItems.loadState.append as LoadState.Error).error
                                item(key = "append_error") {
                                    InlineErrorRetry(
                                        message = error.localizedMessage ?: "Failed to load more",
                                        onRetry = { lazyPagingItems.retry() },
                                    )
                                }
                            }
                            is LoadState.NotLoading -> {
                                // No more items or reached end
                                if (lazyPagingItems.loadState.append.endOfPaginationReached && lazyPagingItems.itemCount > 10) {
                                    item(key = "end_reached") {
                                        EndOfListIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Scroll to top FAB
        ScrollToTopFab(
            visible = showScrollToTop,
            onClick = {
                scope.launch {
                    lazyListState.animateScrollToItem(0)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        )
    }
}

/**
 * Transaction item optimized for paged lists.
 */
@Composable
private fun PagedTransactionItem(transaction: TransactionSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val statusColor = when (transaction.status) {
        TransactionStatus.COMPLETED -> when {
            transaction.code == null -> WormaCeptorColors.StatusAmber
            transaction.code in 200..299 -> WormaCeptorColors.StatusGreen
            transaction.code in 300..399 -> WormaCeptorColors.StatusBlue
            transaction.code in 400..499 -> WormaCeptorColors.StatusAmber
            transaction.code in 500..599 -> WormaCeptorColors.StatusRed
            else -> WormaCeptorColors.StatusGrey
        }
        TransactionStatus.FAILED -> WormaCeptorColors.StatusRed
        TransactionStatus.ACTIVE -> WormaCeptorColors.StatusGrey
    }

    WormaCeptorContainer(
        onClick = onClick,
        style = ContainerStyle.Outlined,
        backgroundColor = statusColor.asSubtleBackground(),
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            ),
    ) {
        Row(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status indicator bar
            Box(
                modifier = Modifier
                    .width(WormaCeptorDesignSystem.BorderWidth.thick)
                    .height(WormaCeptorDesignSystem.Spacing.xxxl)
                    .background(
                        statusColor,
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                    ),
            )

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    WormaCeptorMethodBadge(transaction.method)
                    Text(
                        text = transaction.path,
                        style = WormaCeptorDesignSystem.Typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

                HostChip(transaction.host)
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = statusColor.asSubtleBackground(),
                    contentColor = statusColor,
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                ) {
                    Text(
                        text = transaction.code?.toString() ?: "?",
                        style = WormaCeptorDesignSystem.Typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.sm,
                            vertical = WormaCeptorDesignSystem.Spacing.xxs,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                Text(
                    text = formatDuration(transaction.tookMs),
                    style = WormaCeptorDesignSystem.Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.heavy,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HostChip(host: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.bold),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
    ) {
        Text(
            text = host,
            style = WormaCeptorDesignSystem.Typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
        )
    }
}

/**
 * Indicator shown when the user has scrolled to the end of the list.
 */
@Composable
private fun EndOfListIndicator(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.xl,
                vertical = WormaCeptorDesignSystem.Spacing.lg,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalDivider(
            modifier = Modifier
                .width(40.dp)
                .padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
            thickness = 2.dp,
        )
        Text(
            text = "You've reached the end",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.bold),
        )
    }
}

private fun determineErrorType(error: Throwable): ErrorType {
    val message = error.message?.lowercase() ?: ""
    return when {
        message.contains(
            "network",
        ) || message.contains("connection") || message.contains("timeout") -> ErrorType.NETWORK
        else -> ErrorType.GENERIC
    }
}
