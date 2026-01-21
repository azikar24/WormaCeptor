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
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.components.BulkActionBar
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.util.KeyboardShortcutCallbacks
import com.azikar24.wormaceptor.feature.viewer.ui.util.KeyboardShortcutHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * HomeScreen with multi-select, context menus, and keyboard shortcuts.
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
    filterMethod: String?,
    filterStatusRange: IntRange?,
    onMethodFilterChanged: (String?) -> Unit,
    onStatusFilterChanged: (IntRange?) -> Unit,
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
    // Tools navigation - quick access in overflow menu
    onNavigateToLogs: () -> Unit = {},
    onNavigateToDeviceInfo: () -> Unit = {},
    // Generic tool navigation for Tools tab
    onToolNavigate: (String) -> Unit = {},
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
    val titles = remember(showToolsTab) {
        buildList {
            add("Transactions")
            add("Crashes")
            if (showToolsTab) add("Tools")
        }
    }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showClearTransactionsDialog by remember { mutableStateOf(false) }
    var showClearCrashesDialog by remember { mutableStateOf(false) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

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

    // Keyboard shortcuts callbacks
    val keyboardCallbacks = remember(isSelectionMode, selectedIds) {
        KeyboardShortcutCallbacks(
            onRefresh = onRefreshTransactions,
            onSearch = { showFilterSheet = true },
            onSelectAll = onSelectAll,
            onDelete = {
                if (isSelectionMode && selectedIds.isNotEmpty()) {
                    showDeleteSelectedDialog = true
                }
            },
            onClear = {
                if (isSelectionMode) {
                    onClearSelection()
                } else {
                    onClearFilters()
                    onSearchChanged("")
                }
            },
            onExport = {
                scope.launch { onExportTransactions() }
            },
        )
    }

    KeyboardShortcutHandler(
        callbacks = keyboardCallbacks,
        enabled = selectedTabIndex == 0,
    ) {
        Scaffold(
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
                        onCancel = onClearSelection,
                    )

                    // Regular top bar when not in selection mode
                    AnimatedVisibility(
                        visible = !isSelectionMode,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        TopAppBar(
                            title = { Text("WormaCeptor V2") },
                            navigationIcon = {
                                IconButton(onClick = { (context as? Activity)?.finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                    )
                                }
                            },
                            actions = {
                                if (pagerState.currentPage == 0) {
                                    val isFiltering = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()
                                    val filterCount = listOfNotNull(
                                        filterMethod,
                                        filterStatusRange,
                                        searchQuery.takeIf { it.isNotBlank() },
                                    ).size

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
                                                contentDescription = "Filter",
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

                                // Overflow Menu
                                IconButton(onClick = { showOverflowMenu = true }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More options",
                                    )
                                }

                                DropdownMenu(
                                    expanded = showOverflowMenu,
                                    onDismissRequest = { showOverflowMenu = false },
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                ) {
                                    when (pagerState.currentPage) {
                                        0 -> {
                                            // Transactions tab menu
                                            DropdownMenuItem(
                                                text = { Text("Export Transactions") },
                                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    scope.launch { onExportTransactions() }
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Clear All Transactions") },
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
                                                text = { Text("Export Crashes") },
                                                leadingIcon = { Icon(Icons.Default.Share, null) },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    scope.launch { onExportCrashes() }
                                                },
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Clear All Crashes") },
                                                leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                                                onClick = {
                                                    showOverflowMenu = false
                                                    showClearCrashesDialog = true
                                                },
                                            )
                                        }
                                        // No menu items for Tools tab
                                    }
                                    // Quick access tools - always shown except on Tools tab
                                    val toolsTabIndex = if (showToolsTab) 2 else -1
                                    if (pagerState.currentPage != toolsTabIndex) {
                                        androidx.compose.material3.HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 4.dp),
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Console Logs") },
                                        leadingIcon = { Icon(Icons.Default.Terminal, null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            onNavigateToLogs()
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Device Info") },
                                        leadingIcon = { Icon(Icons.Default.Info, null) },
                                        onClick = {
                                            showOverflowMenu = false
                                            onNavigateToDeviceInfo()
                                        },
                                    )
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
                    val hasActiveFilters = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()
                    AnimatedVisibility(
                        visible = hasActiveFilters,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut(),
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            tonalElevation = WormaCeptorDesignSystem.Elevation.xs,
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
                                    text = "Active filters:",
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
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            },
                                            shape = WormaCeptorDesignSystem.Shapes.chip,
                                            modifier = Modifier.semantics {
                                                role = Role.Button
                                                selected = true
                                                contentDescription = "Search filter: $searchQuery, selected. Double tap to remove"
                                            },
                                        )
                                    }

                                    filterMethod?.let { method ->
                                        AssistChip(
                                            onClick = { onMethodFilterChanged(null) },
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
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            },
                                            shape = WormaCeptorDesignSystem.Shapes.chip,
                                            modifier = Modifier.semantics {
                                                role = Role.Button
                                                selected = true
                                                contentDescription = "HTTP method filter: $method, selected. Double tap to remove"
                                            },
                                        )
                                    }

                                    filterStatusRange?.let { range ->
                                        val statusLabel = when {
                                            range == (200..299) -> "2xx"
                                            range == (300..399) -> "3xx"
                                            range == (400..499) -> "4xx"
                                            range == (500..599) -> "5xx"
                                            else -> "Status"
                                        }
                                        AssistChip(
                                            onClick = { onStatusFilterChanged(null) },
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
                                                    modifier = Modifier.size(16.dp),
                                                )
                                            },
                                            shape = WormaCeptorDesignSystem.Shapes.chip,
                                            modifier = Modifier.semantics {
                                                role = Role.Button
                                                selected = true
                                                contentDescription = "Status code filter: $statusLabel, selected. Double tap to remove"
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
                                        contentDescription = "Clear all filters",
                                        modifier = Modifier.size(20.dp),
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
                            hasActiveFilters = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank(),
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
                        searchQuery = searchQuery,
                        onSearchChanged = onSearchChanged,
                        filterMethod = filterMethod,
                        filterStatusRange = filterStatusRange,
                        onMethodFilterChanged = onMethodFilterChanged,
                        onStatusFilterChanged = onStatusFilterChanged,
                        onClearFilters = onClearFilters,
                        onApply = {
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
                    title = { Text("Clear All Transactions?") },
                    text = {
                        Text(
                            "This will permanently delete all captured network transactions. This action cannot be undone.",
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
                            Text("Clear")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearTransactionsDialog = false }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            // Clear Crashes Confirmation Dialog (SelectableHomeScreen)
            if (showClearCrashesDialog) {
                AlertDialog(
                    onDismissRequest = { showClearCrashesDialog = false },
                    title = { Text("Clear All Crashes?") },
                    text = {
                        Text(
                            "This will permanently delete all captured crash reports. This action cannot be undone.",
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
                            Text("Clear")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showClearCrashesDialog = false }) {
                            Text("Cancel")
                        }
                    },
                )
            }

            // Delete Selected Confirmation Dialog
            if (showDeleteSelectedDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteSelectedDialog = false },
                    title = { Text("Delete ${selectedIds.size} Transactions?") },
                    text = {
                        Text(
                            "This will permanently delete the selected transactions. This action cannot be undone.",
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
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteSelectedDialog = false }) {
                            Text("Cancel")
                        }
                    },
                )
            }
        }
    }
}
