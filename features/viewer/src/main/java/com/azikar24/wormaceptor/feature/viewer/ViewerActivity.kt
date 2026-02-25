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
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.cpu.CpuMonitor
import com.azikar24.wormaceptor.feature.crypto.CryptoScreen
import com.azikar24.wormaceptor.feature.database.navigation.databaseGraph
import com.azikar24.wormaceptor.feature.dependenciesinspector.DependenciesInspector
import com.azikar24.wormaceptor.feature.deviceinfo.DeviceInfoScreen
import com.azikar24.wormaceptor.feature.filebrowser.FileBrowser
import com.azikar24.wormaceptor.feature.fps.FpsMonitor
import com.azikar24.wormaceptor.feature.leakdetection.LeakDetector
import com.azikar24.wormaceptor.feature.loadedlibraries.LoadedLibrariesInspector
import com.azikar24.wormaceptor.feature.location.LocationSimulator
import com.azikar24.wormaceptor.feature.logs.LogViewer
import com.azikar24.wormaceptor.feature.memory.MemoryMonitor
import com.azikar24.wormaceptor.feature.preferences.navigation.preferencesGraph
import com.azikar24.wormaceptor.feature.pushsimulator.PushSimulator
import com.azikar24.wormaceptor.feature.pushtoken.PushTokenManager
import com.azikar24.wormaceptor.feature.ratelimit.RateLimiter
import com.azikar24.wormaceptor.feature.securestorage.SecureStorageViewer
import com.azikar24.wormaceptor.feature.threadviolation.ThreadViolationMonitor
import com.azikar24.wormaceptor.feature.viewer.export.ExportManager
import com.azikar24.wormaceptor.feature.viewer.export.exportCrashes
import com.azikar24.wormaceptor.feature.viewer.navigation.DeepLinkHandler
import com.azikar24.wormaceptor.feature.viewer.ui.CrashDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.HomeScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailScreen
import com.azikar24.wormaceptor.feature.viewer.ui.util.buildFullUrl
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareText
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewEffect
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewModel
import com.azikar24.wormaceptor.feature.websocket.navigation.webSocketGraph
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import com.azikar24.wormaceptor.feature.webviewmonitor.ui.WebViewMonitor as WebViewMonitorScreen

class ViewerActivity : ComponentActivity() {

    // Inject only engines needed directly by ViewerActivity
    private val logCaptureEngine: LogCaptureEngine by inject()
    private val performanceOverlayEngine: PerformanceOverlayEngine by inject()

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
                    applicationContext,
                ) as T
            }
        }
        val viewModel = ViewModelProvider(this, factory)[ViewerViewModel::class.java]

        setContent {
            val snackbarMessages = remember { MutableSharedFlow<String>(extraBufferCapacity = 1) }

            WormaCeptorTheme {
                BaseScreen(
                    viewModel = viewModel,
                    onEffect = { effect ->
                        when (effect) {
                            is ViewerViewEffect.ShowSnackbar -> snackbarMessages.tryEmit(effect.message)
                        }
                    },
                ) { state, onEvent ->
                    val transactions by viewModel.transactions.collectAsState()
                    val allTransactions by viewModel.allTransactions.collectAsState()
                    val crashes by viewModel.crashes.collectAsState()
                    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
                    val scope = rememberCoroutineScope()
                    val snackbarMessage: (String) -> Unit = { message ->
                        onEvent(ViewerViewEvent.ShowMessage(message))
                    }
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
                            startDestination = WormaCeptorNavKeys.Home.route,
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
                            composable(WormaCeptorNavKeys.Home.route) {
                                HomeScreen(
                                    transactions = transactions,
                                    crashes = crashes,
                                    searchQuery = state.searchQuery,
                                    onSearchChanged = {
                                        onEvent(ViewerViewEvent.SearchQueryChanged(it))
                                    },
                                    onTransactionClick = {
                                        navController.navigate(
                                            WormaCeptorNavKeys.TransactionDetail.createRoute(it.id.toString()),
                                        )
                                    },
                                    onCrashClick = { crash ->
                                        navController.navigate(
                                            WormaCeptorNavKeys.CrashDetail.createRoute(crash.timestamp),
                                        )
                                    },
                                    filterMethods = state.filterMethods,
                                    filterStatusRanges = state.filterStatusRanges,
                                    onMethodFiltersChanged = {
                                        onEvent(ViewerViewEvent.MethodFiltersChanged(it))
                                    },
                                    onStatusFiltersChanged = {
                                        onEvent(ViewerViewEvent.StatusFiltersChanged(it))
                                    },
                                    onClearFilters = {
                                        onEvent(ViewerViewEvent.ClearFilters)
                                    },
                                    onClearTransactions = {
                                        onEvent(ViewerViewEvent.ClearAllTransactions)
                                    },
                                    onClearCrashes = {
                                        onEvent(ViewerViewEvent.ClearAllCrashes)
                                    },
                                    onExportTransactions = {
                                        scope.launch {
                                            val qe = requireNotNull(CoreHolder.queryEngine) {
                                                "WormaCeptor not initialized"
                                            }
                                            val exportManager =
                                                ExportManager(
                                                    this@ViewerActivity,
                                                    qe,
                                                    onMessage = snackbarMessage,
                                                )
                                            val allTransactionsForExport =
                                                qe.getAllTransactionsForExport()
                                            exportManager.exportTransactions(allTransactionsForExport)
                                        }
                                    },
                                    onExportCrashes = {
                                        scope.launch {
                                            val allCrashes = crashes
                                            exportCrashes(
                                                this@ViewerActivity,
                                                allCrashes,
                                                onMessage = snackbarMessage,
                                            )
                                        }
                                    },
                                    selectedTabIndex = state.selectedTabIndex,
                                    onTabSelected = {
                                        onEvent(ViewerViewEvent.TabSelected(it))
                                    },
                                    allTransactions = allTransactions,
                                    isInitialLoading = state.isInitialLoading,
                                    isRefreshingTransactions = state.isRefreshingTransactions,
                                    isRefreshingCrashes = state.isRefreshingCrashes,
                                    onRefreshTransactions = {
                                        onEvent(ViewerViewEvent.RefreshTransactions)
                                    },
                                    onRefreshCrashes = {
                                        onEvent(ViewerViewEvent.RefreshCrashes)
                                    },
                                    selectedIds = state.selectedIds,
                                    isSelectionMode = isSelectionMode,
                                    onSelectionToggle = {
                                        onEvent(ViewerViewEvent.SelectionToggled(it))
                                    },
                                    onSelectAll = {
                                        onEvent(ViewerViewEvent.SelectAllClicked)
                                    },
                                    onClearSelection = {
                                        onEvent(ViewerViewEvent.SelectionCleared)
                                    },
                                    onDeleteSelected = {
                                        onEvent(ViewerViewEvent.DeleteSelectedClicked)
                                    },
                                    onShareSelected = {
                                        val selected = viewModel.getSelectedTransactions()
                                        shareTransactions(selected)
                                    },
                                    onExportSelected = {
                                        scope.launch {
                                            val selected = viewModel.getSelectedTransactions()
                                            val exportManager =
                                                ExportManager(
                                                    this@ViewerActivity,
                                                    CoreHolder.queryEngine,
                                                    onMessage = snackbarMessage,
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
                                        onEvent(
                                            ViewerViewEvent.DeleteTransaction(transaction.id),
                                        )
                                    },
                                    onCopyAsCurl = { transaction ->
                                        copyAsCurl(transaction, viewModel)
                                    },
                                    onToolNavigate = { route -> navController.navigate(route) },
                                    collapsedToolCategories = state.collapsedToolCategories,
                                    onToolCategoryCollapseToggled = {
                                        onEvent(ViewerViewEvent.ToolCategoryCollapseToggled(it))
                                    },
                                    showFilterSheet = state.showFilterSheet,
                                    onFilterSheetVisibilityChanged = {
                                        onEvent(ViewerViewEvent.FilterSheetVisibilityChanged(it))
                                    },
                                    showOverflowMenu = state.showOverflowMenu,
                                    onOverflowMenuVisibilityChanged = {
                                        onEvent(ViewerViewEvent.OverflowMenuVisibilityChanged(it))
                                    },
                                    toolsSearchActive = state.toolsSearchActive,
                                    onToolsSearchActiveChanged = {
                                        onEvent(ViewerViewEvent.ToolsSearchActiveChanged(it))
                                    },
                                    toolsSearchQuery = state.toolsSearchQuery,
                                    onToolsSearchQueryChanged = {
                                        onEvent(ViewerViewEvent.ToolsSearchQueryChanged(it))
                                    },
                                    showClearTransactionsDialog = state.showClearTransactionsDialog,
                                    onClearTransactionsDialogVisibilityChanged = {
                                        onEvent(ViewerViewEvent.ClearTransactionsDialogVisibilityChanged(it))
                                    },
                                    showClearCrashesDialog = state.showClearCrashesDialog,
                                    onClearCrashesDialogVisibilityChanged = {
                                        onEvent(ViewerViewEvent.ClearCrashesDialogVisibilityChanged(it))
                                    },
                                    showDeleteSelectedDialog = state.showDeleteSelectedDialog,
                                    onDeleteSelectedDialogVisibilityChanged = {
                                        onEvent(ViewerViewEvent.DeleteSelectedDialogVisibilityChanged(it))
                                    },
                                    snackbarMessage = snackbarMessages,
                                )
                            }

                            composable(WormaCeptorNavKeys.TransactionDetail.route) { backStackEntry ->
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
                                            queryEngine = CoreHolder.queryEngine,
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
                                                queryEngine = CoreHolder.queryEngine,
                                                onBack = { navController.popBackStack() },
                                            )
                                        }
                                    }
                                }
                            }

                            composable(WormaCeptorNavKeys.CrashDetail.route) { backStackEntry ->
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

                            // Multi-screen feature graphs
                            preferencesGraph(
                                navController = navController,
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )

                            // Single-screen tools
                            composable(WormaCeptorNavKeys.Logs.route) {
                                LogViewer(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.DeviceInfo.route) {
                                DeviceInfoScreen(
                                    onBack = { navController.popBackStack() },
                                )
                            }

                            databaseGraph(
                                navController = navController,
                                context = this@ViewerActivity,
                                onNavigateBack = { navController.popBackStack() },
                            )

                            composable(WormaCeptorNavKeys.FileBrowser.route) {
                                FileBrowser(
                                    context = this@ViewerActivity,
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.Memory.route) {
                                MemoryMonitor(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.Fps.route) {
                                FpsMonitor(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            webSocketGraph(
                                navController = navController,
                                onNavigateBack = { navController.popBackStack() },
                            )

                            composable(WormaCeptorNavKeys.Cpu.route) {
                                CpuMonitor(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.Location.route) {
                                LocationSimulator(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.PushSimulator.route) {
                                PushSimulator(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.LeakDetection.route) {
                                LeakDetector(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.ThreadViolation.route) {
                                ThreadViolationMonitor(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.WebViewMonitor.route) {
                                WebViewMonitorScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.Crypto.route) {
                                CryptoScreen(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.SecureStorage.route) {
                                SecureStorageViewer(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.RateLimit.route) {
                                RateLimiter(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.PushToken.route) {
                                PushTokenManager(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.LoadedLibraries.route) {
                                LoadedLibrariesInspector(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }

                            composable(WormaCeptorNavKeys.Dependencies.route) {
                                DependenciesInspector(
                                    onNavigateBack = { navController.popBackStack() },
                                )
                            }
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

    private fun copyAsCurl(
        transaction: TransactionSummary,
        viewModel: ViewerViewModel,
    ) {
        lifecycleScope.launch {
            val fullTransaction = CoreHolder.queryEngine?.getDetails(transaction.id)
            if (fullTransaction == null) {
                viewModel.sendEvent(ViewerViewEvent.ShowMessage("Failed to load transaction details"))
                return@launch
            }

            val curl = buildCurlCommand(fullTransaction)
            val message = copyToClipboard(this@ViewerActivity, "cURL", curl)
            viewModel.sendEvent(ViewerViewEvent.ShowMessage(message))
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
                navController.popBackStack(WormaCeptorNavKeys.Home.route, inclusive = false)
                viewModel.sendEvent(ViewerViewEvent.TabSelected(destination.tabIndex))
            }

            is DeepLinkHandler.DeepLinkDestination.Tool -> {
                // Navigate directly to the tool screen
                // First ensure we're on home, then navigate to the tool
                if (navController.currentDestination?.route != WormaCeptorNavKeys.Home.route) {
                    navController.popBackStack(WormaCeptorNavKeys.Home.route, inclusive = false)
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
