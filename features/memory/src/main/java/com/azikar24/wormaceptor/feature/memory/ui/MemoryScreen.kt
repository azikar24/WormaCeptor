package com.azikar24.wormaceptor.feature.memory.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMonitoringStatusBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.MemoryInfo
import com.azikar24.wormaceptor.feature.memory.ui.components.HeapUsageCard
import com.azikar24.wormaceptor.feature.memory.ui.components.MemoryActionButtons
import com.azikar24.wormaceptor.feature.memory.ui.components.MemoryChartCard
import com.azikar24.wormaceptor.feature.memory.ui.components.MemoryTopAppBar
import com.azikar24.wormaceptor.feature.memory.ui.components.NativeHeapCard
import com.azikar24.wormaceptor.feature.memory.vm.MemoryViewEvent
import com.azikar24.wormaceptor.feature.memory.vm.MemoryViewState
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryScreen(
    state: MemoryViewState,
    onEvent: (MemoryViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            MemoryTopAppBar(
                state = state,
                onEvent = onEvent,
                onBack = onBack,
                onClearHistory = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEvent(MemoryViewEvent.ClearHistory)
                },
            )
        },
    ) { paddingValues ->
        MemoryScreenContent(
            state = state,
            onEvent = onEvent,
            paddingValues = paddingValues,
            scrollState = scrollState,
        )
    }
}

@Composable
private fun MemoryScreenContent(
    state: MemoryViewState,
    onEvent: (MemoryViewEvent) -> Unit,
    paddingValues: PaddingValues,
    scrollState: ScrollState,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
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
        WormaCeptorMonitoringStatusBar(
            isMonitoring = state.isMonitoring,
            sampleCount = state.memoryHistory.size,
        )
        HeapUsageCard(
            currentMemory = state.currentMemory,
        )
        MemoryChartCard(
            history = state.memoryHistory,
        )
        NativeHeapCard(
            currentMemory = state.currentMemory,
        )
        MemoryActionButtons(
            onForceGc = { onEvent(MemoryViewEvent.ForceGc) },
        )
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun MemoryScreenPreview() {
    WormaCeptorTheme {
        MemoryScreen(
            state = MemoryViewState(
                currentMemory = MemoryInfo(
                    timestamp = System.currentTimeMillis(),
                    usedMemory = 50 * 1_048_576L,
                    freeMemory = 30 * 1_048_576L,
                    totalMemory = 80 * 1_048_576L,
                    maxMemory = 256 * 1_048_576L,
                    heapUsagePercent = 62.5f,
                    nativeHeapSize = 64 * 1_048_576L,
                    nativeHeapAllocated = 40 * 1_048_576L,
                ),
                memoryHistory = persistentListOf(
                    MemoryInfo(
                        timestamp = 1L,
                        usedMemory = 40 * 1_048_576L,
                        freeMemory = 40 * 1_048_576L,
                        totalMemory = 80 * 1_048_576L,
                        maxMemory = 256 * 1_048_576L,
                        heapUsagePercent = 50f,
                        nativeHeapSize = 64 * 1_048_576L,
                        nativeHeapAllocated = 35 * 1_048_576L,
                    ),
                    MemoryInfo(
                        timestamp = 2L,
                        usedMemory = 50 * 1_048_576L,
                        freeMemory = 30 * 1_048_576L,
                        totalMemory = 80 * 1_048_576L,
                        maxMemory = 256 * 1_048_576L,
                        heapUsagePercent = 62.5f,
                        nativeHeapSize = 64 * 1_048_576L,
                        nativeHeapAllocated = 40 * 1_048_576L,
                    ),
                ),
                isMonitoring = true,
                isHeapWarning = false,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun MemoryScreenWarningPreview() {
    WormaCeptorTheme {
        MemoryScreen(
            state = MemoryViewState(
                currentMemory = MemoryInfo(
                    timestamp = System.currentTimeMillis(),
                    usedMemory = 220 * 1_048_576L,
                    freeMemory = 10 * 1_048_576L,
                    totalMemory = 230 * 1_048_576L,
                    maxMemory = 256 * 1_048_576L,
                    heapUsagePercent = 89.8f,
                    nativeHeapSize = 128 * 1_048_576L,
                    nativeHeapAllocated = 110 * 1_048_576L,
                ),
                memoryHistory = persistentListOf(
                    MemoryInfo(
                        timestamp = 1L,
                        usedMemory = 180 * 1_048_576L,
                        freeMemory = 30 * 1_048_576L,
                        totalMemory = 210 * 1_048_576L,
                        maxMemory = 256 * 1_048_576L,
                        heapUsagePercent = 82.0f,
                        nativeHeapSize = 128 * 1_048_576L,
                        nativeHeapAllocated = 100 * 1_048_576L,
                    ),
                    MemoryInfo(
                        timestamp = 2L,
                        usedMemory = 220 * 1_048_576L,
                        freeMemory = 10 * 1_048_576L,
                        totalMemory = 230 * 1_048_576L,
                        maxMemory = 256 * 1_048_576L,
                        heapUsagePercent = 89.8f,
                        nativeHeapSize = 128 * 1_048_576L,
                        nativeHeapAllocated = 110 * 1_048_576L,
                    ),
                ),
                isMonitoring = false,
                isHeapWarning = true,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
