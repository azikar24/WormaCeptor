package com.azikar24.wormaceptor.feature.fps.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMonitoringStatusBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.FpsInfo
import com.azikar24.wormaceptor.feature.fps.R
import com.azikar24.wormaceptor.feature.fps.ui.components.CurrentFpsCard
import com.azikar24.wormaceptor.feature.fps.ui.components.DroppedFramesCard
import com.azikar24.wormaceptor.feature.fps.ui.components.FpsChartCard
import com.azikar24.wormaceptor.feature.fps.ui.components.StatisticsRow
import com.azikar24.wormaceptor.feature.fps.vm.FpsViewEvent
import com.azikar24.wormaceptor.feature.fps.vm.FpsViewState
import kotlinx.collections.immutable.persistentListOf

/** Main screen for real-time FPS monitoring. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FpsScreen(
    state: FpsViewState,
    onEvent: (FpsViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            FpsTopAppBar(
                state = state,
                onEvent = onEvent,
                onBack = onBack,
                onResetStats = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEvent(FpsViewEvent.ResetStats)
                },
            )
        },
    ) { paddingValues ->
        if (!state.isMonitoring && state.fpsHistory.isEmpty()) {
            WormaCeptorEmptyState(
                title = stringResource(R.string.fps_ready_title),
                subtitle = stringResource(R.string.fps_ready_subtitle),
                icon = Icons.Default.PlayArrow,
                actionLabel = stringResource(R.string.fps_start_monitoring),
                onAction = { onEvent(FpsViewEvent.StartMonitoring) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .navigationBarsPadding(),
            )
            return@Scaffold
        }

        FpsScreenContent(
            state = state,
            paddingValues = paddingValues,
            scrollState = scrollState,
        )
    }
}

@Composable
private fun FpsScreenContent(
    state: FpsViewState,
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
            sampleCount = state.fpsHistory.size,
        )

        CurrentFpsCard(
            fpsInfo = state.currentFpsInfo,
            isMonitoring = state.isMonitoring,
            modifier = Modifier.fillMaxWidth(),
        )

        StatisticsRow(
            fpsInfo = state.currentFpsInfo,
            modifier = Modifier.fillMaxWidth(),
        )

        DroppedFramesCard(
            droppedFrames = state.currentFpsInfo.droppedFrames,
            jankFrames = state.currentFpsInfo.jankFrames,
            modifier = Modifier.fillMaxWidth(),
        )

        FpsChartCard(
            history = state.fpsHistory,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
        )
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun FpsScreenPreview() {
    WormaCeptorTheme {
        FpsScreen(
            state = FpsViewState(
                currentFpsInfo = FpsInfo(
                    currentFps = 58.5f,
                    averageFps = 55.2f,
                    minFps = 42.0f,
                    maxFps = 60.0f,
                    droppedFrames = 12,
                    jankFrames = 3,
                    timestamp = System.currentTimeMillis(),
                ),
                fpsHistory = persistentListOf(
                    FpsInfo(
                        currentFps = 55f,
                        averageFps = 52f,
                        minFps = 42f,
                        maxFps = 60f,
                        droppedFrames = 8,
                        jankFrames = 2,
                        timestamp = 1L,
                    ),
                    FpsInfo(
                        currentFps = 58.5f,
                        averageFps = 55.2f,
                        minFps = 42f,
                        maxFps = 60f,
                        droppedFrames = 12,
                        jankFrames = 3,
                        timestamp = 2L,
                    ),
                    FpsInfo(
                        currentFps = 45f,
                        averageFps = 53f,
                        minFps = 42f,
                        maxFps = 60f,
                        droppedFrames = 15,
                        jankFrames = 4,
                        timestamp = 3L,
                    ),
                ),
                isMonitoring = true,
                isFpsWarning = false,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
