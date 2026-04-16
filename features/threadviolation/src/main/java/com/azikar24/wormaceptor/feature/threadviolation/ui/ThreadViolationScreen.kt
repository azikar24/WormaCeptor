package com.azikar24.wormaceptor.feature.threadviolation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
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
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorBottomSheet
import com.azikar24.wormaceptor.core.ui.components.monitoring.WormaCeptorMonitoringIndicator
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.ThreadViolation
import com.azikar24.wormaceptor.domain.entities.ThreadViolation.ViolationType
import com.azikar24.wormaceptor.domain.entities.ViolationStats
import com.azikar24.wormaceptor.feature.threadviolation.R
import com.azikar24.wormaceptor.feature.threadviolation.ui.components.TypeFilterChips
import com.azikar24.wormaceptor.feature.threadviolation.ui.components.ViolationCard
import com.azikar24.wormaceptor.feature.threadviolation.ui.components.ViolationDetailContent
import com.azikar24.wormaceptor.feature.threadviolation.ui.components.ViolationSummarySection
import com.azikar24.wormaceptor.feature.threadviolation.vm.ThreadViolationViewEvent
import com.azikar24.wormaceptor.feature.threadviolation.vm.ThreadViolationViewState
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadViolationScreen(
    state: ThreadViolationViewState,
    onEvent: (ThreadViolationViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                    ) {
                        Text(
                            text = stringResource(R.string.threadviolation_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                        WormaCeptorMonitoringIndicator(
                            isActive = state.isMonitoring,
                            activeColor = WormaCeptorTokens.Colors.ThreadViolation.monitoring,
                            inactiveColor = WormaCeptorTokens.Colors.ThreadViolation.idle,
                        )
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.threadviolation_back),
                            )
                        }
                    }
                },
                actions = {
                    WormaCeptorPlayPauseButton(
                        isActive = state.isMonitoring,
                        onToggle = { onEvent(ThreadViolationViewEvent.ToggleMonitoring) },
                        activeContentDescription = stringResource(R.string.threadviolation_action_stop),
                        inactiveContentDescription = stringResource(R.string.threadviolation_action_start),
                    )
                    IconButton(onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onEvent(ThreadViolationViewEvent.ClearViolations)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.threadviolation_clear),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
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
            // Summary cards
            ViolationSummarySection(stats = state.stats)

            // Type filter chips
            TypeFilterChips(
                selectedType = state.selectedType,
                onTypeSelected = { onEvent(ThreadViolationViewEvent.SelectType(it)) },
            )

            // Violations list
            WormaCeptorLoadableContent(
                isLoading = state.isViolationsLoading,
                isEmpty = state.filteredViolations.isEmpty(),
                loading = { WormaCeptorListSkeleton(modifier = Modifier.fillMaxSize()) },
                empty = {
                    WormaCeptorEmptyState(
                        title = stringResource(
                            if (state.isMonitoring) {
                                R.string.threadviolation_empty_monitoring
                            } else {
                                R.string.threadviolation_empty_no_violations
                            },
                        ),
                        modifier = Modifier.fillMaxSize(),
                        subtitle = stringResource(
                            if (state.isMonitoring) {
                                R.string.threadviolation_empty_hint_monitoring
                            } else {
                                R.string.threadviolation_empty_hint_start
                            },
                        ),
                        icon = Icons.Default.Warning,
                    )
                },
                content = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                    ) {
                        items(state.filteredViolations, key = { it.id }) { violation ->
                            ViolationCard(
                                violation = violation,
                                onClick = { onEvent(ThreadViolationViewEvent.SelectViolation(violation)) },
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize().weight(1f),
            )
        }

        state.selectedViolation?.let { violation ->
            WormaCeptorBottomSheet(
                modifier = Modifier.padding(top = WormaCeptorTokens.Spacing.xxxl),
                onDismissRequest = { onEvent(ThreadViolationViewEvent.DismissDetail) },
                sheetState = sheetState,
            ) {
                ViolationDetailContent(violation = violation)
            }
        }
    }
}

@Suppress("MagicNumber")
@Preview(showBackground = true)
@Composable
private fun ThreadViolationScreenPreview() {
    WormaCeptorTheme {
        ThreadViolationScreen(
            state = ThreadViolationViewState(
                isViolationsLoading = false,
                filteredViolations = persistentListOf(
                    ThreadViolation(
                        id = 1L,
                        timestamp = 1712534400000L,
                        violationType = ViolationType.DISK_READ,
                        description = "SharedPreferences read on main thread",
                        stackTrace = listOf(
                            "com.example.app.SettingsRepository.getPrefs(SettingsRepository.kt:42)",
                            "com.example.app.MainActivity.onCreate(MainActivity.kt:28)",
                        ),
                        durationMs = 15L,
                        threadName = "main",
                    ),
                    ThreadViolation(
                        id = 2L,
                        timestamp = 1712534395000L,
                        violationType = ViolationType.NETWORK,
                        description = "Network call on main thread",
                        stackTrace = listOf(
                            "com.example.app.ApiClient.fetch(ApiClient.kt:55)",
                        ),
                        durationMs = 230L,
                        threadName = "main",
                    ),
                    ThreadViolation(
                        id = 3L,
                        timestamp = 1712534390000L,
                        violationType = ViolationType.DISK_WRITE,
                        description = "Database write on main thread",
                        stackTrace = emptyList(),
                        durationMs = 45L,
                        threadName = "main",
                    ),
                ),
                stats = ViolationStats(
                    totalViolations = 3,
                    diskReadCount = 1,
                    diskWriteCount = 1,
                    networkCount = 1,
                    slowCallCount = 0,
                    customSlowCodeCount = 0,
                ),
                isMonitoring = true,
                selectedType = null,
                selectedViolation = null,
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
