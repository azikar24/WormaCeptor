package com.azikar24.wormaceptor.feature.preferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.azikar24.wormaceptor.domain.contracts.PreferencesRepository
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesDataSource
import com.azikar24.wormaceptor.feature.preferences.data.PreferencesRepositoryImpl
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewModel

/**
 * Entry point for the SharedPreferences Inspector feature.
 * Provides factory methods for creating repositories and ViewModel factories.
 *
 * Navigation is handled by [com.azikar24.wormaceptor.feature.preferences.navigation.preferencesGraph].
 */
object PreferencesFeature {

    /**
     * Creates a PreferencesRepository instance for the given context.
     * Use this in your dependency injection setup.
     */
    fun createRepository(context: Context): PreferencesRepository {
        val dataSource = PreferencesDataSource(context.applicationContext)
        return PreferencesRepositoryImpl(dataSource)
    }

    /**
     * Creates a PreferencesViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(repository: PreferencesRepository): PreferencesViewModelFactory {
        return PreferencesViewModelFactory(repository)
    }
}

/**
 * Factory for creating PreferencesViewModel instances.
 */
class PreferencesViewModelFactory(
    private val repository: PreferencesRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {
            return PreferencesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
