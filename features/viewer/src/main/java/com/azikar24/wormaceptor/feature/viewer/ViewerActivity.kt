package com.azikar24.wormaceptor.feature.viewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.ui.CrashDetailScreen
import com.azikar24.wormaceptor.feature.viewer.ui.CrashListScreen
import com.azikar24.wormaceptor.feature.viewer.ui.HomeScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionListScreen
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewModel
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorTheme
import androidx.compose.ui.Modifier

class ViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ViewerViewModel(CoreHolder.queryEngine!!) as T
            }
        }
        val viewModel = ViewModelProvider(this, factory)[ViewerViewModel::class.java]

        setContent {
            val transactions by viewModel.transactions.collectAsState()
            val allTransactions by viewModel.allTransactions.collectAsState()
            val crashes by viewModel.crashes.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val filterMethod by viewModel.filterMethod.collectAsState()
            val filterStatusRange by viewModel.filterStatusRange.collectAsState()
            val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
            val isRefreshingTransactions by viewModel.isRefreshingTransactions.collectAsState()
            val isRefreshingCrashes by viewModel.isRefreshingCrashes.collectAsState()

            WormaCeptorTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home",
                    enterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                        )
                    },
                    exitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.Start,
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                        )
                    },
                    popEnterTransition = {
                        slideIntoContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(400, easing = FastOutSlowInEasing),
                        )
                    },
                    popExitTransition = {
                        slideOutOfContainer(
                            AnimatedContentTransitionScope.SlideDirection.End,
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                        )
                    }
                ) {
                    composable("home") {
                        HomeScreen(
                            transactions = transactions,
                            crashes = crashes,
                            searchQuery = searchQuery,
                            onSearchChanged = viewModel::onSearchQueryChanged,
                            onTransactionClick = { navController.navigate("detail/${it.id}") },
                            onCrashClick = { crash -> navController.navigate("crash/${crash.timestamp}") },
                            filterMethod = filterMethod,
                            filterStatusRange = filterStatusRange,
                            onMethodFilterChanged = viewModel::setMethodFilter,
                            onStatusFilterChanged = viewModel::setStatusFilter,
                            onClearFilters = viewModel::clearFilters,
                            onClearTransactions = { viewModel.clearAllTransactions() },
                            onClearCrashes = { viewModel.clearAllCrashes() },
                            onExportTransactions = {
                                val exportManager = com.azikar24.wormaceptor.feature.viewer.export.ExportManager(this@ViewerActivity)
                                val allTransactionsForExport = CoreHolder.queryEngine!!.getAllTransactionsForExport()
                                exportManager.exportTransactions(allTransactionsForExport)
                            },
                            onExportCrashes = {
                                val allCrashes = crashes
                                com.azikar24.wormaceptor.feature.viewer.export.exportCrashes(this@ViewerActivity, allCrashes)
                            },
                            selectedTabIndex = selectedTabIndex,
                            onTabSelected = viewModel::updateSelectedTab,
                            allTransactions = allTransactions,
                            isRefreshingTransactions = isRefreshingTransactions,
                            isRefreshingCrashes = isRefreshingCrashes,
                            onRefreshTransactions = viewModel::refreshTransactions,
                            onRefreshCrashes = viewModel::refreshCrashes
                        )
                    }

                    composable("detail/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")
                        if (id != null) {
                            val uuid = java.util.UUID.fromString(id)
                            // Use the filtered transaction list for pager navigation
                            val transactionIds = transactions.map { it.id }
                            val initialIndex = transactionIds.indexOf(uuid).coerceAtLeast(0)

                            if (transactionIds.isNotEmpty()) {
                                TransactionDetailPagerScreen(
                                    transactionIds = transactionIds,
                                    initialTransactionIndex = initialIndex,
                                    getTransaction = { transactionId ->
                                        CoreHolder.queryEngine!!.getDetails(transactionId)
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            } else {
                                // Fallback for single transaction view
                                var transaction by remember {
                                    mutableStateOf<NetworkTransaction?>(null)
                                }

                                androidx.compose.runtime.LaunchedEffect(uuid) {
                                    transaction = CoreHolder.queryEngine!!.getDetails(uuid)
                                }

                                transaction?.let {
                                    TransactionDetailScreen(
                                        transaction = it,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }

                    composable("crash/{timestamp}") { backStackEntry ->
                        val timestamp = backStackEntry.arguments?.getString("timestamp")?.toLongOrNull()
                        if (timestamp != null) {
                            val crash = crashes.find { it.timestamp == timestamp }
                            crash?.let {
                                CrashDetailScreen(
                                    crash = it,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }

            }
        }
    }
}
