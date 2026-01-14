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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    transactions: List<TransactionSummary>,
    crashes: List<Crash>,
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
    allTransactions: List<TransactionSummary> = transactions
) {
    val context = LocalContext.current
    
    // Hardware/System Back Button should exit the viewer when at Home
    BackHandler {
        (context as? Activity)?.finish()
    }
    val titles = listOf("Transactions", "Crashes")
    var showFilterSheet by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showClearTransactionsDialog by remember { mutableStateOf(false) }
    var showClearCrashesDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("WormaCeptor V2") },
                    navigationIcon = {
                        IconButton(onClick = { (context as? android.app.Activity)?.finish() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (selectedTabIndex == 0) {
                            val isFiltering = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()
                            val filterCount = listOfNotNull(
                                filterMethod,
                                filterStatusRange,
                                searchQuery.takeIf { it.isNotBlank() }
                            ).size

                            BadgedBox(
                                badge = {
                                    if (isFiltering) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier
                                                .padding(WormaCeptorDesignSystem.Spacing.xs)
                                        ) {
                                            Text(
                                                text = filterCount.toString(),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            ) {
                                IconButton(onClick = { showFilterSheet = true }) {
                                    val iconScale by animateFloatAsState(
                                        targetValue = if (isFiltering) 1.1f else 1f,
                                        animationSpec = tween(durationMillis = WormaCeptorDesignSystem.AnimationDuration.fast)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = if (isFiltering) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        modifier = Modifier.scale(iconScale)
                                    )
                                }
                            }
                        }

                        // Overflow Menu
                        IconButton(onClick = { showOverflowMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More options"
                            )
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            if (selectedTabIndex == 0) {
                                DropdownMenuItem(
                                    text = { Text("Export Transactions") },
                                    leadingIcon = { Icon(Icons.Default.Share, null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        scope.launch { onExportTransactions() }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear All Transactions") },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showClearTransactionsDialog = true
                                    }
                                )
                            } else {
                                DropdownMenuItem(
                                    text = { Text("Export Crashes") },
                                    leadingIcon = { Icon(Icons.Default.Share, null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        scope.launch { onExportCrashes() }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear All Crashes") },
                                    leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showClearCrashesDialog = true
                                    }
                                )
                            }
                        }
                    }
                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { onTabSelected(index) },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Active Filters Banner
            if (selectedTabIndex == 0) {
                val hasActiveFilters = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()
                AnimatedVisibility(
                    visible = hasActiveFilters,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        tonalElevation = WormaCeptorDesignSystem.Elevation.xs
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = WormaCeptorDesignSystem.Spacing.lg,
                                    vertical = WormaCeptorDesignSystem.Spacing.sm
                                ),
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Active filters:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            FlowRow(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs)
                            ) {
                                if (searchQuery.isNotBlank()) {
                                    AssistChip(
                                        onClick = { onSearchChanged("") },
                                        label = {
                                            Text(
                                                text = "\"$searchQuery\"",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        shape = WormaCeptorDesignSystem.Shapes.chip
                                    )
                                }

                                filterMethod?.let { method ->
                                    AssistChip(
                                        onClick = { onMethodFilterChanged(null) },
                                        label = {
                                            Text(
                                                text = method,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        shape = WormaCeptorDesignSystem.Shapes.chip
                                    )
                                }

                                filterStatusRange?.let { range ->
                                    val label = when {
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
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        shape = WormaCeptorDesignSystem.Shapes.chip
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    onClearFilters()
                                    onSearchChanged("")
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear all filters",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedTabIndex == 0) {
                TransactionListScreen(
                    transactions = transactions,
                    onItemClick = onTransactionClick,
                    hasActiveFilters = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank(),
                    onClearFilters = {
                        onClearFilters()
                        onSearchChanged("")
                    },
                    modifier = Modifier.weight(1f),
                    header = { MetricsCard(transactions = transactions) }
                )
            } else {
                CrashListScreen(
                    crashes = crashes,
                    onCrashClick = onCrashClick
                )
            }
        }

        if (showFilterSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val focusManager = LocalFocusManager.current

            // Calculate counts
            val methodCounts = remember(allTransactions) {
                allTransactions.groupBy { it.method }.mapValues { it.value.size }
            }
            val statusCounts = remember(allTransactions) {
                mapOf(
                    200..299 to allTransactions.count { (it.code ?: 0) in 200..299 },
                    300..399 to allTransactions.count { (it.code ?: 0) in 300..399 },
                    400..499 to allTransactions.count { (it.code ?: 0) in 400..499 },
                    500..599 to allTransactions.count { (it.code ?: 0) in 500..599 }
                )
            }

            ModalBottomSheet(
                modifier = Modifier.imePadding(),
                onDismissRequest = {
                    focusManager.clearFocus()
                    showFilterSheet = false
                },
                sheetState = sheetState,
                shape = WormaCeptorDesignSystem.Shapes.sheet
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
                    statusCounts = statusCounts
                )
            }
        }

        // Clear Transactions Confirmation Dialog
        if (showClearTransactionsDialog) {
            AlertDialog(
                onDismissRequest = { showClearTransactionsDialog = false },
                title = { Text("Clear All Transactions?") },
                text = { Text("This will permanently delete all captured network transactions. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                onClearTransactions()
                                showClearTransactionsDialog = false
                            }
                        }
                    ) {
                        Text("Clear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearTransactionsDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Clear Crashes Confirmation Dialog
        if (showClearCrashesDialog) {
            AlertDialog(
                onDismissRequest = { showClearCrashesDialog = false },
                title = { Text("Clear All Crashes?") },
                text = { Text("This will permanently delete all captured crash reports. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                onClearCrashes()
                                showClearCrashesDialog = false
                            }
                        }
                    ) {
                        Text("Clear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showClearCrashesDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
