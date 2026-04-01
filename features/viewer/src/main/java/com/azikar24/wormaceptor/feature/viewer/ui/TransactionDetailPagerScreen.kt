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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.ui.components.gestures.SwipeBackContainer
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewModel
import java.util.UUID

/**
 * Transaction detail screen with swipe navigation between transactions.
 *
 * UX Behavior:
 * - Swipe on toolbar/topBar area: Switch between transactions
 * - Swipe on content area: Switch between Overview/Request/Response tabs
 */
@Composable
internal fun TransactionDetailPagerScreen(
    transactionIds: List<UUID>,
    initialTransactionIndex: Int,
    getTransaction: suspend (UUID) -> NetworkTransaction?,
    detailViewModel: TransactionDetailViewModel,
    onBack: () -> Unit,
) {
    val view = LocalView.current

    var currentTransactionIndex by remember {
        mutableIntStateOf(initialTransactionIndex.coerceIn(0, (transactionIds.size - 1).coerceAtLeast(0)))
    }
    var navigationDirection by remember { mutableIntStateOf(0) }

    val currentTransactionId = transactionIds.getOrNull(currentTransactionIndex)
    var transaction by remember(currentTransactionId) { mutableStateOf<NetworkTransaction?>(null) }
    var isLoading by remember(currentTransactionId) { mutableStateOf(true) }

    LaunchedEffect(currentTransactionId) {
        if (currentTransactionId != null) {
            isLoading = true
            val loaded = getTransaction(currentTransactionId)
            transaction = loaded
            if (loaded != null) {
                detailViewModel.sendEvent(TransactionDetailViewEvent.Lifecycle.TransactionLoaded(loaded))
            }
            isLoading = false
        }
    }

    val canNavigatePrev = currentTransactionIndex > 0
    val canNavigateNext = currentTransactionIndex < transactionIds.size - 1

    fun navigateToPrevTransaction() {
        if (canNavigatePrev) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            navigationDirection = -1
            currentTransactionIndex--
        }
    }

    fun navigateToNextTransaction() {
        if (canNavigateNext) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            navigationDirection = 1
            currentTransactionIndex++
        }
    }

    val animDuration = 250
    val slideOffset = 100

    SwipeBackContainer(
        onBack = onBack,
        enabled = currentTransactionIndex == 0,
    ) {
        AnimatedContent(
            targetState = currentTransactionIndex to transaction,
            transitionSpec = {
                val slideSpec = tween<IntOffset>(animDuration, easing = FastOutSlowInEasing)
                if (navigationDirection >= 0) {
                    slideInHorizontally(slideSpec) { slideOffset } togetherWith
                        slideOutHorizontally(slideSpec) { -slideOffset }
                } else {
                    slideInHorizontally(slideSpec) { -slideOffset } togetherWith
                        slideOutHorizontally(slideSpec) { slideOffset }
                }
            },
            label = "transaction_transition",
        ) { (_, currentTransaction) ->
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (currentTransaction != null) {
                TransactionDetailContent(
                    transaction = currentTransaction,
                    detailViewModel = detailViewModel,
                    onBack = onBack,
                    onNavigatePrevTransaction = ::navigateToPrevTransaction,
                    onNavigateNextTransaction = ::navigateToNextTransaction,
                    canNavigatePrev = canNavigatePrev,
                    canNavigateNext = canNavigateNext,
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "Transaction not found",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}
