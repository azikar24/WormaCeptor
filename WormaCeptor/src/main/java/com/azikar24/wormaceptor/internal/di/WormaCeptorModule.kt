/*
 * Copyright AziKar24 23/12/2025.
 */

package com.azikar24.wormaceptor.internal.di

import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.internal.data.TransactionDao
import com.azikar24.wormaceptor.internal.ui.ToolbarViewModel
import com.azikar24.wormaceptor.internal.ui.features.crashes.CrashTransactionViewModel
import com.azikar24.wormaceptor.internal.ui.features.network.NetworkTransactionViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val wormaCeptorModule = module {
    single<TransactionDao?> { WormaCeptor.storage?.transactionDao }
    
    single { ToolbarViewModel() }
    viewModel { NetworkTransactionViewModel(get()) }
    viewModel { CrashTransactionViewModel(get()) }
}