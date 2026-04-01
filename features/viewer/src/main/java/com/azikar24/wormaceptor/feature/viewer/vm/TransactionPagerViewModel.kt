package com.azikar24.wormaceptor.feature.viewer.vm

import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.QueryEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ViewModel for the transaction pager screen.
 *
 * Manages transaction navigation index, loading, and animation direction.
 */
internal class TransactionPagerViewModel(
    private val queryEngine: QueryEngine,
) : BaseViewModel<TransactionPagerViewState, TransactionPagerEffect, TransactionPagerEvent>(
    initialState = TransactionPagerViewState(),
) {

    private var transactionIds: List<UUID> = emptyList()

    override fun handleEvent(event: TransactionPagerEvent) {
        when (event) {
            is TransactionPagerEvent.Initialize -> {
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
        updateState { copy(isLoading = true) }
        viewModelScope.launch {
            val tx = withContext(Dispatchers.IO) { queryEngine.getDetails(id) }
            updateState { copy(transaction = tx, isLoading = false) }
        }
    }
}
