/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.list

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao

class CrashTransactionViewModel : ViewModel() {

    private val config: PagedList.Config = PagedList.Config.Builder()
        .setPageSize(15)
        .setInitialLoadSizeHint(30)
        .setPrefetchDistance(10)
        .setEnablePlaceholders(true)
        .build()

    private val transactionDao: TransactionDao? = WormaCeptor.storage?.transactionDao


    private val transactions: LiveData<PagedList<CrashTransaction>>? = transactionDao?.getAllCrashes()?.let { LivePagedListBuilder(it, config).build() }

    fun getAllCrashes(): LiveData<PagedList<CrashTransaction>>? {
          return  transactions
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