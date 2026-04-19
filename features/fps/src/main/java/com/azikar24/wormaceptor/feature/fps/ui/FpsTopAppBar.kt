package com.azikar24.wormaceptor.feature.fps.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorWarningBadge
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorPlayPauseButton
import com.azikar24.wormaceptor.feature.fps.R
import com.azikar24.wormaceptor.feature.fps.vm.FpsViewEvent
import com.azikar24.wormaceptor.feature.fps.vm.FpsViewState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FpsTopAppBar(
    state: FpsViewState,
    onEvent: (FpsViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    onResetStats: () -> Unit,
) {
    WormaCeptorTopBar(
        title = stringResource(R.string.fps_monitor_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.fps_back),
        titleTrailing = {
            AnimatedVisibility(
                visible = state.isFpsWarning,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                WormaCeptorWarningBadge(
                    contentDescription = stringResource(R.string.fps_warning),
                )
            }
        },
        actions = {
            WormaCeptorIconButton(onClick = onResetStats) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.fps_reset_statistics),
                )
            }

            WormaCeptorPlayPauseButton(
                isActive = state.isMonitoring,
                onToggle = { onEvent(FpsViewEvent.ToggleMonitoring) },
            )
        },
    )
}
