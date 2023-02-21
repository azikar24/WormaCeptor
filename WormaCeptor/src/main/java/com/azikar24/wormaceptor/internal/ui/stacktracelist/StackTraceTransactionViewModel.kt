/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.stacktracelist

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.data.StackTraceTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao

class StackTraceTransactionViewModel : ViewModel() {

    private val config: PagedList.Config = PagedList.Config.Builder()
        .setPageSize(15)
        .setInitialLoadSizeHint(30)
        .setPrefetchDistance(10)
        .setEnablePlaceholders(true)
        .build()

    private val mTransactionDao: TransactionDao? = WormaCeptor.storage?.transactionDao


    private val mTransactions: LiveData<PagedList<StackTraceTransaction>>? = mTransactionDao?.getAllStackTraces()?.let { LivePagedListBuilder(it, config).build() }

    fun getAllStackTraces(key: String?): LiveData<PagedList<StackTraceTransaction>>? {
          return  mTransactions
    }

    fun clearAll() {
        ClearAsyncTask(mTransactionDao).execute()
    }

    private class ClearAsyncTask(private val transactionDao: TransactionDao?) : AsyncTask<StackTraceTransaction, Void, Int>() {
        override fun doInBackground(vararg params: StackTraceTransaction): Int? {
            return transactionDao?.clearAllStackTraces()
        }
    }

    private class DeleteAsyncTask(private val transactionDao: TransactionDao?) : AsyncTask<StackTraceTransaction, Void, Int>() {
        override fun doInBackground(vararg params: StackTraceTransaction): Int? {
            return transactionDao?.deleteStackTrace(*params)
        }
    }

}