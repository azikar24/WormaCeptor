/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.list

import android.os.AsyncTask
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper
import com.azikar24.wormaceptor.internal.data.HttpTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao

class NetworkTransactionViewModel  : ViewModel() {

    private val config: PagedList.Config = PagedList.Config.Builder()
        .setPageSize(15)
        .setInitialLoadSizeHint(30)
        .setPrefetchDistance(10)
        .setEnablePlaceholders(true)
        .build()

    private val mTransactionDao: TransactionDao? = WormaCeptor.storage?.transactionDao


    val factory = mTransactionDao?.getAllTransactions()?.map(HttpTransactionUIHelper.HTTP_TRANSACTION_UI_HELPER_FUNCTION)
    private val mTransactions: LiveData<PagedList<HttpTransactionUIHelper>>? = factory?.let { LivePagedListBuilder(factory, config).build() }

    fun getTransactions(key: String?): LiveData<PagedList<HttpTransactionUIHelper>>? {
        return if (key?.trim()?.isEmpty() == null) {
            mTransactions
        } else {
            val factory = mTransactionDao?.getAllTransactionsWith(key, TransactionDao.SearchType.DEFAULT)?.map(HttpTransactionUIHelper.HTTP_TRANSACTION_UI_HELPER_FUNCTION)
            factory?.let {
                LivePagedListBuilder(it, config).build()
            }
        }
    }

    fun clearAll() {
        ClearAsyncTask(mTransactionDao).execute()
    }

    private class ClearAsyncTask(private val transactionDao: TransactionDao?) : AsyncTask<HttpTransaction, Void, Int>() {
        override fun doInBackground(vararg params: HttpTransaction): Int? {
            return transactionDao?.clearAll()
        }
    }

    private class DeleteAsyncTask(private val transactionDao: TransactionDao?) : AsyncTask<HttpTransaction, Void, Int>() {
        override fun doInBackground(vararg params: HttpTransaction): Int? {
            return transactionDao?.deleteTransactions(*params)
        }
    }

}