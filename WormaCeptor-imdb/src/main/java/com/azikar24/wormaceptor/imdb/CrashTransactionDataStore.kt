/*
 * Copyright AziKar24 24/12/2025.
 */

package com.azikar24.wormaceptor.imdb

import com.azikar24.wormaceptor.internal.data.CrashTransaction


interface CrashTransactionDataStore {

    interface DataChangeListener {
        fun onDataChange(event: NetworkTransactionDataStore.Companion.Event?, crashTransaction: CrashTransaction?)
    }

    fun addTransaction(crashTransaction: CrashTransaction?)

    fun updateTransaction(crashTransaction: CrashTransaction?): Boolean

    fun removeTransactionWithIndex(index: Long): Boolean

    fun clearAllTransactions(): Int

    fun getDataList(): List<CrashTransaction>?

    fun getTransactionWithId(id: Long): CrashTransaction?

    fun addDataChangeListener(dataChangeListener: DataChangeListener?)

    fun removeDataChangeListener(dataChangeListener: DataChangeListener?)
}
