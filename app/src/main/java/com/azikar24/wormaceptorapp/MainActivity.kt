/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.navigation.TestToolsRoutes
import com.azikar24.wormaceptorapp.screens.LocationTestScreen
import com.azikar24.wormaceptorapp.screens.SecureStorageTestScreen
import com.azikar24.wormaceptorapp.screens.WebViewTestScreen
import com.azikar24.wormaceptorapp.wormaceptorui.components.TestToolsTab
import com.azikar24.wormaceptorapp.wormaceptorui.components.ToolStatus
import com.azikar24.wormaceptorapp.wormaceptorui.components.WelcomeScreen
import com.azikar24.wormaceptorapp.wormaceptorui.effects.GlitchEffect
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    companion object {
        // Intentional memory leak for testing leak detection tools
        // This list holds strong references to Activity instances, preventing GC
        private val _leakedActivities = mutableListOf<MainActivity>()

        // Flag to detect rotation after leak was triggered
        private var _leakAwaitingRotation = false

        // Inline status feedback durations
        private const val STATUS_RUNNING_DURATION = 800L
        private const val STATUS_DONE_DURATION = 1500L

        // Navigation transition durations
        private const val NAV_TRANSITION_DURATION = 200
        private const val SHEET_DISMISS_DELAY = 100L

        /**
         * Check if a leak was triggered and rotation occurred.
         * Clears the flag after checking.
         */
        fun checkLeakRotationDetected(): Boolean {
            val detected = _leakAwaitingRotation
            _leakAwaitingRotation = false
            return detected
        }
    }

    /**
     * Intentionally leaks this Activity by storing it in a static list.
     * Use this to test memory leak detection tools like LeakCanary.
     * @return true to indicate leak was triggered and waiting for rotation
     */
    fun triggerMemoryLeak(): Boolean {
        _leakedActivities.add(this)
        _leakAwaitingRotation = true
        return true
    }

    /**
     * Intentionally performs disk I/O on the main thread.
     * Use this to test StrictMode thread violation detection.
     */
    fun triggerThreadViolation() {
        // Perform disk I/O on main thread - this is a StrictMode violation
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
    private fun MainActivityContent(viewModel: MainActivityViewModel = MainActivityViewModel()) {
        val scope = rememberCoroutineScope()
        val navController = rememberNavController()
        var showCrashDialog by remember { mutableStateOf(false) }
        var isGlitchEffectActive by remember { mutableStateOf(false) }
        var glitchProgress by remember { mutableFloatStateOf(0f) }
        var showTestToolsSheet by remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        // Tool status states for inline feedback
        var apiTestStatus by remember { mutableStateOf(ToolStatus.Idle) }
        var webSocketStatus by remember { mutableStateOf(ToolStatus.Idle) }
        var leakStatus by remember { mutableStateOf(ToolStatus.Idle) }
        var threadViolationStatus by remember { mutableStateOf(ToolStatus.Idle) }

        // Check if leak was triggered and rotation occurred (Activity recreated)
        LaunchedEffect(Unit) {
            if (checkLeakRotationDetected()) {
                leakStatus = ToolStatus.Done
                delay(STATUS_DONE_DURATION)
                leakStatus = ToolStatus.Idle
            }
        }

        LaunchedEffect(isGlitchEffectActive) {
            if (isGlitchEffectActive) {
                glitchProgress = 0f
                var hasCrashed = false
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(1500, easing = FastOutSlowInEasing),
                ) { value, _ ->
                    glitchProgress = value
                    if (value >= 0.96f && !hasCrashed) {
                        hasCrashed = true
                        viewModel.simulateCrash()
                    }
                }
            }
        }

        WormaCeptorMainTheme {
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
                        exitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { -it / 4 },
                                animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ) + fadeOut(animationSpec = tween(NAV_TRANSITION_DURATION))
                        },
                        popEnterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { -it / 4 },
                                animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            ) + fadeIn(animationSpec = tween(NAV_TRANSITION_DURATION))
                        },
                    ) {
                        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
                        GlitchEffect(
                            isActive = isGlitchEffectActive,
                            progress = glitchProgress,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            Scaffold(
                                containerColor = MaterialTheme.colorScheme.background,
                            ) { _ ->
                                WelcomeScreen(
                                    onLaunchClick = { viewModel.startWormaCeptor(this@MainActivity) },
                                    onTestToolsClick = { showTestToolsSheet = true },
                                    onGitHubClick = { viewModel.goToGithub(this@MainActivity) },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }

                    composable(
                        route = TestToolsRoutes.LOCATION,
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            )
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            )
                        },
                    ) {
                        LocationTestScreen(onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = TestToolsRoutes.WEBVIEW,
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            )
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            )
                        },
                    ) {
                        WebViewTestScreen(onBack = { navController.popBackStack() })
                    }

                    composable(
                        route = TestToolsRoutes.SECURE_STORAGE,
                        enterTransition = {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            )
                        },
                        popExitTransition = {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(NAV_TRANSITION_DURATION, easing = FastOutSlowInEasing),
                            )
                        },
                    ) {
                        SecureStorageTestScreen(onBack = { navController.popBackStack() })
                    }
                }
            }

            // Test Tools Bottom Sheet
            if (showTestToolsSheet) {
                ModalBottomSheet(
                    modifier = Modifier.padding(top = WormaCeptorDesignSystem.Spacing.xxxl),
                    onDismissRequest = { showTestToolsSheet = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                ) {
                    TestToolsTab(
                        onRunApiTests = {
                            viewModel.doHttpActivity(baseContext)
                            viewModel.doContentTypeTests()
                            scope.launch {
                                apiTestStatus = ToolStatus.Running
                                delay(STATUS_RUNNING_DURATION)
                                apiTestStatus = ToolStatus.Done
                                delay(STATUS_DONE_DURATION)
                                apiTestStatus = ToolStatus.Idle
                            }
                        },
                        onWebSocketTest = {
                            viewModel.doWebSocketTest()
                            scope.launch {
                                webSocketStatus = ToolStatus.Running
                                delay(STATUS_RUNNING_DURATION)
                                webSocketStatus = ToolStatus.Done
                                delay(STATUS_DONE_DURATION)
                                webSocketStatus = ToolStatus.Idle
                            }
                        },
                        onTriggerCrash = {
                            showTestToolsSheet = false
                            showCrashDialog = true
                        },
                        onTriggerLeak = {
                            triggerMemoryLeak()
                            leakStatus = ToolStatus.WaitingForAction
                        },
                        onThreadViolation = {
                            triggerThreadViolation()
                            scope.launch {
                                threadViolationStatus = ToolStatus.Done
                                delay(STATUS_DONE_DURATION)
                                threadViolationStatus = ToolStatus.Idle
                            }
                        },
                        onLocationClick = {
                            scope.launch {
                                sheetState.hide()
                                showTestToolsSheet = false
                            }
                            scope.launch {
                                delay(SHEET_DISMISS_DELAY)
                                navController.navigate(TestToolsRoutes.LOCATION)
                            }
                        },
                        onWebViewClick = {
                            scope.launch {
                                sheetState.hide()
                                showTestToolsSheet = false
                            }
                            scope.launch {
                                delay(SHEET_DISMISS_DELAY)
                                navController.navigate(TestToolsRoutes.WEBVIEW)
                            }
                        },
                        onSecureStorageClick = {
                            scope.launch {
                                sheetState.hide()
                                showTestToolsSheet = false
                            }
                            scope.launch {
                                delay(SHEET_DISMISS_DELAY)
                                navController.navigate(TestToolsRoutes.SECURE_STORAGE)
                            }
                        },
                        apiTestStatus = apiTestStatus,
                        webSocketStatus = webSocketStatus,
                        leakStatus = leakStatus,
                        threadViolationStatus = threadViolationStatus,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                }
            }

            if (showCrashDialog) {
                CrashConfirmationDialog(
                    onConfirm = {
                        showCrashDialog = false
                        isGlitchEffectActive = true
                    },
                    onDismiss = { showCrashDialog = false },
                )
            }
        }
    }

    @Composable
    private fun CrashConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp),
                )
            },
            title = {
                Text(
                    text = stringResource(id = R.string.crash_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.crash_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(
                        text = stringResource(id = R.string.crash_dialog_confirm),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.crash_dialog_cancel))
                }
            },
        )
    }
}
