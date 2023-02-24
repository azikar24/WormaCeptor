/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.list

import android.os.AsyncTask
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
        ClearAsyncTask(transactionDao).execute()
    }

    private class ClearAsyncTask(private val transactionDao: TransactionDao?) : AsyncTask<CrashTransaction, Void, Int>() {
        override fun doInBackground(vararg params: CrashTransaction): Int? {
            return transactionDao?.clearAllCrashes()
        }
    }

    private class DeleteAsyncTask(private val transactionDao: TransactionDao?) : AsyncTask<CrashTransaction, Void, Int>() {
        override fun doInBackground(vararg params: CrashTransaction): Int? {
            return transactionDao?.deleteCrash(*params)
        }
    }

}