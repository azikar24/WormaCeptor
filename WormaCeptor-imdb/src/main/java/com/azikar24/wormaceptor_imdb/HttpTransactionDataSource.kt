/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor_imdb

import androidx.paging.PositionalDataSource
import com.azikar24.wormaceptor.internal.data.HttpTransaction
import java.util.ArrayList


internal class HttpTransactionDataSource(
    transactionDataStore: TransactionDataStore,
    filter: Predicate<HttpTransaction>,
) : PositionalDataSource<HttpTransaction>(), TransactionDataStore.DataChangeListener {
    private val transactionDataStore: TransactionDataStore
    private val filter: Predicate<HttpTransaction>
    private var filteredTransactions: List<HttpTransaction>? = null

    init {
        this.filter = filter
        this.transactionDataStore = transactionDataStore
        updateTransactions()
        this.transactionDataStore.addDataChangeListener(this)
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<HttpTransaction>) {
        val totalCount = countItems()
        if (totalCount == 0) {
            callback.onResult(emptyList(), 0, 0)
            return
        }
        val firstLoadPosition = computeInitialLoadPosition(params, totalCount)
        val firstLoadSize = computeInitialLoadSize(params, firstLoadPosition, totalCount)
        val list: List<HttpTransaction>? = loadRange(firstLoadPosition, firstLoadSize)
        if (list?.size == firstLoadSize) {
            callback.onResult(list, firstLoadPosition, totalCount)
        } else {
            invalidate()
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<HttpTransaction>) {
        val list: List<HttpTransaction>? = loadRange(params.startPosition, params.loadSize)
        list?.let { callback.onResult(it) }
    }

    override fun onDataChange(event: TransactionDataStore.Companion.Event?, httpTransaction: HttpTransaction?) {
        if (httpTransaction?.let { isInTheList(it) } == true || httpTransaction?.let { event?.let { it1 -> checkIfEventCanEffectTheList(it1, it) } } == true) {
            updateTransactions()
            invalidate()
        }
    }

    private fun loadRange(startPosition: Int, loadSize: Int): List<HttpTransaction>? {
        return filteredTransactions?.subList(startPosition, startPosition + loadSize)
    }

    private fun countItems(): Int {
        return filteredTransactions?.size ?: 0
    }

    private fun isInTheList(modifiedTransaction: HttpTransaction): Boolean {
        val modifiedTransactionId: Long = modifiedTransaction.id
        return filteredTransactions?.let {
            for (httpTransaction in it) {
                if (httpTransaction.id == modifiedTransactionId) {
                    return true
                }
            }
            return false
        } ?: false

    }

    private fun checkIfEventCanEffectTheList(event: TransactionDataStore.Companion.Event, httpTransaction: HttpTransaction): Boolean {
        return (event == TransactionDataStore.Companion.Event.ADDED || event == TransactionDataStore.Companion.Event.UPDATED) && filter.apply(httpTransaction)
    }

    private fun updateTransactions() {
        val newFilteredTransactions: MutableList<HttpTransaction> = ArrayList<HttpTransaction>()
        for (httpTransaction in transactionDataStore.getDataList()!!) {
            if (filter.apply(httpTransaction)) newFilteredTransactions.add(httpTransaction)
        }
        newFilteredTransactions.sortWith { httpTransaction1, httpTransaction2 ->
            val httpTransactionId2: Long = httpTransaction2?.id ?: -1
            val httpTransactionId1: Long = httpTransaction1?.id ?: -1
            if (httpTransactionId2 < httpTransactionId1) -1 else if (httpTransactionId2 == httpTransactionId1) 0 else 1
        }
        filteredTransactions = newFilteredTransactions
    }

}