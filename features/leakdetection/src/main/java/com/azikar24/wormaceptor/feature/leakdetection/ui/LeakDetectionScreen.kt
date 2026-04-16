package com.azikar24.wormaceptor.feature.leakdetection.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorBottomSheet
import com.azikar24.wormaceptor.core.ui.components.monitoring.WormaCeptorMonitoringIndicator
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LeakInfo
import com.azikar24.wormaceptor.domain.entities.LeakSummary
import com.azikar24.wormaceptor.feature.leakdetection.R
import com.azikar24.wormaceptor.feature.leakdetection.ui.components.LeakCard
import com.azikar24.wormaceptor.feature.leakdetection.ui.components.LeakDetailContent
import com.azikar24.wormaceptor.feature.leakdetection.ui.components.LeakSummarySection
import com.azikar24.wormaceptor.feature.leakdetection.ui.components.SeverityFilterChips
import com.azikar24.wormaceptor.feature.leakdetection.vm.LeakDetectionViewEvent
import com.azikar24.wormaceptor.feature.leakdetection.vm.LeakDetectionViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Main screen for Memory Leak Detection.
 *
 * Features:
 * - Summary cards showing leak counts by severity
 * - Severity filter chips
 * - Leak list with detail sheet
 * - Manual trigger check button
 * - Monitoring status indicator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeakDetectionScreen(
    state: LeakDetectionViewState,
    onEvent: (LeakDetectionViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            LeakDetectionTopBar(
                isRunning = state.isRunning,
                onBack = onBack,
                onEvent = onEvent,
            )
        },
    ) { paddingValues ->
        LeakDetectionBody(
            state = state,
            onEvent = onEvent,
            paddingValues = paddingValues,
        )

        state.selectedLeak?.let { leak ->
            WormaCeptorBottomSheet(
                onDismissRequest = { onEvent(LeakDetectionViewEvent.DismissDetail) },
                sheetState = sheetState,
            ) {
                LeakDetailContent(leak = leak)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LeakDetectionTopBar(
    isRunning: Boolean,
    onBack: (() -> Unit)?,
    onEvent: (LeakDetectionViewEvent) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.leakdetection_title),
                    fontWeight = FontWeight.SemiBold,
                )
                WormaCeptorMonitoringIndicator(
                    isActive = isRunning,
                    activeColor = WormaCeptorTokens.Colors.LeakDetection.monitoring,
                    inactiveColor = WormaCeptorTokens.Colors.LeakDetection.idle,
                )
            }
        },
        navigationIcon = {
            onBack?.let { back ->
                IconButton(onClick = back) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.leakdetection_back),
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { onEvent(LeakDetectionViewEvent.TriggerCheck) }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.leakdetection_trigger_check),
                )
            }
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEvent(LeakDetectionViewEvent.ClearLeaks)
            }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.leakdetection_clear),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun LeakDetectionBody(
    state: LeakDetectionViewState,
    onEvent: (LeakDetectionViewEvent) -> Unit,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
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
        LeakSummarySection(summary = state.summary)

        SeverityFilterChips(
            selectedSeverity = state.selectedSeverity,
            onSelectSeverity = { onEvent(LeakDetectionViewEvent.SelectSeverity(it)) },
        )

        WormaCeptorLoadableContent(
            isLoading = state.isLeaksLoading,
            isEmpty = state.filteredLeaks.isEmpty(),
            loading = { WormaCeptorListSkeleton(modifier = Modifier.fillMaxSize()) },
            empty = {
                LeakEmptyState(
                    isRunning = state.isRunning,
                    modifier = Modifier.fillMaxSize(),
                )
            },
            content = {
                LeakList(
                    leaks = state.filteredLeaks,
                    onSelectLeak = { onEvent(LeakDetectionViewEvent.SelectLeak(it)) },
                    modifier = Modifier.fillMaxSize(),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun LeakEmptyState(
    isRunning: Boolean,
    modifier: Modifier = Modifier,
) {
    WormaCeptorEmptyState(
        title = stringResource(
            if (isRunning) {
                R.string.leakdetection_empty_monitoring
            } else {
                R.string.leakdetection_empty_no_leaks
            },
        ),
        modifier = modifier,
        subtitle = stringResource(
            if (isRunning) {
                R.string.leakdetection_empty_hint_monitoring
            } else {
                R.string.leakdetection_empty_hint_start
            },
        ),
        icon = Icons.Default.BugReport,
    )
}

@Composable
private fun LeakList(
    leaks: ImmutableList<LeakInfo>,
    onSelectLeak: (LeakInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        itemsIndexed(leaks, key = { index, leak -> "${index}_${leak.timestamp}_${leak.objectClass}" }) { _, leak ->
            LeakCard(
                leak = leak,
                onClick = { onSelectLeak(leak) },
            )
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun LeakDetectionScreenPreview() {
    WormaCeptorTheme {
        LeakDetectionScreen(
            state = LeakDetectionViewState(
                filteredLeaks = persistentListOf(
                    LeakInfo(
                        timestamp = System.currentTimeMillis(),
                        objectClass = "com.example.app.ui.HomeActivity",
                        leakDescription = "Activity retained after onDestroy",
                        retainedSize = 2 * 1_048_576L,
                        referencePath = listOf(
                            "GC Root -> static field",
                            "AppManager.instance -> activity",
                        ),
                        severity = LeakInfo.LeakSeverity.CRITICAL,
                    ),
                    LeakInfo(
                        timestamp = System.currentTimeMillis() - 30_000L,
                        objectClass = "com.example.app.data.CacheManager",
                        leakDescription = "Cache not cleared on low memory",
                        retainedSize = 512 * 1024L,
                        referencePath = listOf(
                            "GC Root -> thread local",
                            "Handler.callback -> cacheManager",
                        ),
                        severity = LeakInfo.LeakSeverity.HIGH,
                    ),
                    LeakInfo(
                        timestamp = System.currentTimeMillis() - 60_000L,
                        objectClass = "com.example.app.util.ImageLoader",
                        leakDescription = "Bitmap not recycled",
                        retainedSize = 128 * 1024L,
                        referencePath = emptyList(),
                        severity = LeakInfo.LeakSeverity.MEDIUM,
                    ),
                ),
                summary = LeakSummary(
                    totalLeaks = 3,
                    criticalCount = 1,
                    highCount = 1,
                    mediumCount = 1,
                    lowCount = 0,
                    totalRetainedBytes = 2_686_976L,
                ),
                isRunning = true,
                selectedSeverity = null,
                selectedLeak = null,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
