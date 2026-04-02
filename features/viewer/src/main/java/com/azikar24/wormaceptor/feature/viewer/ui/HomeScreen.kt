package com.azikar24.wormaceptor.feature.viewer.ui

import android.app.Activity
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
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * HomeScreen with multi-select, context menus, and consolidated event dispatch.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    transactions: ImmutableList<TransactionSummary>,
    allTransactions: ImmutableList<TransactionSummary>,
    crashes: ImmutableList<Crash>,
    isSelectionMode: Boolean,
    state: ViewerViewState,
    onEvent: (ViewerViewEvent) -> Unit,
    onTransactionClick: (TransactionSummary) -> Unit,
    onCrashClick: (Crash) -> Unit,
    onToolNavigate: (String) -> Unit,
    snackBarMessage: Flow<String>? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Hardware/System Back Button behavior
    BackHandler {
        if (isSelectionMode) {
            onEvent(ViewerViewEvent.SelectionCleared)
        } else {
            (context as? Activity)?.finish()
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
        initialPage = state.selectedTabIndex,
        pageCount = { titles.size },
    )

    // Side-effects: pager sync + snackbar observer
    HomeScreenEffects(
        pagerState = pagerState,
        state = state,
        isSelectionMode = isSelectionMode,
        onEvent = onEvent,
        snackBarMessage = snackBarMessage,
        snackBarHostState = snackBarHostState,
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        floatingActionButton = {
            val isFiltering = state.filterMethods.isNotEmpty() ||
                state.filterStatusRanges.isNotEmpty() ||
                state.searchQuery.isNotBlank()
            val filterCount = state.filterMethods.size +
                state.filterStatusRanges.size +
                if (state.searchQuery.isNotBlank()) 1 else 0

            AnimatedVisibility(
                visible = pagerState.currentPage == 0 && !isSelectionMode,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                BadgedBox(
                    badge = {
                        if (isFiltering) {
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
                            onEvent(ViewerViewEvent.FilterSheetVisibilityChanged(true))
                        },
                        icon = Icons.Default.FilterList,
                        contentDescription = stringResource(R.string.viewer_home_filter),
                    )
                }
            }
        },
        topBar = {
            HomeTopBar(
                state = state,
                isSelectionMode = isSelectionMode,
                transactionCount = transactions.size,
                onEvent = onEvent,
                pagerState = pagerState,
                titles = titles,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Active Filters Banner
            ActiveFiltersBanner(
                state = state,
                isSelectionMode = isSelectionMode,
                currentPage = pagerState.currentPage,
                onEvent = onEvent,
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
                        transactions = transactions,
                        onItemClick = onTransactionClick,
                        isInitialLoading = state.isInitialLoading,
                        hasActiveFilters = state.filterMethods.isNotEmpty() ||
                            state.filterStatusRanges.isNotEmpty() ||
                            state.searchQuery.isNotBlank(),
                        onClearFilters = {
                            onEvent(ViewerViewEvent.ClearFilters)
                            onEvent(ViewerViewEvent.SearchQueryChanged(""))
                        },
                        isRefreshing = state.isRefreshingTransactions,
                        onRefresh = { onEvent(ViewerViewEvent.RefreshTransactions) },
                        selectedIds = state.selectedIds,
                        isSelectionMode = isSelectionMode,
                        onSelectionToggle = { onEvent(ViewerViewEvent.SelectionToggled(it)) },
                        onLongClick = { id ->
                            if (!isSelectionMode) {
                                onEvent(ViewerViewEvent.SelectionToggled(id))
                            }
                        },
                        onCopyUrl = { onEvent(ViewerViewEvent.CopyTransactionUrl(it)) },
                        onShare = { onEvent(ViewerViewEvent.ShareTransaction(it)) },
                        onShareAsHar = {
                            onEvent(ViewerViewEvent.ShareTransactionAsHar(it.id))
                        },
                        onDelete = { onEvent(ViewerViewEvent.DeleteTransaction(it.id)) },
                        onCopyAsCurl = {
                            onEvent(ViewerViewEvent.CopyTransactionAsCurl(it.id))
                        },
                        modifier = Modifier.fillMaxSize(),
                        header = { MetricsCard(transactions = transactions) },
                    )
                    1 -> CrashListScreen(
                        crashes = crashes,
                        onCrashClick = onCrashClick,
                        isRefreshing = state.isRefreshingCrashes,
                        onRefresh = { onEvent(ViewerViewEvent.RefreshCrashes) },
                    )
                    2 -> if (showToolsTab) {
                        ToolsTab(
                            onNavigate = onToolNavigate,
                            onShowMessage = { message ->
                                scope.launch {
                                    snackBarHostState.showSnackbar(message)
                                }
                            },
                            searchActive = state.toolsSearchActive,
                            searchQuery = state.toolsSearchQuery,
                            onSearchQueryChanged = {
                                onEvent(ViewerViewEvent.ToolsSearchQueryChanged(it))
                            },
                            collapsedCategories = state.collapsedToolCategories,
                            onToggleCollapse = {
                                onEvent(ViewerViewEvent.ToolCategoryCollapseToggled(it))
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        // Dialogs and bottom sheets
        HomeDialogs(
            state = state,
            onEvent = onEvent,
            transactions = transactions,
            allTransactions = allTransactions,
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
    state: ViewerViewState,
    isSelectionMode: Boolean,
    currentPage: Int,
    onEvent: (ViewerViewEvent) -> Unit,
) {
    val context = LocalContext.current

    if (currentPage != 0 || isSelectionMode) return

    val hasActiveFilters = state.filterMethods.isNotEmpty() ||
        state.filterStatusRanges.isNotEmpty() ||
        state.searchQuery.isNotBlank()

    AnimatedVisibility(
        visible = hasActiveFilters,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Surface(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.lg,
                        vertical = WormaCeptorDesignSystem.Spacing.sm,
                    ),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.viewer_home_active_filters),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )

                WormaCeptorFlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(
                        WormaCeptorDesignSystem.Spacing.xs,
                    ),
                ) {
                    if (state.searchQuery.isNotBlank()) {
                        AssistChip(
                            onClick = { onEvent(ViewerViewEvent.SearchQueryChanged("")) },
                            label = {
                                Text(
                                    text = "\"${state.searchQuery}\"",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                                )
                            },
                            shape = WormaCeptorDesignSystem.Shapes.chip,
                            modifier = Modifier.semantics {
                                role = Role.Button
                                selected = true
                                contentDescription = context.getString(
                                    R.string.viewer_home_search_filter_description,
                                    state.searchQuery,
                                )
                            },
                        )
                    }

                    state.filterMethods.forEach { method ->
                        AssistChip(
                            onClick = {
                                onEvent(
                                    ViewerViewEvent.MethodFiltersChanged(
                                        state.filterMethods - method,
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
                                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                                )
                            },
                            shape = WormaCeptorDesignSystem.Shapes.chip,
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

                    state.filterStatusRanges.forEach { range ->
                        val statusLabel = when (range) {
                            200..299 -> "2xx"
                            300..399 -> "3xx"
                            400..499 -> "4xx"
                            500..599 -> "5xx"
                            else -> context.getString(R.string.viewer_home_status_label)
                        }
                        AssistChip(
                            onClick = {
                                onEvent(
                                    ViewerViewEvent.StatusFiltersChanged(
                                        state.filterStatusRanges.filter { it != range }.toSet(),
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
                                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                                )
                            },
                            shape = WormaCeptorDesignSystem.Shapes.chip,
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

                IconButton(
                    onClick = {
                        onEvent(ViewerViewEvent.ClearFilters)
                        onEvent(ViewerViewEvent.SearchQueryChanged(""))
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(
                            R.string.viewer_home_clear_all_filters,
                        ),
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                    )
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
            transactions = kotlinx.collections.immutable.persistentListOf(
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
            allTransactions = kotlinx.collections.immutable.persistentListOf(),
            crashes = kotlinx.collections.immutable.persistentListOf(
                Crash(
                    id = 1L,
                    timestamp = System.currentTimeMillis() - 60_000,
                    exceptionType = "NullPointerException",
                    message = "Attempt to invoke on null",
                    stackTrace = "java.lang.NullPointerException\n\tat com.example.App.run(App.kt:10)",
                ),
            ),
            isSelectionMode = false,
            state = ViewerViewState(),
            onEvent = {},
            onTransactionClick = {},
            onCrashClick = {},
            onToolNavigate = {},
        )
    }
}
