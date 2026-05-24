package com.azikar24.wormaceptorapp.main.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.api.compose.trackRecomposition
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorAlertDialog
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorBottomSheet
import com.azikar24.wormaceptor.core.ui.navigation.WormaCeptorNavTransitions
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptorapp.R
import com.azikar24.wormaceptorapp.main.uimodel.MainViewEffect
import com.azikar24.wormaceptorapp.main.uimodel.MainViewEvent
import com.azikar24.wormaceptorapp.main.uimodel.MainViewState
import com.azikar24.wormaceptorapp.main.viewmodel.MainViewModel
import com.azikar24.wormaceptorapp.navigation.TestToolsRoutes
import com.azikar24.wormaceptorapp.sampleservice.SampleDatabaseService
import com.azikar24.wormaceptorapp.sampleservice.SampleFileService
import com.azikar24.wormaceptorapp.sampleservice.SampleLogger
import com.azikar24.wormaceptorapp.sampleservice.SamplePreferencesService
import com.azikar24.wormaceptorapp.sampleservice.StressService
import com.azikar24.wormaceptorapp.screens.LocationTestScreen
import com.azikar24.wormaceptorapp.screens.SecureStorageTestScreen
import com.azikar24.wormaceptorapp.screens.WebViewTestScreen
import com.azikar24.wormaceptorapp.screens.location.LocationTestViewModel
import com.azikar24.wormaceptorapp.screens.securestorage.SecureStorageTestViewModel
import com.azikar24.wormaceptorapp.screens.webview.WebViewTestViewModel
import com.azikar24.wormaceptorapp.wormaceptorui.components.TestToolsSheetContent
import com.azikar24.wormaceptorapp.wormaceptorui.components.WelcomeScreen
import com.azikar24.wormaceptorapp.wormaceptorui.effects.GlitchEffect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    private fun triggerMemoryLeak() {
        MainViewModel.registerLeak(this)
    }

    private fun triggerThreadViolation() {
        val file = File(cacheDir, "thread_violation_test.txt")
        file.writeText("This write operation on the main thread triggers a StrictMode violation")
        file.readText()
        file.delete()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainActivityContent()
        }
        WormaCeptorApi.startActivityOnShake(this)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainActivityContent() {
        val mainViewModel: MainViewModel = viewModel()
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val glitchProgress = remember { Animatable(0f) }

        BaseScreen(
            viewModel = mainViewModel,
            onEffect = { effect ->
                handleEffect(effect, navController, sheetState, scope)
            },
        ) { state, onEvent ->
            LaunchedEffect(Unit) {
                onEvent(MainViewEvent.CheckLeakRotation)
            }

            GlitchAnimationEffect(state, glitchProgress, onEvent)

            WormaCeptorTheme {
                MainNavHost(state, onEvent, navController, glitchProgress.value)

                RecompositionStorm(active = state.recompositionStormActive)

                TestToolsSheet(state, onEvent, sheetState)

                if (state.showCrashDialog) {
                    WormaCeptorAlertDialog(
                        title = stringResource(id = R.string.crash_dialog_title),
                        message = stringResource(id = R.string.crash_dialog_message),
                        confirmLabel = stringResource(id = R.string.crash_dialog_confirm),
                        onConfirm = { onEvent(MainViewEvent.CrashConfirmed) },
                        dismissLabel = stringResource(id = R.string.crash_dialog_cancel),
                        onDismiss = { onEvent(MainViewEvent.CrashDialogDismissed) },
                        icon = Icons.Default.Warning,
                        destructive = true,
                    )
                }
            }
        }
    }

    @Composable
    private fun GlitchAnimationEffect(
        state: MainViewState,
        glitchProgress: Animatable<Float, *>,
        onEvent: (MainViewEvent) -> Unit,
    ) {
        LaunchedEffect(state.isGlitchEffectActive) {
            if (state.isGlitchEffectActive) {
                glitchProgress.snapTo(0f)
                var hasCrashed = false
                glitchProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(GLITCH_ANIMATION_DURATION, easing = WormaCeptorTokens.Easing.standard),
                ) {
                    if (value >= GLITCH_CRASH_THRESHOLD && !hasCrashed) {
                        hasCrashed = true
                        onEvent(MainViewEvent.GlitchAnimationCompleted)
                    }
                }
            }
        }
    }

    @Composable
    private fun MainNavHost(
        state: MainViewState,
        onEvent: (MainViewEvent) -> Unit,
        navController: NavHostController,
        glitchProgress: Float,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WormaCeptorTokens.semantic().background),
        ) {
            NavHost(
                navController = navController,
                startDestination = TestToolsRoutes.HOME,
                enterTransition = WormaCeptorNavTransitions.enterTransition,
                exitTransition = WormaCeptorNavTransitions.exitTransition,
                popEnterTransition = WormaCeptorNavTransitions.popEnterTransition,
                popExitTransition = WormaCeptorNavTransitions.popExitTransition,
            ) {
                composable(route = TestToolsRoutes.HOME) {
                    HomeScreen(state, onEvent, glitchProgress)
                }

                composable(route = TestToolsRoutes.LOCATION) {
                    val locationViewModel: LocationTestViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return LocationTestViewModel(application) as T
                            }
                        },
                    )
                    LocationTestScreen(
                        viewModel = locationViewModel,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(route = TestToolsRoutes.WEBVIEW) {
                    val webViewViewModel: WebViewTestViewModel = viewModel()
                    WebViewTestScreen(
                        viewModel = webViewViewModel,
                        onBack = { navController.popBackStack() },
                    )
                }

                composable(route = TestToolsRoutes.SECURE_STORAGE) {
                    val secureStorageViewModel: SecureStorageTestViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return SecureStorageTestViewModel(application) as T
                            }
                        },
                    )
                    SecureStorageTestScreen(
                        viewModel = secureStorageViewModel,
                        onBack = { navController.popBackStack() },
                    )
                }
            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    private fun HomeScreen(
        state: MainViewState,
        onEvent: (MainViewEvent) -> Unit,
        glitchProgress: Float,
    ) {
        GlitchEffect(
            isActive = state.isGlitchEffectActive,
            progress = glitchProgress,
            modifier = Modifier.fillMaxSize(),
        ) {
            Scaffold(
                containerColor = WormaCeptorTokens.semantic().background,
            ) { _ ->
                WelcomeScreen(
                    onLaunchClick = { onEvent(MainViewEvent.LaunchWormaCeptorClicked) },
                    onTestToolsClick = { onEvent(MainViewEvent.TestToolsClicked) },
                    onGitHubClick = { onEvent(MainViewEvent.GitHubClicked) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TestToolsSheet(
        state: MainViewState,
        onEvent: (MainViewEvent) -> Unit,
        sheetState: SheetState,
    ) {
        if (state.showTestToolsSheet) {
            WormaCeptorBottomSheet(
                onDismissRequest = { onEvent(MainViewEvent.TestToolsSheetDismissed) },
                modifier = Modifier.padding(top = WormaCeptorTokens.Spacing.xxxl),
                sheetState = sheetState,
                contentPadding = PaddingValues(),
            ) {
                TestToolsSheetContent(
                    modifier = Modifier.padding(bottom = WormaCeptorTokens.Spacing.lg),
                    apiTestStatus = state.apiTestStatus,
                    webSocketStatus = state.webSocketStatus,
                    leakStatus = state.leakStatus,
                    threadViolationStatus = state.threadViolationStatus,
                    seedDatabaseStatus = state.seedDatabaseStatus,
                    seedPreferencesStatus = state.seedPreferencesStatus,
                    writeFilesStatus = state.writeFilesStatus,
                    logsStatus = state.logsStatus,
                    cpuStressStatus = state.cpuStressStatus,
                    memoryStressStatus = state.memoryStressStatus,
                    frameDropStatus = state.frameDropStatus,
                    recompositionStormActive = state.recompositionStormActive,
                    onRunApiTests = { onEvent(MainViewEvent.RunApiTestsClicked) },
                    onWebSocketTest = { onEvent(MainViewEvent.WebSocketTestClicked) },
                    onTriggerCrash = { onEvent(MainViewEvent.TriggerCrashClicked) },
                    onTriggerLeak = { onEvent(MainViewEvent.TriggerLeakClicked) },
                    onThreadViolation = { onEvent(MainViewEvent.ThreadViolationClicked) },
                    onSeedDatabase = { onEvent(MainViewEvent.SeedDatabaseClicked) },
                    onSeedPreferences = { onEvent(MainViewEvent.SeedPreferencesClicked) },
                    onWriteSampleFiles = { onEvent(MainViewEvent.WriteSampleFilesClicked) },
                    onEmitSampleLogs = { onEvent(MainViewEvent.EmitSampleLogsClicked) },
                    onBurnCpu = { onEvent(MainViewEvent.BurnCpuClicked) },
                    onAllocateMemory = { onEvent(MainViewEvent.AllocateMemoryClicked) },
                    onDropFrames = { onEvent(MainViewEvent.DropFramesClicked) },
                    onRecompositionStorm = { onEvent(MainViewEvent.RecompositionStormClicked) },
                    onLocationClick = { onEvent(MainViewEvent.LocationClicked) },
                    onWebViewClick = { onEvent(MainViewEvent.WebViewClicked) },
                    onSecureStorageClick = { onEvent(MainViewEvent.SecureStorageClicked) },
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("LongMethod", "CyclomaticComplexMethod")
    private fun handleEffect(
        effect: MainViewEffect,
        navController: NavHostController,
        sheetState: SheetState,
        scope: CoroutineScope,
    ) {
        when (effect) {
            MainViewEffect.OpenWormaCeptor -> {
                startActivity(WormaCeptorApi.getLaunchIntent(this))
            }
            MainViewEffect.OpenGitHub -> {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = getString(R.string.github_link).toUri()
                }
                startActivity(intent)
            }
            MainViewEffect.NavigateToLocation -> hideSheetAndNavigate(
                scope,
                sheetState,
                navController,
                TestToolsRoutes.LOCATION,
            )
            MainViewEffect.NavigateToWebView -> hideSheetAndNavigate(
                scope,
                sheetState,
                navController,
                TestToolsRoutes.WEBVIEW,
            )
            MainViewEffect.NavigateToSecureStorage -> hideSheetAndNavigate(
                scope,
                sheetState,
                navController,
                TestToolsRoutes.SECURE_STORAGE,
            )
            MainViewEffect.SimulateCrash -> {
                @Suppress("MagicNumber")
                arrayOf("")[4]
            }
            MainViewEffect.TriggerMemoryLeak -> triggerMemoryLeak()
            MainViewEffect.TriggerThreadViolation -> triggerThreadViolation()

            MainViewEffect.SeedDatabase -> scope.launch(
                Dispatchers.IO,
            ) { SampleDatabaseService.seed(this@MainActivity) }
            MainViewEffect.SeedPreferences -> scope.launch(
                Dispatchers.IO,
            ) { SamplePreferencesService.seed(this@MainActivity) }
            MainViewEffect.WriteSampleFiles -> scope.launch(
                Dispatchers.IO,
            ) { SampleFileService.seed(this@MainActivity) }

            MainViewEffect.EmitSampleLogs -> SampleLogger.emitSamples()

            MainViewEffect.BurnCpu -> StressService.burnCpu()
            MainViewEffect.AllocateMemory -> StressService.allocateMemory()
            MainViewEffect.DropFrames -> StressService.jankMainThread()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    private fun hideSheetAndNavigate(
        scope: CoroutineScope,
        sheetState: SheetState,
        navController: NavHostController,
        route: String,
    ) {
        scope.launch { sheetState.hide() }
        scope.launch {
            delay(SHEET_DISMISS_DELAY)
            navController.navigate(route)
        }
    }

    companion object {
        private const val SHEET_DISMISS_DELAY = 100L
        private const val GLITCH_ANIMATION_DURATION = 1500
        private const val GLITCH_CRASH_THRESHOLD = 0.96f
    }
}

private object RecompositionStormConfig {
    const val INTERVAL_MS = 16L
}

@Composable
private fun RecompositionStorm(active: Boolean) {
    var ticker by remember { mutableIntStateOf(0) }
    LaunchedEffect(active) {
        while (active) {
            ticker++
            delay(RecompositionStormConfig.INTERVAL_MS)
        }
    }
    Box(
        modifier = Modifier
            .size(0.dp)
            .trackRecomposition("DemoRecompositionStorm"),
    ) {
        @Suppress("UnusedPrivateMember")
        val read = ticker
    }
}
