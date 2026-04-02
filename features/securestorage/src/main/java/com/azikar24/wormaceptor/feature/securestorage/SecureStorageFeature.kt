package com.azikar24.wormaceptor.feature.securestorage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.SecureStorageEngine
import com.azikar24.wormaceptor.feature.securestorage.ui.SecureStorageScreen
import com.azikar24.wormaceptor.feature.securestorage.vm.SecureStorageViewEvent
import com.azikar24.wormaceptor.feature.securestorage.vm.SecureStorageViewModel
import org.koin.compose.koinInject

/**
 * Entry point for the Secure Storage Viewer feature.
 * Provides factory methods and composable entry point.
 *
 * SECURITY WARNING: This feature is intended for debugging purposes only.
 * It should never be included in production builds as it exposes
 * information about secure storage.
 */
object SecureStorageFeature {

    /**
     * Creates a SecureStorageViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(engine: SecureStorageEngine): SecureStorageViewModelFactory {
        return SecureStorageViewModelFactory(engine)
    }
}

/**
 * Factory for creating SecureStorageViewModel instances.
 */
class SecureStorageViewModelFactory(
    private val engine: SecureStorageEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SecureStorageViewModel::class.java)) {
            return SecureStorageViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Secure Storage Viewer feature.
 * Displays secure storage entries with filtering and search capabilities.
 */
@Composable
fun SecureStorageViewer(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val engine: SecureStorageEngine = koinInject()
    val factory = remember { SecureStorageFeature.createViewModelFactory(engine) }
    val viewModel: SecureStorageViewModel = viewModel(factory = factory)

    BaseScreen(viewModel) { state, onEvent ->
        SecureStorageScreen(
            entries = state.filteredEntries,
            summary = state.summary,
            isLoading = state.isLoading,
            error = state.error,
            selectedType = state.selectedType,
            searchQuery = state.searchQuery,
            selectedEntry = state.selectedEntry,
            keystoreAccessible = state.keystoreAccessible,
            encryptedPrefsAccessible = state.encryptedPrefsAccessible,
            lastRefreshTime = state.lastRefreshTime,
            onTypeSelected = { onEvent(SecureStorageViewEvent.SelectType(it)) },
            onSearchQueryChanged = { onEvent(SecureStorageViewEvent.UpdateSearchQuery(it)) },
            onEntrySelected = { onEvent(SecureStorageViewEvent.SelectEntry(it)) },
            onDismissDetail = { onEvent(SecureStorageViewEvent.DismissDetail) },
            onRefresh = { onEvent(SecureStorageViewEvent.Refresh) },
            onBack = onNavigateBack,
            modifier = modifier,
        )
    }
}
