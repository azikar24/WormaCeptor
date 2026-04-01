package com.azikar24.wormaceptor.feature.viewer.ui

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.gestures.SwipeBackContainer
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionPagerEffect
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionPagerEvent
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionPagerViewModel
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionPagerViewState
import java.util.UUID

/**
 * Transaction detail screen with swipe navigation between transactions.
 *
 * Uses [TransactionPagerViewModel] for pager state and [TransactionDetailViewModel]
 * for the detail content.
 */
@Composable
internal fun TransactionDetailPagerScreen(
    transactionIds: List<UUID>,
    initialTransactionIndex: Int,
    pagerViewModel: TransactionPagerViewModel,
    detailViewModel: TransactionDetailViewModel,
    onBack: () -> Unit,
) {
    val view = LocalView.current

    LaunchedEffect(transactionIds, initialTransactionIndex) {
        pagerViewModel.sendEvent(TransactionPagerEvent.Initialize(transactionIds, initialTransactionIndex))
    }

    BaseScreen(
        viewModel = pagerViewModel,
        onEffect = { effect ->
            when (effect) {
                is TransactionPagerEffect.HapticFeedback ->
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        },
    ) { pagerState, onPagerEvent ->
        // Bridge: notify detail VM when pager loads a new transaction
        LaunchedEffect(pagerState.transaction) {
            pagerState.transaction?.let {
                detailViewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(it))
            }
        }

        TransactionPagerContent(
            pagerState = pagerState,
            detailViewModel = detailViewModel,
            onBack = onBack,
            onNavigatePrev = { onPagerEvent(TransactionPagerEvent.NavigatePrev) },
            onNavigateNext = { onPagerEvent(TransactionPagerEvent.NavigateNext) },
        )
    }
}

@Composable
private fun TransactionPagerContent(
    pagerState: TransactionPagerViewState,
    detailViewModel: TransactionDetailViewModel,
    onBack: () -> Unit,
    onNavigatePrev: () -> Unit,
    onNavigateNext: () -> Unit,
) {
    @Suppress("MagicNumber")
    val animDuration = 250

    @Suppress("MagicNumber")
    val slideOffset = 100

    SwipeBackContainer(
        onBack = onBack,
        enabled = !pagerState.canNavigatePrev,
    ) {
        AnimatedContent(
            targetState = pagerState.currentIndex to pagerState.transaction,
            transitionSpec = {
                val slideSpec = tween<IntOffset>(animDuration, easing = FastOutSlowInEasing)
                if (pagerState.navigationDirection >= 0) {
                    slideInHorizontally(slideSpec) { slideOffset } togetherWith
                        slideOutHorizontally(slideSpec) { -slideOffset }
                } else {
                    slideInHorizontally(slideSpec) { -slideOffset } togetherWith
                        slideOutHorizontally(slideSpec) { slideOffset }
                }
            },
            label = "transaction_transition",
        ) { (_, currentTransaction) ->
            when {
                pagerState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                currentTransaction != null -> {
                    TransactionDetailContent(
                        transaction = currentTransaction,
                        detailViewModel = detailViewModel,
                        onBack = onBack,
                        onNavigatePrevTransaction = onNavigatePrev,
                        onNavigateNextTransaction = onNavigateNext,
                        canNavigatePrev = pagerState.canNavigatePrev,
                        canNavigateNext = pagerState.canNavigateNext,
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(R.string.viewer_transaction_detail_not_found),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }
}
