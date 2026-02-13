package com.azikar24.wormaceptor.feature.location

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.feature.location.ui.LocationScreen
import com.azikar24.wormaceptor.feature.location.vm.LocationViewModel
import org.osmdroid.util.GeoPoint

/**
 * Entry point for the Location Simulation feature.
 * Provides factory methods and the main composable.
 */
object LocationFeature {

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
fun LocationSimulator(
    engine: LocationSimulatorEngine,
    repository: LocationSimulatorRepository,
    context: Context,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
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
    val realDeviceLocation by viewModel.realDeviceLocation.collectAsState()

    // Observe lifecycle to refresh mock location availability and manage location updates
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.refreshMockLocationAvailability()
                    viewModel.startRealLocationUpdates()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.stopRealLocationUpdates()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopRealLocationUpdates()
        }
    }

    // Convert real device location to GeoPoint for the map
    val realGeoPoint = realDeviceLocation?.let {
        GeoPoint(it.latitude, it.longitude)
    }

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
        realDeviceLocation = realGeoPoint,
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
        onMapTap = { geoPoint ->
            viewModel.setMockLocationFromCoordinates(geoPoint.latitude, geoPoint.longitude)
        },
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
