/*
 * Copyright AziKar24 23/12/2025.
 */

package com.azikar24.wormaceptor.internal.di

import com.azikar24.wormaceptor.internal.ui.ToolbarViewModel
import com.azikar24.wormaceptor.internal.ui.features.crashes.CrashTransactionViewModel
import com.azikar24.wormaceptor.internal.ui.features.network.NetworkTransactionViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val wormaCeptorModule = module {
    single { ToolbarViewModel() }
    viewModel { NetworkTransactionViewModel(getOrNull()) }
    viewModel { CrashTransactionViewModel(getOrNull()) }
}
