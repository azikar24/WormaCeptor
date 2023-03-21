/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import com.azikar24.wormaceptor.internal.data.NetworkTransaction

interface NetworkTransactionDataStore {

    interface DataChangeListener {
        fun onDataChange(event: Event?, networkTransaction: NetworkTransaction?)
    }

    fun addTransaction(networkTransaction: NetworkTransaction?)

    fun updateTransaction(networkTransaction: NetworkTransaction?): Boolean

    fun removeTransactionWithIndex(index: Long): Boolean

    fun clearAllTransactions(): Int

    fun getDataList(): List<NetworkTransaction>?

    fun getTransactionWithId(id: Long): NetworkTransaction?

    fun addDataChangeListener(dataChangeListener: DataChangeListener?)

    fun removeDataChangeListener(dataChangeListener: DataChangeListener?)

    class IndexDoesNotExistException : RuntimeException()

    class NegativeIndexException : RuntimeException()

    class ZeroIndexException : RuntimeException()

    companion object {
        enum class Event {
            ADDED, UPDATED, DELETED
        }
    }
}