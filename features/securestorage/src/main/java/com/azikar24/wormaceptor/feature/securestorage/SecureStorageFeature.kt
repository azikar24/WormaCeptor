package com.azikar24.wormaceptor.feature.securestorage

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.SecureStorageEngine
import com.azikar24.wormaceptor.feature.securestorage.ui.SecureStorageScreen
import com.azikar24.wormaceptor.feature.securestorage.vm.SecureStorageViewModel

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
     * Creates a SecureStorageEngine instance.
     * Use this in your dependency injection setup or as a singleton.
     */
    fun createEngine(context: Context): SecureStorageEngine {
        return SecureStorageEngine(context.applicationContext)
    }

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
 *
 * @param context Android context for accessing storage
 * @param modifier Modifier for the root layout
 * @param onNavigateBack Optional callback for back navigation
 */
@Composable
fun SecureStorageViewer(context: Context, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine = remember { SecureStorageFeature.createEngine(context) }
    val factory = remember { SecureStorageFeature.createViewModelFactory(engine) }
    val viewModel: SecureStorageViewModel = viewModel(factory = factory)

    // Collect state
    val entries by viewModel.filteredEntries.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedType by viewModel.selectedType.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedEntry by viewModel.selectedEntry.collectAsState()
    val keystoreAccessible by viewModel.keystoreAccessible.collectAsState()
    val encryptedPrefsAccessible by viewModel.encryptedPrefsAccessible.collectAsState()
    val lastRefreshTime by viewModel.lastRefreshTime.collectAsState()

    SecureStorageScreen(
        entries = entries,
        summary = summary,
        isLoading = isLoading,
        error = error,
        selectedType = selectedType,
        searchQuery = searchQuery,
        selectedEntry = selectedEntry,
        keystoreAccessible = keystoreAccessible,
        encryptedPrefsAccessible = encryptedPrefsAccessible,
        lastRefreshTime = lastRefreshTime,
        onTypeSelected = viewModel::setSelectedType,
        onSearchQueryChanged = viewModel::setSearchQuery,
        onEntrySelected = viewModel::selectEntry,
        onDismissDetail = viewModel::dismissDetail,
        onRefresh = viewModel::refresh,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
