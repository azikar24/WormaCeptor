package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.BackHandler
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import com.azikar24.wormaceptor.domain.entities.Crash
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.theme.asLightBackground
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
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Sticky Header with Result Count
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = WormaCeptorDesignSystem.Elevation.sm
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = WormaCeptorDesignSystem.Spacing.lg,
                                    vertical = WormaCeptorDesignSystem.Spacing.md
                                )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Search & Filter",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )

                                Surface(
                                    shape = WormaCeptorDesignSystem.Shapes.chip,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Showing ${transactions.size} of ${allTransactions.size}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = WormaCeptorDesignSystem.Spacing.md,
                                            vertical = WormaCeptorDesignSystem.Spacing.xs
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Scrollable Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                bottom = WormaCeptorDesignSystem.Spacing.xxl,
                                start = WormaCeptorDesignSystem.Spacing.lg,
                                end = WormaCeptorDesignSystem.Spacing.lg,
                                top = WormaCeptorDesignSystem.Spacing.lg
                            )
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChanged,
                            label = { Text("Search path, method, or status") },
                            placeholder = { Text("Type to search...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { onSearchChanged("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                imeAction = androidx.compose.ui.text.input.ImeAction.Search
                            ),
                            keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                onSearch = {
                                    focusManager.clearFocus()
                                }
                            ),
                            shape = WormaCeptorDesignSystem.Shapes.button
                        )

                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

                        // HTTP Method Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                                Text(
                                    text = "HTTP Method",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
                        ) {
                            val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
                            methods.forEach { method ->
                                val count = methodCounts[method] ?: 0
                                FilterChip(
                                    selected = filterMethod == method,
                                    onClick = {
                                        onMethodFilterChanged(if (filterMethod == method) null else method)
                                    },
                                    label = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(method)
                                            Surface(
                                                shape = CircleShape,
                                                color = if (filterMethod == method)
                                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                            ) {
                                                Text(
                                                    text = count.toString(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(
                                                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                                        vertical = WormaCeptorDesignSystem.Spacing.xxs
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    shape = WormaCeptorDesignSystem.Shapes.chip
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

                        // Status Code Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                                Text(
                                    text = "Status Code",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
                        ) {
                            val statusFilters = listOf(
                                "2xx Success" to (200..299),
                                "3xx Redirect" to (300..399),
                                "4xx Client Error" to (400..499),
                                "5xx Server Error" to (500..599)
                            )
                            statusFilters.forEach { (label, range) ->
                                val count = statusCounts[range] ?: 0
                                FilterChip(
                                    selected = filterStatusRange == range,
                                    onClick = {
                                        onStatusFilterChanged(if (filterStatusRange == range) null else range)
                                    },
                                    label = {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(label)
                                            Surface(
                                                shape = CircleShape,
                                                color = if (filterStatusRange == range)
                                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                                            ) {
                                                Text(
                                                    text = count.toString(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(
                                                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                                        vertical = WormaCeptorDesignSystem.Spacing.xxs
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    shape = WormaCeptorDesignSystem.Shapes.chip
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md)
                        ) {
                            val filtersActive = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()

                            OutlinedButton(
                                onClick = {
                                    onClearFilters()
                                    onSearchChanged("")
                                },
                                modifier = Modifier.weight(1f),
                                enabled = filtersActive,
                                shape = WormaCeptorDesignSystem.Shapes.button
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                                Text("Clear All")
                            }

                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    showFilterSheet = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = WormaCeptorDesignSystem.Shapes.button
                            ) {
                                Text("Apply Filters")
                            }
                        }
                    }
                }
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
