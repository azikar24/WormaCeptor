/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class NetworkTransactionViewModel : ViewModel() {

    private val config = PagingConfig(
        pageSize = 15,
        prefetchDistance = 10,
        enablePlaceholders = true,
        initialLoadSize = 30,
    )

    private val transactionDao: TransactionDao? = WormaCeptor.storage?.transactionDao

    val pageEventFlow = MutableStateFlow<PagingData<NetworkTransactionUIHelper>>(PagingData.empty())

    fun fetchData(key: String?) {
        if (key?.trim()?.isEmpty() == null) {
            transactionDao?.getAllTransactions()?.map(NetworkTransactionUIHelper.NETWORK_TRANSACTION_UI_HELPER_FUNCTION)?.asPagingSourceFactory()?.let {
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
            transactionDao?.getAllTransactionsWith(key, TransactionDao.SearchType.DEFAULT)?.map(NetworkTransactionUIHelper.NETWORK_TRANSACTION_UI_HELPER_FUNCTION)?.asPagingSourceFactory()?.let {

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

    fun clearAll() {
        ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS, LinkedBlockingQueue()).apply {
            execute {
                transactionDao?.clearAll()
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