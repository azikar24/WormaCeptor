package com.azikar24.wormaceptor.feature.deviceinfo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatDateFull
import com.azikar24.wormaceptor.domain.entities.AppDetails
import com.azikar24.wormaceptor.domain.entities.DeviceDetails
import com.azikar24.wormaceptor.domain.entities.DeviceInfo
import com.azikar24.wormaceptor.domain.entities.MemoryDetails
import com.azikar24.wormaceptor.domain.entities.NetworkDetails
import com.azikar24.wormaceptor.domain.entities.OsDetails
import com.azikar24.wormaceptor.domain.entities.ScreenDetails
import com.azikar24.wormaceptor.domain.entities.StorageDetails
import com.azikar24.wormaceptor.feature.deviceinfo.R
import com.azikar24.wormaceptor.feature.deviceinfo.ui.components.AppSection
import com.azikar24.wormaceptor.feature.deviceinfo.ui.components.DeviceSection
import com.azikar24.wormaceptor.feature.deviceinfo.ui.components.MemorySection
import com.azikar24.wormaceptor.feature.deviceinfo.ui.components.NetworkSection
import com.azikar24.wormaceptor.feature.deviceinfo.ui.components.OsSection
import com.azikar24.wormaceptor.feature.deviceinfo.ui.components.ScreenSection
import com.azikar24.wormaceptor.feature.deviceinfo.ui.components.StorageSection
import com.azikar24.wormaceptor.feature.deviceinfo.vm.DeviceInfoSection
import com.azikar24.wormaceptor.feature.deviceinfo.vm.DeviceInfoViewEvent
import com.azikar24.wormaceptor.feature.deviceinfo.vm.DeviceInfoViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreenContent(
    state: DeviceInfoViewState,
    snackBarHostState: SnackbarHostState,
    pullToRefreshState: PullToRefreshState,
    onBack: () -> Unit,
    onEvent: (DeviceInfoViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            DeviceInfoTopBar(
                hasDeviceInfo = state.deviceInfo != null,
                onBack = onBack,
                onEvent = onEvent,
            )
        },
    ) { padding ->
        if (state.isInitialLoading && state.deviceInfo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (state.deviceInfo == null && state.error != null) {
            DeviceInfoErrorContent(
                error = state.error,
                onRetry = { onEvent(DeviceInfoViewEvent.LoadDeviceInfo) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            DeviceInfoPullToRefresh(
                state = state,
                pullToRefreshState = pullToRefreshState,
                onEvent = onEvent,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceInfoTopBar(
    hasDeviceInfo: Boolean,
    onBack: () -> Unit,
    onEvent: (DeviceInfoViewEvent) -> Unit,
) {
    WormaCeptorTopBar(
        title = stringResource(R.string.deviceinfo_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.deviceinfo_back),
        actions = {
            if (hasDeviceInfo) {
                IconButton(onClick = { onEvent(DeviceInfoViewEvent.CopyAll) }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = stringResource(R.string.deviceinfo_copy_all),
                    )
                }
                IconButton(onClick = { onEvent(DeviceInfoViewEvent.ShareReport) }) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = stringResource(R.string.deviceinfo_share),
                    )
                }
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeviceInfoPullToRefresh(
    state: DeviceInfoViewState,
    pullToRefreshState: PullToRefreshState,
    onEvent: (DeviceInfoViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { onEvent(DeviceInfoViewEvent.Refresh) },
        state = pullToRefreshState,
        modifier = modifier,
        indicator = {
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = state.isRefreshing,
                state = pullToRefreshState,
                containerColor = WormaCeptorTokens.semantic().surfaceVariant,
                color = WormaCeptorTokens.semantic().accent,
            )
        },
    ) {
        state.deviceInfo?.let { info ->
            DeviceInfoSectionList(info = info, onEvent = onEvent)
        }
    }
}

@Composable
private fun DeviceInfoSectionList(
    info: DeviceInfo,
    onEvent: (DeviceInfoViewEvent) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                start = WormaCeptorTokens.Spacing.lg,
                top = WormaCeptorTokens.Spacing.lg,
                end = WormaCeptorTokens.Spacing.lg,
                bottom = WormaCeptorTokens.Spacing.lg +
                    WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
            ),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
    ) {
        DeviceSection(
            device = info.device,
            onCopy = { onEvent(DeviceInfoViewEvent.CopySection(DeviceInfoSection.DEVICE)) },
        )
        OsSection(
            os = info.os,
            onCopy = { onEvent(DeviceInfoViewEvent.CopySection(DeviceInfoSection.OS)) },
        )
        ScreenSection(
            screen = info.screen,
            onCopy = { onEvent(DeviceInfoViewEvent.CopySection(DeviceInfoSection.DISPLAY)) },
        )
        MemorySection(
            memory = info.memory,
            onCopy = { onEvent(DeviceInfoViewEvent.CopySection(DeviceInfoSection.MEMORY)) },
        )
        StorageSection(
            storage = info.storage,
            onCopy = { onEvent(DeviceInfoViewEvent.CopySection(DeviceInfoSection.STORAGE)) },
        )
        AppSection(
            app = info.app,
            onCopy = { onEvent(DeviceInfoViewEvent.CopySection(DeviceInfoSection.APPLICATION)) },
        )
        NetworkSection(
            network = info.network,
            onCopy = { onEvent(DeviceInfoViewEvent.CopySection(DeviceInfoSection.NETWORK)) },
        )

        Text(
            text = stringResource(R.string.deviceinfo_collected, formatDateFull(info.timestamp)),
            style = MaterialTheme.typography.bodySmall,
            color = WormaCeptorTokens.semantic().textSecondary,
            modifier = Modifier.padding(
                top = WormaCeptorTokens.Spacing.sm,
            ).align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))
    }
}

@Composable
private fun DeviceInfoErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodyLarge,
                color = WormaCeptorTokens.semantic().textSecondary,
            )
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))
            WormaCeptorButton(
                text = stringResource(R.string.deviceinfo_retry),
                onClick = onRetry,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun DeviceInfoScreenContentPreview() {
    WormaCeptorTheme {
        DeviceInfoScreenContent(
            state = DeviceInfoViewState(
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
            onEvent = {},
        )
    }
}
