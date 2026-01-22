package com.azikar24.wormaceptor.feature.viewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.MonitoringEngineHolder
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.CrashDetailScreen
import com.azikar24.wormaceptor.feature.viewer.ui.HomeScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailScreen
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.feature.deviceinfo.DeviceInfoScreen
import com.azikar24.wormaceptor.feature.logs.ui.LogsScreen
import com.azikar24.wormaceptor.feature.logs.vm.LogsViewModel
import com.azikar24.wormaceptor.feature.preferences.PreferencesInspector
import com.azikar24.wormaceptor.feature.database.DatabaseBrowser
import com.azikar24.wormaceptor.feature.filebrowser.FileBrowser
import com.azikar24.wormaceptor.feature.memory.MemoryMonitor
import com.azikar24.wormaceptor.feature.fps.FpsMonitor
import com.azikar24.wormaceptor.feature.websocket.WebSocketMonitor
import com.azikar24.wormaceptor.feature.cookies.CookiesInspector
import com.azikar24.wormaceptor.feature.cpu.CpuMonitor
import com.azikar24.wormaceptor.feature.touchvisualization.TouchVisualization
import com.azikar24.wormaceptor.feature.viewborders.ViewBorders
import com.azikar24.wormaceptor.feature.location.LocationSimulator
import com.azikar24.wormaceptor.feature.pushsimulator.PushSimulator
import com.azikar24.wormaceptor.feature.viewhierarchy.ViewHierarchyInspector
import com.azikar24.wormaceptor.feature.leakdetection.LeakDetector
import com.azikar24.wormaceptor.feature.threadviolation.ThreadViolationMonitor
import com.azikar24.wormaceptor.feature.webviewmonitor.WebViewMonitor as WebViewMonitorScreen
import com.azikar24.wormaceptor.feature.crypto.CryptoTool
import com.azikar24.wormaceptor.feature.gridoverlay.GridOverlayControl
import com.azikar24.wormaceptor.feature.measurement.MeasurementTool
import com.azikar24.wormaceptor.feature.securestorage.SecureStorageViewer
import com.azikar24.wormaceptor.feature.composerender.ComposeRenderTracker
import com.azikar24.wormaceptor.feature.ratelimit.RateLimiter
import com.azikar24.wormaceptor.feature.pushtoken.PushTokenManager
import com.azikar24.wormaceptor.feature.loadedlibraries.LoadedLibrariesInspector
import com.azikar24.wormaceptor.feature.interception.InterceptionFramework
import com.azikar24.wormaceptor.feature.viewer.ui.util.buildFullUrl
import com.azikar24.wormaceptor.feature.viewer.ui.util.copyToClipboard
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareText
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewModel
import kotlinx.coroutines.launch

class ViewerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Initialize log capture engine (uses singleton from holder)
        MonitoringEngineHolder.logCaptureEngine.start()

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ViewerViewModel(requireNotNull(CoreHolder.queryEngine) {
                    "WormaCeptor not initialized. Call WormaCeptor.init() before launching ViewerActivity"
                }) as T
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
            val selectedIds by viewModel.selectedIds.collectAsState()
            val isSelectionMode by viewModel.isSelectionMode.collectAsState()
            val scope = rememberCoroutineScope()

            WormaCeptorTheme {
                val navController = rememberNavController()

                // Wrap NavHost in Surface to ensure proper background during navigation transitions
                // This prevents white flash in dark mode when navigating back
                Surface(modifier = Modifier.fillMaxSize()) {
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
                        },
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
                                onClearTransactions = { scope.launch { viewModel.clearAllTransactions() } },
                                onClearCrashes = { scope.launch { viewModel.clearAllCrashes() } },
                                onExportTransactions = {
                                    scope.launch {
                                        val exportManager = com.azikar24.wormaceptor.feature.viewer.export.ExportManager(
                                            this@ViewerActivity,
                                        )
                                        val allTransactionsForExport = requireNotNull(CoreHolder.queryEngine) {
                                            "WormaCeptor not initialized. Call WormaCeptor.init() before launching ViewerActivity"
                                        }.getAllTransactionsForExport()
                                        exportManager.exportTransactions(allTransactionsForExport)
                                    }
                                },
                                onExportCrashes = {
                                    val allCrashes = crashes
                                    com.azikar24.wormaceptor.feature.viewer.export.exportCrashes(
                                        this@ViewerActivity,
                                        allCrashes,
                                    )
                                },
                                selectedTabIndex = selectedTabIndex,
                                onTabSelected = viewModel::updateSelectedTab,
                                allTransactions = allTransactions,
                                isRefreshingTransactions = isRefreshingTransactions,
                                isRefreshingCrashes = isRefreshingCrashes,
                                onRefreshTransactions = viewModel::refreshTransactions,
                                onRefreshCrashes = viewModel::refreshCrashes,
                                selectedIds = selectedIds,
                                isSelectionMode = isSelectionMode,
                                onSelectionToggle = viewModel::toggleSelection,
                                onSelectAll = viewModel::selectAll,
                                onClearSelection = viewModel::clearSelection,
                                onDeleteSelected = { scope.launch { viewModel.deleteSelected() } },
                                onShareSelected = {
                                    val selected = viewModel.getSelectedTransactions()
                                    shareTransactions(selected)
                                },
                                onExportSelected = {
                                    scope.launch {
                                        val selected = viewModel.getSelectedTransactions()
                                        val exportManager = com.azikar24.wormaceptor.feature.viewer.export.ExportManager(
                                            this@ViewerActivity,
                                        )
                                        val fullTransactions = selected.mapNotNull { summary ->
                                            CoreHolder.queryEngine?.getDetails(summary.id)
                                        }
                                        exportManager.exportTransactions(fullTransactions)
                                    }
                                },
                                onCopyUrl = { transaction -> copyUrlToClipboard(transaction) },
                                onShare = { transaction -> shareTransaction(transaction) },
                                onDelete = { transaction ->
                                    scope.launch { viewModel.deleteTransaction(transaction.id) }
                                },
                                onCopyAsCurl = { transaction -> copyAsCurl(transaction) },
                                // Quick access navigation in overflow menu
                                onNavigateToLogs = { navController.navigate("logs") },
                                onNavigateToDeviceInfo = { navController.navigate("deviceinfo") },
                                // Generic tool navigation for Tools tab
                                onToolNavigate = { route -> navController.navigate(route) },
                            )
                        }

                        composable("detail/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")
                            if (id != null) {
                                val uuid = java.util.UUID.fromString(id)
                                // Snapshot the transaction list when entering the detail screen
                                // This prevents the pager from jumping when new requests come in
                                val snapshotKey = backStackEntry.id
                                val (transactionIds, initialIndex) = remember(snapshotKey) {
                                    val ids = transactions.map { it.id }
                                    val index = ids.indexOf(uuid).coerceAtLeast(0)
                                    ids to index
                                }

                                if (transactionIds.isNotEmpty()) {
                                    TransactionDetailPagerScreen(
                                        transactionIds = transactionIds,
                                        initialTransactionIndex = initialIndex,
                                        getTransaction = { transactionId ->
                                            requireNotNull(CoreHolder.queryEngine) {
                    "WormaCeptor not initialized. Call WormaCeptor.init() before launching ViewerActivity"
                }.getDetails(transactionId)
                                        },
                                        onBack = { navController.popBackStack() },
                                    )
                                } else {
                                    // Fallback for single transaction view
                                    var transaction by remember {
                                        mutableStateOf<NetworkTransaction?>(null)
                                    }

                                    androidx.compose.runtime.LaunchedEffect(uuid) {
                                        transaction = requireNotNull(CoreHolder.queryEngine) {
                    "WormaCeptor not initialized. Call WormaCeptor.init() before launching ViewerActivity"
                }.getDetails(uuid)
                                    }

                                    transaction?.let {
                                        TransactionDetailScreen(
                                            transaction = it,
                                            onBack = { navController.popBackStack() },
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
                                        onBack = { navController.popBackStack() },
                                    )
                                }
                            }
                        }

                        // Preferences route - uses PreferencesInspector which handles internal navigation
                        composable("preferences") {
                            PreferencesInspector(
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Logs route
                        composable("logs") {
                            val logsViewModel = remember { LogsViewModel(MonitoringEngineHolder.logCaptureEngine) }
                            LogsScreen(
                                viewModel = logsViewModel,
                                onBack = { navController.popBackStack() },
                            )
                        }

                        // Device Info route
                        composable("deviceinfo") {
                            DeviceInfoScreen(
                                onBack = { navController.popBackStack() },
                            )
                        }

                        // Database Browser route
                        composable("database") {
                            DatabaseBrowser(
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // File Browser route
                        composable("filebrowser") {
                            FileBrowser(
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Memory Monitor route
                        composable("memory") {
                            MemoryMonitor(
                                engine = MonitoringEngineHolder.memoryMonitorEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // FPS Monitor route
                        composable("fps") {
                            FpsMonitor(
                                engine = MonitoringEngineHolder.fpsMonitorEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // WebSocket Monitor route
                        composable("websocket") {
                            WebSocketMonitor(
                                engine = MonitoringEngineHolder.webSocketMonitorEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Cookies Manager route
                        composable("cookies") {
                            CookiesInspector(
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // CPU Monitor route
                        composable("cpu") {
                            CpuMonitor(
                                engine = MonitoringEngineHolder.cpuMonitorEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Touch Visualization route
                        composable("touchviz") {
                            TouchVisualization(
                                engine = MonitoringEngineHolder.getTouchVisualizationEngine(this@ViewerActivity),
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // View Borders route
                        composable("viewborders") {
                            ViewBorders(
                                activity = this@ViewerActivity,
                                engine = MonitoringEngineHolder.viewBordersEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Location Simulator route
                        composable("location") {
                            LocationSimulator(
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Push Notification Simulator route
                        composable("pushsimulator") {
                            PushSimulator(
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Phase 5 Routes

                        // View Hierarchy Inspector route
                        composable("viewhierarchy") {
                            ViewHierarchyInspector(
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Leak Detection route
                        composable("leakdetection") {
                            LeakDetector(
                                engine = MonitoringEngineHolder.leakDetectionEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Thread Violation Detection route
                        composable("threadviolation") {
                            ThreadViolationMonitor(
                                engine = MonitoringEngineHolder.threadViolationEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // WebView Monitor route
                        composable("webviewmonitor") {
                            WebViewMonitorScreen(
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Crypto Tool route
                        composable("crypto") {
                            CryptoTool(
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Grid Overlay route
                        composable("gridoverlay") {
                            GridOverlayControl(
                                activity = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Measurement Tool route
                        composable("measurement") {
                            MeasurementTool(
                                activity = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Secure Storage Viewer route
                        composable("securestorage") {
                            SecureStorageViewer(
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Compose Render Tracker route
                        composable("composerender") {
                            ComposeRenderTracker(
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Rate Limiter route
                        composable("ratelimit") {
                            RateLimiter(
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Push Token Manager route
                        composable("pushtoken") {
                            PushTokenManager(
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Loaded Libraries Inspector route
                        composable("loadedlibraries") {
                            LoadedLibrariesInspector(
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Interception Framework route
                        composable("interception") {
                            InterceptionFramework(
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }
    }

    private fun copyUrlToClipboard(transaction: TransactionSummary) {
        val url = buildFullUrl(transaction.host, transaction.path)
        copyToClipboard(this, "URL", url)
    }

    private fun shareTransaction(transaction: TransactionSummary) {
        val url = buildFullUrl(transaction.host, transaction.path)
        val text = buildString {
            appendLine("${transaction.method} $url")
            appendLine("Status: ${transaction.code ?: "Pending"}")
            transaction.tookMs?.let { appendLine("Duration: ${it}ms") }
        }
        shareText(this, text, "Share Transaction")
    }

    private fun shareTransactions(transactions: List<TransactionSummary>) {
        val text = transactions.joinToString("\n\n") { transaction ->
            val url = buildFullUrl(transaction.host, transaction.path)
            buildString {
                appendLine("${transaction.method} $url")
                appendLine("Status: ${transaction.code ?: "Pending"}")
                transaction.tookMs?.let { appendLine("Duration: ${it}ms") }
            }
        }
        shareText(this, text, "Share ${transactions.size} Transactions")
    }

    private fun copyAsCurl(transaction: TransactionSummary) {
        lifecycleScope.launch {
            val fullTransaction = CoreHolder.queryEngine?.getDetails(transaction.id)
            if (fullTransaction == null) {
                android.widget.Toast.makeText(this@ViewerActivity, "Failed to load transaction details", android.widget.Toast.LENGTH_SHORT).show()
                return@launch
            }

            val curl = buildCurlCommand(fullTransaction)
            copyToClipboard(this@ViewerActivity, "cURL", curl)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Note: Engines are NOT stopped here - they persist across Activity lifecycle
        // via MonitoringEngineHolder. User controls monitoring via explicit start/stop.
    }

    private suspend fun buildCurlCommand(transaction: NetworkTransaction): String = buildString {
        append("curl -X ${transaction.request.method} \"${transaction.request.url}\"")
        transaction.request.headers.forEach { (key, values) ->
            values.forEach { value ->
                val escapedKey = key.replace("'", "'\\''")
                val escapedValue = value.replace("'", "'\\''")
                append(" -H '$escapedKey: $escapedValue'")
            }
        }
        // Include request body for methods that typically have a body
        val methodsWithBody = setOf("POST", "PUT", "PATCH", "DELETE")
        if (transaction.request.method.uppercase() in methodsWithBody && transaction.request.bodyRef != null) {
            val body = CoreHolder.queryEngine?.getBody(transaction.request.bodyRef!!)
            if (body != null) {
                val escapedBody = body.replace("'", "'\\''")
                append(" -d '$escapedBody'")
            }
        }
    }
}
