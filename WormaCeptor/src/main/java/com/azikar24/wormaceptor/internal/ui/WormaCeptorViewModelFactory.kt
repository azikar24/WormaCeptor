/*
 * Copyright AziKar24 23/12/2025.
 */

package com.azikar24.wormaceptor.internal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.ui.features.crashes.CrashTransactionViewModel
import com.azikar24.wormaceptor.internal.ui.features.network.NetworkTransactionViewModel

@Suppress("UNCHECKED_CAST")
class WormaCeptorViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val transactionDao = WormaCeptor.storage?.transactionDao
        return when {
            modelClass.isAssignableFrom(NetworkTransactionViewModel::class.java) -> {
                NetworkTransactionViewModel(transactionDao) as T
            }
            modelClass.isAssignableFrom(CrashTransactionViewModel::class.java) -> {
                CrashTransactionViewModel(transactionDao) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}