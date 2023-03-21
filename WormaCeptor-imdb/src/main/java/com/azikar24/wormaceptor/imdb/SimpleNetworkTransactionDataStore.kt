/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import androidx.collection.ArraySet
import androidx.collection.LongSparseArray
import com.azikar24.wormaceptor.internal.data.NetworkTransaction

internal class SimpleNetworkTransactionDataStore : NetworkTransactionDataStore {
    private val data: LongSparseArray<NetworkTransaction> = LongSparseArray<NetworkTransaction>(200)
    private val dataChangeListeners: MutableSet<NetworkTransactionDataStore.DataChangeListener?> = ArraySet()
    override fun addTransaction(networkTransaction: NetworkTransaction?) {
        guardForZeroAndNegativeIndices(networkTransaction?.id)
        addVerifiedTransaction(networkTransaction)
        sendDataChangeEvent(NetworkTransactionDataStore.Companion.Event.ADDED, networkTransaction)
    }

    override fun updateTransaction(networkTransaction: NetworkTransaction?): Boolean {
        guardForZeroAndNegativeIndices(networkTransaction?.id)
        if (contains(networkTransaction)) {
            addVerifiedTransaction(networkTransaction)
            sendDataChangeEvent(NetworkTransactionDataStore.Companion.Event.UPDATED, networkTransaction)
            return true
        }
        return false
    }

    override fun removeTransactionWithIndex(index: Long): Boolean {
        guardForZeroAndNegativeIndices(index)
        if (data.containsKey(index)) {
            val deletedTransaction: NetworkTransaction? = data[index]
            data.remove(index)
            sendDataChangeEvent(NetworkTransactionDataStore.Companion.Event.DELETED, deletedTransaction)
            return true
        }
        return false
    }

    override fun clearAllTransactions(): Int {
        val toBeDeletedData: List<NetworkTransaction> = getDataList()
        data.clear()
        for (networkTransaction in toBeDeletedData) {
            sendDataChangeEvent(NetworkTransactionDataStore.Companion.Event.DELETED, networkTransaction)
        }
        return toBeDeletedData.size
    }

    override fun getDataList(): List<NetworkTransaction> {
        val list: MutableList<NetworkTransaction> = ArrayList(data.size())
        for (i in 0 until data.size()) {
            val networkTransaction: NetworkTransaction = data.valueAt(i)
            list.add(networkTransaction)
        }
        return list
    }

    override fun getTransactionWithId(id: Long): NetworkTransaction? {
        guardForZeroAndNegativeIndices(id)
        if (data.containsKey(id)) {
            return data[id]
        }
        throw NetworkTransactionDataStore.IndexDoesNotExistException()
    }

    override fun addDataChangeListener(
        dataChangeListener: NetworkTransactionDataStore.DataChangeListener?
    ) {
        dataChangeListeners.add(dataChangeListener)
    }

    override fun removeDataChangeListener(
        dataChangeListener: NetworkTransactionDataStore.DataChangeListener?
    ) {
        dataChangeListeners.remove(dataChangeListener)
    }

    private fun addVerifiedTransaction(networkTransaction: NetworkTransaction?) {
        networkTransaction?.id?.let { data.append(it, networkTransaction) }
    }

    private fun sendDataChangeEvent(
        event: NetworkTransactionDataStore.Companion.Event,
        networkTransaction: NetworkTransaction?
    ) {
        for (dataChangeListener in dataChangeListeners) {
            dataChangeListener?.onDataChange(event, networkTransaction)
        }
    }

    private operator fun contains(networkTransaction: NetworkTransaction?): Boolean {
        return networkTransaction?.id?.let { data.containsKey(it) } == true
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
