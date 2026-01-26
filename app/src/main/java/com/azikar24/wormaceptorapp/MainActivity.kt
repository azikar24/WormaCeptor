/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.wormaceptorui.components.SegmentedControl
import com.azikar24.wormaceptorapp.wormaceptorui.components.ShowcaseTab
import com.azikar24.wormaceptorapp.wormaceptorui.components.TestToolsTab
import com.azikar24.wormaceptorapp.wormaceptorui.effects.GlitchMeltdownEffect
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import kotlinx.coroutines.launch
import java.io.File

private const val TAB_CROSSFADE_DURATION_MS = 200

class MainActivity : ComponentActivity() {

    companion object {
        // Intentional memory leak for testing leak detection tools
        // This list holds strong references to Activity instances, preventing GC
        @Suppress("ObjectPropertyName")
        private val _leakedActivities = mutableListOf<MainActivity>()
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
    @Suppress("BlockingMethodInNonBlockingContext")
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

    @Composable
    private fun MainActivityContent(viewModel: MainActivityViewModel = MainActivityViewModel()) {
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var showCrashDialog by remember { mutableStateOf(false) }
        var isGlitchEffectActive by remember { mutableStateOf(false) }
        var glitchProgress by remember { mutableFloatStateOf(0f) }
        var selectedTab by remember { mutableIntStateOf(0) }

        val tabSegments = listOf("Showcase", "Test Tools")

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

        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        WormaCeptorMainTheme {
            GlitchMeltdownEffect(
                isActive = isGlitchEffectActive,
                progress = glitchProgress,
                modifier = Modifier.fillMaxSize(),
            ) {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.background,
                ) { _ ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
                    ) {
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

                        // Segmented Control at top
                        SegmentedControl(
                            segments = tabSegments,
                            selectedIndex = selectedTab,
                            onSelectedChange = { selectedTab = it },
                            modifier = Modifier.padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
                        )

                        // Tab content with crossfade animation
                        Crossfade(
                            targetState = selectedTab,
                            animationSpec = tween(TAB_CROSSFADE_DURATION_MS),
                            label = "tab_crossfade",
                            modifier = Modifier.weight(1f),
                        ) { tabIndex ->
                            when (tabIndex) {
                                0 -> ShowcaseTab(
                                    onLaunchClick = { viewModel.startWormaCeptor(this@MainActivity) },
                                    onGitHubClick = { viewModel.goToGithub(this@MainActivity) },
                                )

                                1 -> TestToolsTab(
                                    onRunApiTests = {
                                        viewModel.doHttpActivity(baseContext)
                                        viewModel.doContentTypeTests()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Running API tests...")
                                        }
                                    },
                                    onWebSocketTest = {
                                        viewModel.doWebSocketTest()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Running WebSocket test...")
                                        }
                                    },
                                    onTriggerCrash = { showCrashDialog = true },
                                    onTriggerLeak = {
                                        triggerMemoryLeak()
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Memory leak triggered! Rotate device to detect.",
                                            )
                                        }
                                    },
                                    onThreadViolation = {
                                        triggerThreadViolation()
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Thread violation triggered! Check StrictMode logs.",
                                            )
                                        }
                                    },
                                    onLocationClick = {
                                        startActivity(
                                            Intent(this@MainActivity, LocationTestActivity::class.java),
                                        )
                                    },
                                    onCookiesClick = {
                                        startActivity(
                                            Intent(this@MainActivity, CookiesTestActivity::class.java),
                                        )
                                    },
                                    onWebViewClick = {
                                        startActivity(
                                            Intent(this@MainActivity, WebViewTestActivity::class.java),
                                        )
                                    },
                                    onSecureStorageClick = {
                                        startActivity(
                                            Intent(this@MainActivity, SecureStorageTestActivity::class.java),
                                        )
                                    },
                                    onComposeRenderClick = {
                                        startActivity(
                                            Intent(this@MainActivity, ComposeRenderTestActivity::class.java),
                                        )
                                    },
                                )
                            }
                        }
                    }
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
