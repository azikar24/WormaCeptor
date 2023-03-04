/*
 * Copyright AziKar24 3/3/2023.
 */

package com.azikar24.wormaceptor.internal.ui.features.crashes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class CrashTransactionViewModel : ViewModel() {
    private val config = PagingConfig(
        pageSize = 15,
        prefetchDistance = 10,
        enablePlaceholders = true,
        initialLoadSize = 30,
    )

    private val transactionDao: TransactionDao? = WormaCeptor.storage?.transactionDao

    val pageEventFlow = MutableStateFlow<PagingData<CrashTransaction>>(PagingData.empty())

    fun fetchData() {
        transactionDao?.getAllCrashes()?.let {
            val pager = Pager(config = config) {
                it.asPagingSourceFactory().invoke()
            }.flow.cachedIn(viewModelScope)

            viewModelScope.launch {
                pager.collectLatest {
                    pageEventFlow.value = it
                }
            }
        }
    }

    fun clearAll() {
        ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS, LinkedBlockingQueue()).apply {
            execute {
                transactionDao?.clearAllCrashes()
            }
        }
    }

    fun delete(vararg params: CrashTransaction) {
        ThreadPoolExecutor(4, 8, 60L, TimeUnit.SECONDS, LinkedBlockingQueue()).apply {
            execute {
                transactionDao?.deleteCrash(*params)
            }
        }
    }
}