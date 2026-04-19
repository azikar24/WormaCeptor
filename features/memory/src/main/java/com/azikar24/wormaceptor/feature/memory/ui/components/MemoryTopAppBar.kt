package com.azikar24.wormaceptor.feature.memory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorWarningBadge
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.feature.memory.R
import com.azikar24.wormaceptor.feature.memory.vm.MemoryViewEvent
import com.azikar24.wormaceptor.feature.memory.vm.MemoryViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MemoryTopAppBar(
    state: MemoryViewState,
    onEvent: (MemoryViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    onClearHistory: () -> Unit,
) {
    WormaCeptorTopBar(
        title = stringResource(R.string.memory_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.memory_back),
        titleTrailing = {
            AnimatedVisibility(
                visible = state.isHeapWarning,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                WormaCeptorWarningBadge(
                    contentDescription = stringResource(R.string.memory_warning),
                )
            }
        },
        actions = {
            WormaCeptorPlayPauseButton(
                isActive = state.isMonitoring,
                onToggle = {
                    if (state.isMonitoring) {
                        onEvent(MemoryViewEvent.StopMonitoring)
                    } else {
                        onEvent(MemoryViewEvent.StartMonitoring)
                    }
                },
            )
            WormaCeptorIconButton(onClick = onClearHistory) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.memory_clear_history),
                )
            }
        },
    )
}
