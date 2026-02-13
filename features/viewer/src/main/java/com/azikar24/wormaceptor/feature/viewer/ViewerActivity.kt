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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.CpuMonitorEngine
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.core.engine.DependenciesInspectorEngine
import com.azikar24.wormaceptor.core.engine.FpsMonitorEngine
import com.azikar24.wormaceptor.core.engine.LeakDetectionEngine
import com.azikar24.wormaceptor.core.engine.LoadedLibrariesEngine
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.MemoryMonitorEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.PushSimulatorEngine
import com.azikar24.wormaceptor.core.engine.PushTokenEngine
import com.azikar24.wormaceptor.core.engine.RateLimitEngine
import com.azikar24.wormaceptor.core.engine.SecureStorageEngine
import com.azikar24.wormaceptor.core.engine.ThreadViolationEngine
import com.azikar24.wormaceptor.core.engine.WebSocketMonitorEngine
import com.azikar24.wormaceptor.core.engine.WebViewMonitorEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.domain.contracts.PushSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.cpu.CpuMonitor
import com.azikar24.wormaceptor.feature.crypto.ui.CryptoHistoryScreen
import com.azikar24.wormaceptor.feature.crypto.ui.CryptoTool
import com.azikar24.wormaceptor.feature.database.DatabaseBrowser
import com.azikar24.wormaceptor.feature.dependenciesinspector.DependenciesInspector
import com.azikar24.wormaceptor.feature.deviceinfo.DeviceInfoScreen
import com.azikar24.wormaceptor.feature.filebrowser.FileBrowser
import com.azikar24.wormaceptor.feature.fps.FpsMonitor
import com.azikar24.wormaceptor.feature.leakdetection.LeakDetector
import com.azikar24.wormaceptor.feature.loadedlibraries.LoadedLibrariesInspector
import com.azikar24.wormaceptor.feature.location.LocationSimulator
import com.azikar24.wormaceptor.feature.logs.ui.LogsScreen
import com.azikar24.wormaceptor.feature.logs.vm.LogsViewModel
import com.azikar24.wormaceptor.feature.memory.MemoryMonitor
import com.azikar24.wormaceptor.feature.preferences.PreferencesInspector
import com.azikar24.wormaceptor.feature.pushsimulator.PushSimulator
import com.azikar24.wormaceptor.feature.pushtoken.PushTokenManager
import com.azikar24.wormaceptor.feature.ratelimit.RateLimiter
import com.azikar24.wormaceptor.feature.securestorage.SecureStorageViewer
import com.azikar24.wormaceptor.feature.threadviolation.ThreadViolationMonitor
import com.azikar24.wormaceptor.feature.viewer.navigation.DeepLinkHandler
import com.azikar24.wormaceptor.feature.viewer.ui.CrashDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.HomeScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailScreen
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.feature.viewer.ui.util.buildFullUrl
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareText
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewModel
import com.azikar24.wormaceptor.feature.websocket.WebSocketMonitor
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.WebViewMonitor as WebViewMonitorScreen

class ViewerActivity : ComponentActivity() {

    // Inject engines via Koin
    private val memoryMonitorEngine: MemoryMonitorEngine by inject()
    private val fpsMonitorEngine: FpsMonitorEngine by inject()
    private val cpuMonitorEngine: CpuMonitorEngine by inject()
    private val logCaptureEngine: LogCaptureEngine by inject()
    private val webSocketMonitorEngine: WebSocketMonitorEngine by inject()
    private val leakDetectionEngine: LeakDetectionEngine by inject()
    private val threadViolationEngine: ThreadViolationEngine by inject()
    private val performanceOverlayEngine: PerformanceOverlayEngine by inject()
    private val rateLimitEngine: RateLimitEngine by inject()
    private val webViewMonitorEngine: WebViewMonitorEngine by inject()
    private val cryptoEngine: CryptoEngine by inject()
    private val secureStorageEngine: SecureStorageEngine by inject()
    private val loadedLibrariesEngine: LoadedLibrariesEngine by inject()
    private val dependenciesInspectorEngine: DependenciesInspectorEngine by inject()
    private val pushTokenEngine: PushTokenEngine by inject()
    private val locationSimulatorEngine: LocationSimulatorEngine by inject()
    private val pushSimulatorEngine: PushSimulatorEngine by inject()
    private val locationSimulatorRepository: LocationSimulatorRepository by inject()
    private val pushSimulatorRepository: PushSimulatorRepository by inject()

    // Deep link handling - use SharedFlow to emit navigation events
    private val _deepLinkNavigation = MutableSharedFlow<DeepLinkHandler.DeepLinkDestination>(
        extraBufferCapacity = 1,
    )
    private val deepLinkNavigation = _deepLinkNavigation.asSharedFlow()

    // Store initial deep link destination to handle on first composition
    private var initialDeepLink: DeepLinkHandler.DeepLinkDestination? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize Koin before super.onCreate() to ensure injection works
        WormaCeptorKoin.init(applicationContext)

        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Start log capture engine
        logCaptureEngine.start()

        // Handle initial deep link (if activity was launched via deep link)
        if (savedInstanceState == null) {
            initialDeepLink = DeepLinkHandler.parseDeepLink(intent)
                .takeIf { it !is DeepLinkHandler.DeepLinkDestination.Invalid }
        }

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ViewerViewModel(
                    requireNotNull(CoreHolder.queryEngine) {
                        "WormaCeptor not initialized. Call WormaCeptor.init() before launching ViewerActivity"
                    },
                ) as T
            }
        }
        val viewModel = ViewModelProvider(this, factory)[ViewerViewModel::class.java]

        setContent {
            val transactions by viewModel.transactions.collectAsState()
            val allTransactions by viewModel.allTransactions.collectAsState()
            val crashes by viewModel.crashes.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val filterMethods by viewModel.filterMethods.collectAsState()
            val filterStatusRanges by viewModel.filterStatusRanges.collectAsState()
            val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
            val isInitialLoading by viewModel.isInitialLoading.collectAsState()
            val isRefreshingTransactions by viewModel.isRefreshingTransactions.collectAsState()
            val isRefreshingCrashes by viewModel.isRefreshingCrashes.collectAsState()
            val selectedIds by viewModel.selectedIds.collectAsState()
            val isSelectionMode by viewModel.isSelectionMode.collectAsState()
            val scope = rememberCoroutineScope()

            WormaCeptorTheme {
                val navController = rememberNavController()

                // Handle deep link navigation
                LaunchedEffect(Unit) {
                    // Handle initial deep link
                    initialDeepLink?.let { destination ->
                        handleDeepLinkDestination(navController, viewModel, destination)
                        initialDeepLink = null
                    }

                    // Handle subsequent deep links (when activity receives new intent)
                    deepLinkNavigation.collect { destination ->
                        handleDeepLinkDestination(navController, viewModel, destination)
                    }
                }

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
                                filterMethods = filterMethods,
                                filterStatusRanges = filterStatusRanges,
                                onMethodFiltersChanged = viewModel::setMethodFilters,
                                onStatusFiltersChanged = viewModel::setStatusFilters,
                                onClearFilters = viewModel::clearFilters,
                                onClearTransactions = { scope.launch { viewModel.clearAllTransactions() } },
                                onClearCrashes = { scope.launch { viewModel.clearAllCrashes() } },
                                onExportTransactions = {
                                    scope.launch {
                                        val exportManager = com.azikar24.wormaceptor.feature.viewer.export.ExportManager(
                                            this@ViewerActivity,
                                            onMessage = viewModel::showMessage,
                                        )
                                        val allTransactionsForExport = requireNotNull(CoreHolder.queryEngine) {
                                            "WormaCeptor not initialized. Call WormaCeptor.init() before launching ViewerActivity"
                                        }.getAllTransactionsForExport()
                                        exportManager.exportTransactions(allTransactionsForExport)
                                    }
                                },
                                onExportCrashes = {
                                    scope.launch {
                                        val allCrashes = crashes
                                        com.azikar24.wormaceptor.feature.viewer.export.exportCrashes(
                                            this@ViewerActivity,
                                            allCrashes,
                                            onMessage = viewModel::showMessage,
                                        )
                                    }
                                },
                                selectedTabIndex = selectedTabIndex,
                                onTabSelected = viewModel::updateSelectedTab,
                                allTransactions = allTransactions,
                                isInitialLoading = isInitialLoading,
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
                                            onMessage = viewModel::showMessage,
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
                                onCopyAsCurl = { transaction -> copyAsCurl(transaction, viewModel) },
                                // Generic tool navigation for Tools tab
                                onToolNavigate = { route -> navController.navigate(route) },
                                // Snackbar message flow
                                snackbarMessage = viewModel.snackbarMessage,
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
                                // Snapshot the crash list when entering the detail screen
                                // This prevents the pager from jumping when new crashes come in
                                val snapshotKey = backStackEntry.id
                                val (crashList, initialIndex) = remember(snapshotKey) {
                                    val index = crashes.indexOfFirst { it.timestamp == timestamp }
                                        .coerceAtLeast(0)
                                    crashes to index
                                }

                                if (crashList.isNotEmpty()) {
                                    CrashDetailPagerScreen(
                                        crashes = crashList,
                                        initialCrashIndex = initialIndex,
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
                            val logsViewModel = remember { LogsViewModel(logCaptureEngine) }
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
                            // Enable memory metric in overlay if overlay is enabled
                            LaunchedEffect(Unit) {
                                performanceOverlayEngine.enableMetricForMonitorScreen(memory = true)
                            }
                            MemoryMonitor(
                                engine = memoryMonitorEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // FPS Monitor route
                        composable("fps") {
                            // Enable FPS metric in overlay if overlay is enabled
                            LaunchedEffect(Unit) {
                                performanceOverlayEngine.enableMetricForMonitorScreen(fps = true)
                            }
                            FpsMonitor(
                                engine = fpsMonitorEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // WebSocket Monitor route
                        composable("websocket") {
                            WebSocketMonitor(
                                engine = webSocketMonitorEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // CPU Monitor route
                        composable("cpu") {
                            // Enable CPU metric in overlay if overlay is enabled
                            LaunchedEffect(Unit) {
                                performanceOverlayEngine.enableMetricForMonitorScreen(cpu = true)
                            }
                            CpuMonitor(
                                engine = cpuMonitorEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Location Simulator route
                        composable("location") {
                            LocationSimulator(
                                engine = locationSimulatorEngine,
                                repository = locationSimulatorRepository,
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Push Notification Simulator route
                        composable("pushsimulator") {
                            PushSimulator(
                                engine = pushSimulatorEngine,
                                repository = pushSimulatorRepository,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Phase 5 Routes

                        // Leak Detection route
                        composable("leakdetection") {
                            LeakDetector(
                                engine = leakDetectionEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Thread Violation Detection route
                        composable("threadviolation") {
                            ThreadViolationMonitor(
                                engine = threadViolationEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // WebView Monitor route
                        composable("webviewmonitor") {
                            WebViewMonitorScreen(
                                engine = webViewMonitorEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Crypto routes with shared engine
                        composable("crypto") { backStackEntry ->
                            var showHistory by remember { mutableStateOf(false) }

                            if (showHistory) {
                                CryptoHistoryScreen(
                                    engine = cryptoEngine,
                                    onNavigateBack = { showHistory = false },
                                    onLoadResult = { showHistory = false },
                                )
                            } else {
                                CryptoTool(
                                    engine = cryptoEngine,
                                    onNavigateBack = { navController.popBackStack() },
                                    onNavigateToHistory = { showHistory = true },
                                )
                            }
                        }

                        // Secure Storage Viewer route
                        composable("securestorage") {
                            SecureStorageViewer(
                                engine = secureStorageEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Rate Limiter route
                        composable("ratelimit") {
                            RateLimiter(
                                engine = rateLimitEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Push Token Manager route
                        composable("pushtoken") {
                            PushTokenManager(
                                engine = pushTokenEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Loaded Libraries Inspector route
                        composable("loadedlibraries") {
                            LoadedLibrariesInspector(
                                engine = loadedLibrariesEngine,
                                onNavigateBack = { navController.popBackStack() },
                            )
                        }

                        // Dependencies Inspector route
                        composable("dependencies") {
                            DependenciesInspector(
                                engine = dependenciesInspectorEngine,
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

    private fun copyAsCurl(transaction: TransactionSummary, viewModel: ViewerViewModel) {
        lifecycleScope.launch {
            val fullTransaction = CoreHolder.queryEngine?.getDetails(transaction.id)
            if (fullTransaction == null) {
                viewModel.showMessage("Failed to load transaction details")
                return@launch
            }

            val curl = buildCurlCommand(fullTransaction)
            val message = copyToClipboard(this@ViewerActivity, "cURL", curl)
            viewModel.showMessage(message)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle deep link when activity is already running (singleTask launch mode)
        val destination = DeepLinkHandler.parseDeepLink(intent)
        if (destination !is DeepLinkHandler.DeepLinkDestination.Invalid) {
            _deepLinkNavigation.tryEmit(destination)
        }
    }

    override fun onDestroy() {
        // Clear activity references from engines to prevent memory leaks
        // Note: Engines themselves are NOT stopped here - they persist across Activity
        // lifecycle via Koin singleton scope. User controls monitoring via explicit start/stop.
        // We only clear references to THIS activity to allow garbage collection.
        performanceOverlayEngine.clearActivityReferences()

        super.onDestroy()
    }

    /**
     * Handles a deep link destination by navigating to the appropriate screen.
     */
    private fun handleDeepLinkDestination(
        navController: NavHostController,
        viewModel: ViewerViewModel,
        destination: DeepLinkHandler.DeepLinkDestination,
    ) {
        when (destination) {
            is DeepLinkHandler.DeepLinkDestination.Tab -> {
                // Navigate to home and select the specified tab
                navController.popBackStack("home", inclusive = false)
                viewModel.updateSelectedTab(destination.tabIndex)
            }

            is DeepLinkHandler.DeepLinkDestination.Tool -> {
                // Navigate directly to the tool screen
                // First ensure we're on home, then navigate to the tool
                if (navController.currentDestination?.route != "home") {
                    navController.popBackStack("home", inclusive = false)
                }
                navController.navigate(destination.route)
            }

            is DeepLinkHandler.DeepLinkDestination.Invalid -> {
                // Do nothing for invalid deep links
            }
        }
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
        val bodyRef = transaction.request.bodyRef
        if (transaction.request.method.uppercase() in methodsWithBody && bodyRef != null) {
            CoreHolder.queryEngine?.getBody(bodyRef)?.let { body ->
                val escapedBody = body.replace("'", "'\\''")
                append(" -d '$escapedBody'")
            }
        }
    }
}
