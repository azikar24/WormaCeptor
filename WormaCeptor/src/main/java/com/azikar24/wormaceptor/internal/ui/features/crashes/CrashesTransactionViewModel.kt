package com.azikar24.wormaceptor.internal.ui.features.crashes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.data.TransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CrashTransactionViewModel(private val transactionDao: TransactionDao?) : ViewModel() {
    private val config = PagingConfig(
        pageSize = 15,
        prefetchDistance = 10,
        enablePlaceholders = true,
        initialLoadSize = 30,
    )

    val pageEventFlow = MutableStateFlow<PagingData<CrashTransaction>>(PagingData.empty())

    fun fetchData(key: String? = null) {
        if (key?.trim()?.isEmpty() == true || key == null) {
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
        } else {
            transactionDao?.getAllCrashesWith(key, TransactionDao.SearchType.DEFAULT)?.let {
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
    }

    fun getCrashWithId(id: Long): Flow<CrashTransaction>? {
        return transactionDao?.getCrashWithId(id)?.asFlow()
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao?.clearAllCrashes()
        }
    }

    fun delete(vararg params: CrashTransaction) {
        viewModelScope.launch(Dispatchers.IO) {
            transactionDao?.deleteCrash(*params)
        }
    }
}
