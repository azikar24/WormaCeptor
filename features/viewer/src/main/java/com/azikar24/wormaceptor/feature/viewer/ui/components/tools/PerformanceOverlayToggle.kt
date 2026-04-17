package com.azikar24.wormaceptor.feature.viewer.ui.components.tools

import androidx.activity.ComponentActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R
import org.koin.java.KoinJavaComponent.get

@Composable
internal fun PerformanceOverlayToggle(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var canDrawOverlays by remember { mutableStateOf(WormaCeptorApi.canShowFloatingButton(context)) }

    val performanceOverlayEngine = remember { get<PerformanceOverlayEngine>(PerformanceOverlayEngine::class.java) }
    val overlayState by performanceOverlayEngine.state.collectAsState()
    val isOverlayEnabled = overlayState.isOverlayEnabled

    // Load saved metric toggle states (FPS, Memory, CPU) from preferences
    LaunchedEffect(Unit) {
        performanceOverlayEngine.loadSavedMetricStates()
    }

    // Re-check permission when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canDrawOverlays = WormaCeptorApi.canShowFloatingButton(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val backgroundColor = when {
        !canDrawOverlays -> MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorTokens.Alpha.MODERATE,
        )
        isOverlayEnabled -> WormaCeptorTokens.Colors.Category.performance.copy(alpha = WormaCeptorTokens.Alpha.SOFT)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD)
    }
    val borderColor = when {
        !canDrawOverlays -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)
        isOverlayEnabled -> WormaCeptorTokens.Colors.Category.performance.copy(alpha = WormaCeptorTokens.Alpha.STRONG)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)
    }

    Surface(
        onClick = {
            if (canDrawOverlays) {
                (context as? ComponentActivity)?.let { activity ->
                    performanceOverlayEngine.toggleOverlayWithAllMetrics(activity)
                }
            } else {
                WormaCeptorApi.getOverlayPermissionIntent(context)?.let { intent ->
                    context.startActivity(intent)
                }
            }
        },
        modifier = modifier,
        shape = WormaCeptorTokens.Shapes.cardLarge,
        color = backgroundColor,
        border = BorderStroke(WormaCeptorTokens.BorderWidth.regular, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isOverlayEnabled && canDrawOverlays) {
                            WormaCeptorTokens.Colors.Category.performance.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)
                        } else {
                            WormaCeptorTokens.Colors.Category.performance.copy(alpha = WormaCeptorTokens.Alpha.SOFT)
                        },
                        shape = WormaCeptorTokens.Shapes.card,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                    tint = if (isOverlayEnabled && canDrawOverlays) {
                        WormaCeptorTokens.Colors.Category.performance
                    } else {
                        WormaCeptorTokens.Colors.Category.performance.copy(alpha = WormaCeptorTokens.Alpha.HEAVY)
                    },
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.viewer_tools_performance_overlay),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isOverlayEnabled && canDrawOverlays) {
                        stringResource(R.string.viewer_tools_overlay_showing)
                    } else {
                        stringResource(R.string.viewer_tools_overlay_tap_enable)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!canDrawOverlays) {
                Surface(
                    shape = WormaCeptorTokens.Shapes.button,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Text(
                        text = stringResource(R.string.viewer_tools_overlay_grant),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorTokens.Spacing.sm,
                            vertical = WormaCeptorTokens.Spacing.xs,
                        ),
                    )
                }
            } else {
                // On/Off indicator
                Surface(
                    shape = WormaCeptorTokens.Shapes.button,
                    color = if (isOverlayEnabled) {
                        WormaCeptorTokens.Colors.Category.performance.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Text(
                        text = if (isOverlayEnabled) {
                            stringResource(
                                R.string.viewer_tools_overlay_on,
                            )
                        } else {
                            stringResource(R.string.viewer_tools_overlay_off)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverlayEnabled) {
                            WormaCeptorTokens.Colors.Category.performance
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorTokens.Spacing.sm,
                            vertical = WormaCeptorTokens.Spacing.xs,
                        ),
                    )
                }
            }
        }
    }
}
