/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.location

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.feature.location.data.LocationDataSource
import com.azikar24.wormaceptor.feature.location.data.LocationRepositoryImpl
import com.azikar24.wormaceptor.feature.location.ui.LocationScreen
import com.azikar24.wormaceptor.feature.location.vm.LocationViewModel

/**
 * Entry point for the Location Simulation feature.
 * Provides factory methods and the main composable.
 */
object LocationFeature {

    /**
     * Creates a LocationSimulatorEngine instance for the given context.
     * This engine handles the actual mock location setting via Android APIs.
     */
    fun createEngine(context: Context): LocationSimulatorEngine {
        return LocationSimulatorEngine(context.applicationContext)
    }

    /**
     * Creates a LocationSimulatorRepository instance.
     * The repository coordinates between persistence and the engine.
     */
    fun createRepository(context: Context, engine: LocationSimulatorEngine): LocationSimulatorRepository {
        val dataSource = LocationDataSource(context.applicationContext)
        return LocationRepositoryImpl(dataSource, engine)
    }

    /**
     * Creates a LocationViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(
        repository: LocationSimulatorRepository,
        engine: LocationSimulatorEngine,
        context: Context,
    ): LocationViewModelFactory {
        return LocationViewModelFactory(repository, engine, context)
    }
}

/**
 * Factory for creating LocationViewModel instances.
 */
class LocationViewModelFactory(
    private val repository: LocationSimulatorRepository,
    private val engine: LocationSimulatorEngine,
    private val context: Context,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            return LocationViewModel(repository, engine, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Location Simulation feature.
 * Call this from your navigation host with route "location".
 */
@Composable
fun LocationSimulator(context: Context, modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine = remember { LocationFeature.createEngine(context) }
    val repository = remember { LocationFeature.createRepository(context, engine) }
    val factory = remember { LocationFeature.createViewModelFactory(repository, engine, context) }
    val viewModel: LocationViewModel = viewModel(factory = factory)

    val latitudeInput by viewModel.latitudeInput.collectAsState()
    val longitudeInput by viewModel.longitudeInput.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val presets by viewModel.presets.collectAsState()
    val currentMockLocation by viewModel.currentMockLocation.collectAsState()
    val isMockEnabled by viewModel.isMockEnabled.collectAsState()
    val isMockLocationAvailable by viewModel.isMockLocationAvailable.collectAsState()
    val isInputValid by viewModel.isInputValid.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    LocationScreen(
        latitudeInput = latitudeInput,
        longitudeInput = longitudeInput,
        searchQuery = searchQuery,
        presets = presets,
        currentMockLocation = currentMockLocation,
        isMockEnabled = isMockEnabled,
        isMockLocationAvailable = isMockLocationAvailable,
        isInputValid = isInputValid,
        isLoading = isLoading,
        errorMessage = errorMessage,
        successMessage = successMessage,
        onLatitudeChanged = viewModel::onLatitudeChanged,
        onLongitudeChanged = viewModel::onLongitudeChanged,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onSetMockLocation = viewModel::setMockLocationFromInput,
        onClearMockLocation = viewModel::clearMockLocation,
        onSetToCurrentLocation = viewModel::setToCurrentRealLocation,
        onPresetClick = viewModel::setMockLocationFromPreset,
        onDeletePreset = viewModel::deletePreset,
        onSavePreset = viewModel::saveCurrentAsPreset,
        onClearError = viewModel::clearError,
        onClearSuccessMessage = viewModel::clearSuccessMessage,
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
