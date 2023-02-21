/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.http.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper
import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper.Companion.HTTP_TRANSACTION_UI_HELPER_FUNCTION
import com.azikar24.wormaceptor.internal.data.TransactionDao

class TransactionDetailViewModel : ViewModel() {
    private val mTransactionDao: TransactionDao? = WormaCeptor.storage?.transactionDao

    fun getTransactionWithId(id: Long): LiveData<HttpTransactionUIHelper>? {
        return mTransactionDao?.getTransactionsWithId(id)?.let {
            Transformations.map(it, HTTP_TRANSACTION_UI_HELPER_FUNCTION)
        }
    }

}