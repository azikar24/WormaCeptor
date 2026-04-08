package com.azikar24.wormaceptor.feature.database

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.feature.database.data.DatabaseDataSource
import com.azikar24.wormaceptor.feature.database.data.DatabaseRepositoryImpl
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewModel

object DatabaseFeature {

    fun createRepository(context: Context): DatabaseRepository {
        val dataSource = DatabaseDataSource(context.applicationContext)
        return DatabaseRepositoryImpl(dataSource)
    }

    fun createViewModelFactory(
        repository: DatabaseRepository,
        application: Application,
    ): DatabaseViewModelFactory {
        return DatabaseViewModelFactory(repository, application)
    }
}

class DatabaseViewModelFactory(
    private val repository: DatabaseRepository,
    private val application: Application,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
            return DatabaseViewModel(repository, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
