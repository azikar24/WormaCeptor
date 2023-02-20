/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor_imdb

import androidx.collection.ArraySet
import androidx.collection.LongSparseArray
import com.azikar24.wormaceptor.internal.data.HttpTransaction


internal class SimpleTransactionDataStore : TransactionDataStore {
    private val data: LongSparseArray<HttpTransaction> = LongSparseArray<HttpTransaction>(200)
    private val dataChangeListeners: MutableSet<TransactionDataStore.DataChangeListener?> = ArraySet()
    override fun addTransaction(httpTransaction: HttpTransaction?) {
        guardForZeroAndNegativeIndices(httpTransaction?.id)
        addVerifiedTransaction(httpTransaction)
        sendDataChangeEvent(TransactionDataStore.Companion.Event.ADDED, httpTransaction)
    }

    override fun updateTransaction(httpTransaction: HttpTransaction?): Boolean {
        guardForZeroAndNegativeIndices(httpTransaction?.id)
        if (contains(httpTransaction)) {
            addVerifiedTransaction(httpTransaction)
            sendDataChangeEvent(TransactionDataStore.Companion.Event.UPDATED, httpTransaction)
            return true
        }
        return false
    }

    override fun removeTransactionWithIndex(index: Long): Boolean {
        guardForZeroAndNegativeIndices(index)
        if (data.containsKey(index)) {
            val deletedTransaction: HttpTransaction? = data[index]
            data.remove(index)
            sendDataChangeEvent(TransactionDataStore.Companion.Event.DELETED, deletedTransaction)
            return true
        }
        return false
    }

    override fun clearAllTransactions(): Int {
        val toBeDeletedData: List<HttpTransaction> = getDataList()
        data.clear()
        for (httpTransaction in toBeDeletedData) {
            sendDataChangeEvent(TransactionDataStore.Companion.Event.DELETED, httpTransaction)
        }
        return toBeDeletedData.size
    }

    override fun getDataList(): List<HttpTransaction> {
        val list: MutableList<HttpTransaction> = ArrayList<HttpTransaction>(data.size())
        for (i in 0 until data.size()) {
            val httpTransaction: HttpTransaction = data.valueAt(i)
            list.add(httpTransaction)
        }
        return list
    }

    override fun getTransactionWithId(id: Long): HttpTransaction? {
        guardForZeroAndNegativeIndices(id)
        if (data.containsKey(id)) {
            return data[id]
        }
        throw TransactionDataStore.IndexDoesNotExistException()
    }

    override fun addDataChangeListener(dataChangeListener: TransactionDataStore.DataChangeListener?) {
        dataChangeListeners.add(dataChangeListener)
    }

    override fun removeDataChangeListener(dataChangeListener: TransactionDataStore.DataChangeListener?) {
        dataChangeListeners.remove(dataChangeListener)
    }

    private fun addVerifiedTransaction(httpTransaction: HttpTransaction?) {
        httpTransaction?.id?.let { data.append(it, httpTransaction) }
    }

    private fun sendDataChangeEvent(event: TransactionDataStore.Companion.Event, httpTransaction: HttpTransaction?) {
        for (dataChangeListener in dataChangeListeners) {
            dataChangeListener?.onDataChange(event, httpTransaction)
        }
    }

    private operator fun contains(httpTransaction: HttpTransaction?): Boolean {
        return httpTransaction?.id?.let { data.containsKey(it) } == true
    }

    private fun guardForZeroAndNegativeIndices(index: Long?) {
        if (index != null) {
            if (index < 0) {
                throw TransactionDataStore.NegativeIndexException()
            } else if (index == 0L) {
                throw TransactionDataStore.ZeroIndexException()
            }
        }
    }
}
