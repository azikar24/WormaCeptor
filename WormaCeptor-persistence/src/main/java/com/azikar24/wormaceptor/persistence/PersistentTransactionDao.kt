/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.persistence

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.paging.DataSource
import com.azikar24.wormaceptor.internal.data.TransactionDao
import java.util.*
import androidx.arch.core.util.Function
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.data.NetworkTransaction


class PersistentTransactionDao(private val roomTransactionDao: RoomTransactionDao?) : TransactionDao {

    override fun insertCrash(crashTransaction: CrashTransaction?) {
        crashTransaction?.let {
            val crashData = CRASH_DATA_TO_PERSISTENT_TRANSACTION_FUNCTION.apply(it)
            roomTransactionDao?.insertCrash(crashData)
        }
    }

    override fun getAllCrashes(): DataSource.Factory<Int, CrashTransaction>? {
        return roomTransactionDao?.allCrashes?.map { input ->
            PERSISTENT_TO_CRASH_DATA_TRANSACTION_FUNCTION.apply(input)
        }
    }

    override fun getCrashWithId(id: Long?): LiveData<CrashTransaction>? {
        return id?.let {
            roomTransactionDao?.getCrashWithId(it)?.map { input ->
                PERSISTENT_TO_CRASH_DATA_TRANSACTION_FUNCTION.apply(input)
            }
        }
    }

    override fun clearAllCrashes(): Int? {
        return roomTransactionDao?.clearAllCrashes()
    }

    override fun deleteCrash(vararg crashTransaction: CrashTransaction?): Int? {
        val persistentCrashTransactions = crashTransaction.mapNotNull {
            it?.let { CRASH_DATA_TO_PERSISTENT_TRANSACTION_FUNCTION.apply(it) }
        }.toTypedArray()

        return if (persistentCrashTransactions.isNotEmpty()) {
            roomTransactionDao?.deleteCrash(*persistentCrashTransactions)
        } else {
            0
        }
    }

    override fun insertTransaction(networkTransaction: NetworkTransaction?): Long? {
        return networkTransaction?.let {
            val dataToPersistence = DATA_TO_PERSISTENT_TRANSACTION_FOUNDATION.apply(it)
            roomTransactionDao?.insertNetworkTransaction(dataToPersistence)
        }
    }

    override fun updateTransaction(networkTransaction: NetworkTransaction?): Int? {
        return networkTransaction?.let {
            roomTransactionDao?.updateNetworkTransaction(DATA_TO_PERSISTENT_TRANSACTION_FOUNDATION.apply(it))
        }
    }

    override fun deleteTransactions(vararg networkTransactions: NetworkTransaction?): Int? {
        val persistentNetworkTransactions = networkTransactions.mapNotNull {
            it?.let { DATA_TO_PERSISTENT_TRANSACTION_FOUNDATION.apply(it) }
        }.toTypedArray()

        return if (persistentNetworkTransactions.isNotEmpty()) {
            roomTransactionDao?.deleteNetworkTransactions(*persistentNetworkTransactions)
        } else {
            0
        }
    }

    override fun deleteTransactionsBefore(beforeDate: Date?): Int? {
        return roomTransactionDao?.deleteNetworkTransactionsBefore(beforeDate)
    }


    override fun clearAll(): Int? {
        return roomTransactionDao?.clearAllNetworkTransactions()
    }

    override fun getAllTransactions(): DataSource.Factory<Int, NetworkTransaction>? {
        return roomTransactionDao?.allNetworkTransactions?.map { input ->
            PERSISTENT_TO_DATA_TRANSACTION_FUNCTION.apply(input)
        }
    }

    override fun getTransactionsWithId(id: Long?): LiveData<NetworkTransaction>? {
        return id?.let {
            roomTransactionDao?.getNetworkTransactionsWithId(it)?.map { input ->
                PERSISTENT_TO_DATA_TRANSACTION_FUNCTION.apply(input)
            }
        }
    }


    override fun getAllTransactionsWith(key: String?, searchType: TransactionDao.SearchType?): DataSource.Factory<Int, NetworkTransaction>? {
        val endWildCard = "$key%"
        val doubleSideWildCard = "%$key%"
        val factory: DataSource.Factory<Int, PersistentNetworkTransaction>? = when (searchType) {
            TransactionDao.SearchType.DEFAULT -> roomTransactionDao?.getAllNetworkTransactions(endWildCard, doubleSideWildCard)
            TransactionDao.SearchType.INCLUDE_REQUEST -> roomTransactionDao?.getAllNetworkTransactionsIncludeRequest(endWildCard, doubleSideWildCard)
            TransactionDao.SearchType.INCLUDE_RESPONSE -> roomTransactionDao?.getAllNetworkTransactionsIncludeResponse(endWildCard, doubleSideWildCard)
            TransactionDao.SearchType.INCLUDE_REQUEST_RESPONSE -> roomTransactionDao?.getAllNetworkTransactionsIncludeRequestResponse(endWildCard, doubleSideWildCard)
            else -> roomTransactionDao?.getAllNetworkTransactions(endWildCard, doubleSideWildCard)
        }
        return factory?.map { input ->
            PERSISTENT_TO_DATA_TRANSACTION_FUNCTION.apply(input)
        }
    }


    private val PERSISTENT_TO_DATA_TRANSACTION_FUNCTION: Function<PersistentNetworkTransaction, NetworkTransaction> = Function { input ->
        NetworkTransaction.Builder().apply {
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

    private val DATA_TO_PERSISTENT_TRANSACTION_FOUNDATION: Function<NetworkTransaction, PersistentNetworkTransaction> = Function { input ->
        PersistentNetworkTransaction().apply {
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
