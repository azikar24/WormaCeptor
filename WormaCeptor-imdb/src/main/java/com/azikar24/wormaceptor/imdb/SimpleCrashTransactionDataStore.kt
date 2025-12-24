/*
 * Copyright AziKar24 24/12/2025.
 */

package com.azikar24.wormaceptor.imdb

import androidx.collection.ArraySet
import androidx.collection.LongSparseArray
import com.azikar24.wormaceptor.internal.data.CrashTransaction


internal class SimpleCrashTransactionDataStore : CrashTransactionDataStore {
    private val data: LongSparseArray<CrashTransaction> = LongSparseArray<CrashTransaction>(100)
    private val dataChangeListeners: MutableSet<CrashTransactionDataStore.DataChangeListener?> = ArraySet()
    
    override fun addTransaction(crashTransaction: CrashTransaction?) {
        guardForZeroAndNegativeIndices(crashTransaction?.id)
        addVerifiedTransaction(crashTransaction)
        sendDataChangeEvent(NetworkTransactionDataStore.Companion.Event.ADDED, crashTransaction)
    }

    override fun updateTransaction(crashTransaction: CrashTransaction?): Boolean {
        guardForZeroAndNegativeIndices(crashTransaction?.id)
        if (contains(crashTransaction)) {
            addVerifiedTransaction(crashTransaction)
            sendDataChangeEvent(NetworkTransactionDataStore.Companion.Event.UPDATED, crashTransaction)
            return true
        }
        return false
    }

    override fun removeTransactionWithIndex(index: Long): Boolean {
        guardForZeroAndNegativeIndices(index)
        if (data.containsKey(index)) {
            val deletedTransaction: CrashTransaction? = data[index]
            data.remove(index)
            sendDataChangeEvent(NetworkTransactionDataStore.Companion.Event.DELETED, deletedTransaction)
            return true
        }
        return false
    }

    override fun clearAllTransactions(): Int {
        val toBeDeletedData: List<CrashTransaction> = getDataList()
        data.clear()
        for (crashTransaction in toBeDeletedData) {
            sendDataChangeEvent(NetworkTransactionDataStore.Companion.Event.DELETED, crashTransaction)
        }
        return toBeDeletedData.size
    }

    override fun getDataList(): List<CrashTransaction> {
        val list: MutableList<CrashTransaction> = ArrayList(data.size())
        for (i in 0 until data.size()) {
            val crashTransaction: CrashTransaction = data.valueAt(i)
            list.add(crashTransaction)
        }
        return list
    }

    override fun getTransactionWithId(id: Long): CrashTransaction? {
        guardForZeroAndNegativeIndices(id)
        if (data.containsKey(id)) {
            return data[id]
        }
        throw NetworkTransactionDataStore.IndexDoesNotExistException()
    }

    override fun addDataChangeListener(dataChangeListener: CrashTransactionDataStore.DataChangeListener?) {
        dataChangeListeners.add(dataChangeListener)
    }

    override fun removeDataChangeListener(dataChangeListener: CrashTransactionDataStore.DataChangeListener?) {
        dataChangeListeners.remove(dataChangeListener)
    }

    private fun addVerifiedTransaction(crashTransaction: CrashTransaction?) {
        crashTransaction?.id?.let { data.append(it, crashTransaction) }
    }

    private fun sendDataChangeEvent(event: NetworkTransactionDataStore.Companion.Event, crashTransaction: CrashTransaction?) {
        for (dataChangeListener in dataChangeListeners) {
            dataChangeListener?.onDataChange(event, crashTransaction)
        }
    }

    private operator fun contains(crashTransaction: CrashTransaction?): Boolean {
        return crashTransaction?.id?.let { data.containsKey(it) } == true
    }

    private fun guardForZeroAndNegativeIndices(index: Long?) {
        if (index != null) {
            if (index < 0) {
                throw NetworkTransactionDataStore.NegativeIndexException()
            } else if (index == 0L) {
                throw NetworkTransactionDataStore.ZeroIndexException()
            }
        }
    }
}
