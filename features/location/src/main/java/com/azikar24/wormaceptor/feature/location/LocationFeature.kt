package com.azikar24.wormaceptor.feature.location

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.feature.location.ui.LocationScreen
import com.azikar24.wormaceptor.feature.location.vm.LocationViewEffect
import com.azikar24.wormaceptor.feature.location.vm.LocationViewEvent
import com.azikar24.wormaceptor.feature.location.vm.LocationViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
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
fun LocationSimulator(modifier: Modifier = Modifier, onNavigateBack: (() -> Unit)? = null) {
    val engine: LocationSimulatorEngine = koinInject()
    val repository: LocationSimulatorRepository = koinInject()
    val context = LocalContext.current
    val factory = remember { LocationFeature.createViewModelFactory(repository, engine, context) }
    val viewModel: LocationViewModel = viewModel(factory = factory)

    val snackBarState = remember { androidx.compose.material3.SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe lifecycle to refresh mock location availability and manage location updates
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.sendEvent(LocationViewEvent.RefreshMockLocationAvailability)
                    viewModel.sendEvent(LocationViewEvent.StartRealLocationUpdates)
                }
                Lifecycle.Event.ON_PAUSE -> {
                    viewModel.sendEvent(LocationViewEvent.StopRealLocationUpdates)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.sendEvent(LocationViewEvent.StopRealLocationUpdates)
        }
    }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is LocationViewEffect.ShowError -> scope.launch { snackBarState.showSnackbar(effect.message) }
                is LocationViewEffect.ShowSuccess -> scope.launch { snackBarState.showSnackbar(effect.message) }
            }
        },
    ) { state, onEvent ->
        val presets by viewModel.presets.collectAsState()
        val currentMockLocation by viewModel.currentMockLocation.collectAsState()
        val isMockEnabled by viewModel.isMockEnabled.collectAsState()
        val isInputValid by viewModel.isInputValid.collectAsState()
        val realDeviceLocation by viewModel.realDeviceLocation.collectAsState()

        // Convert real device location to GeoPoint for the map
        val realGeoPoint = realDeviceLocation?.let {
            GeoPoint(it.latitude, it.longitude)
        }

        LocationScreen(
            latitudeInput = state.latitudeInput,
            longitudeInput = state.longitudeInput,
            searchQuery = state.searchQuery,
            presets = presets,
            currentMockLocation = currentMockLocation,
            isMockEnabled = isMockEnabled,
            isMockLocationAvailable = state.isMockLocationAvailable,
            isInputValid = isInputValid,
            isLoading = state.isLoading,
            errorMessage = null,
            successMessage = null,
            realDeviceLocation = realGeoPoint,
            onLatitudeChanged = { onEvent(LocationViewEvent.LatitudeChanged(it)) },
            onLongitudeChanged = { onEvent(LocationViewEvent.LongitudeChanged(it)) },
            onSearchQueryChanged = { onEvent(LocationViewEvent.SearchQueryChanged(it)) },
            onSetMockLocation = { onEvent(LocationViewEvent.SetMockLocationFromInput) },
            onClearMockLocation = { onEvent(LocationViewEvent.ClearMockLocation) },
            onSetToCurrentLocation = { onEvent(LocationViewEvent.SetToCurrentRealLocation) },
            onPresetClick = { onEvent(LocationViewEvent.SetMockLocationFromPreset(it)) },
            onDeletePreset = { onEvent(LocationViewEvent.DeletePreset(it)) },
            onSavePreset = { onEvent(LocationViewEvent.SaveCurrentAsPreset(it)) },
            onClearError = {},
            onClearSuccessMessage = {},
            onMapTap = { geoPoint ->
                onEvent(LocationViewEvent.MapTapped(geoPoint.latitude, geoPoint.longitude))
            },
            onBack = onNavigateBack,
            modifier = modifier,
            snackBarHostState = snackBarState,
        )
    }
}
