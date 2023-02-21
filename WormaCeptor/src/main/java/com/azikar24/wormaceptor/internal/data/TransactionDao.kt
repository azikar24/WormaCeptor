/*
 * Copyright AziKar24 19/2/2023.
 */

package com.azikar24.wormaceptor.internal.data

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import java.util.*


interface TransactionDao {

    enum class SearchType {
        DEFAULT, INCLUDE_REQUEST, INCLUDE_RESPONSE, INCLUDE_REQUEST_RESPONSE
    }

    fun insertStackTrace(stackTraceTransaction: StackTraceTransaction?)

    fun getAllStackTraces(): DataSource.Factory<Int, StackTraceTransaction>?

    fun clearAllStackTraces(): Int?

    fun deleteStackTrace(vararg stackTraceTransaction: StackTraceTransaction?): Int?

    fun insertTransaction(httpTransaction: HttpTransaction?): Long?

    fun updateTransaction(httpTransaction: HttpTransaction?): Int?

    fun deleteTransactions(vararg httpTransactions: HttpTransaction?): Int?

    fun deleteTransactionsBefore(beforeDate: Date?): Int?

    fun clearAll(): Int?

    fun getAllTransactions(): DataSource.Factory<Int, HttpTransaction>?

    fun getTransactionsWithId(id: Long?): LiveData<HttpTransaction>?

    fun getAllTransactionsWith(key: String?, searchType: SearchType?): DataSource.Factory<Int, HttpTransaction>?

}