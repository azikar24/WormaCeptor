/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.persistence

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.paging.DataSource
import com.azikar24.wormaceptor.internal.data.HttpTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import java.util.*
import androidx.arch.core.util.Function
import com.azikar24.wormaceptor.internal.data.CrashTransaction


class PersistentTransactionDao(private val roomTransactionDao: RoomTransactionDao?) : TransactionDao {

    override fun insertCrash(crashTransaction: CrashTransaction?) {
        val crashData = CRASH_DATA_TO_PERSISTENT_TRANSACTION_FUNCTION.apply(crashTransaction)
        roomTransactionDao?.insertCrash(crashData)
    }

    override fun getAllCrashes(): DataSource.Factory<Int, CrashTransaction>? {
        return roomTransactionDao?.allCrashes?.map(PERSISTENT_TO_CRASH_DATA_TRANSACTION_FUNCTION)
    }

    override fun clearAllCrashes(): Int? {
        return roomTransactionDao?.clearAllCrashes()
    }

    override fun deleteCrash(vararg crashTransaction: CrashTransaction?): Int? {
        val persistentCrashTransaction = arrayOfNulls<PersistentCrashTransaction>(crashTransaction.size)
        var index = 0
        for (transaction in crashTransaction) {
            persistentCrashTransaction[index++] = CRASH_DATA_TO_PERSISTENT_TRANSACTION_FUNCTION.apply(transaction)
        }
        return roomTransactionDao?.deleteCrash(*persistentCrashTransaction)
    }

    override fun insertTransaction(httpTransaction: HttpTransaction?): Long? {
        val dataToPersistence = DATA_TO_PERSISTENT_TRANSACTION_FUNCTION.apply(httpTransaction)
        return roomTransactionDao?.insertTransaction(dataToPersistence)
    }

    override fun updateTransaction(httpTransaction: HttpTransaction?): Int? {
        return roomTransactionDao?.updateTransaction(DATA_TO_PERSISTENT_TRANSACTION_FUNCTION.apply(httpTransaction))
    }

    override fun deleteTransactions(vararg httpTransactions: HttpTransaction?): Int? {
        val persistentHttpTransactions = arrayOfNulls<PersistentHttpTransaction>(httpTransactions.size)
        var index = 0
        for (transaction in httpTransactions) {
            persistentHttpTransactions[index++] = DATA_TO_PERSISTENT_TRANSACTION_FUNCTION.apply(transaction)
        }
        return roomTransactionDao?.deleteTransactions(*persistentHttpTransactions)
    }

    override fun deleteTransactionsBefore(beforeDate: Date?): Int? {
        return roomTransactionDao?.deleteTransactionsBefore(beforeDate)
    }


    override fun clearAll(): Int? {
        return roomTransactionDao?.clearAll()
    }

    override fun getAllTransactions(): DataSource.Factory<Int, HttpTransaction>? {
        return roomTransactionDao?.allTransactions?.map(PERSISTENT_TO_DATA_TRANSACTION_FUNCTION)
    }

    override fun getTransactionsWithId(id: Long?): LiveData<HttpTransaction>? {
        return id?.let {
            roomTransactionDao?.getTransactionsWithId(id)?.let {
                Transformations.map(it, PERSISTENT_TO_DATA_TRANSACTION_FUNCTION)
            }
        }
    }


    override fun getAllTransactionsWith(key: String?, searchType: TransactionDao.SearchType?): DataSource.Factory<Int, HttpTransaction>? {
        val endWildCard = "$key%"
        val doubleSideWildCard = "%$key%"
        val factory: DataSource.Factory<Int, PersistentHttpTransaction>? = when (searchType) {
            TransactionDao.SearchType.DEFAULT -> roomTransactionDao?.getAllTransactions(endWildCard, doubleSideWildCard)
            TransactionDao.SearchType.INCLUDE_REQUEST -> roomTransactionDao?.getAllTransactionsIncludeRequest(endWildCard, doubleSideWildCard)
            TransactionDao.SearchType.INCLUDE_RESPONSE -> roomTransactionDao?.getAllTransactionsIncludeResponse(endWildCard, doubleSideWildCard)
            TransactionDao.SearchType.INCLUDE_REQUEST_RESPONSE -> roomTransactionDao?.getAllTransactionsIncludeRequestResponse(endWildCard, doubleSideWildCard)
            else -> roomTransactionDao?.getAllTransactions(endWildCard, doubleSideWildCard)
        }
        return factory?.map(PERSISTENT_TO_DATA_TRANSACTION_FUNCTION)
    }


    private val PERSISTENT_TO_DATA_TRANSACTION_FUNCTION: Function<PersistentHttpTransaction, HttpTransaction> = Function { input ->
        HttpTransaction.Builder().apply {
            id = input.id
            requestDate = input.requestDate
            responseDate = input.responseDate
            tookMs = input.tookMs
            protocol = input.protocol
            method = input.method
            url = input.url
            host = input.host
            path = input.path
            scheme = input.scheme
            requestContentLength = input.requestContentLength
            requestContentType = input.requestContentType
            requestHeaders = input.requestHeaders
            requestBody = input.requestBody
            requestBodyIsPlainText = input.requestBodyIsPlainText
            responseCode = input.responseCode
            responseMessage = input.responseMessage
            error = input.error
            responseContentLength = input.responseContentLength
            responseContentType = input.responseContentType
            responseHeaders = input.responseHeaders
            responseBody = input.responseBody
            responseBodyIsPlainText = input.responseBodyIsPlainText
        }.build()
    }

    private val DATA_TO_PERSISTENT_TRANSACTION_FUNCTION: Function<HttpTransaction, PersistentHttpTransaction> = Function { input ->
        PersistentHttpTransaction().apply {
            id = input.id
            requestDate = input.requestDate
            responseDate = input.responseDate
            tookMs = input.tookMs
            protocol = input.protocol
            method = input.method
            url = input.url
            host = input.host
            path = input.path
            scheme = input.scheme
            requestContentLength = input.requestContentLength
            requestContentType = input.requestContentType
            requestHeaders = input.requestHeaders
            requestBody = input.requestBody
            requestBodyIsPlainText = input.requestBodyIsPlainText
            responseCode = input.responseCode
            responseMessage = input.responseMessage
            error = input.error
            responseContentLength = input.responseContentLength
            responseContentType = input.responseContentType
            responseHeaders = input.responseHeaders
            responseBody = input.responseBody
            responseBodyIsPlainText = input.responseBodyIsPlainText
        }
    }


    private val PERSISTENT_TO_CRASH_DATA_TRANSACTION_FUNCTION: Function<PersistentCrashTransaction, CrashTransaction> = Function { input ->
        CrashTransaction.Builder().apply {
            id = input.id
            crashList = input.crashList
            crashDate = input.crashDate
            throwable = input.throwable
        }.build()
    }

    private val CRASH_DATA_TO_PERSISTENT_TRANSACTION_FUNCTION: Function<CrashTransaction, PersistentCrashTransaction> = Function { input ->
        PersistentCrashTransaction().apply {
            id = input.id
            crashList = input.crashList
            crashDate = input.crashDate
            throwable = input.throwable
        }
    }

}