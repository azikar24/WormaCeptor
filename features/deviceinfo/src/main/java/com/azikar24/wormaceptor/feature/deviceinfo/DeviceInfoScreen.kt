package com.azikar24.wormaceptor.feature.deviceinfo

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDetailRow
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorInfoCard
import com.azikar24.wormaceptor.core.ui.components.rememberHapticOnce
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDateFull
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uiState by remember { mutableStateOf(DeviceInfoUiState()) }
    val snackBarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    val haptic = rememberHapticOnce()

    // Initial load
    LaunchedEffect(Unit) {
        uiState = uiState.copy(isInitialLoading = true)
        val info = withContext(Dispatchers.IO) {
            GetDeviceInfoUseCase(context).execute()
        }
        uiState = uiState.copy(deviceInfo = info, isInitialLoading = false)
    }

    // Trigger haptic feedback when pull threshold is reached
    LaunchedEffect(pullToRefreshState.distanceFraction) {
        if (pullToRefreshState.distanceFraction >= 1f && !haptic.isTriggered) {
            haptic.triggerHaptic()
        } else if (pullToRefreshState.distanceFraction < 1f) {
            haptic.resetHaptic()
        }
    }

    // Reset haptic state when refreshing ends
    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            haptic.resetHaptic()
        }
    }

    DeviceInfoScreenContent(
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        pullToRefreshState = pullToRefreshState,
        onBack = onBack,
        onCopyAll = { info ->
            val message = copyAllToClipboard(context, info)
            scope.launch { snackBarHostState.showSnackbar(message) }
        },
        onShare = { info -> shareDeviceInfo(context, info) },
        onRefresh = {
            uiState = uiState.copy(isRefreshing = true)
            scope.launch {
                delay(200)
                val info = withContext(Dispatchers.IO) {
                    GetDeviceInfoUseCase(context).execute()
                }
                uiState = uiState.copy(deviceInfo = info, isRefreshing = false)
            }
        },
        onShowMessage = { message ->
            scope.launch { snackBarHostState.showSnackbar(message) }
        },
    )
}

@Suppress("LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeviceInfoScreenContent(
    uiState: DeviceInfoUiState,
    snackBarHostState: SnackbarHostState,
    pullToRefreshState: androidx.compose.material3.pulltorefresh.PullToRefreshState,
    onBack: () -> Unit,
    onCopyAll: (DeviceInfo) -> Unit,
    onShare: (DeviceInfo) -> Unit,
    onRefresh: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
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
                    uiState.deviceInfo?.let { info ->
                        IconButton(onClick = { onCopyAll(info) }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = stringResource(R.string.deviceinfo_copy_all),
                            )
                        }
                        IconButton(onClick = { onShare(info) }) {
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

        if (uiState.isInitialLoading && uiState.deviceInfo == null) {
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
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                indicator = {
                    Indicator(
                        modifier = Modifier.align(Alignment.TopCenter),
                        isRefreshing = uiState.isRefreshing,
                        state = pullToRefreshState,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        color = MaterialTheme.colorScheme.primary,
                    )
                },
            ) {
                uiState.deviceInfo?.let { info ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(WormaCeptorDesignSystem.Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
                    ) {
                        // Device Section
                        DeviceSection(info.device, onShowMessage)

                        // OS Section
                        OsSection(info.os, onShowMessage)

                        // Screen Section
                        ScreenSection(info.screen, onShowMessage)

                        // Memory Section
                        MemorySection(info.memory, onShowMessage)

                        // Storage Section
                        StorageSection(info.storage, onShowMessage)

                        // App Section
                        AppSection(info.app, onShowMessage)

                        // Network Section
                        NetworkSection(info.network, onShowMessage)

                        // Timestamp footer
                        Text(
                            text = stringResource(R.string.deviceinfo_collected, formatDateFull(info.timestamp)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                top = WormaCeptorDesignSystem.Spacing.sm,
                            ).align(Alignment.CenterHorizontally),
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
    WormaCeptorInfoCard(
        title = sectionTitle,
        icon = Icons.Default.PhoneAndroid,
        iconTint = MaterialTheme.colorScheme.primary,
        onAction = {
            val message = copyToClipboard(context, sectionTitle, formatDeviceDetails(device))
            onShowMessage(message)
        },
        actionContentDescription = stringResource(R.string.deviceinfo_copy_section, sectionTitle),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_manufacturer), device.manufacturer)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_model), device.model)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_brand), device.brand)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_device), device.device)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_hardware), device.hardware)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_board), device.board)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_product), device.product)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_device_emulator), if (device.isEmulator) yes else no)
    }
}

@Composable
private fun OsSection(os: OsDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_os)
    WormaCeptorInfoCard(
        title = sectionTitle,
        icon = Icons.Default.SystemUpdate,
        iconTint = WormaCeptorColors.StatusGreen,
        onAction = {
            val message = copyToClipboard(context, sectionTitle, formatOsDetails(os))
            onShowMessage(message)
        },
        actionContentDescription = stringResource(R.string.deviceinfo_copy_section, sectionTitle),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_android_version), os.androidVersion)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_sdk_level), os.sdkLevel.toString())
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_build_id), os.buildId)
        os.securityPatch?.let { WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_security_patch), it) }
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_bootloader), os.bootloader)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_os_incremental), os.incremental)
        CollapsibleInfoRow(stringResource(R.string.deviceinfo_os_fingerprint), os.fingerprint)
    }
}

@Composable
private fun ScreenSection(screen: ScreenDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_display)
    WormaCeptorInfoCard(
        title = sectionTitle,
        icon = Icons.Default.ScreenRotation,
        iconTint = WormaCeptorColors.StatusBlue,
        onAction = {
            val message = copyToClipboard(context, sectionTitle, formatScreenDetails(screen))
            onShowMessage(message)
        },
        actionContentDescription = stringResource(R.string.deviceinfo_copy_section, sectionTitle),
    ) {
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_screen_resolution),
            "${screen.widthPixels} x ${screen.heightPixels}",
        )
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_screen_density_dpi), screen.densityDpi.toString())
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_screen_density),
            String.format(Locale.US, "%.2f", screen.density),
        )
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_screen_scaled_density),
            String.format(Locale.US, "%.2f", screen.scaledDensity),
        )
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_screen_size_category), screen.sizeCategory)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_screen_orientation), screen.orientation)
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_screen_refresh_rate),
            "${screen.refreshRate.toInt()} Hz",
        )
    }
}

@Composable
private fun MemorySection(memory: MemoryDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_memory)
    val yes = stringResource(R.string.deviceinfo_yes)
    val no = stringResource(R.string.deviceinfo_no)
    WormaCeptorInfoCard(
        title = sectionTitle,
        icon = Icons.Default.Memory,
        iconTint = WormaCeptorColors.StatusAmber,
        onAction = {
            val message = copyToClipboard(context, sectionTitle, formatMemoryDetails(memory))
            onShowMessage(message)
        },
        actionContentDescription = stringResource(R.string.deviceinfo_copy_section, sectionTitle),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_memory_total_ram), formatBytes(memory.totalRam))
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_memory_available_ram), formatBytes(memory.availableRam))
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_memory_used_ram), formatBytes(memory.usedRam))

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
                    text = "${String.format(Locale.US, "%.1f", memory.usagePercentage)}%",
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
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_memory_low_threshold),
            formatBytes(memory.lowMemoryThreshold),
        )
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_memory_low_memory), if (memory.isLowMemory) yes else no)
    }
}

@Composable
private fun StorageSection(storage: StorageDetails, onShowMessage: (String) -> Unit) {
    val context = LocalContext.current
    val sectionTitle = stringResource(R.string.deviceinfo_section_storage)
    val totalLabel = stringResource(R.string.deviceinfo_label_total)
    val availableLabel = stringResource(R.string.deviceinfo_label_available)
    val usedLabel = stringResource(R.string.deviceinfo_label_used)
    WormaCeptorInfoCard(
        title = sectionTitle,
        icon = Icons.Default.Storage,
        iconTint = WormaCeptorColors.Category.Simulation,
        onAction = {
            val message = copyToClipboard(context, sectionTitle, formatStorageDetails(storage))
            onShowMessage(message)
        },
        actionContentDescription = stringResource(R.string.deviceinfo_copy_section, sectionTitle),
    ) {
        // Internal Storage
        Text(
            text = stringResource(R.string.deviceinfo_storage_internal),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
        WormaCeptorDetailRow(totalLabel, formatBytes(storage.internalTotal))
        WormaCeptorDetailRow(availableLabel, formatBytes(storage.internalAvailable))
        WormaCeptorDetailRow(usedLabel, formatBytes(storage.internalUsed))

        val internalUsagePercent = if (storage.internalTotal > 0) {
            storage.internalUsed.toFloat() / storage.internalTotal.toFloat() * 100f
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
            WormaCeptorDivider(style = DividerStyle.Subtle)
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
            WormaCeptorDetailRow(totalLabel, formatBytes(extTotal))
            storage.externalAvailable?.let { WormaCeptorDetailRow(availableLabel, formatBytes(it)) }
            storage.externalUsed?.let { WormaCeptorDetailRow(usedLabel, formatBytes(it)) }

            val externalUsagePercent = if (extTotal > 0) {
                extUsed.toFloat() / extTotal.toFloat() * 100f
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
    WormaCeptorInfoCard(
        title = sectionTitle,
        icon = Icons.Default.Apps,
        iconTint = WormaCeptorColors.Accent.Tertiary,
        onAction = {
            val message = copyToClipboard(context, sectionTitle, formatAppDetails(app))
            onShowMessage(message)
        },
        actionContentDescription = stringResource(R.string.deviceinfo_copy_section, sectionTitle),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_package_name), app.packageName)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_version_name), app.versionName)
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_version_code), app.versionCode.toString())
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_target_sdk), app.targetSdk.toString())
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_min_sdk), app.minSdk.toString())
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_app_first_install),
            formatDateFull(app.firstInstallTime),
        )
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_last_update), formatDateFull(app.lastUpdateTime))
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_app_debuggable), if (app.isDebuggable) yes else no)
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
    WormaCeptorInfoCard(
        title = sectionTitle,
        icon = Icons.Default.NetworkCheck,
        iconTint = if (network.isConnected) WormaCeptorColors.StatusGreen else WormaCeptorColors.StatusRed,
        onAction = {
            val message = copyToClipboard(context, sectionTitle, formatNetworkDetails(network))
            onShowMessage(message)
        },
        actionContentDescription = stringResource(R.string.deviceinfo_copy_section, sectionTitle),
    ) {
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_network_connection_type), network.connectionType)
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_network_connected),
            if (network.isConnected) yes else no,
        )
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_network_wifi),
            if (network.isWifiConnected) connected else notConnected,
        )
        WormaCeptorDetailRow(
            stringResource(R.string.deviceinfo_network_cellular),
            if (network.isCellularConnected) connected else notConnected,
        )
        WormaCeptorDetailRow(stringResource(R.string.deviceinfo_network_metered), if (network.isMetered) yes else no)
        network.cellularNetworkType?.let {
            WormaCeptorDetailRow(
                stringResource(R.string.deviceinfo_network_cellular_type),
                it,
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
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
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
                            MaterialTheme.colorScheme.surfaceVariant.copy(
                                alpha = WormaCeptorDesignSystem.Alpha.moderate,
                            ),
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

internal data class DeviceInfoUiState(
    val deviceInfo: DeviceInfo? = null,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
)

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
        )}/${formatBytes(
            info.memory.totalRam,
        )} (${String.format(Locale.US, "%.1f", info.memory.usagePercentage)}% used)",
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
    appendLine("Collected: ${formatDateFull(info.timestamp)}")
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
    appendLine("Density: ${String.format(Locale.US, "%.2f", screen.density)}")
    appendLine("Scaled Density: ${String.format(Locale.US, "%.2f", screen.scaledDensity)}")
    appendLine("Size Category: ${screen.sizeCategory}")
    appendLine("Orientation: ${screen.orientation}")
    appendLine("Refresh Rate: ${screen.refreshRate.toInt()} Hz")
}

private fun formatMemoryDetails(memory: MemoryDetails): String = buildString {
    appendLine("Total RAM: ${formatBytes(memory.totalRam)}")
    appendLine("Available RAM: ${formatBytes(memory.availableRam)}")
    appendLine("Used RAM: ${formatBytes(memory.usedRam)}")
    appendLine("Usage: ${String.format(Locale.US, "%.1f", memory.usagePercentage)}%")
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
    appendLine("First Install: ${formatDateFull(app.firstInstallTime)}")
    appendLine("Last Update: ${formatDateFull(app.lastUpdateTime)}")
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun DeviceInfoScreenContentPreview() {
    WormaCeptorTheme {
        DeviceInfoScreenContent(
            uiState = DeviceInfoUiState(
                deviceInfo = DeviceInfo(
                    device = DeviceDetails(
                        manufacturer = "Google",
                        model = "Pixel 8",
                        brand = "google",
                        device = "shiba",
                        hardware = "shiba",
                        board = "shiba",
                        product = "shiba",
                        isEmulator = false,
                    ),
                    os = OsDetails(
                        androidVersion = "14",
                        sdkLevel = 34,
                        buildId = "UP1A.231005.007",
                        securityPatch = "2024-01-05",
                        bootloader = "shiba-1.0",
                        fingerprint = "google/shiba/shiba:14/UP1A/1234567:userdebug/dev-keys",
                        incremental = "1234567",
                    ),
                    screen = ScreenDetails(
                        widthPixels = 1080,
                        heightPixels = 2400,
                        densityDpi = 420,
                        density = 2.625f,
                        scaledDensity = 2.625f,
                        sizeCategory = "normal",
                        orientation = "Portrait",
                        refreshRate = 120f,
                    ),
                    memory = MemoryDetails(
                        totalRam = 8_000_000_000L,
                        availableRam = 4_000_000_000L,
                        lowMemoryThreshold = 500_000_000L,
                        isLowMemory = false,
                        usedRam = 4_000_000_000L,
                        usagePercentage = 50f,
                    ),
                    storage = StorageDetails(
                        internalTotal = 128_000_000_000L,
                        internalAvailable = 64_000_000_000L,
                        internalUsed = 64_000_000_000L,
                        externalTotal = null,
                        externalAvailable = null,
                        externalUsed = null,
                        hasExternalStorage = false,
                    ),
                    app = AppDetails(
                        packageName = "com.example.app",
                        versionName = "1.0.0",
                        versionCode = 1,
                        targetSdk = 34,
                        minSdk = 23,
                        firstInstallTime = 1_700_000_000_000L,
                        lastUpdateTime = 1_700_000_000_000L,
                        isDebuggable = true,
                    ),
                    network = NetworkDetails(
                        connectionType = "WiFi",
                        isConnected = true,
                        isWifiConnected = true,
                        isCellularConnected = false,
                        isMetered = false,
                        wifiSsid = null,
                        cellularNetworkType = null,
                    ),
                ),
                isInitialLoading = false,
                isRefreshing = false,
            ),
            snackBarHostState = remember { SnackbarHostState() },
            pullToRefreshState = rememberPullToRefreshState(),
            onBack = {},
            onCopyAll = {},
            onShare = {},
            onRefresh = {},
            onShowMessage = {},
        )
    }
}
