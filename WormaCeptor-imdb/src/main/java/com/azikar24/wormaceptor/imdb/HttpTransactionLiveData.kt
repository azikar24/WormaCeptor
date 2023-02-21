/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import androidx.lifecycle.MutableLiveData
import com.azikar24.wormaceptor.internal.data.HttpTransaction


class HttpTransactionLiveData(private val transactionDataStore: TransactionDataStore, private val transactionId: Long) : MutableLiveData<HttpTransaction>(), TransactionDataStore.DataChangeListener {
    init {
        transactionDataStore.addDataChangeListener(this)
        updateData()
    }

    private fun updateData() {
        postValue(transactionDataStore.getTransactionWithId(transactionId))
    }

    override fun onInactive() {
        transactionDataStore.removeDataChangeListener(this)
        super.onInactive()
    }

    override fun onActive() {
        super.onActive()
        updateData()
        transactionDataStore.addDataChangeListener(this)
    }

    override fun onDataChange(event: TransactionDataStore.Companion.Event?, httpTransaction: HttpTransaction?) {
        if (httpTransaction?.id == transactionId) {
            updateData()
        }
    }
}
