/*
 * Copyright AziKar24 25/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.network

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import com.azikar24.wormaceptor.internal.support.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NetworkTransactionViewModel : ViewModel() {

    private val config = PagingConfig(
        pageSize = 100,
        prefetchDistance = 5,
        enablePlaceholders = true,
        initialLoadSize = 500,
    )

    private val transactionDao: TransactionDao? = WormaCeptor.storage?.transactionDao

    val pageEventFlow = MutableStateFlow<PagingData<NetworkTransaction>>(PagingData.empty())

    fun fetchData(key: String?) {
        if (key?.trim()?.isEmpty() == true) {
            transactionDao?.getAllTransactions()?.asPagingSourceFactory()?.let {
                val pager = Pager(config = config) {
                    it.invoke()
                }.flow.cachedIn(viewModelScope)

                viewModelScope.launch {
                    pager.collectLatest {
                        pageEventFlow.value = it
                    }
                }
            }

        } else {
            transactionDao?.getAllTransactionsWith(key, TransactionDao.SearchType.DEFAULT)?.asPagingSourceFactory()?.let {

                val pager = Pager(config = config) {
                    it.invoke()
                }.flow.cachedIn(viewModelScope)

                viewModelScope.launch {
                    pager.collectLatest {
                        pageEventFlow.value = it
                    }
                }
            }
        }
    }

    fun getTransactionWithId(id: Long): Flow<NetworkTransaction>? {
        return transactionDao?.getTransactionsWithId(id)?.asFlow()
    }

    fun clearAll() {

        ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS, LinkedBlockingQueue()).apply {
            execute {
                transactionDao?.clearAll()
                NotificationHelper.clearBuffer()
            }
        }
    }

    fun delete(vararg params: NetworkTransaction) {
        ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS, LinkedBlockingQueue()).apply {
            execute {
                transactionDao?.deleteTransactions(*params)
            }
        }
    }
}