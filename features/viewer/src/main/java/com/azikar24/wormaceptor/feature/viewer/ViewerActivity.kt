package com.azikar24.wormaceptor.feature.viewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.core.ui.navigation.FeatureRegistry
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavTransitions
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.feature.viewer.export.ExportManager
import com.azikar24.wormaceptor.feature.viewer.export.exportCrashes
import com.azikar24.wormaceptor.feature.viewer.navigation.DeepLinkHandler
import com.azikar24.wormaceptor.feature.viewer.ui.CrashDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.HomeScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareText
import com.azikar24.wormaceptor.feature.viewer.vm.CrashListViewEffect
import com.azikar24.wormaceptor.feature.viewer.vm.CrashListViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewEffect
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewEffect
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionPagerViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.UUID

/** Main activity hosting the WormaCeptor debugging UI with navigation and deep link support. */
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

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Start log capture engine
        logCaptureEngine.start()

        // Handle initial deep link (if activity was launched via deep link)
        if (savedInstanceState == null) {
            initialDeepLink = DeepLinkHandler.parseDeepLink(intent)
                .takeIf { it !is DeepLinkHandler.DeepLinkDestination.Invalid }
        }

        val queryEngine = requireNotNull(CoreHolder.queryEngine) {
            "WormaCeptor not initialized. Call WormaCeptor.init() before launching ViewerActivity"
        }

        val homeViewModel = ViewModelProvider(
            this,
            viewModelFactory { HomeViewModel(applicationContext) },
        )[HomeViewModel::class.java]

        val transactionListViewModel = ViewModelProvider(
            this,
            viewModelFactory { TransactionListViewModel(queryEngine) },
        )[TransactionListViewModel::class.java]

        val crashListViewModel = ViewModelProvider(
            this,
            viewModelFactory { CrashListViewModel(queryEngine) },
        )[CrashListViewModel::class.java]

        setContent {
            val snackbarMessages = remember { MutableSharedFlow<String>(extraBufferCapacity = 1) }

            val homeState by homeViewModel.uiState.collectAsState()
            val transactionState by transactionListViewModel.uiState.collectAsState()
            val crashState by crashListViewModel.uiState.collectAsState()

            // Collect effects from TransactionListViewModel
            LaunchedEffect(transactionListViewModel) {
                transactionListViewModel.effects.collect { effect ->
                    when (effect) {
                        is TransactionListViewEffect.ShowSnackBar ->
                            snackbarMessages.tryEmit(effect.message)

                        is TransactionListViewEffect.ShareText ->
                            shareText(this@ViewerActivity, effect.text, effect.title)

                        is TransactionListViewEffect.CopyToClipboard -> {
                            val message = copyToClipboard(
                                this@ViewerActivity,
                                effect.label,
                                effect.content,
                            )
                            snackbarMessages.tryEmit(message)
                        }

                        is TransactionListViewEffect.ExportTransactions -> {
                            launch {
                                val exportManager = ExportManager(
                                    this@ViewerActivity,
                                    CoreHolder.queryEngine,
                                    onMessage = { snackbarMessages.tryEmit(it) },
                                )
                                exportManager.exportTransactions(
                                    effect.transactions,
                                    format = effect.format,
                                )
                            }
                        }
                    }
                }
            }

            // Collect effects from CrashListViewModel
            LaunchedEffect(crashListViewModel) {
                crashListViewModel.effects.collect { effect ->
                    when (effect) {
                        is CrashListViewEffect.ShowSnackBar ->
                            snackbarMessages.tryEmit(effect.message)

                        is CrashListViewEffect.ExportCrashes -> {
                            launch {
                                exportCrashes(
                                    this@ViewerActivity,
                                    effect.crashes,
                                    onMessage = { snackbarMessages.tryEmit(it) },
                                )
                            }
                        }
                    }
                }
            }

            WormaCeptorTheme {
                val navController = rememberNavController()

                // Collect effects from HomeViewModel
                LaunchedEffect(homeViewModel) {
                    homeViewModel.effects.collect { effect ->
                        when (effect) {
                            is HomeViewEffect.ShowSnackBar ->
                                snackbarMessages.tryEmit(effect.message)

                            is HomeViewEffect.NavigateToTransaction ->
                                navController.navigate(
                                    WormaCeptorNavKeys.TransactionDetail.createRoute(
                                        effect.summary.id.toString(),
                                    ),
                                )

                            is HomeViewEffect.NavigateToCrash ->
                                navController.navigate(
                                    WormaCeptorNavKeys.CrashDetail.createRoute(
                                        effect.crash.timestamp,
                                    ),
                                )

                            is HomeViewEffect.NavigateToTool ->
                                navController.navigate(effect.route)

                            is HomeViewEffect.NavigateBack -> finish()
                        }
                    }
                }

                // Handle deep link navigation
                LaunchedEffect(Unit) {
                    // Handle initial deep link
                    initialDeepLink?.let { destination ->
                        handleDeepLinkDestination(navController, homeViewModel, destination)
                        initialDeepLink = null
                    }

                    // Handle subsequent deep links (when activity receives new intent)
                    deepLinkNavigation.collect { destination ->
                        handleDeepLinkDestination(navController, homeViewModel, destination)
                    }
                }

                // Wrap NavHost in Surface to ensure proper background during navigation transitions
                // This prevents white flash in dark mode when navigating back
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavHost(
                        navController = navController,
                        startDestination = WormaCeptorNavKeys.Home.route,
                        enterTransition = WormaCeptorNavTransitions.enterTransition,
                        exitTransition = WormaCeptorNavTransitions.exitTransition,
                        popEnterTransition = WormaCeptorNavTransitions.popEnterTransition,
                        popExitTransition = WormaCeptorNavTransitions.popExitTransition,
                    ) {
                        composable(WormaCeptorNavKeys.Home.route) {
                            HomeScreen(
                                homeState = homeState,
                                transactionState = transactionState,
                                crashState = crashState,
                                onHomeEvent = homeViewModel::sendEvent,
                                onTransactionEvent = transactionListViewModel::sendEvent,
                                onCrashEvent = crashListViewModel::sendEvent,
                                snackBarMessage = snackbarMessages,
                            )
                        }

                        composable(WormaCeptorNavKeys.TransactionDetail.route) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id")
                            val uuid = id?.let {
                                try {
                                    UUID.fromString(it)
                                } catch (_: IllegalArgumentException) {
                                    null
                                }
                            }

                            if (uuid != null) {
                                // Snapshot the transaction list when entering the detail screen
                                // This prevents the pager from jumping when new requests come in
                                val snapshotKey = backStackEntry.id
                                val (transactionIds, initialIndex) = remember(snapshotKey) {
                                    val ids = transactionState.transactions.map { it.id }
                                    val index = ids.indexOf(uuid).coerceAtLeast(0)
                                    ids to index
                                }

                                // Use single-item list when transaction list hasn't loaded yet (e.g., deep link)
                                val effectiveIds = transactionIds.ifEmpty { listOf(uuid) }

                                val detailViewModel: TransactionDetailViewModel = viewModel(
                                    factory = viewModelFactory {
                                        TransactionDetailViewModel(queryEngine)
                                    },
                                )

                                val pagerViewModel: TransactionPagerViewModel = viewModel(
                                    factory = viewModelFactory {
                                        TransactionPagerViewModel(queryEngine)
                                    },
                                )

                                TransactionDetailPagerScreen(
                                    transactionIds = effectiveIds,
                                    initialTransactionIndex = if (transactionIds.isNotEmpty()) {
                                        initialIndex
                                    } else {
                                        0
                                    },
                                    pagerViewModel = pagerViewModel,
                                    detailViewModel = detailViewModel,
                                    onBack = { navController.popBackStack() },
                                )
                            } else {
                                // Invalid UUID — navigate back
                                LaunchedEffect(Unit) {
                                    navController.popBackStack()
                                }
                            }
                        }

                        composable(WormaCeptorNavKeys.CrashDetail.route) { backStackEntry ->
                            val timestamp =
                                backStackEntry.arguments?.getString("timestamp")?.toLongOrNull()
                            if (timestamp != null) {
                                // Snapshot the crash list when entering the detail screen
                                // This prevents the pager from jumping when new crashes come in
                                val snapshotKey = backStackEntry.id
                                val (crashList, initialIndex) = remember(snapshotKey) {
                                    val index =
                                        crashState.crashes.indexOfFirst { it.timestamp == timestamp }
                                            .coerceAtLeast(0)
                                    crashState.crashes to index
                                }

                                if (crashList.isNotEmpty()) {
                                    CrashDetailPagerScreen(
                                        crashes = crashList,
                                        initialCrashIndex = initialIndex,
                                        onBack = { navController.popBackStack() },
                                    )
                                } else {
                                    // Empty crash list — navigate back
                                    LaunchedEffect(Unit) {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }

                        // Feature tools (dynamically registered via FeatureRegistry)
                        FeatureRegistry.contributeAll(
                            builder = this@NavHost,
                            navController = navController,
                            context = this@ViewerActivity,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
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
        homeViewModel: HomeViewModel,
        destination: DeepLinkHandler.DeepLinkDestination,
    ) {
        when (destination) {
            is DeepLinkHandler.DeepLinkDestination.Tab -> {
                // Navigate to home and select the specified tab
                navController.popBackStack(WormaCeptorNavKeys.Home.route, inclusive = false)
                homeViewModel.sendEvent(HomeViewEvent.TabSelected(destination.tabIndex))
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
}

private inline fun <reified T : ViewModel> viewModelFactory(crossinline create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <V : ViewModel> create(modelClass: Class<V>): V = create() as V
    }
