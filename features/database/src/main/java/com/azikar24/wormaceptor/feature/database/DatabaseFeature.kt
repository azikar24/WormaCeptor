package com.azikar24.wormaceptor.feature.database

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.domain.contracts.DatabaseRepository
import com.azikar24.wormaceptor.feature.database.data.DatabaseDataSource
import com.azikar24.wormaceptor.feature.database.data.DatabaseRepositoryImpl
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewModel

/**
 * Entry point for the SQLite Database Browser feature.
 * Provides factory methods for creating repositories and ViewModel factories.
 *
 * Navigation is handled by [com.azikar24.wormaceptor.feature.database.navigation.databaseGraph].
 */
object DatabaseFeature {

    /**
     * Creates a DatabaseRepository instance for the given context.
     * Use this in your dependency injection setup.
     */
    fun createRepository(context: Context): DatabaseRepository {
        val dataSource = DatabaseDataSource(context.applicationContext)
        return DatabaseRepositoryImpl(dataSource)
    }

    /**
     * Creates a DatabaseViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(repository: DatabaseRepository): DatabaseViewModelFactory {
        return DatabaseViewModelFactory(repository)
    }
}

/**
 * Factory for creating DatabaseViewModel instances.
 */
class DatabaseViewModelFactory(
    private val repository: DatabaseRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DatabaseViewModel::class.java)) {
            return DatabaseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
