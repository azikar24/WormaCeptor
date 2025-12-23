/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import java.util.*

internal class IMDBTransactionDao(networkTransactionDataStore: NetworkTransactionDataStore, transactionArchComponentProvider: TransactionArchComponentProvider, transactionPredicateProvider: TransactionPredicateProvider) : TransactionDao {
    private var currentIndex: Long = 1
    private val networkTransactionDataStore: NetworkTransactionDataStore
    private val transactionArchComponentProvider: TransactionArchComponentProvider
    private val transactionPredicateProvider: TransactionPredicateProvider

    init {
        this.networkTransactionDataStore = networkTransactionDataStore
        this.transactionArchComponentProvider = transactionArchComponentProvider
        this.transactionPredicateProvider = transactionPredicateProvider
    }

    override fun insertCrash(crashTransaction: CrashTransaction?) = Unit

    override fun getAllCrashes(): DataSource.Factory<Int, CrashTransaction>? = null

    override fun getCrashWithId(id: Long?): LiveData<CrashTransaction>? = null

    override fun clearAllCrashes(): Int? = null

    override fun deleteCrash(vararg crashTransaction: CrashTransaction?): Int? = null

    override fun insertTransaction(networkTransaction: NetworkTransaction?): Long? {
        val newTransactionIndex: Long? = if (networkTransaction?.id == 0L) {
            currentIndex
        } else {
            networkTransaction?.id
        }
        return addTransactionWithIndex(networkTransaction, newTransactionIndex)
    }

    override fun updateTransaction(networkTransaction: NetworkTransaction?): Int {
        return if ((networkTransaction?.id ?: -1) > 0) if (networkTransactionDataStore.updateTransaction(networkTransaction)) 1 else 0 else 0
    }


    override fun deleteTransactions(vararg networkTransactions: NetworkTransaction?): Int {
        var updates = 0
        for (networkTransaction in networkTransactions) {
            if ((networkTransaction?.id ?: -1) > 0 && networkTransaction?.id?.let { networkTransactionDataStore.removeTransactionWithIndex(it) } == true) {
                updates++
            }
        }
        return updates
    }

    override fun deleteTransactionsBefore(beforeDate: Date?): Int {
        var deletedTransactionCount = 0
        networkTransactionDataStore.getDataList()?.also {
            for (transaction in it) {
                if (transaction.requestDate != null && transaction.requestDate?.before(beforeDate) == true) {
                    if (networkTransactionDataStore.removeTransactionWithIndex(transaction.id)) {
                        deletedTransactionCount++
                    }
                }
            }
        }

        return deletedTransactionCount
    }

    override fun clearAll(): Int {
        return networkTransactionDataStore.clearAllTransactions()
    }


    override fun getTransactionsWithId(id: Long?): LiveData<NetworkTransaction>? {
        return id?.let { transactionArchComponentProvider.getLiveData(networkTransactionDataStore, it) }
    }

    override fun getAllTransactions(): DataSource.Factory<Int, NetworkTransaction>? {
        return transactionArchComponentProvider.getDataSourceFactory(networkTransactionDataStore, Predicate.ALLOW_ALL)
    }

    override fun getAllTransactionsWith(key: String?, searchType: TransactionDao.SearchType?): DataSource.Factory<Int, NetworkTransaction>? {
        if (key == null) return null
        val predicate: Predicate<NetworkTransaction> = when (searchType) {
            TransactionDao.SearchType.DEFAULT -> transactionPredicateProvider.getDefaultSearchPredicate(key)
            TransactionDao.SearchType.INCLUDE_REQUEST -> transactionPredicateProvider.getRequestSearchPredicate(key)
            TransactionDao.SearchType.INCLUDE_RESPONSE -> transactionPredicateProvider.getResponseSearchPredicate(key)
            TransactionDao.SearchType.INCLUDE_REQUEST_RESPONSE -> transactionPredicateProvider.getRequestResponseSearchPredicate(key)
            else -> transactionPredicateProvider.getDefaultSearchPredicate(key)
        }
        return transactionArchComponentProvider.getDataSourceFactory(networkTransactionDataStore, predicate)
    }

    private fun addTransactionWithIndex(networkTransaction: NetworkTransaction?, newTransactionIndex: Long?): Long? {
        networkTransactionDataStore.addTransaction(newTransactionIndex?.let { networkTransaction?.toBuilder()?.setId(it)?.build() })
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
