package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.feature.webviewmonitor.R

@Composable
internal fun ListTopBar(
    searchActive: Boolean,
    searchQuery: String,
    onSearchToggle: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClearRequests: () -> Unit,
    onNavigateBack: (() -> Unit)?,
) {
    val haptic = LocalHapticFeedback.current

    Column {
        WormaCeptorTopBar(
            title = stringResource(R.string.webviewmonitor_title),
            onBack = onNavigateBack,
            backContentDescription = stringResource(R.string.webviewmonitor_action_back),
            actions = {
                WormaCeptorIconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = stringResource(R.string.webviewmonitor_action_search),
                    )
                }
                WormaCeptorIconButton(onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClearRequests()
                }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.webviewmonitor_action_clear),
                    )
                }
            },
        )
        ExpandableSearchBar(
            visible = searchActive,
            query = searchQuery,
            onQueryChange = onSearchQueryChanged,
        )
    }
}
