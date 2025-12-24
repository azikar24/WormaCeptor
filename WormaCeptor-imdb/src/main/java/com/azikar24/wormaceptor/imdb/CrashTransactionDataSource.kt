/*
 * Copyright AziKar24 24/12/2025.
 */

package com.azikar24.wormaceptor.imdb

import androidx.paging.PositionalDataSource
import com.azikar24.wormaceptor.internal.data.CrashTransaction

internal class CrashTransactionDataSource(
    private val crashTransactionDataStore: CrashTransactionDataStore,
    private val filter: Predicate<CrashTransaction>?
) : PositionalDataSource<CrashTransaction>(), CrashTransactionDataStore.DataChangeListener {

    init {
        this.crashTransactionDataStore.addDataChangeListener(this)
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<CrashTransaction>) {
        val list = getList()
        val totalCount = list.size
        val firstLoadPosition = computeInitialLoadPosition(params, totalCount)
        val firstLoadSize = computeInitialLoadSize(params, firstLoadPosition, totalCount)
        val sublist = list.subList(firstLoadPosition, firstLoadPosition + firstLoadSize)
        callback.onResult(sublist, firstLoadPosition, totalCount)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<CrashTransaction>) {
        val list = getList()
        callback.onResult(list.subList(params.startPosition, params.startPosition + params.loadSize))
    }

    private fun getList(): List<CrashTransaction> {
        val list = crashTransactionDataStore.getDataList() ?: emptyList()
        return if (filter != null) {
            list.filter { filter.apply(it) }.reversed()
        } else {
            list.reversed()
        }
    }

    override fun onDataChange(event: NetworkTransactionDataStore.Companion.Event?, crashTransaction: CrashTransaction?) {
        if (event != null && crashTransaction != null && checkIfEventCanEffectTheList(event, crashTransaction)) {
            invalidate()
        }
    }

    private fun checkIfEventCanEffectTheList(event: NetworkTransactionDataStore.Companion.Event, crashTransaction: CrashTransaction?): Boolean {
        return (event == NetworkTransactionDataStore.Companion.Event.ADDED || event == NetworkTransactionDataStore.Companion.Event.DELETED || event == NetworkTransactionDataStore.Companion.Event.UPDATED)
    }
}
