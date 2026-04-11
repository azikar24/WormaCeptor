package com.azikar24.wormaceptor.feature.crypto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewModel

class CryptoViewModelFactory(
    private val engine: CryptoEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
            return CryptoViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
