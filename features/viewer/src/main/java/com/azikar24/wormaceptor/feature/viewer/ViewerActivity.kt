package com.azikar24.wormaceptor.feature.viewer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionListScreen
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewModel
// Wait, `WormaCeptorTheme` might not be available in `:features:viewer` directly as it's in `:app`.
// Phase 4 summary says `ViewerActivity` was created. Let's check imports in original file.
// Original file had no theme import? 
// Original file had `WormaCeptorTheme` in the `setContent` block in my memory? 
// Retrying viewing original `ViewerActivity.kt`...
// It does NOT have `WormaCeptorTheme` import. It likely uses MaterialTheme or default.
// Wait, `ViewerActivity.kt` line 37: `setContent {`. No theme wrapper visible in lines 37-70.
// I should wrap in MaterialTheme or just leave as is. User mentioned "Design Aesthetics".
// I will not add `WormaCeptorTheme` if I cannot verify it exists here. I will use `MaterialTheme` from material3.
import androidx.compose.material3.MaterialTheme
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
            val navController = rememberNavController()
            val transactions by viewModel.transactions.collectAsState()
            val crashes by viewModel.crashes.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val filterMethod by viewModel.filterMethod.collectAsState()
            val filterStatusRange by viewModel.filterStatusRange.collectAsState()
            val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()

            MaterialTheme {
                val navController = rememberNavController()
                
                NavHost(navController = navController, startDestination = "home") {
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
                            onClearTransactions = { kotlinx.coroutines.runBlocking { viewModel.clearAllTransactions() } },
                            onClearCrashes = { kotlinx.coroutines.runBlocking { viewModel.clearAllCrashes() } },
                            onExportTransactions = {
                                val exportManager = com.azikar24.wormaceptor.feature.viewer.export.ExportManager(this@ViewerActivity)
                                val allTransactions = CoreHolder.queryEngine!!.getAllTransactionsForExport()
                                exportManager.exportTransactions(allTransactions)
                            },
                            onExportCrashes = {
                                val allCrashes = crashes
                                com.azikar24.wormaceptor.feature.viewer.export.exportCrashes(this@ViewerActivity, allCrashes)
                            },
                            selectedTabIndex = selectedTabIndex,
                            onTabSelected = viewModel::updateSelectedTab
                        )
                    }
                    
                    composable("detail/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")
                        if (id != null) {
                            val uuid = java.util.UUID.fromString(id)
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
