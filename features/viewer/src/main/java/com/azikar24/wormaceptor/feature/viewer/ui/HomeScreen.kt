package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    onClearTransactions: () -> Unit,
    onClearCrashes: () -> Unit,
    onExportTransactions: suspend () -> Unit,
    onExportCrashes: suspend () -> Unit,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val context = LocalContext.current
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
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = if (filterMethod != null || filterStatusRange != null || searchQuery.isNotBlank()) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
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
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            if (selectedTabIndex == 0) {
                                // Transactions tab menu
                                DropdownMenuItem(
                                    text = { Text("Export Transactions") },
                                    onClick = {
                                        showOverflowMenu = false
                                        scope.launch {
                                            onExportTransactions()
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear All Transactions") },
                                    onClick = {
                                        showOverflowMenu = false
                                        showClearTransactionsDialog = true
                                    }
                                )
                            } else {
                                // Crashes tab menu
                                DropdownMenuItem(
                                    text = { Text("Export Crashes") },
                                    onClick = {
                                        showOverflowMenu = false
                                        scope.launch {
                                            onExportCrashes()
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Clear All Crashes") },
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
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
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
                        singleLine = true
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
                    if (filterMethod != null || filterStatusRange != null) {
                        OutlinedButton(
                            onClick = {
                                onClearFilters()
                                showFilterSheet = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear All Filters")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
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
