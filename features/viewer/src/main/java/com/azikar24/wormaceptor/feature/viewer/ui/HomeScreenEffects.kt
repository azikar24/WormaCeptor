package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewState
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewState
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
    homeState: HomeViewState,
    transactionState: TransactionListViewState,
    onHomeEvent: (HomeViewEvent) -> Unit,
    onTransactionEvent: (TransactionListViewEvent) -> Unit,
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
    LaunchedEffect(homeState.selectedTabIndex) {
        if (pagerState.currentPage != homeState.selectedTabIndex) {
            pagerState.animateScrollToPage(homeState.selectedTabIndex)
        }
    }

    // Sync selectedTabIndex with pagerState when user swipes
    val haptic = LocalHapticFeedback.current
    val currentSelectedTabIndex by rememberUpdatedState(homeState.selectedTabIndex)
    val currentSelectedIds by rememberUpdatedState(transactionState.selectedIds)
    val currentOnHomeEvent by rememberUpdatedState(onHomeEvent)
    val currentOnTransactionEvent by rememberUpdatedState(onTransactionEvent)

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
            .distinctUntilChanged()
            .collect { (page, scrolling) ->
                if (!scrolling && page != currentSelectedTabIndex) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (currentSelectedIds.isNotEmpty()) {
                        currentOnTransactionEvent(TransactionListViewEvent.SelectionCleared)
                    }
                    currentOnHomeEvent(HomeViewEvent.TabSelected(page))
                }
            }
    }
}
