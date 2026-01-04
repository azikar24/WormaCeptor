/*
 * Copyright AziKar24 2024.
 */

package com.azikar24.wormaceptor.internal.ui.features.network

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.azikar24.wormaceptor.annotations.ScreenPreviews
import com.azikar24.wormaceptor.internal.ui.ToolbarViewModel
import com.azikar24.wormaceptor.internal.ui.features.network.components.NetworkList
import com.azikar24.wormaceptor.internal.ui.navigation.Route
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcDelete
import com.azikar24.wormaceptor.ui.drawables.myiconpack.IcSearch
import com.azikar24.wormaceptor.ui.theme.WormaCeptorMainTheme
import com.example.wormaceptor.ui.drawables.MyIconPack
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListScreen(
    navController: NavController,
    viewModel: NetworkTransactionViewModel = koinViewModel(),
    toolbarViewModel: ToolbarViewModel = koinViewModel(),
) {
    var showLocalSearch by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedMethod by remember { mutableStateOf<String?>(null) }
    var selectedStatus by remember { mutableStateOf<String?>(null) }

    val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")
    val statuses = listOf("2xx", "3xx", "4xx", "5xx")

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, showLocalSearch, showFilters) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                toolbarViewModel.reset()
                toolbarViewModel.title = "Network Calls"
                toolbarViewModel.subtitle = ""
                toolbarViewModel.showSearch = showLocalSearch
                toolbarViewModel.menuActions = {
                    Row {
                        IconButton(
                            onClick = {
                                showLocalSearch = !showLocalSearch
                                if (!showLocalSearch) toolbarViewModel.searchKey = ""
                            }
                        ) {
                            Icon(
                                imageVector = MyIconPack.IcSearch,
                                contentDescription = "Search",
                            )
                        }
                        IconButton(
                            onClick = {
                                showFilters = !showFilters
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (showFilters || selectedMethod != null || selectedStatus != null) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    LocalContentColor.current
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.clearAll()
                            }
                        ) {
                            Icon(
                                imageVector = MyIconPack.IcDelete,
                                contentDescription = "Clear All",
                            )
                        }
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(toolbarViewModel.searchKey, selectedMethod, selectedStatus) {
        viewModel.fetchData(toolbarViewModel.searchKey, selectedMethod, selectedStatus)
    }

    val data = viewModel.pageEventFlow.collectAsLazyPagingItems()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Filter Chips Row
        AnimatedVisibility(
            visible = showFilters,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Method Filters
                    methods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = if (selectedMethod == method) null else method },
                            label = { Text(method) }
                        )
                    }
                    
                    VerticalDivider(modifier = Modifier.height(32.dp).padding(horizontal = 4.dp))
                    
                    // Status Filters
                    statuses.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = if (selectedStatus == status) null else status },
                            label = { Text(status) }
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }
        }

        Box(Modifier.fillMaxSize().weight(1f)) {
            NetworkList(data, toolbarViewModel.searchKey) {
                navController.navigate(Route.NetworkDetails(it.id))
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun PreviewNetworkListScreen() {
    WormaCeptorMainTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NetworkListScreen(rememberNavController())
        }
    }
}
