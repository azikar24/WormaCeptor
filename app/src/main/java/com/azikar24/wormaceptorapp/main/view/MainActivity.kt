package com.azikar24.wormaceptorapp.main.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.R
import com.azikar24.wormaceptorapp.main.uimodel.MainViewEffect
import com.azikar24.wormaceptorapp.main.uimodel.MainViewEvent
import com.azikar24.wormaceptorapp.main.uimodel.MainViewState
import com.azikar24.wormaceptorapp.main.viewmodel.MainViewModel
import com.azikar24.wormaceptorapp.navigation.TestToolsRoutes
import com.azikar24.wormaceptorapp.screens.LocationTestScreen
import com.azikar24.wormaceptorapp.screens.SecureStorageTestScreen
import com.azikar24.wormaceptorapp.screens.WebViewTestScreen
import com.azikar24.wormaceptorapp.wormaceptorui.components.TestToolsTab
import com.azikar24.wormaceptorapp.wormaceptorui.components.WelcomeScreen
import com.azikar24.wormaceptorapp.wormaceptorui.effects.GlitchEffect
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import kotlinx.coroutines.CoroutineScope
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

            WormaCeptorMainTheme {
                MainNavHost(state, onEvent, navController, glitchProgress.value)

                TestToolsSheet(state, onEvent, sheetState)

                if (state.showCrashDialog) {
                    CrashConfirmationDialog(
                        onConfirm = { onEvent(MainViewEvent.CrashConfirmed) },
                        onDismiss = { onEvent(MainViewEvent.CrashDialogDismissed) },
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
                    animationSpec = tween(GLITCH_ANIMATION_DURATION, easing = FastOutSlowInEasing),
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
                .background(MaterialTheme.colorScheme.background),
        ) {
            NavHost(
                navController = navController,
                startDestination = TestToolsRoutes.HOME,
            ) {
                composable(
                    route = TestToolsRoutes.HOME,
                    exitTransition = { slideOutLeft() + fadeOut(animationSpec = tween(NAV_TRANSITION_DURATION)) },
                    popEnterTransition = { slideInLeft() + fadeIn(animationSpec = tween(NAV_TRANSITION_DURATION)) },
                ) {
                    HomeScreen(state, onEvent, glitchProgress)
                }

                composable(
                    route = TestToolsRoutes.LOCATION,
                    enterTransition = { slideInRight() },
                    popExitTransition = { slideOutRight() },
                ) {
                    LocationTestScreen(onBack = { navController.popBackStack() })
                }

                composable(
                    route = TestToolsRoutes.WEBVIEW,
                    enterTransition = { slideInRight() },
                    popExitTransition = { slideOutRight() },
                ) {
                    WebViewTestScreen(onBack = { navController.popBackStack() })
                }

                composable(
                    route = TestToolsRoutes.SECURE_STORAGE,
                    enterTransition = { slideInRight() },
                    popExitTransition = { slideOutRight() },
                ) {
                    SecureStorageTestScreen(onBack = { navController.popBackStack() })
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
                containerColor = MaterialTheme.colorScheme.background,
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

    private fun slideOutLeft() = slideOutHorizontally(
        targetOffsetX = { -it / 4 },
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
    )

    private fun slideInLeft() = slideInHorizontally(
        initialOffsetX = { -it / 4 },
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
    )

    private fun slideInRight() = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
    )

    private fun slideOutRight() = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun TestToolsSheet(
        state: MainViewState,
        onEvent: (MainViewEvent) -> Unit,
        sheetState: SheetState,
    ) {
        if (state.showTestToolsSheet) {
            ModalBottomSheet(
                modifier = Modifier.padding(top = WormaCeptorDesignSystem.Spacing.xxxl),
                onDismissRequest = { onEvent(MainViewEvent.TestToolsSheetDismissed) },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                TestToolsTab(
                    onRunApiTests = { onEvent(MainViewEvent.RunApiTestsClicked) },
                    onWebSocketTest = { onEvent(MainViewEvent.WebSocketTestClicked) },
                    onTriggerCrash = { onEvent(MainViewEvent.TriggerCrashClicked) },
                    onTriggerLeak = { onEvent(MainViewEvent.TriggerLeakClicked) },
                    onThreadViolation = { onEvent(MainViewEvent.ThreadViolationClicked) },
                    onLocationClick = { onEvent(MainViewEvent.LocationClicked) },
                    onWebViewClick = { onEvent(MainViewEvent.WebViewClicked) },
                    onSecureStorageClick = { onEvent(MainViewEvent.SecureStorageClicked) },
                    apiTestStatus = state.apiTestStatus,
                    webSocketStatus = state.webSocketStatus,
                    leakStatus = state.leakStatus,
                    threadViolationStatus = state.threadViolationStatus,
                    modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.lg),
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
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
            MainViewEffect.NavigateToLocation -> {
                scope.launch {
                    sheetState.hide()
                }
                scope.launch {
                    delay(SHEET_DISMISS_DELAY)
                    navController.navigate(TestToolsRoutes.LOCATION)
                }
            }
            MainViewEffect.NavigateToWebView -> {
                scope.launch {
                    sheetState.hide()
                }
                scope.launch {
                    delay(SHEET_DISMISS_DELAY)
                    navController.navigate(TestToolsRoutes.WEBVIEW)
                }
            }
            MainViewEffect.NavigateToSecureStorage -> {
                scope.launch {
                    sheetState.hide()
                }
                scope.launch {
                    delay(SHEET_DISMISS_DELAY)
                    navController.navigate(TestToolsRoutes.SECURE_STORAGE)
                }
            }
            MainViewEffect.SimulateCrash -> {
                @Suppress("MagicNumber")
                arrayOf("")[4]
            }
            MainViewEffect.TriggerMemoryLeak -> triggerMemoryLeak()
            MainViewEffect.TriggerThreadViolation -> triggerThreadViolation()
        }
    }

    companion object {
        private const val NAV_TRANSITION_DURATION = 200
        private const val SHEET_DISMISS_DELAY = 100L
        private const val GLITCH_ANIMATION_DURATION = 1500
        private const val GLITCH_CRASH_THRESHOLD = 0.96f
    }
}
