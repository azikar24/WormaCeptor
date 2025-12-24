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

    fun insertCrash(crashTransaction: CrashTransaction?)

    fun getAllCrashes(): DataSource.Factory<Int, CrashTransaction>?

    fun getCrashWithId(id: Long?): LiveData<CrashTransaction>?

    fun clearAllCrashes(): Int?

    fun deleteCrash(vararg crashTransaction: CrashTransaction?): Int?

    fun insertTransaction(networkTransaction: NetworkTransaction?): Long?

    fun updateTransaction(networkTransaction: NetworkTransaction?): Int?

    fun deleteTransactions(vararg networkTransactions: NetworkTransaction?): Int?

    fun deleteTransactionsBefore(beforeDate: Date?): Int?

    fun clearAll(): Int?

    fun getAllTransactions(): DataSource.Factory<Int, NetworkTransaction>?

    fun getTransactionsWithId(id: Long?): LiveData<NetworkTransaction>?

    fun getAllTransactionsWith(key: String?, searchType: SearchType?): DataSource.Factory<Int, NetworkTransaction>?

    fun getAllCrashesWith(key: String?, searchType: SearchType?): DataSource.Factory<Int, CrashTransaction>?

}