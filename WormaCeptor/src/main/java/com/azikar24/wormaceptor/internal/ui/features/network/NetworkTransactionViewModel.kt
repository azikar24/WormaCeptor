/*
 * Copyright AziKar24 2024.
 */

package com.azikar24.wormaceptor.internal.ui.features.network

import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.azikar24.wormaceptor.internal.data.NetworkTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import com.azikar24.wormaceptor.internal.support.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NetworkTransactionViewModel(private val transactionDao: TransactionDao?) : ViewModel() {

    private val config = PagingConfig(
        pageSize = 100,
        prefetchDistance = 5,
        enablePlaceholders = true,
        initialLoadSize = 500,
    )

    val pageEventFlow = MutableStateFlow<PagingData<NetworkTransaction>>(PagingData.empty())

    fun fetchData(key: String?, method: String? = null, statusRange: String? = null) {
        val queryKey = if (key?.trim()?.isEmpty() == true) null else key
        
        if (queryKey == null && method == null && statusRange == null) {
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
            transactionDao?.getAllTransactionsWith(
                key = queryKey.orEmpty(),
                searchType = TransactionDao.SearchType.DEFAULT,
                method = method,
                statusRange = statusRange
            )?.asPagingSourceFactory()?.let {
                val pager = Pager(config = config) {
                    it.invoke()
                }.flow.cachedIn(viewModelScope)

                viewModelScope.launch {
                    pager.collectLatest { transaction ->
                        pageEventFlow.value = transaction
                    }
                }
            }
        }
    }

    fun getTransactionWithId(id: Long): Flow<NetworkTransaction>? {
        return transactionDao?.getTransactionsWithId(id)?.asFlow()
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao?.clearAll()
            NotificationHelper.clearBuffer()
        }
    }

    fun delete(vararg params: NetworkTransaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao?.deleteTransactions(*params)
        }
    }
}
