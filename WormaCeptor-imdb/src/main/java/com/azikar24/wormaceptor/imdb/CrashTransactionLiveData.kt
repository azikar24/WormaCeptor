/*
 * Copyright AziKar24 24/12/2025.
 */

package com.azikar24.wormaceptor.imdb

import androidx.lifecycle.LiveData
import com.azikar24.wormaceptor.internal.data.CrashTransaction

internal class CrashTransactionLiveData(
    private val crashTransactionDataStore: CrashTransactionDataStore,
    private val crashTransactionId: Long
) : LiveData<CrashTransaction>(), CrashTransactionDataStore.DataChangeListener {

    init {
        crashTransactionDataStore.addDataChangeListener(this)
    }

    override fun onActive() {
        super.onActive()
        postValue(crashTransactionDataStore.getTransactionWithId(crashTransactionId))
    }

    override fun onInactive() {
        crashTransactionDataStore.removeDataChangeListener(this)
        super.onInactive()
    }

    override fun onDataChange(event: NetworkTransactionDataStore.Companion.Event?, crashTransaction: CrashTransaction?) {
        crashTransaction?.let {
            if (it.id == crashTransactionId) {
                postValue(it)
            }
        }
        }
}
