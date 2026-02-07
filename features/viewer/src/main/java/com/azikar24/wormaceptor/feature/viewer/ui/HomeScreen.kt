package com.azikar24.wormaceptor.feature.viewer.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.BulkActionBar
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * HomeScreen with multi-select and context menus.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    transactions: ImmutableList<TransactionSummary>,
    crashes: ImmutableList<Crash>,
    searchQuery: String,
    onSearchChanged: (String) -> Unit,
    onTransactionClick: (TransactionSummary) -> Unit,
    onCrashClick: (Crash) -> Unit,
    filterMethods: Set<String>,
    filterStatusRanges: Set<IntRange>,
    onMethodFiltersChanged: (Set<String>) -> Unit,
    onStatusFiltersChanged: (Set<IntRange>) -> Unit,
    onClearFilters: () -> Unit,
    onClearTransactions: suspend () -> Unit,
    onClearCrashes: suspend () -> Unit,
    onExportTransactions: suspend () -> Unit,
    onExportCrashes: suspend () -> Unit,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    allTransactions: ImmutableList<TransactionSummary> = transactions,
    // Pull-to-refresh parameters
    isRefreshingTransactions: Boolean = false,
    isRefreshingCrashes: Boolean = false,
    onRefreshTransactions: () -> Unit = {},
    onRefreshCrashes: () -> Unit = {},
    // Selection parameters
    selectedIds: Set<UUID> = emptySet(),
    isSelectionMode: Boolean = false,
    onSelectionToggle: (UUID) -> Unit = {},
    onSelectAll: () -> Unit = {},
    onClearSelection: () -> Unit = {},
    onDeleteSelected: suspend () -> Unit = {},
    onShareSelected: () -> Unit = {},
    onExportSelected: () -> Unit = {},
    // Context menu action parameters
    onCopyUrl: (TransactionSummary) -> Unit = {},
    onShare: (TransactionSummary) -> Unit = {},
    onDelete: (TransactionSummary) -> Unit = {},
    onCopyAsCurl: (TransactionSummary) -> Unit = {},
    // Generic tool navigation for Tools tab
    onToolNavigate: (String) -> Unit = {},
    // Snackbar message flow from ViewModel
    snackbarMessage: SharedFlow<String>? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Hardware/System Back Button behavior
    BackHandler {
        if (isSelectionMode) {
            onClearSelection()
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
    var showFilterSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showClearTransactionsDialog by remember { mutableStateOf(false) }
    var showClearCrashesDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe snackbar messages from ViewModel
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    // Pager state for swipe between tabs - defined here so TabRow can access it
    val pagerState = rememberPagerState(
        initialPage = selectedTabIndex,
        pageCount = { titles.size },
    )

    // Sync pagerState with selectedTabIndex when tab is clicked
    LaunchedEffect(selectedTabIndex) {
        if (pagerState.currentPage != selectedTabIndex) {
            pagerState.animateScrollToPage(selectedTabIndex)
        }
    }

    // Sync selectedTabIndex with pagerState when user swipes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page != selectedTabIndex) {
                    if (isSelectionMode) onClearSelection()
                    onTabSelected(page)
                }
            }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                // Show bulk action bar when in selection mode
                BulkActionBar(
                    selectedCount = selectedIds.size,
                    totalCount = transactions.size,
                    onShare = onShareSelected,
                    onDelete = { showDeleteSelectedDialog = true },
                    onExport = onExportSelected,
                    onSelectAll = onSelectAll,
                    onDeselectAll = onClearSelection,
                    onCancel = onClearSelection,
                )

                // Regular top bar when not in selection mode
                AnimatedVisibility(
                    visible = !isSelectionMode,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
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
                            if (pagerState.currentPage == 0) {
                                val isFiltering = filterMethods.isNotEmpty() || filterStatusRanges.isNotEmpty() || searchQuery.isNotBlank()
                                val filterCount = filterMethods.size + filterStatusRanges.size + if (searchQuery.isNotBlank()) 1 else 0

                                BadgedBox(
                                    badge = {
                                        if (isFiltering) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.xs),
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
                                    IconButton(onClick = { showFilterSheet = true }) {
                                        val iconScale by animateFloatAsState(
                                            targetValue = if (isFiltering) 1.1f else 1f,
                                            animationSpec = tween(
                                                durationMillis = WormaCeptorDesignSystem.AnimationDuration.fast,
                                            ),
                                        )
                                        Icon(
                                            imageVector = Icons.Default.FilterList,
                                            contentDescription = stringResource(R.string.viewer_home_filter),
                                            tint = if (isFiltering) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            modifier = Modifier.scale(iconScale),
                                        )
                                    }
                                }
                            }

                            // Overflow Menu - only show on Transactions and Crashes tabs
                            if (pagerState.currentPage < 2) {
                                IconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = stringResource(R.string.viewer_home_more_options),
                                    )
                                }

                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false },
                                    shape = WormaCeptorDesignSystem.Shapes.cardLarge,
                                ) {
                                    when (pagerState.currentPage) {
                                        0 -> {
                                            // Transactions tab menu
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        stringResource(R.string.viewer_home_export_transactions),
                                                    )
                                                },
                                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    scope.launch { onExportTransactions() }
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        stringResource(R.string.viewer_home_clear_all_transactions),
                                                    )
                                                },
                                                leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    showClearTransactionsDialog = true
                                                },
                                            )
                                        }
                                        1 -> {
                                            // Crashes tab menu
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.viewer_home_export_crashes)) },
                                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    scope.launch { onExportCrashes() }
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.viewer_home_clear_all_crashes)) },
                                                leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    showClearCrashesDialog = true
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        },
                    )
                }

                TabRow(selectedTabIndex = pagerState.currentPage) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                if (isSelectionMode) onClearSelection()
                                scope.launch { pagerState.animateScrollToPage(index) }
                            },
                            text = { Text(title) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Active Filters Banner
            if (pagerState.currentPage == 0 && !isSelectionMode) {
                val hasActiveFilters = filterMethods.isNotEmpty() || filterStatusRanges.isNotEmpty() || searchQuery.isNotBlank()
                AnimatedVisibility(
                    visible = hasActiveFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
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

                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                            ) {
                                if (searchQuery.isNotBlank()) {
                                    AssistChip(
                                        onClick = { onSearchChanged("") },
                                        label = {
                                            Text(
                                                text = "\"$searchQuery\"",
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
                                                searchQuery,
                                            )
                                        },
                                    )
                                }

                                filterMethods.forEach { method ->
                                    AssistChip(
                                        onClick = { onMethodFiltersChanged(filterMethods - method) },
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

                                filterStatusRanges.forEach { range ->
                                    val statusLabel = when (range) {
                                        200..299 -> "2xx"
                                        300..399 -> "3xx"
                                        400..499 -> "4xx"
                                        500..599 -> "5xx"
                                        else -> context.getString(R.string.viewer_home_status_label)
                                    }
                                    AssistChip(
                                        onClick = {
                                            onStatusFiltersChanged(
                                                filterStatusRanges.filter { it != range }.toSet(),
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
                                    onClearFilters()
                                    onSearchChanged("")
                                },
                                modifier = Modifier.size(48.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.viewer_home_clear_all_filters),
                                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                                )
                            }
                        }
                    }
                }
            }

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
                        hasActiveFilters = filterMethods.isNotEmpty() || filterStatusRanges.isNotEmpty() || searchQuery.isNotBlank(),
                        onClearFilters = {
                            onClearFilters()
                            onSearchChanged("")
                        },
                        isRefreshing = isRefreshingTransactions,
                        onRefresh = onRefreshTransactions,
                        selectedIds = selectedIds,
                        isSelectionMode = isSelectionMode,
                        onSelectionToggle = onSelectionToggle,
                        onLongClick = { id ->
                            if (!isSelectionMode) {
                                onSelectionToggle(id)
                            }
                        },
                        onCopyUrl = onCopyUrl,
                        onShare = onShare,
                        onDelete = onDelete,
                        onCopyAsCurl = onCopyAsCurl,
                        modifier = Modifier.fillMaxSize(),
                        header = { MetricsCard(transactions = transactions) },
                    )
                    1 -> CrashListScreen(
                        crashes = crashes,
                        onCrashClick = onCrashClick,
                        isRefreshing = isRefreshingCrashes,
                        onRefresh = onRefreshCrashes,
                    )
                    2 -> if (showToolsTab) {
                        ToolsTab(
                            onNavigate = onToolNavigate,
                            onShowMessage = { message ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(message)
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }

        // Filter bottom sheet (SelectableHomeScreen)
        if (showFilterSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val focusManager = LocalFocusManager.current

            val methodCounts = remember(allTransactions) {
                allTransactions.groupBy { it.method }.mapValues { it.value.size }.toImmutableMap()
            }
            val statusCounts = remember(allTransactions) {
                mapOf(
                    200..299 to allTransactions.count { (it.code ?: 0) in 200..299 },
                    300..399 to allTransactions.count { (it.code ?: 0) in 300..399 },
                    400..499 to allTransactions.count { (it.code ?: 0) in 400..499 },
                    500..599 to allTransactions.count { (it.code ?: 0) in 500..599 },
                ).toImmutableMap()
            }

            ModalBottomSheet(
                modifier = Modifier.imePadding(),
                onDismissRequest = {
                    focusManager.clearFocus()
                    showFilterSheet = false
                },
                sheetState = sheetState,
                shape = WormaCeptorDesignSystem.Shapes.sheet,
            ) {
                FilterBottomSheetContent(
                    initialSearchQuery = searchQuery,
                    initialFilterMethods = filterMethods,
                    initialFilterStatusRanges = filterStatusRanges,
                    onApply = { query, methods, statusRanges ->
                        onSearchChanged(query)
                        onMethodFiltersChanged(methods)
                        onStatusFiltersChanged(statusRanges)
                        focusManager.clearFocus()
                        showFilterSheet = false
                    },
                    filteredCount = transactions.size,
                    totalCount = allTransactions.size,
                    methodCounts = methodCounts,
                    statusCounts = statusCounts,
                )
            }
        }

        // Clear Transactions Confirmation Dialog (SelectableHomeScreen)
        if (showClearTransactionsDialog) {
            AlertDialog(
                onDismissRequest = { showClearTransactionsDialog = false },
                title = { Text(stringResource(R.string.viewer_dialog_clear_transactions_title)) },
                text = {
                    Text(
                        stringResource(R.string.viewer_dialog_clear_transactions_message),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                onClearTransactions()
                                showClearTransactionsDialog = false
                            }
                        },
                    ) {
                        Text(stringResource(R.string.viewer_dialog_button_clear))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearTransactionsDialog = false }) {
                        Text(stringResource(R.string.viewer_dialog_button_cancel))
                    }
                },
            )
        }

        // Clear Crashes Confirmation Dialog (SelectableHomeScreen)
        if (showClearCrashesDialog) {
            AlertDialog(
                onDismissRequest = { showClearCrashesDialog = false },
                title = { Text(stringResource(R.string.viewer_dialog_clear_crashes_title)) },
                text = {
                    Text(
                        stringResource(R.string.viewer_dialog_clear_crashes_message),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                onClearCrashes()
                                showClearCrashesDialog = false
                            }
                        },
                    ) {
                        Text(stringResource(R.string.viewer_dialog_button_clear))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCrashesDialog = false }) {
                        Text(stringResource(R.string.viewer_dialog_button_cancel))
                    }
                },
            )
        }

        // Delete Selected Confirmation Dialog
        if (showDeleteSelectedDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteSelectedDialog = false },
                title = { Text(stringResource(R.string.viewer_dialog_delete_selected_title, selectedIds.size)) },
                text = {
                    Text(
                        stringResource(R.string.viewer_dialog_delete_selected_message),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                onDeleteSelected()
                                showDeleteSelectedDialog = false
                            }
                        },
                    ) {
                        Text(
                            stringResource(R.string.viewer_dialog_button_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteSelectedDialog = false }) {
                        Text(stringResource(R.string.viewer_dialog_button_cancel))
                    }
                },
            )
        }
    }
}
