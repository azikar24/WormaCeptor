/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.azikar24.wormaceptor.internal.data.HttpTransaction

internal class TransactionArchComponentProvider {
    fun getDataSourceFactory(transactionDataStore: TransactionDataStore?, filter: Predicate<HttpTransaction>?): DataSource.Factory<Int, HttpTransaction>? {
        return if (filter != null && transactionDataStore != null) {
            object : DataSource.Factory<Int, HttpTransaction>() {
                override fun create(): DataSource<Int, HttpTransaction> {
                    return HttpTransactionDataSource(transactionDataStore, filter)
                }
            }
        } else null
    }

    fun getLiveData(transactionDataStore: TransactionDataStore?, id: Long): LiveData<HttpTransaction>? {
        return transactionDataStore?.let { HttpTransactionLiveData(it, id) }
    }
}
