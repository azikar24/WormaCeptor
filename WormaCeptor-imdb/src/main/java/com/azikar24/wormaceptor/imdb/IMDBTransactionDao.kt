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

internal class IMDBTransactionDao(
    private val networkTransactionDataStore: NetworkTransactionDataStore,
    private val crashTransactionDataStore: CrashTransactionDataStore,
    private val transactionArchComponentProvider: TransactionArchComponentProvider,
    private val transactionPredicateProvider: TransactionPredicateProvider
) : TransactionDao {
    private var currentIndex: Long = 1
    private var currentCrashIndex: Long = 1

    override fun insertCrash(crashTransaction: CrashTransaction?) {
        val newTransactionIndex: Long? = if (crashTransaction?.id == 0L) {
            currentCrashIndex
        } else {
            crashTransaction?.id
        }
        crashTransactionDataStore.addTransaction(newTransactionIndex?.let {
            crashTransaction?.copy(id = it)
        })
        if (newTransactionIndex != null) {
            updateCurrentCrashIndex(newTransactionIndex)
        }
    }

    override fun getAllCrashes(): DataSource.Factory<Int, CrashTransaction>? {
        return transactionArchComponentProvider.getCrashDataSourceFactory(
            crashTransactionDataStore,
            Predicate.ALLOW_ALL_CRASHES
        )
    }

    override fun getCrashWithId(id: Long?): LiveData<CrashTransaction>? {
        return id?.let {
            transactionArchComponentProvider.getCrashLiveData(
                crashTransactionDataStore,
                it
            )
        }
    }

    override fun clearAllCrashes(): Int {
        return crashTransactionDataStore.clearAllTransactions()
    }

    override fun deleteCrash(vararg crashTransaction: CrashTransaction?): Int {
        var updates = 0
        for (crash in crashTransaction) {
            if ((crash?.id ?: -1) > 0 && crash?.id?.let {
                    crashTransactionDataStore.removeTransactionWithIndex(
                        it
                    )
                } == true) {
                updates++
            }
        }
        return updates
    }

    override fun insertTransaction(networkTransaction: NetworkTransaction?): Long? {
        val newTransactionIndex: Long? = if (networkTransaction?.id == 0L) {
            currentIndex
        } else {
            networkTransaction?.id
        }
        return addTransactionWithIndex(networkTransaction, newTransactionIndex)
    }

    override fun updateTransaction(networkTransaction: NetworkTransaction?): Int {
        if ((networkTransaction?.id ?: -1) <= 0) return 0

        if (networkTransactionDataStore.updateTransaction(networkTransaction)) {
            return 1
        }
        return 0
    }


    override fun deleteTransactions(vararg networkTransactions: NetworkTransaction?): Int {
        var updates = 0
        for (networkTransaction in networkTransactions) {
            if ((networkTransaction?.id ?: -1) > 0 && networkTransaction?.id?.let {
                    networkTransactionDataStore.removeTransactionWithIndex(
                        it
                    )
                } == true) {
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
        return id?.let {
            transactionArchComponentProvider.getLiveData(
                networkTransactionDataStore,
                it
            )
        }
    }

    override fun getAllTransactions(): DataSource.Factory<Int, NetworkTransaction>? {
        return transactionArchComponentProvider.getDataSourceFactory(
            networkTransactionDataStore,
            Predicate.ALLOW_ALL
        )
    }

    override fun getAllTransactionsWith(
        key: String?,
        searchType: TransactionDao.SearchType?
    ): DataSource.Factory<Int, NetworkTransaction>? {
        if (key == null) return null
        val predicate: Predicate<NetworkTransaction> = when (searchType) {
            TransactionDao.SearchType.DEFAULT -> transactionPredicateProvider.getDefaultSearchPredicate(
                key
            )

            TransactionDao.SearchType.INCLUDE_REQUEST -> transactionPredicateProvider.getRequestSearchPredicate(
                key
            )

            TransactionDao.SearchType.INCLUDE_RESPONSE -> transactionPredicateProvider.getResponseSearchPredicate(
                key
            )

            TransactionDao.SearchType.INCLUDE_REQUEST_RESPONSE -> transactionPredicateProvider.getRequestResponseSearchPredicate(
                key
            )

            else -> transactionPredicateProvider.getDefaultSearchPredicate(key)
        }
        return transactionArchComponentProvider.getDataSourceFactory(
            networkTransactionDataStore,
            predicate
        )
    }

    override fun getAllCrashesWith(
        key: String?,
        searchType: TransactionDao.SearchType?
    ): DataSource.Factory<Int, CrashTransaction>? {
        if (key == null) return null
        val predicate = transactionPredicateProvider.getCrashSearchPredicate(key)
        return transactionArchComponentProvider.getCrashDataSourceFactory(
            crashTransactionDataStore,
            predicate
        )
    }

    private fun addTransactionWithIndex(
        networkTransaction: NetworkTransaction?,
        newTransactionIndex: Long?
    ): Long? {
        networkTransactionDataStore.addTransaction(newTransactionIndex?.let {
            networkTransaction?.toBuilder()?.setId(it)?.build()
        })
        if (newTransactionIndex != null) {
            updateCurrentIndex(newTransactionIndex)
        }
        return newTransactionIndex
    }

    private fun updateCurrentCrashIndex(newTransactionIndex: Long) {
        if (currentCrashIndex <= newTransactionIndex) {
            currentCrashIndex = newTransactionIndex + 1
        }
    }

    private fun updateCurrentIndex(newTransactionIndex: Long) {
        if (currentIndex <= newTransactionIndex) {
            currentIndex = newTransactionIndex + 1
        }
    }
}
