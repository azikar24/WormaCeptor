package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.ViewerViewState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Manages side-effects for [HomeScreen]: pager ↔ tab synchronisation and snackbar observation.
 *
 * This composable renders no UI — it only launches effects.
 */
@Composable
fun HomeScreenEffects(
    pagerState: PagerState,
    state: ViewerViewState,
    isSelectionMode: Boolean,
    onEvent: (ViewerViewEvent) -> Unit,
    snackBarMessage: Flow<String>?,
    snackBarHostState: SnackbarHostState,
) {
    // Observe snackbar messages from ViewModel
    LaunchedEffect(snackBarMessage) {
        snackBarMessage?.collect { message ->
            snackBarHostState.showSnackbar(message)
        }
    }

    // Sync pagerState with selectedTabIndex when tab is clicked
    LaunchedEffect(state.selectedTabIndex) {
        if (pagerState.currentPage != state.selectedTabIndex) {
            pagerState.animateScrollToPage(state.selectedTabIndex)
        }
    }

    // Sync selectedTabIndex with pagerState when user swipes
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page != state.selectedTabIndex) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isSelectionMode) onEvent(ViewerViewEvent.SelectionCleared)
                    onEvent(ViewerViewEvent.TabSelected(page))
                }
            }
    }
}
