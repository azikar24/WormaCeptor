/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.deviceinfo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.AppDetails
import com.azikar24.wormaceptor.domain.entities.DeviceDetails
import com.azikar24.wormaceptor.domain.entities.DeviceInfo
import com.azikar24.wormaceptor.domain.entities.MemoryDetails
import com.azikar24.wormaceptor.domain.entities.NetworkDetails
import com.azikar24.wormaceptor.domain.entities.OsDetails
import com.azikar24.wormaceptor.domain.entities.ScreenDetails
import com.azikar24.wormaceptor.domain.entities.StorageDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var deviceInfo by remember { mutableStateOf<DeviceInfo?>(null) }
    var isInitialLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    // Initial load
    LaunchedEffect(Unit) {
        isInitialLoading = true
        deviceInfo = withContext(Dispatchers.IO) {
            GetDeviceInfoUseCase(context).execute()
        }
        isInitialLoading = false
    }

    // Trigger haptic feedback when pull threshold is reached
    LaunchedEffect(pullToRefreshState.distanceFraction) {
        if (pullToRefreshState.distanceFraction >= 1f && !hasTriggeredHaptic) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            hasTriggeredHaptic = true
        } else if (pullToRefreshState.distanceFraction < 1f) {
            hasTriggeredHaptic = false
        }
    }

    // Reset haptic state when refreshing ends
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            hasTriggeredHaptic = false
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.deviceinfo_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.deviceinfo_back),
                        )
                    }
                },
                actions = {
                    deviceInfo?.let { info ->
                        IconButton(onClick = {
                            val message = copyAllToClipboard(context, info)
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.deviceinfo_copy_all),
                            )
                        }
                        IconButton(onClick = { shareDeviceInfo(context, info) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(R.string.deviceinfo_share),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        val scrollState = rememberScrollState()

        if (isInitialLoading && deviceInfo == null) {
            // Initial loading state - show centered spinner
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    scope.launch {
                        delay(200)
                        deviceInfo = withContext(Dispatchers.IO) {
                            GetDeviceInfoUseCase(context).execute()
                        }
                        isRefreshing = false
                    }
                },
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = isRefreshing,
                        state = pullToRefreshState,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                deviceInfo?.let { info ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(WormaCeptorDesignSystem.Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
                    ) {
                        val showMessage: (String) -> Unit = { message ->
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }

                        // Device Section
                        DeviceSection(info.device, showMessage)

                        // OS Section
                        OsSection(info.os, showMessage)

                        // Screen Section
                        ScreenSection(info.screen, showMessage)

                        // Memory Section
                        MemorySection(info.memory, showMessage)

                        // Storage Section
                        StorageSection(info.storage, showMessage)

                        // App Section
                        AppSection(info.app, showMessage)

                        // Network Section
                        NetworkSection(info.network, showMessage)

                        // Timestamp footer
                        Text(
                            text = stringResource(R.string.deviceinfo_collected, formatTimestamp(info.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = WormaCeptorDesignSystem.Spacing.sm),
                        )

                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceSection(device: DeviceDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_device)
    val yes = stringResource(R.string.deviceinfo_yes)
    val no = stringResource(R.string.deviceinfo_no)
    InfoCard(
        title = sectionTitle,
        icon = Icons.Default.PhoneAndroid,
        iconTint = MaterialTheme.colorScheme.primary,
        onCopy = {
            val message = copyToClipboard(context, sectionTitle, formatDeviceDetails(device))
            onShowMessage(message)
        },
    ) {
        InfoRow(stringResource(R.string.deviceinfo_device_manufacturer), device.manufacturer)
        InfoRow(stringResource(R.string.deviceinfo_device_model), device.model)
        InfoRow(stringResource(R.string.deviceinfo_device_brand), device.brand)
        InfoRow(stringResource(R.string.deviceinfo_device_device), device.device)
        InfoRow(stringResource(R.string.deviceinfo_device_hardware), device.hardware)
        InfoRow(stringResource(R.string.deviceinfo_device_board), device.board)
        InfoRow(stringResource(R.string.deviceinfo_device_product), device.product)
        InfoRow(stringResource(R.string.deviceinfo_device_emulator), if (device.isEmulator) yes else no)
    }
}

@Composable
private fun OsSection(os: OsDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_os)
    InfoCard(
        title = sectionTitle,
        icon = Icons.Default.SystemUpdate,
        iconTint = WormaCeptorColors.StatusGreen,
        onCopy = {
            val message = copyToClipboard(context, sectionTitle, formatOsDetails(os))
            onShowMessage(message)
        },
    ) {
        InfoRow(stringResource(R.string.deviceinfo_os_android_version), os.androidVersion)
        InfoRow(stringResource(R.string.deviceinfo_os_sdk_level), os.sdkLevel.toString())
        InfoRow(stringResource(R.string.deviceinfo_os_build_id), os.buildId)
        os.securityPatch?.let { InfoRow(stringResource(R.string.deviceinfo_os_security_patch), it) }
        InfoRow(stringResource(R.string.deviceinfo_os_bootloader), os.bootloader)
        InfoRow(stringResource(R.string.deviceinfo_os_incremental), os.incremental)
        CollapsibleInfoRow(stringResource(R.string.deviceinfo_os_fingerprint), os.fingerprint)
    }
}

@Composable
private fun ScreenSection(screen: ScreenDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_display)
    InfoCard(
        title = sectionTitle,
        icon = Icons.Default.ScreenRotation,
        iconTint = WormaCeptorColors.StatusBlue,
        onCopy = {
            val message = copyToClipboard(context, sectionTitle, formatScreenDetails(screen))
            onShowMessage(message)
        },
    ) {
        InfoRow(stringResource(R.string.deviceinfo_screen_resolution), "${screen.widthPixels} x ${screen.heightPixels}")
        InfoRow(stringResource(R.string.deviceinfo_screen_density_dpi), screen.densityDpi.toString())
        InfoRow(stringResource(R.string.deviceinfo_screen_density), String.format("%.2f", screen.density))
        InfoRow(stringResource(R.string.deviceinfo_screen_scaled_density), String.format("%.2f", screen.scaledDensity))
        InfoRow(stringResource(R.string.deviceinfo_screen_size_category), screen.sizeCategory)
        InfoRow(stringResource(R.string.deviceinfo_screen_orientation), screen.orientation)
        InfoRow(stringResource(R.string.deviceinfo_screen_refresh_rate), "${screen.refreshRate.toInt()} Hz")
    }
}

@Composable
private fun MemorySection(memory: MemoryDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_memory)
    val yes = stringResource(R.string.deviceinfo_yes)
    val no = stringResource(R.string.deviceinfo_no)
    InfoCard(
        title = sectionTitle,
        icon = Icons.Default.Memory,
        iconTint = WormaCeptorColors.StatusAmber,
        onCopy = {
            val message = copyToClipboard(context, sectionTitle, formatMemoryDetails(memory))
            onShowMessage(message)
        },
    ) {
        InfoRow(stringResource(R.string.deviceinfo_memory_total_ram), formatBytes(memory.totalRam))
        InfoRow(stringResource(R.string.deviceinfo_memory_available_ram), formatBytes(memory.availableRam))
        InfoRow(stringResource(R.string.deviceinfo_memory_used_ram), formatBytes(memory.usedRam))

        // Memory usage progress bar
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.deviceinfo_label_usage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${String.format("%.1f", memory.usagePercentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = getUsageColor(memory.usagePercentage),
                )
            }
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            LinearProgressIndicator(
                progress = { memory.usagePercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = getUsageColor(memory.usagePercentage),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
        InfoRow(stringResource(R.string.deviceinfo_memory_low_threshold), formatBytes(memory.lowMemoryThreshold))
        InfoRow(stringResource(R.string.deviceinfo_memory_low_memory), if (memory.isLowMemory) yes else no)
    }
}

@Composable
private fun StorageSection(storage: StorageDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_storage)
    val totalLabel = stringResource(R.string.deviceinfo_label_total)
    val availableLabel = stringResource(R.string.deviceinfo_label_available)
    val usedLabel = stringResource(R.string.deviceinfo_label_used)
    InfoCard(
        title = sectionTitle,
        icon = Icons.Default.Storage,
        iconTint = WormaCeptorColors.Category.Simulation,
        onCopy = {
            val message = copyToClipboard(context, sectionTitle, formatStorageDetails(storage))
            onShowMessage(message)
        },
    ) {
        // Internal Storage
        Text(
            text = stringResource(R.string.deviceinfo_storage_internal),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
        InfoRow(totalLabel, formatBytes(storage.internalTotal))
        InfoRow(availableLabel, formatBytes(storage.internalAvailable))
        InfoRow(usedLabel, formatBytes(storage.internalUsed))

        val internalUsagePercent = if (storage.internalTotal > 0) {
            (storage.internalUsed.toFloat() / storage.internalTotal.toFloat()) * 100f
        } else {
            0f
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
        LinearProgressIndicator(
            progress = { internalUsagePercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = getUsageColor(internalUsagePercent),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )

        // External Storage (if available)
        if (storage.hasExternalStorage && storage.externalTotal != null) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            )
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            val extTotal = storage.externalTotal ?: 0L
            val extUsed = storage.externalUsed ?: 0L

            Text(
                text = stringResource(R.string.deviceinfo_storage_external),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            InfoRow(totalLabel, formatBytes(extTotal))
            storage.externalAvailable?.let { InfoRow(availableLabel, formatBytes(it)) }
            storage.externalUsed?.let { InfoRow(usedLabel, formatBytes(it)) }

            val externalUsagePercent = if (extTotal > 0) {
                (extUsed.toFloat() / extTotal.toFloat()) * 100f
            } else {
                0f
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            LinearProgressIndicator(
                progress = { externalUsagePercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = getUsageColor(externalUsagePercent),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun AppSection(app: AppDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_application)
    val yes = stringResource(R.string.deviceinfo_yes)
    val no = stringResource(R.string.deviceinfo_no)
    InfoCard(
        title = sectionTitle,
        icon = Icons.Default.Apps,
        iconTint = WormaCeptorColors.Accent.Tertiary,
        onCopy = {
            val message = copyToClipboard(context, sectionTitle, formatAppDetails(app))
            onShowMessage(message)
        },
    ) {
        InfoRow(stringResource(R.string.deviceinfo_app_package_name), app.packageName)
        InfoRow(stringResource(R.string.deviceinfo_app_version_name), app.versionName)
        InfoRow(stringResource(R.string.deviceinfo_app_version_code), app.versionCode.toString())
        InfoRow(stringResource(R.string.deviceinfo_app_target_sdk), app.targetSdk.toString())
        InfoRow(stringResource(R.string.deviceinfo_app_min_sdk), app.minSdk.toString())
        InfoRow(stringResource(R.string.deviceinfo_app_first_install), formatTimestamp(app.firstInstallTime))
        InfoRow(stringResource(R.string.deviceinfo_app_last_update), formatTimestamp(app.lastUpdateTime))
        InfoRow(stringResource(R.string.deviceinfo_app_debuggable), if (app.isDebuggable) yes else no)
    }
}

@Composable
private fun NetworkSection(network: NetworkDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_network)
    val yes = stringResource(R.string.deviceinfo_yes)
    val no = stringResource(R.string.deviceinfo_no)
    val connected = stringResource(R.string.deviceinfo_connected)
    val notConnected = stringResource(R.string.deviceinfo_not_connected)
    InfoCard(
        title = sectionTitle,
        icon = Icons.Default.NetworkCheck,
        iconTint = if (network.isConnected) WormaCeptorColors.StatusGreen else WormaCeptorColors.StatusRed,
        onCopy = {
            val message = copyToClipboard(context, sectionTitle, formatNetworkDetails(network))
            onShowMessage(message)
        },
    ) {
        InfoRow(stringResource(R.string.deviceinfo_network_connection_type), network.connectionType)
        InfoRow(stringResource(R.string.deviceinfo_network_connected), if (network.isConnected) yes else no)
        InfoRow(
            stringResource(R.string.deviceinfo_network_wifi),
            if (network.isWifiConnected) connected else notConnected,
        )
        InfoRow(
            stringResource(R.string.deviceinfo_network_cellular),
            if (network.isCellularConnected) connected else notConnected,
        )
        InfoRow(stringResource(R.string.deviceinfo_network_metered), if (network.isMetered) yes else no)
        network.cellularNetworkType?.let { InfoRow(stringResource(R.string.deviceinfo_network_cellular_type), it) }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onCopy: () -> Unit,
    content: @Composable () -> Unit,
) {
    val gradientColors = listOf(
        iconTint.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
        iconTint.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle / 2),
        Color.Transparent,
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm),
        ),
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.thin,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
        ),
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(gradientColors)),
        ) {
            Column(
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(
                        onClick = onCopy,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = stringResource(R.string.deviceinfo_copy_section, title),
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

                content()
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorDesignSystem.Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f),
        )
        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))
        SelectionContainer {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(0.6f),
                fontFamily = if (value.length > 20) FontFamily.Monospace else FontFamily.Default,
            )
        }
    }
}

@Composable
private fun CollapsibleInfoRow(label: String, value: String) {
    var isExpanded by remember { mutableStateOf(false) }
    val collapse = stringResource(R.string.deviceinfo_collapse)
    val expand = stringResource(R.string.deviceinfo_expand)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorDesignSystem.Spacing.xs),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) collapse else expand,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            ) + fadeOut(),
        ) {
            SelectionContainer {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WormaCeptorDesignSystem.Spacing.xs)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                        )
                        .padding(WormaCeptorDesignSystem.Spacing.sm),
                )
            }
        }
    }
}

@Composable
private fun getUsageColor(percentage: Float): Color {
    return when {
        percentage >= 90 -> WormaCeptorColors.StatusRed
        percentage >= 70 -> WormaCeptorColors.StatusAmber
        percentage >= 50 -> WormaCeptorColors.StatusAmber
        else -> WormaCeptorColors.StatusGreen
    }
}

// Utility functions

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
        else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
    }
}

private fun formatTimestamp(millis: Long): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(Date(millis))
}

private fun copyToClipboard(context: Context, label: String, text: String): String {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    return "$label copied to clipboard"
}

private fun copyAllToClipboard(context: Context, info: DeviceInfo): String {
    val text = generateCompactReport(info)
    return copyToClipboard(context, "Device Information", text)
}

private fun shareDeviceInfo(context: Context, info: DeviceInfo) {
    val text = generateFullReport(info)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, "Device Information Report")
    }
    context.startActivity(Intent.createChooser(intent, "Share Device Info"))
}

private fun generateCompactReport(info: DeviceInfo): String = buildString {
    // Device
    appendLine("Manufacturer: ${info.device.manufacturer}")
    appendLine("Model: ${info.device.model}")
    appendLine("Brand: ${info.device.brand}")
    appendLine("Device: ${info.device.device}")
    appendLine("Emulator: ${if (info.device.isEmulator) "Yes" else "No"}")
    appendLine()

    // OS
    appendLine("Android: ${info.os.androidVersion} (SDK ${info.os.sdkLevel})")
    appendLine("Build ID: ${info.os.buildId}")
    info.os.securityPatch?.let { appendLine("Security Patch: $it") }
    appendLine()

    // Screen
    appendLine("Screen: ${info.screen.widthPixels}x${info.screen.heightPixels} @ ${info.screen.densityDpi}dpi")
    appendLine("Refresh Rate: ${info.screen.refreshRate.toInt()}Hz")
    appendLine()

    // Memory
    appendLine(
        "RAM: ${formatBytes(
            info.memory.usedRam,
        )}/${formatBytes(info.memory.totalRam)} (${String.format("%.1f", info.memory.usagePercentage)}% used)",
    )
    appendLine()

    // Storage
    appendLine("Storage: ${formatBytes(info.storage.internalUsed)}/${formatBytes(info.storage.internalTotal)}")
    appendLine()

    // App
    appendLine("Package: ${info.app.packageName}")
    appendLine("Version: ${info.app.versionName} (${info.app.versionCode})")
    appendLine("Target SDK: ${info.app.targetSdk}, Min SDK: ${info.app.minSdk}")
    appendLine("Debuggable: ${if (info.app.isDebuggable) "Yes" else "No"}")
    appendLine()

    // Network
    appendLine(
        "Network: ${info.network.connectionType} (${if (info.network.isConnected) "Connected" else "Disconnected"})",
    )
}

private fun generateFullReport(info: DeviceInfo): String = buildString {
    appendLine("=== WormaCeptor Device Information Report ===")
    appendLine()
    appendLine("Collected: ${formatTimestamp(info.timestamp)}")
    appendLine()

    appendLine("--- DEVICE ---")
    appendLine(formatDeviceDetails(info.device))
    appendLine()

    appendLine("--- OPERATING SYSTEM ---")
    appendLine(formatOsDetails(info.os))
    appendLine()

    appendLine("--- DISPLAY ---")
    appendLine(formatScreenDetails(info.screen))
    appendLine()

    appendLine("--- MEMORY ---")
    appendLine(formatMemoryDetails(info.memory))
    appendLine()

    appendLine("--- STORAGE ---")
    appendLine(formatStorageDetails(info.storage))
    appendLine()

    appendLine("--- APPLICATION ---")
    appendLine(formatAppDetails(info.app))
    appendLine()

    appendLine("--- NETWORK ---")
    appendLine(formatNetworkDetails(info.network))
}

private fun formatDeviceDetails(device: DeviceDetails): String = buildString {
    appendLine("Manufacturer: ${device.manufacturer}")
    appendLine("Model: ${device.model}")
    appendLine("Brand: ${device.brand}")
    appendLine("Device: ${device.device}")
    appendLine("Hardware: ${device.hardware}")
    appendLine("Board: ${device.board}")
    appendLine("Product: ${device.product}")
    appendLine("Emulator: ${if (device.isEmulator) "Yes" else "No"}")
}

private fun formatOsDetails(os: OsDetails): String = buildString {
    appendLine("Android Version: ${os.androidVersion}")
    appendLine("SDK Level: ${os.sdkLevel}")
    appendLine("Build ID: ${os.buildId}")
    os.securityPatch?.let { appendLine("Security Patch: $it") }
    appendLine("Bootloader: ${os.bootloader}")
    appendLine("Incremental: ${os.incremental}")
    appendLine("Fingerprint: ${os.fingerprint}")
}

private fun formatScreenDetails(screen: ScreenDetails): String = buildString {
    appendLine("Resolution: ${screen.widthPixels} x ${screen.heightPixels}")
    appendLine("Density DPI: ${screen.densityDpi}")
    appendLine("Density: ${String.format("%.2f", screen.density)}")
    appendLine("Scaled Density: ${String.format("%.2f", screen.scaledDensity)}")
    appendLine("Size Category: ${screen.sizeCategory}")
    appendLine("Orientation: ${screen.orientation}")
    appendLine("Refresh Rate: ${screen.refreshRate.toInt()} Hz")
}

private fun formatMemoryDetails(memory: MemoryDetails): String = buildString {
    appendLine("Total RAM: ${formatBytes(memory.totalRam)}")
    appendLine("Available RAM: ${formatBytes(memory.availableRam)}")
    appendLine("Used RAM: ${formatBytes(memory.usedRam)}")
    appendLine("Usage: ${String.format("%.1f", memory.usagePercentage)}%")
    appendLine("Low Memory Threshold: ${formatBytes(memory.lowMemoryThreshold)}")
    appendLine("Low Memory: ${if (memory.isLowMemory) "Yes" else "No"}")
}

private fun formatStorageDetails(storage: StorageDetails): String = buildString {
    appendLine("Internal Storage:")
    appendLine("  Total: ${formatBytes(storage.internalTotal)}")
    appendLine("  Available: ${formatBytes(storage.internalAvailable)}")
    appendLine("  Used: ${formatBytes(storage.internalUsed)}")
    val extTotal = storage.externalTotal
    if (storage.hasExternalStorage && extTotal != null) {
        appendLine("External Storage:")
        appendLine("  Total: ${formatBytes(extTotal)}")
        storage.externalAvailable?.let { appendLine("  Available: ${formatBytes(it)}") }
        storage.externalUsed?.let { appendLine("  Used: ${formatBytes(it)}") }
    }
}

private fun formatAppDetails(app: AppDetails): String = buildString {
    appendLine("Package Name: ${app.packageName}")
    appendLine("Version Name: ${app.versionName}")
    appendLine("Version Code: ${app.versionCode}")
    appendLine("Target SDK: ${app.targetSdk}")
    appendLine("Min SDK: ${app.minSdk}")
    appendLine("First Install: ${formatTimestamp(app.firstInstallTime)}")
    appendLine("Last Update: ${formatTimestamp(app.lastUpdateTime)}")
    appendLine("Debuggable: ${if (app.isDebuggable) "Yes" else "No"}")
}

private fun formatNetworkDetails(network: NetworkDetails): String = buildString {
    appendLine("Connection Type: ${network.connectionType}")
    appendLine("Connected: ${if (network.isConnected) "Yes" else "No"}")
    appendLine("WiFi: ${if (network.isWifiConnected) "Connected" else "Not Connected"}")
    appendLine("Cellular: ${if (network.isCellularConnected) "Connected" else "Not Connected"}")
    appendLine("Metered: ${if (network.isMetered) "Yes" else "No"}")
    network.cellularNetworkType?.let { appendLine("Cellular Type: $it") }
}
