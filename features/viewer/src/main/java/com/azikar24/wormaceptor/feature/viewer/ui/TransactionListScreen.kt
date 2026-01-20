package com.azikar24.wormaceptor.feature.viewer.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.components.ErrorState
import com.azikar24.wormaceptor.feature.viewer.ui.components.ErrorType
import com.azikar24.wormaceptor.feature.viewer.ui.components.InlineErrorRetry
import com.azikar24.wormaceptor.feature.viewer.ui.components.LoadingMoreIndicator
import com.azikar24.wormaceptor.feature.viewer.ui.components.ScrollToTopFab
import com.azikar24.wormaceptor.feature.viewer.ui.components.SelectableTransactionItem
import com.azikar24.wormaceptor.feature.viewer.ui.components.TransactionListSkeleton
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.feature.viewer.ui.util.formatDuration
import com.azikar24.wormaceptor.feature.viewer.ui.util.getMethodColor
import com.azikar24.wormaceptor.feature.viewer.ui.util.getStatusColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * TransactionListScreen with pull-to-refresh support.
 *
 * @param transactions List of transactions to display
 * @param onItemClick Callback when a transaction is clicked
 * @param hasActiveFilters Whether filters are currently active
 * @param onClearFilters Callback to clear filters
 * @param isRefreshing Whether the list is currently refreshing
 * @param onRefresh Callback triggered on pull-to-refresh
 * @param modifier Modifier for the screen
 * @param header Optional header composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionListScreen(
    transactions: ImmutableList<TransactionSummary>,
    onItemClick: (TransactionSummary) -> Unit,
    hasActiveFilters: Boolean = false,
    onClearFilters: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val view = LocalView.current
    val pullToRefreshState = rememberPullToRefreshState()
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
                modifier = modifier,
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.xs),
                ) {
                    if (header != null) {
                        item {
                            header()
                        }
                    }
                    items(transactions, key = { it.id }) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onItemClick(transaction) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.xs),
            ) {
                if (header != null) {
                    item {
                        header()
                    }
                }
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onItemClick(transaction) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

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
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "No transactions",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        // Title
        Text(
            text = if (hasActiveFilters) "No matches found" else "No transactions yet",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        // Description
        Text(
            text = if (hasActiveFilters) {
                "Try adjusting your filters to see more results"
            } else {
                "Network requests will appear here"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )

        if (hasActiveFilters) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
            Button(
                onClick = onClearFilters,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
            ) {
                Text(
                    text = "Clear Filters",
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
private fun TransactionItem(transaction: TransactionSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val statusColor = getStatusColor(transaction.status, transaction.code)

    // Press interaction for scale animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "itemScale",
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs,
            )
            .scale(scale)
            .clip(WormaCeptorDesignSystem.Shapes.card)
            .border(
                width = WormaCeptorDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = WormaCeptorDesignSystem.Shapes.card,
            )
            .background(
                color = statusColor.asSubtleBackground(),
                shape = WormaCeptorDesignSystem.Shapes.card,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(WormaCeptorDesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 2dp left border as status indicator
        Box(
            modifier = Modifier
                .width(WormaCeptorDesignSystem.BorderWidth.thick)
                .height(48.dp)
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
                MethodBadge(transaction.method)
                Text(
                    text = transaction.path,
                    fontSize = 15.sp,
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
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                )
            }
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
            Text(
                text = formatDuration(transaction.tookMs),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun MethodBadge(method: String) {
    val color = getMethodColor(method)
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
    ) {
        Text(
            text = method.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.xs,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
        )
    }
}

@Composable
private fun HostChip(host: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
    ) {
        Text(
            text = host,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
        )
    }
}


// ============================================================================
// PAGED VERSION WITH LAZY LOADING
// ============================================================================

/**
 * Paged version of TransactionListScreen that uses Paging 3 for efficient
 * database-level pagination. Use this for large datasets (1000+ transactions)
 * to achieve smooth 60fps scrolling performance.
 *
 * @param lazyPagingItems The paged items from collectAsLazyPagingItems()
 * @param onItemClick Callback when a transaction is clicked
 * @param hasActiveFilters Whether filters are currently active
 * @param onClearFilters Callback to clear filters
 * @param modifier Modifier for the screen
 * @param header Optional header composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagedTransactionListScreen(
    lazyPagingItems: LazyPagingItems<TransactionSummary>,
    onItemClick: (TransactionSummary) -> Unit,
    hasActiveFilters: Boolean = false,
    onClearFilters: () -> Unit = {},
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Show scroll-to-top FAB when scrolled down
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 5 }
    }

    Box(modifier = modifier.fillMaxSize()) {
        when (val refreshState = lazyPagingItems.loadState.refresh) {
            is LoadState.Loading -> {
                // Initial loading state - show skeleton
                TransactionListSkeleton(itemCount = 5)
            }

            is LoadState.Error -> {
                // Error state with retry
                ErrorState(
                    message = refreshState.error.localizedMessage ?: "Failed to load transactions",
                    onRetry = { lazyPagingItems.refresh() },
                    errorType = ErrorType.GENERIC,
                )
            }

            is LoadState.NotLoading -> {
                if (lazyPagingItems.itemCount == 0) {
                    // Empty state
                    EmptyState(
                        hasActiveFilters = hasActiveFilters,
                        onClearFilters = onClearFilters,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    // Content loaded - show list
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.xs),
                    ) {
                        // Optional header
                        if (header != null) {
                            item(key = "header") {
                                header()
                            }
                        }

                        // Paged items
                        items(
                            count = lazyPagingItems.itemCount,
                            key = lazyPagingItems.itemKey { it.id },
                        ) { index ->
                            lazyPagingItems[index]?.let { transaction ->
                                TransactionItem(
                                    transaction = transaction,
                                    onClick = { onItemClick(transaction) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }

                        // Append loading state
                        when (val appendState = lazyPagingItems.loadState.append) {
                            is LoadState.Loading -> {
                                item(key = "loading_more") {
                                    LoadingMoreIndicator()
                                }
                            }

                            is LoadState.Error -> {
                                item(key = "append_error") {
                                    InlineErrorRetry(
                                        message = appendState.error.localizedMessage ?: "Failed to load more",
                                        onRetry = { lazyPagingItems.retry() },
                                    )
                                }
                            }

                            is LoadState.NotLoading -> {
                                // No indicator needed when not loading
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
                    listState.animateScrollToItem(0)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        )
    }
}

/**
 * Paged TransactionListScreen with pull-to-refresh support.
 *
 * @param lazyPagingItems The paged items from collectAsLazyPagingItems()
 * @param onItemClick Callback when a transaction is clicked
 * @param hasActiveFilters Whether filters are currently active
 * @param onClearFilters Callback to clear filters
 * @param isRefreshing Whether the list is currently refreshing
 * @param onRefresh Callback triggered on pull-to-refresh
 * @param modifier Modifier for the screen
 * @param header Optional header composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagedTransactionListScreenWithRefresh(
    lazyPagingItems: LazyPagingItems<TransactionSummary>,
    onItemClick: (TransactionSummary) -> Unit,
    hasActiveFilters: Boolean = false,
    onClearFilters: () -> Unit = {},
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
) {
    val view = LocalView.current
    val pullToRefreshState = rememberPullToRefreshState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    // Show scroll-to-top FAB when scrolled down
    val showScrollToTop by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 5 }
    }

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

    // Determine if we're actually loading
    val isActuallyRefreshing = isRefreshing || lazyPagingItems.loadState.refresh is LoadState.Loading

    Box(modifier = modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isActuallyRefreshing,
            onRefresh = {
                onRefresh()
                lazyPagingItems.refresh()
            },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isActuallyRefreshing,
                    state = pullToRefreshState,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
        ) {
            when (val refreshState = lazyPagingItems.loadState.refresh) {
                is LoadState.Loading -> {
                    // Initial loading state - show skeleton
                    TransactionListSkeleton(itemCount = 5)
                }

                is LoadState.Error -> {
                    // Error state with retry
                    ErrorState(
                        message = refreshState.error.localizedMessage ?: "Failed to load transactions",
                        onRetry = { lazyPagingItems.refresh() },
                        errorType = ErrorType.GENERIC,
                    )
                }

                is LoadState.NotLoading -> {
                    if (lazyPagingItems.itemCount == 0) {
                        // Empty state
                        EmptyState(
                            hasActiveFilters = hasActiveFilters,
                            onClearFilters = onClearFilters,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        // Content loaded - show list
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.xs),
                        ) {
                            // Optional header
                            if (header != null) {
                                item(key = "header") {
                                    header()
                                }
                            }

                            // Paged items
                            items(
                                count = lazyPagingItems.itemCount,
                                key = lazyPagingItems.itemKey { it.id },
                            ) { index ->
                                lazyPagingItems[index]?.let { transaction ->
                                    TransactionItem(
                                        transaction = transaction,
                                        onClick = { onItemClick(transaction) },
                                        modifier = Modifier.animateItem(),
                                    )
                                }
                            }

                            // Append loading state
                            when (val appendState = lazyPagingItems.loadState.append) {
                                is LoadState.Loading -> {
                                    item(key = "loading_more") {
                                        LoadingMoreIndicator()
                                    }
                                }

                                is LoadState.Error -> {
                                    item(key = "append_error") {
                                        InlineErrorRetry(
                                            message = appendState.error.localizedMessage ?: "Failed to load more",
                                            onRetry = { lazyPagingItems.retry() },
                                        )
                                    }
                                }

                                is LoadState.NotLoading -> {
                                    // No indicator needed when not loading
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
                    listState.animateScrollToItem(0)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(WormaCeptorDesignSystem.Spacing.lg),
        )
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
            contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.xs),
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
