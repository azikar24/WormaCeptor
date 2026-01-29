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
import com.azikar24.wormaceptorapp.screens.ComposeRenderTestScreen
import com.azikar24.wormaceptorapp.screens.CookiesTestScreen
import com.azikar24.wormaceptorapp.screens.LocationTestScreen
import com.azikar24.wormaceptorapp.screens.SecureStorageTestScreen
import com.azikar24.wormaceptorapp.screens.WebViewTestScreen
import com.azikar24.wormaceptorapp.wormaceptorui.components.ShowcaseTab
import com.azikar24.wormaceptorapp.wormaceptorui.components.TestToolsTab
import com.azikar24.wormaceptorapp.wormaceptorui.components.ToolStatus
import com.azikar24.wormaceptorapp.wormaceptorui.effects.GlitchMeltdownEffect
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : ComponentActivity() {

    companion object {
        // Intentional memory leak for testing leak detection tools
        // This list holds strong references to Activity instances, preventing GC
        private val _leakedActivities = mutableListOf<MainActivity>()

        // Inline status feedback durations
        private const val STATUS_RUNNING_DURATION = 800L
        private const val STATUS_DONE_DURATION = 1500L
    }

    /**
     * Intentionally leaks this Activity by storing it in a static list.
     * Use this to test memory leak detection tools like LeakCanary.
     */
    fun triggerMemoryLeak() {
        _leakedActivities.add(this)
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
            NavHost(
                navController = navController,
                startDestination = TestToolsRoutes.HOME,
            ) {
                composable(TestToolsRoutes.HOME) {
                    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
                    GlitchMeltdownEffect(
                        isActive = isGlitchEffectActive,
                        progress = glitchProgress,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        Scaffold(
                            containerColor = MaterialTheme.colorScheme.background,
                        ) { _ ->
                            ShowcaseTab(
                                onLaunchClick = { viewModel.startWormaCeptor(this@MainActivity) },
                                onTestToolsClick = { showTestToolsSheet = true },
                                onGitHubClick = { viewModel.goToGithub(this@MainActivity) },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }

                composable(TestToolsRoutes.LOCATION) {
                    LocationTestScreen(onBack = { navController.popBackStack() })
                }

                composable(TestToolsRoutes.COOKIES) {
                    CookiesTestScreen(onBack = { navController.popBackStack() })
                }

                composable(TestToolsRoutes.WEBVIEW) {
                    WebViewTestScreen(onBack = { navController.popBackStack() })
                }

                composable(TestToolsRoutes.SECURE_STORAGE) {
                    SecureStorageTestScreen(onBack = { navController.popBackStack() })
                }

                composable(TestToolsRoutes.COMPOSE_RENDER) {
                    ComposeRenderTestScreen(onBack = { navController.popBackStack() })
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
                            scope.launch {
                                leakStatus = ToolStatus.Done
                                delay(STATUS_DONE_DURATION)
                                leakStatus = ToolStatus.Idle
                            }
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
                            showTestToolsSheet = false
                            navController.navigate(TestToolsRoutes.LOCATION)
                        },
                        onCookiesClick = {
                            showTestToolsSheet = false
                            navController.navigate(TestToolsRoutes.COOKIES)
                        },
                        onWebViewClick = {
                            showTestToolsSheet = false
                            navController.navigate(TestToolsRoutes.WEBVIEW)
                        },
                        onSecureStorageClick = {
                            showTestToolsSheet = false
                            navController.navigate(TestToolsRoutes.SECURE_STORAGE)
                        },
                        onComposeRenderClick = {
                            showTestToolsSheet = false
                            navController.navigate(TestToolsRoutes.COMPOSE_RENDER)
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
