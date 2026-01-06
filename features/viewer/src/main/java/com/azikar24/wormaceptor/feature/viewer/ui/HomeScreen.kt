package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    onTabSelected: (Int) -> Unit
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
                            IconButton(onClick = { showFilterSheet = true }) {
                                Box {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Filter",
                                        tint = if (isFiltering) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (isFiltering) {
                                        androidx.compose.foundation.Canvas(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .align(Alignment.TopEnd)
                                                .padding(1.dp)
                                        ) {
                                            drawCircle(color = androidx.compose.ui.graphics.Color.Red)
                                        }
                                    }
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
            ModalBottomSheet(
                modifier = Modifier.imePadding(),
                onDismissRequest = { showFilterSheet = false },
                sheetState = sheetState,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp, top = 8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Search & Filter",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Search Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChanged,
                        label = { Text("Search transactions") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChanged("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                            onSearch = { showFilterSheet = false }
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "HTTP Method",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
                        methods.forEach { method ->
                            FilterChip(
                                selected = filterMethod == method,
                                onClick = {
                                    onMethodFilterChanged(if (filterMethod == method) null else method)
                                },
                                label = { Text(method) },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Filter
                    Text(
                        text = "Status Code",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val statusFilters = listOf(
                            "2xx" to (200..299),
                            "3xx" to (300..399),
                            "4xx" to (400..499),
                            "5xx" to (500..599)
                        )
                        statusFilters.forEach { (label, range) ->
                            FilterChip(
                                selected = filterStatusRange == range,
                                onClick = {
                                    onStatusFilterChanged(if (filterStatusRange == range) null else range)
                                },
                                label = { Text(label) },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Clear Filters Button
                    val filtersActive = filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()
                    OutlinedButton(
                        onClick = {
                            onClearFilters()
                            onSearchChanged("")
                            showFilterSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = filtersActive
                    ) {
                        Text("Clear All Filters")
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
