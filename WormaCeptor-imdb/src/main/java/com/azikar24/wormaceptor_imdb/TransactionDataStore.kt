/*
 * Copyright AziKar24 20/2/2023.
 */

package com.azikar24.wormaceptor_imdb

import com.azikar24.wormaceptor.internal.data.HttpTransaction


interface TransactionDataStore {

    interface DataChangeListener {
        fun onDataChange(event: Event?, httpTransaction: HttpTransaction?)
    }

    fun addTransaction(httpTransaction: HttpTransaction?)

    fun updateTransaction(httpTransaction: HttpTransaction?): Boolean

    fun removeTransactionWithIndex(index: Long): Boolean

    fun clearAllTransactions(): Int

    fun getDataList(): List<HttpTransaction>?

    fun getTransactionWithId(id: Long): HttpTransaction?

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