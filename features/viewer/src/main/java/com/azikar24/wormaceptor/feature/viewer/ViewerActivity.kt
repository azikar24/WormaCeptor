package com.azikar24.wormaceptor.feature.viewer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.CoreHolder
import com.azikar24.wormaceptor.core.engine.LogCaptureEngine
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.engine.di.WormaCeptorKoin
import com.azikar24.wormaceptor.core.ui.navigation.FeatureRegistry
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavKeys
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.export.ExportManager
import com.azikar24.wormaceptor.feature.viewer.export.exportCrashes
import com.azikar24.wormaceptor.feature.viewer.navigation.DeepLinkHandler
import com.azikar24.wormaceptor.feature.viewer.ui.CrashDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.HomeScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailPagerScreen
import com.azikar24.wormaceptor.feature.viewer.ui.TransactionDetailScreen
import com.azikar24.wormaceptor.feature.viewer.ui.util.shareText
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionPagerViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewEffect
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

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
                            is ViewerViewEffect.ShowSnackBar ->
                                snackbarMessages.tryEmit(effect.message)

                            is ViewerViewEffect.ShareText ->
                                shareText(this@ViewerActivity, effect.text, effect.title)

                            is ViewerViewEffect.CopyToClipboard -> {
                                val message = copyToClipboard(
                                    this@ViewerActivity,
                                    effect.label,
                                    effect.content,
                                )
                                snackbarMessages.tryEmit(message)
                            }

                            is ViewerViewEffect.ExportTransactions -> {
                                lifecycleScope.launch {
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

                            is ViewerViewEffect.ExportCrashes -> {
                                lifecycleScope.launch {
                                    exportCrashes(
                                        this@ViewerActivity,
                                        effect.crashes,
                                        onMessage = { snackbarMessages.tryEmit(it) },
                                    )
                                }
                            }
                        }
                    },
                ) { state, onEvent ->
                    val transactions by viewModel.transactions.collectAsState()
                    val allTransactions by viewModel.allTransactions.collectAsState()
                    val crashes by viewModel.crashes.collectAsState()
                    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
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
                                    allTransactions = allTransactions,
                                    crashes = crashes,
                                    isSelectionMode = isSelectionMode,
                                    state = state,
                                    onEvent = onEvent,
                                    onTransactionClick = {
                                        navController.navigate(
                                            WormaCeptorNavKeys.TransactionDetail.createRoute(
                                                it.id.toString(),
                                            ),
                                        )
                                    },
                                    onCrashClick = { crash ->
                                        navController.navigate(
                                            WormaCeptorNavKeys.CrashDetail.createRoute(
                                                crash.timestamp,
                                            ),
                                        )
                                    },
                                    onToolNavigate = { route -> navController.navigate(route) },
                                    snackBarMessage = snackbarMessages,
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

                                    val queryEngine = requireNotNull(CoreHolder.queryEngine) {
                                        "WormaCeptor not initialized. " +
                                            "Call WormaCeptor.init() before launching ViewerActivity"
                                    }

                                    val detailViewModel: TransactionDetailViewModel = viewModel(
                                        factory = object : ViewModelProvider.Factory {
                                            @Suppress("UNCHECKED_CAST")
                                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                return TransactionDetailViewModel(queryEngine) as T
                                            }
                                        },
                                    )

                                    if (transactionIds.isNotEmpty()) {
                                        val pagerViewModel: TransactionPagerViewModel = viewModel(
                                            factory = object : ViewModelProvider.Factory {
                                                @Suppress("UNCHECKED_CAST")
                                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                                    return TransactionPagerViewModel(queryEngine) as T
                                                }
                                            },
                                        )

                                        TransactionDetailPagerScreen(
                                            transactionIds = transactionIds,
                                            initialTransactionIndex = initialIndex,
                                            pagerViewModel = pagerViewModel,
                                            detailViewModel = detailViewModel,
                                            onBack = { navController.popBackStack() },
                                        )
                                    } else {
                                        // Fallback for single transaction view
                                        var transaction by remember {
                                            mutableStateOf<NetworkTransaction?>(null)
                                        }

                                        androidx.compose.runtime.LaunchedEffect(uuid) {
                                            transaction = queryEngine.getDetails(uuid)
                                        }

                                        transaction?.let {
                                            TransactionDetailScreen(
                                                transaction = it,
                                                detailViewModel = detailViewModel,
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
}
