package com.azikar24.wormaceptor.feature.fps.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
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
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.fps_monitor_title),
                    fontWeight = FontWeight.SemiBold,
                )
                AnimatedVisibility(
                    visible = state.isFpsWarning,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    WormaCeptorWarningBadge(
                        contentDescription = stringResource(R.string.fps_warning),
                    )
                }
            }
        },
        navigationIcon = {
            onBack?.let { back ->
                IconButton(onClick = back) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.fps_back),
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onResetStats) {
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}
