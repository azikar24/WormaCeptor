/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import androidx.paging.PositionalDataSource
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import java.util.ArrayList


internal class NetworkTransactionDataSource(
    networkTransactionDataStore: NetworkTransactionDataStore,
    filter: Predicate<NetworkTransaction>,
) : PositionalDataSource<NetworkTransaction>(), NetworkTransactionDataStore.DataChangeListener {
    private val networkTransactionDataStore: NetworkTransactionDataStore
    private val filter: Predicate<NetworkTransaction>
    private var filteredNetworkTransactions: List<NetworkTransaction>? = null

    init {
        this.filter = filter
        this.networkTransactionDataStore = networkTransactionDataStore
        updateNetworkTransactions()
        this.networkTransactionDataStore.addDataChangeListener(this)
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<NetworkTransaction>) {
        val totalCount = countItems()
        if (totalCount == 0) {
            callback.onResult(emptyList(), 0, 0)
            return
        }
        val firstLoadPosition = computeInitialLoadPosition(params, totalCount)
        val firstLoadSize = computeInitialLoadSize(params, firstLoadPosition, totalCount)
        val list: List<NetworkTransaction>? = loadRange(firstLoadPosition, firstLoadSize)
        if (list?.size == firstLoadSize) {
            callback.onResult(list, firstLoadPosition, totalCount)
        } else {
            invalidate()
        }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<NetworkTransaction>) {
        val list: List<NetworkTransaction>? = loadRange(params.startPosition, params.loadSize)
        list?.let { callback.onResult(it) }
    }

    override fun onDataChange(event: NetworkTransactionDataStore.Companion.Event?, networkTransaction: NetworkTransaction?) {
        if (networkTransaction?.let { isInTheList(it) } == true || networkTransaction?.let { event?.let { it1 -> checkIfEventCanEffectTheList(it1, it) } } == true) {
            updateNetworkTransactions()
            invalidate()
        }
    }

    private fun loadRange(startPosition: Int, loadSize: Int): List<NetworkTransaction>? {
        return filteredNetworkTransactions?.subList(startPosition, startPosition + loadSize)
    }

    private fun countItems(): Int {
        return filteredNetworkTransactions?.size ?: 0
    }

    private fun isInTheList(modifiedTransaction: NetworkTransaction): Boolean {
        val modifiedTransactionId: Long = modifiedTransaction.id
        return filteredNetworkTransactions?.let {
            for (networkTransaction in it) {
                if (networkTransaction.id == modifiedTransactionId) {
                    return true
                }
            }
            return false
        } ?: false

    }

    private fun checkIfEventCanEffectTheList(event: NetworkTransactionDataStore.Companion.Event, networkTransaction: NetworkTransaction): Boolean {
        return (event == NetworkTransactionDataStore.Companion.Event.ADDED || event == NetworkTransactionDataStore.Companion.Event.UPDATED) && filter.apply(networkTransaction)
    }

    private fun updateNetworkTransactions() {
        val newFilteredNetworkTransactions: MutableList<NetworkTransaction> = ArrayList<NetworkTransaction>()
        networkTransactionDataStore.getDataList()?.let {
            for (networkTransaction in it) {
                if (filter.apply(networkTransaction)) newFilteredNetworkTransactions.add(networkTransaction)
            }
        }
        newFilteredNetworkTransactions.sortWith { networkTransaction1, networkTransaction2 ->
            val networkTransactionId2: Long = networkTransaction2?.id ?: -1
            val networkTransactionId1: Long = networkTransaction1?.id ?: -1
            if (networkTransactionId2 < networkTransactionId1) -1 else if (networkTransactionId2 == networkTransactionId1) 0 else 1
        }
        filteredNetworkTransactions = newFilteredNetworkTransactions
    }
}