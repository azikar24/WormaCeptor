/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.network.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper
import com.azikar24.wormaceptor.internal.NetworkTransactionUIHelper.Companion.NETWORK_TRANSACTION_UI_HELPER_FUNCTION
import com.azikar24.wormaceptor.internal.data.TransactionDao

class NetworkDetailTransactionViewModel : ViewModel() {
    private val transactionDao: TransactionDao? = WormaCeptor.storage?.transactionDao

    fun getTransactionWithId(id: Long): LiveData<NetworkTransactionUIHelper>? {
        return transactionDao?.getTransactionsWithId(id)?.let {
            Transformations.map(it, NETWORK_TRANSACTION_UI_HELPER_FUNCTION)
        }
    }

}