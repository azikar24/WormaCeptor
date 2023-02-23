/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.imdb

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.azikar24.wormaceptor.internal.data.NetworkTransaction

internal class TransactionArchComponentProvider {
    fun getDataSourceFactory(networkTransactionDataStore: NetworkTransactionDataStore?, filter: Predicate<NetworkTransaction>?): DataSource.Factory<Int, NetworkTransaction>? {
        return if (filter != null && networkTransactionDataStore != null) {
            object : DataSource.Factory<Int, NetworkTransaction>() {
                override fun create(): DataSource<Int, NetworkTransaction> {
                    return NetworkTransactionDataSource(networkTransactionDataStore, filter)
                }
            }
        } else null
    }

    fun getLiveData(networkTransactionDataStore: NetworkTransactionDataStore?, id: Long): LiveData<NetworkTransaction>? {
        return networkTransactionDataStore?.let { NetworkTransactionLiveData(it, id) }
    }
}
