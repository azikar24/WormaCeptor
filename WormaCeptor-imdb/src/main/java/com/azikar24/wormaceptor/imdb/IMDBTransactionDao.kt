/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.azikar24.wormaceptor.internal.data.HttpTransaction
import com.azikar24.wormaceptor.internal.data.StackTraceTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import java.util.*

internal class IMDBTransactionDao(transactionDataStore: TransactionDataStore, transactionArchComponentProvider: TransactionArchComponentProvider, transactionPredicateProvider: TransactionPredicateProvider) : TransactionDao {
    private var currentIndex: Long = 1
    private val transactionDataStore: TransactionDataStore
    private val transactionArchComponentProvider: TransactionArchComponentProvider
    private val transactionPredicateProvider: TransactionPredicateProvider

    init {
        this.transactionDataStore = transactionDataStore
        this.transactionArchComponentProvider = transactionArchComponentProvider
        this.transactionPredicateProvider = transactionPredicateProvider
    }

    override fun insertStackTrace(stackTraceTransaction: StackTraceTransaction?) = Unit

    override fun getAllStackTraces(): DataSource.Factory<Int, StackTraceTransaction>? = null

    override fun insertTransaction(httpTransaction: HttpTransaction?): Long? {
        val newTransactionIndex: Long? = if (httpTransaction?.id == 0L) {
            currentIndex
        } else {
            httpTransaction?.id
        }
        return addTransactionWithIndex(httpTransaction, newTransactionIndex)
    }

    override fun updateTransaction(httpTransaction: HttpTransaction?): Int {
        return if ((httpTransaction?.id ?: -1) > 0) if (transactionDataStore.updateTransaction(httpTransaction)) 1 else 0 else 0
    }


    override fun deleteTransactions(vararg httpTransactions: HttpTransaction?): Int {
        var updates = 0
        for (httpTransaction in httpTransactions) {
            if ((httpTransaction?.id ?: -1) > 0 && httpTransaction?.id?.let { transactionDataStore.removeTransactionWithIndex(it) } == true) {
                updates++
            }
        }
        return updates
    }

    override fun deleteTransactionsBefore(beforeDate: Date?): Int {
        var deletedTransactionCount = 0
        transactionDataStore.getDataList()?.also {
            for (transaction in it) {
                if (transaction.requestDate != null && transaction.requestDate?.before(beforeDate) == true) {
                    if (transactionDataStore.removeTransactionWithIndex(transaction.id)) {
                        deletedTransactionCount++
                    }
                }
            }
        }

        return deletedTransactionCount
    }

    override fun clearAll(): Int {
        return transactionDataStore.clearAllTransactions()
    }


    override fun getTransactionsWithId(id: Long?): LiveData<HttpTransaction>? {
        return id?.let { transactionArchComponentProvider.getLiveData(transactionDataStore, it) }
    }

    override fun getAllTransactions(): DataSource.Factory<Int, HttpTransaction>? {
        return transactionArchComponentProvider.getDataSourceFactory(transactionDataStore, Predicate.ALLOW_ALL)
    }

    override fun getAllTransactionsWith(key: String?, searchType: TransactionDao.SearchType?): DataSource.Factory<Int, HttpTransaction>? {
        if (key == null) return null
        val predicate: Predicate<HttpTransaction> = when (searchType) {
            TransactionDao.SearchType.DEFAULT -> transactionPredicateProvider.getDefaultSearchPredicate(key)
            TransactionDao.SearchType.INCLUDE_REQUEST -> transactionPredicateProvider.getRequestSearchPredicate(key)
            TransactionDao.SearchType.INCLUDE_RESPONSE -> transactionPredicateProvider.getResponseSearchPredicate(key)
            TransactionDao.SearchType.INCLUDE_REQUEST_RESPONSE -> transactionPredicateProvider.getRequestResponseSearchPredicate(key)
            else -> transactionPredicateProvider.getDefaultSearchPredicate(key)
        }
        return transactionArchComponentProvider.getDataSourceFactory(transactionDataStore, predicate)
    }

    private fun addTransactionWithIndex(httpTransaction: HttpTransaction?, newTransactionIndex: Long?): Long? {
        transactionDataStore.addTransaction(newTransactionIndex?.let { httpTransaction?.toBuilder()?.setId(it)?.build() })
        if (newTransactionIndex != null) {
            updateCurrentIndex(newTransactionIndex)
        }
        return newTransactionIndex
    }

    private fun updateCurrentIndex(newTransactionIndex: Long) {
        if (currentIndex <= newTransactionIndex) {
            currentIndex = newTransactionIndex + 1
        }
    }
}
