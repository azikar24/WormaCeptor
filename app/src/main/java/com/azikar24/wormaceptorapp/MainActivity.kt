/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptorapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import com.azikar24.wormaceptorapp.wormaceptorui.theme.asLightBackground
import com.azikar24.wormaceptorapp.wormaceptorui.theme.drawables.MyIconPack
import kotlinx.coroutines.launch

// Warning color for crash-related UI elements
private val StatusAmber = Color(0x66FC0026)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
        @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
        WormaCeptorMainTheme {
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                containerColor = MaterialTheme.colorScheme.background
            ) { _ ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center
                ) {
                    Header()
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
                    InfoBanner()
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
                    Content(
                        viewModel = viewModel,
                        onHttpActivityClick = {
                            viewModel.doHttpActivity(baseContext)
                            scope.launch {
                                snackbarHostState.showSnackbar("Sending test requests...")
                            }
                        },
                        onContentTypeTestClick = {
                            viewModel.doContentTypeTests()
                            scope.launch {
                                snackbarHostState.showSnackbar("Testing response types...")
                            }
                        },
                        onCrashClick = { showCrashDialog = true }
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))
                    Footer(onGitHubClick = { viewModel.goToGithub(this@MainActivity) })
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
                }
            }

            if (showCrashDialog) {
                CrashConfirmationDialog(
                    onConfirm = {
                        showCrashDialog = false
                        viewModel.simulateCrash()
                    },
                    onDismiss = { showCrashDialog = false }
                )
            }
        }
    }

    @Composable
    private fun CrashConfirmationDialog(
        onConfirm: () -> Unit,
        onDismiss: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = StatusAmber,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = stringResource(id = R.string.crash_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.crash_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = StatusAmber
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.crash_dialog_confirm),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(id = R.string.crash_dialog_cancel))
                }
            }
        )
    }

    @Composable
    private fun Header() {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
                .padding(top = WormaCeptorDesignSystem.Spacing.xxl)
        ) {
            Image(
                imageVector = MyIconPack.icIconFull(),
                contentDescription = stringResource(id = R.string.app_name),
                modifier = Modifier
                    .width(100.dp)
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

            Text(
                text = stringResource(id = R.string.app_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
    }

    @Composable
    private fun InfoBanner() {
        Surface(
            shape = WormaCeptorDesignSystem.Shapes.card,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.info_shake_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))

                    Text(
                        text = stringResource(id = R.string.info_shake_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    @Composable
    private fun Content(
        viewModel: MainActivityViewModel,
        onHttpActivityClick: () -> Unit,
        onContentTypeTestClick: () -> Unit,
        onCrashClick: () -> Unit
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
        ) {
            // Navigation card (full width with chevron) - Launch Inspector first
            ActionCard(
                icon = Icons.Default.Launch,
                title = stringResource(id = R.string.action_launch_title),
                description = stringResource(id = R.string.action_launch_description),
                onClick = { viewModel.startWormaCeptor(this@MainActivity) },
                showChevron = true
            )

            // Action buttons in rows (non-navigating)
            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                modifier = Modifier.fillMaxWidth()
            ) {
                CompactActionCard(
                    icon = Icons.Default.PlayArrow,
                    title = stringResource(id = R.string.action_http_title),
                    onClick = onHttpActivityClick,
                    modifier = Modifier.weight(1f)
                )

                CompactActionCard(
                    icon = Icons.Default.Image,
                    title = stringResource(id = R.string.action_content_type_title),
                    onClick = onContentTypeTestClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Crash button with warning style at the bottom
            WarningActionCard(
                icon = Icons.Default.BugReport,
                title = stringResource(id = R.string.action_crash_title),
                onClick = onCrashClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Composable
    private fun Footer(onGitHubClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onGitHubClick)
                .padding(vertical = WormaCeptorDesignSystem.Spacing.xl),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = MyIconPack.IcGithub,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Text(
                text = stringResource(id = R.string.view_on_github),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }
    }

    @Composable
    private fun CompactActionCard(
        icon: ImageVector,
        title: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            label = "compact_scale"
        )

        Surface(
            shape = WormaCeptorDesignSystem.Shapes.card,
            color = MaterialTheme.colorScheme.surface,
            modifier = modifier
                .scale(scale)
                .border(
                    width = WormaCeptorDesignSystem.BorderWidth.regular,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = WormaCeptorDesignSystem.Shapes.card
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WormaCeptorDesignSystem.Spacing.lg)
            ) {
                Surface(
                    shape = WormaCeptorDesignSystem.Shapes.button,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    @Composable
    private fun WarningActionCard(
        icon: ImageVector,
        title: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.96f else 1f,
            label = "warning_compact_scale"
        )

        Surface(
            shape = WormaCeptorDesignSystem.Shapes.card,
            color = StatusAmber.copy(alpha = 0.15f),
            modifier = modifier
                .scale(scale)
                .border(
                    width = WormaCeptorDesignSystem.BorderWidth.regular,
                    color = StatusAmber.copy(alpha = 0.3f),
                    shape = WormaCeptorDesignSystem.Shapes.card
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WormaCeptorDesignSystem.Spacing.lg)
            ) {
                Surface(
                    shape = WormaCeptorDesignSystem.Shapes.button,
                    color = StatusAmber.copy(alpha = 0.25f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = StatusAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }

    @Composable
    private fun ActionCard(
        icon: ImageVector,
        title: String,
        description: String,
        onClick: () -> Unit,
        showChevron: Boolean = false
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.98f else 1f,
            label = "scale"
        )

        Surface(
            shape = WormaCeptorDesignSystem.Shapes.card,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .border(
                    width = WormaCeptorDesignSystem.BorderWidth.regular,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = WormaCeptorDesignSystem.Shapes.card
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg)
            ) {
                Surface(
                    shape = WormaCeptorDesignSystem.Shapes.button,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.lg))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                if (showChevron) {
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

}
