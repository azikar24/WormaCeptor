package com.azikar24.wormaceptor.feature.memory.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorWarningBadge
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
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
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.memory_title),
                    fontWeight = FontWeight.SemiBold,
                )
                AnimatedVisibility(
                    visible = state.isHeapWarning,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    WormaCeptorWarningBadge(
                        contentDescription = stringResource(R.string.memory_warning),
                    )
                }
            }
        },
        navigationIcon = {
            onBack?.let { back ->
                IconButton(onClick = back) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.memory_back),
                    )
                }
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
            IconButton(onClick = onClearHistory) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.memory_clear_history),
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}
