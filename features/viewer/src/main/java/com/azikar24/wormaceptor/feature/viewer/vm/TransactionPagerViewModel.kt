package com.azikar24.wormaceptor.feature.viewer.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.common.presentation.NoOpNavigator
import com.azikar24.wormaceptor.core.engine.QueryEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel for the transaction pager screen.
 *
 * Manages transaction navigation index, live observation of the current transaction, and animation direction.
 */
internal class TransactionPagerViewModel(
    private val queryEngine: QueryEngine,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseViewModel<TransactionPagerViewState, TransactionPagerEffect, TransactionPagerEvent, NoOpNavigator>(
    initialState = TransactionPagerViewState(),
    navigator = NoOpNavigator,
) {

    private var transactionIds: List<UUID> = emptyList()
    private var isInitialized = false
    private var observeJob: Job? = null

    override fun handleEvent(event: TransactionPagerEvent) {
        when (event) {
            is TransactionPagerEvent.Initialize -> {
                // The screen re-sends Initialize after recreation; the retained state wins.
                if (isInitialized) return
                isInitialized = true
                transactionIds = event.transactionIds
                val index = event.initialIndex.coerceIn(0, (transactionIds.size - 1).coerceAtLeast(0))
                updateState {
                    copy(
                        currentIndex = index,
                        canNavigatePrev = index > 0,
                        canNavigateNext = index < transactionIds.size - 1,
                    )
                }
                loadTransaction(index)
            }

            is TransactionPagerEvent.NavigatePrev -> {
                val newIndex = uiState.value.currentIndex - 1
                if (newIndex >= 0) {
                    emitEffect(TransactionPagerEffect.HapticFeedback)
                    updateState {
                        copy(
                            currentIndex = newIndex,
                            navigationDirection = -1,
                            canNavigatePrev = newIndex > 0,
                            canNavigateNext = newIndex < transactionIds.size - 1,
                        )
                    }
                    loadTransaction(newIndex)
                }
            }

            is TransactionPagerEvent.NavigateNext -> {
                val newIndex = uiState.value.currentIndex + 1
                if (newIndex < transactionIds.size) {
                    emitEffect(TransactionPagerEffect.HapticFeedback)
                    updateState {
                        copy(
                            currentIndex = newIndex,
                            navigationDirection = 1,
                            canNavigatePrev = newIndex > 0,
                            canNavigateNext = newIndex < transactionIds.size - 1,
                        )
                    }
                    loadTransaction(newIndex)
                }
            }
        }
    }

    private fun loadTransaction(index: Int) {
        val id = transactionIds.getOrNull(index) ?: return
        observeJob?.cancel()
        updateState { copy(isLoading = true) }
        observeJob = viewModelScope.launch {
            queryEngine.observeDetails(id)
                .flowOn(ioDispatcher)
                .collect { tx -> updateState { copy(transaction = tx, isLoading = false) } }
        }
    }
}
