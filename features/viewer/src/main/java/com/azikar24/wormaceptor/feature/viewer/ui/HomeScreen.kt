package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.vm.CrashListViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.CrashListViewState
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewState
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * HomeScreen with multi-select, context menus, and consolidated event dispatch.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    homeState: HomeViewState,
    transactionState: TransactionListViewState,
    crashState: CrashListViewState,
    onHomeEvent: (HomeViewEvent) -> Unit,
    onTransactionEvent: (TransactionListViewEvent) -> Unit,
    onCrashEvent: (CrashListViewEvent) -> Unit,
    snackBarMessage: Flow<String>? = null,
) {
    val scope = rememberCoroutineScope()
    val isSelectionMode = transactionState.selectedIds.isNotEmpty()

    // Hardware/System Back Button behavior
    BackHandler {
        if (isSelectionMode) {
            onTransactionEvent(TransactionListViewEvent.SelectionCleared)
        } else {
            onHomeEvent(HomeViewEvent.BackPressed)
        }
    }

    // Dynamic tabs based on enabled features
    val enabledFeatures = remember { WormaCeptorApi.getEnabledFeatures() }
    val showToolsTab = remember(enabledFeatures) {
        ToolCategories.hasAnyEnabledTools(enabledFeatures)
    }
    val transactionsTitle = stringResource(R.string.viewer_home_tab_transactions)
    val crashesTitle = stringResource(R.string.viewer_home_tab_crashes)
    val toolsTitle = stringResource(R.string.viewer_home_tab_tools)
    val titles = remember(showToolsTab, transactionsTitle, crashesTitle, toolsTitle) {
        buildList {
            add(transactionsTitle)
            add(crashesTitle)
            if (showToolsTab) add(toolsTitle)
        }
    }
    val snackBarHostState = remember { SnackbarHostState() }

    // Pager state for swipe between tabs
    val pagerState = rememberPagerState(
        initialPage = homeState.selectedTabIndex,
        pageCount = { titles.size },
    )

    // Side-effects: pager sync + snackbar observer
    HomeScreenEffects(
        pagerState = pagerState,
        homeState = homeState,
        transactionState = transactionState,
        onHomeEvent = onHomeEvent,
        onTransactionEvent = onTransactionEvent,
        snackBarMessage = snackBarMessage,
        snackBarHostState = snackBarHostState,
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            val filterCount = transactionState.filterMethods.size +
                transactionState.filterStatusRanges.size +
                if (transactionState.searchQuery.isNotBlank()) 1 else 0

            AnimatedVisibility(
                visible = pagerState.currentPage == 0 && !isSelectionMode,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                BadgedBox(
                    badge = {
                        if (transactionState.hasActiveFilters) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ) {
                                Text(
                                    text = filterCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    },
                ) {
                    WormaCeptorFAB(
                        onClick = {
                            onTransactionEvent(TransactionListViewEvent.Filter.SheetOpened)
                        },
                        icon = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.viewer_home_filter),
                    )
                }
            }
        },
        topBar = {
            HomeTopBar(
                homeState = homeState,
                transactionState = transactionState,
                onHomeEvent = onHomeEvent,
                onTransactionEvent = onTransactionEvent,
                onCrashEvent = onCrashEvent,
                pagerState = pagerState,
                titles = titles,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Active Filters Banner
            ActiveFiltersBanner(
                transactionState = transactionState,
                currentPage = pagerState.currentPage,
                onTransactionEvent = onTransactionEvent,
            )

            // HorizontalPager for swipe between Transactions and Crashes
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                beyondViewportPageCount = 1,
            ) { page ->
                when (page) {
                    0 -> SelectableTransactionListScreen(
                        transactions = transactionState.transactions,
                        onItemClick = { onHomeEvent(HomeViewEvent.TransactionClicked(it)) },
                        selectionState = TransactionSelectionState(
                            selectedIds = transactionState.selectedIds,
                            isSelectionMode = isSelectionMode,
                            onSelectionToggle = {
                                onTransactionEvent(TransactionListViewEvent.SelectionToggled(it))
                            },
                            onLongClick = { id ->
                                if (!isSelectionMode) {
                                    onTransactionEvent(
                                        TransactionListViewEvent.SelectionToggled(id),
                                    )
                                }
                            },
                        ),
                        itemActions = TransactionItemActions(
                            onCopyUrl = {
                                onTransactionEvent(TransactionListViewEvent.CopyTransactionUrl(it))
                            },
                            onShare = {
                                onTransactionEvent(TransactionListViewEvent.ShareTransaction(it))
                            },
                            onShareAsHar = {
                                onTransactionEvent(
                                    TransactionListViewEvent.ShareTransactionAsHar(it.id),
                                )
                            },
                            onDelete = {
                                onTransactionEvent(
                                    TransactionListViewEvent.DeleteTransaction(it.id),
                                )
                            },
                            onCopyAsCurl = {
                                onTransactionEvent(
                                    TransactionListViewEvent.CopyTransactionAsCurl(it.id),
                                )
                            },
                        ),
                        isInitialLoading = transactionState.isInitialLoading,
                        hasActiveFilters = transactionState.hasActiveFilters,
                        onClearFilters = {
                            onTransactionEvent(TransactionListViewEvent.ClearFilters)
                        },
                        isRefreshing = transactionState.isRefreshingTransactions,
                        onRefresh = {
                            onTransactionEvent(TransactionListViewEvent.RefreshTransactions)
                        },
                        modifier = Modifier.fillMaxSize(),
                        header = {
                            MetricsCard(transactions = transactionState.transactions)
                        },
                    )

                    1 -> CrashListScreen(
                        crashes = crashState.crashes,
                        onCrashClick = { onHomeEvent(HomeViewEvent.CrashClicked(it)) },
                        isRefreshing = crashState.isRefreshingCrashes,
                        onRefresh = { onCrashEvent(CrashListViewEvent.RefreshCrashes) },
                    )

                    2 -> ToolsTab(
                        state = homeState,
                        onEvent = onHomeEvent,
                        onNavigate = { onHomeEvent(HomeViewEvent.ToolNavigated(it)) },
                        onShowMessage = { message ->
                            scope.launch {
                                snackBarHostState.showSnackbar(message)
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        // Dialogs and bottom sheets
        HomeDialogs(
            transactionState = transactionState,
            crashState = crashState,
            onTransactionEvent = onTransactionEvent,
            onCrashEvent = onCrashEvent,
        )
    }
}

/**
 * Banner showing active filters with dismiss chips.
 * Only visible on the Transactions tab when not in selection mode.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveFiltersBanner(
    transactionState: TransactionListViewState,
    currentPage: Int,
    onTransactionEvent: (TransactionListViewEvent) -> Unit,
) {
    val context = LocalContext.current

    if (currentPage != 0 || transactionState.selectedIds.isNotEmpty()) return

    AnimatedVisibility(
        visible = transactionState.hasActiveFilters,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = WormaCeptorTokens.Spacing.lg,
                        vertical = WormaCeptorTokens.Spacing.xs,
                    ),
                horizontalAlignment = Alignment.Start,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.viewer_home_active_filters),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                    )
                    IconButton(
                        onClick = {
                            onTransactionEvent(TransactionListViewEvent.ClearFilters)
                        },
                        modifier = Modifier.size(WormaCeptorTokens.IconSize.xxxl),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(
                                R.string.viewer_home_clear_all_filters,
                            ),
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                        )
                    }
                }

                WormaCeptorFlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        WormaCeptorTokens.Spacing.xs,
                    ),
                ) {
                    if (transactionState.searchQuery.isNotBlank()) {
                        AssistChip(
                            onClick = {
                                onTransactionEvent(
                                    TransactionListViewEvent.SearchQueryChanged(""),
                                )
                            },
                            label = {
                                Text(
                                    text = "\"${transactionState.searchQuery}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                                )
                            },
                            shape = WormaCeptorTokens.Shapes.chip,
                            modifier = Modifier.semantics {
                                role = Role.Button
                                selected = true
                                contentDescription = context.getString(
                                    R.string.viewer_home_search_filter_description,
                                    transactionState.searchQuery,
                                )
                            },
                        )
                    }

                    transactionState.filterMethods.forEach { method ->
                        AssistChip(
                            onClick = {
                                onTransactionEvent(
                                    TransactionListViewEvent.MethodFiltersChanged(
                                        transactionState.filterMethods - method,
                                    ),
                                )
                            },
                            label = {
                                Text(
                                    text = method,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                                )
                            },
                            shape = WormaCeptorTokens.Shapes.chip,
                            modifier = Modifier.semantics {
                                role = Role.Button
                                selected = true
                                contentDescription = context.getString(
                                    R.string.viewer_home_method_filter_description,
                                    method,
                                )
                            },
                        )
                    }

                    transactionState.filterStatusRanges.forEach { range ->
                        val statusLabel = when (range) {
                            200..299 -> "2xx"
                            300..399 -> "3xx"
                            400..499 -> "4xx"
                            500..599 -> "5xx"
                            else -> context.getString(R.string.viewer_home_status_label)
                        }
                        AssistChip(
                            onClick = {
                                onTransactionEvent(
                                    TransactionListViewEvent.StatusFiltersChanged(
                                        transactionState.filterStatusRanges.minusElement(range),
                                    ),
                                )
                            },
                            label = {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                                )
                            },
                            shape = WormaCeptorTokens.Shapes.chip,
                            modifier = Modifier.semantics {
                                role = Role.Button
                                selected = true
                                contentDescription = context.getString(
                                    R.string.viewer_home_status_filter_description,
                                    statusLabel,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    WormaCeptorTheme {
        HomeScreen(
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
                        timestamp = System.currentTimeMillis() - 30_000,
                    ),
                ),
            ),
            crashState = CrashListViewState(
                crashes = persistentListOf(
                    Crash(
                        id = 1L,
                        timestamp = System.currentTimeMillis() - 60_000,
                        exceptionType = "NullPointerException",
                        message = "Attempt to invoke on null",
                        stackTrace = "java.lang.NullPointerException\n\tat com.example.App.run(App.kt:10)",
                    ),
                ),
            ),
            onHomeEvent = {},
            onTransactionEvent = {},
            onCrashEvent = {},
        )
    }
}
