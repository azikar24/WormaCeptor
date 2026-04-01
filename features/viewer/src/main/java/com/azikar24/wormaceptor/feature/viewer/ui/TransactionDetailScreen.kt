package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.gestures.SwipeBackContainer
import com.azikar24.wormaceptor.feature.viewer.ui.util.extractUrlPath
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewState
import kotlinx.coroutines.launch

/**
 * Transaction detail screen with swipe-back navigation.
 */
@Composable
internal fun TransactionDetailScreen(
    transaction: NetworkTransaction,
    detailViewModel: TransactionDetailViewModel,
    onBack: () -> Unit,
) {
    LaunchedEffect(transaction) {
        detailViewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(transaction))
    }

    SwipeBackContainer(onBack = onBack) {
        TransactionDetailContent(
            transaction = transaction,
            detailViewModel = detailViewModel,
            onBack = onBack,
        )
    }
}

/**
 * Transaction detail content — connects ViewModel via [BaseScreen] and delegates
 * to the scaffold with extracted header, pager, and search overlay.
 */
@Composable
internal fun TransactionDetailContent(
    transaction: NetworkTransaction,
    detailViewModel: TransactionDetailViewModel,
    onBack: () -> Unit,
    onNavigatePrevTransaction: () -> Unit = {},
    onNavigateNextTransaction: () -> Unit = {},
    canNavigatePrev: Boolean = false,
    canNavigateNext: Boolean = false,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    BaseScreen(
        viewModel = detailViewModel,
        onEffect = { effect ->
            handleTransactionDetailEffect(effect, context, scope, snackBarHostState)
        },
    ) { state, onEvent ->
        TransactionDetailScaffold(
            transaction = transaction,
            state = state,
            snackBarHostState = snackBarHostState,
            onEvent = onEvent,
            onBack = onBack,
            onNavigatePrev = onNavigatePrevTransaction,
            onNavigateNext = onNavigateNextTransaction,
            canNavigatePrev = canNavigatePrev,
            canNavigateNext = canNavigateNext,
        )
    }
}

@Composable
private fun TransactionDetailScaffold(
    transaction: NetworkTransaction,
    state: TransactionDetailViewState,
    snackBarHostState: SnackbarHostState,
    onEvent: (TransactionDetailViewEvent) -> Unit,
    onBack: () -> Unit,
    onNavigatePrev: () -> Unit,
    onNavigateNext: () -> Unit,
    canNavigatePrev: Boolean,
    canNavigateNext: Boolean,
) {
    val scope = rememberCoroutineScope()
    val tabs = listOf(
        stringResource(R.string.viewer_transaction_detail_tab_overview),
        stringResource(R.string.viewer_transaction_detail_tab_request),
        stringResource(R.string.viewer_transaction_detail_tab_response),
    )

    val tabPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size },
    )

    val matchCount = when (tabPagerState.currentPage) {
        1 -> state.requestMatchCount
        2 -> state.responseMatchCount
        else -> 0
    }

    val focusRequester = remember { FocusRequester() }

    val requestHasContent = transaction.request.headers.isNotEmpty() || transaction.request.bodyRef != null
    val responseHasContent = transaction.response?.headers?.isNotEmpty() == true ||
        transaction.response?.bodyRef != null
    val currentTabHasContent = when (tabPagerState.currentPage) {
        1 -> requestHasContent
        2 -> responseHasContent
        else -> false
    }

    LaunchedEffect(state.showSearch) {
        if (state.showSearch) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(tabPagerState.currentPage) {
        onEvent(TransactionDetailViewEvent.Search.ActiveTabChanged(tabPagerState.currentPage))
    }

    val title = remember(transaction.request.url) {
        extractUrlPath(transaction.request.url)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TransactionDetailHeader(
                title = title,
                showSearch = state.showSearch,
                showMenu = state.showMenu,
                currentTabHasContent = currentTabHasContent,
                tabs = tabs,
                selectedTabIndex = tabPagerState.currentPage,
                onTabSelected = { index -> scope.launch { tabPagerState.animateScrollToPage(index) } },
                onEvent = onEvent,
                onBack = onBack,
                onNavigatePrev = onNavigatePrev,
                onNavigateNext = onNavigateNext,
                canNavigatePrev = canNavigatePrev,
                canNavigateNext = canNavigateNext,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .windowInsetsPadding(WindowInsets.ime),
            ) {
                AnimatedVisibility(visible = state.showSearch) {
                    WormaCeptorSearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onEvent(TransactionDetailViewEvent.Search.QueryChanged(it)) },
                        placeholder = stringResource(R.string.viewer_transaction_detail_search_placeholder),
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                                vertical = WormaCeptorDesignSystem.Spacing.sm,
                            ),
                    )
                }

                HorizontalPager(
                    state = tabPagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 2,
                ) { page ->
                    when (page) {
                        0 -> OverviewTab(transaction, Modifier.fillMaxSize())
                        1 -> RequestTab(
                            transaction = transaction,
                            requestState = state.requestState,
                            searchQuery = state.debouncedSearchQuery,
                            currentMatchIndex = state.currentMatchIndex,
                            isSearchActive = state.showSearch,
                            onEvent = onEvent,
                            modifier = Modifier.fillMaxSize(),
                        )

                        2 -> ResponseTab(
                            transaction = transaction,
                            responseState = state.responseState,
                            searchQuery = state.debouncedSearchQuery,
                            currentMatchIndex = state.currentMatchIndex,
                            isSearchActive = state.showSearch,
                            onEvent = onEvent,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            TransactionDetailSearchOverlay(
                visible = state.showSearch && state.debouncedSearchQuery.isNotEmpty(),
                matchCount = matchCount,
                currentMatchIndex = state.currentMatchIndex,
                onEvent = onEvent,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .imePadding()
                    .padding(
                        bottom = WormaCeptorDesignSystem.Spacing.xxl,
                        end = WormaCeptorDesignSystem.Spacing.lg,
                    ),
            )
        }
    }
}
