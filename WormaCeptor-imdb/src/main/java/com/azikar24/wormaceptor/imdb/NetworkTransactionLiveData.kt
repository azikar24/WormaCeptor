/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import androidx.lifecycle.MutableLiveData
import com.azikar24.wormaceptor.internal.data.NetworkTransaction

class NetworkTransactionLiveData(
    private val networkTransactionDataStore: NetworkTransactionDataStore,
    private val networkTransactionId: Long
) : MutableLiveData<NetworkTransaction>(), NetworkTransactionDataStore.DataChangeListener {
    init {
        networkTransactionDataStore.addDataChangeListener(this)
        updateData()
    }

    private fun updateData() {
        postValue(networkTransactionDataStore.getTransactionWithId(networkTransactionId))
    }

    override fun onInactive() {
        networkTransactionDataStore.removeDataChangeListener(this)
        super.onInactive()
    }

    override fun onActive() {
        super.onActive()
        updateData()
        networkTransactionDataStore.addDataChangeListener(this)
    }

    override fun onDataChange(
        event: NetworkTransactionDataStore.Companion.Event?,
        networkTransaction: NetworkTransaction?
    ) {
        if (networkTransaction?.id == networkTransactionId) {
            updateData()
        }
    }
}
