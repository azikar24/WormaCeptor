package com.azikar24.wormaceptor.feature.location.vm

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.domain.contracts.LocationSimulatorRepository
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * ViewModel for the Location Simulation feature.
 * Manages mock location state, presets, and coordinate input.
 */
@Suppress("TooManyFunctions")
class LocationViewModel(
    private val repository: LocationSimulatorRepository,
    private val engine: LocationSimulatorEngine,
    context: Context,
) : BaseViewModel<LocationViewState, LocationViewEffect, LocationViewEvent>(
    LocationViewState(isMockLocationAvailable = engine.isMockLocationAvailable()),
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Real device location tracking (kept separate as it's driven by platform callbacks)
    private val _realDeviceLocation = MutableStateFlow<Location?>(null)

    /** Real device location from GPS. */
    val realDeviceLocation: StateFlow<Location?> = _realDeviceLocation.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var isTrackingLocation = false

    // Engine-driven reactive flows kept as separate StateFlows

    /** Current mock location combining engine (active) with repository (persisted). */
    val currentMockLocation: StateFlow<MockLocation?> = combine(
        repository.getCurrentMockLocation(),
        engine.currentMockLocation,
    ) { persisted, active ->
        active ?: persisted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), null)

    /** Whether mock location is currently enabled. */
    val isMockEnabled: StateFlow<Boolean> = engine.isEnabled

    /** Engine error state. */
    val engineError: StateFlow<String?> = engine.lastError

    /** All presets filtered by search query. */
    val presets: StateFlow<ImmutableList<LocationPreset>> = combine(
        repository.getPresets(),
        uiState,
    ) { presets, state ->
        val query = state.searchQuery
        presets.filter { preset ->
            query.isBlank() ||
                preset.name.contains(query, ignoreCase = true) ||
                preset.location.name?.contains(query, ignoreCase = true) == true
        }.toImmutableList()
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), persistentListOf())

    /** Validation state for coordinate input. */
    val isInputValid: StateFlow<Boolean> = uiState.let { stateFlow ->
        combine(stateFlow) { states ->
            val state = states[0]
            val latitude = state.latitudeInput.toDoubleOrNull()
            val longitude = state.longitudeInput.toDoubleOrNull()
            latitude != null && longitude != null &&
                latitude in -LAT_RANGE..LAT_RANGE && longitude in -LON_RANGE..LON_RANGE
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT), false)
    }

    override fun handleEvent(event: LocationViewEvent) {
        when (event) {
            is LocationViewEvent.LatitudeChanged -> updateState { copy(latitudeInput = event.value) }
            is LocationViewEvent.LongitudeChanged -> updateState { copy(longitudeInput = event.value) }
            is LocationViewEvent.SearchQueryChanged -> updateState { copy(searchQuery = event.query) }
            LocationViewEvent.SetMockLocationFromInput -> handleSetMockLocationFromInput()
            is LocationViewEvent.SetMockLocationFromPreset -> handleSetMockLocationFromPreset(event.preset)
            is LocationViewEvent.SetMockLocation -> handleSetMockLocation(event.location)
            LocationViewEvent.ClearMockLocation -> handleClearMockLocation()
            LocationViewEvent.ToggleMockLocation -> handleToggleMockLocation()
            LocationViewEvent.SetToCurrentRealLocation -> handleSetToCurrentRealLocation()
            is LocationViewEvent.SaveCurrentAsPreset -> handleSaveCurrentAsPreset(event.name)
            is LocationViewEvent.DeletePreset -> handleDeletePreset(event.presetId)
            is LocationViewEvent.MapTapped -> handleMapTapped(event.latitude, event.longitude)
            LocationViewEvent.RefreshMockLocationAvailability -> {
                updateState { copy(isMockLocationAvailable = engine.isMockLocationAvailable()) }
            }
            LocationViewEvent.StartRealLocationUpdates -> startRealLocationUpdates()
            LocationViewEvent.StopRealLocationUpdates -> stopRealLocationUpdates()
        }
    }

    private fun handleSetMockLocationFromInput() {
        val state = uiState.value
        if (isInputValid.value) {
            val latitude = state.latitudeInput.toDouble()
            val longitude = state.longitudeInput.toDouble()
            handleSetMockLocation(MockLocation.from(latitude, longitude))
            return
        }

        val lastUsed = currentMockLocation.value
        if (lastUsed != null) {
            updateState {
                copy(
                    latitudeInput = "%.6f".format(lastUsed.latitude),
                    longitudeInput = "%.6f".format(lastUsed.longitude),
                )
            }
            handleSetMockLocation(lastUsed)
            return
        }

        val firstPreset = presets.value.firstOrNull()
        if (firstPreset != null) {
            updateState {
                copy(
                    latitudeInput = firstPreset.location.latitude.toString(),
                    longitudeInput = firstPreset.location.longitude.toString(),
                )
            }
            handleSetMockLocation(firstPreset.location)
            return
        }

        emitEffect(LocationViewEffect.ShowError("Please enter valid coordinates"))
    }

    private fun handleSetMockLocationFromPreset(preset: LocationPreset) {
        handleSetMockLocation(preset.location)
        updateState {
            copy(
                latitudeInput = preset.location.latitude.toString(),
                longitudeInput = preset.location.longitude.toString(),
            )
        }
    }

    private fun handleSetMockLocation(location: MockLocation) {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                val success = engine.setLocation(location)
                if (success) {
                    repository.setMockLocation(location)
                    updateState { copy(isLoading = false) }
                    emitEffect(LocationViewEffect.ShowSuccess("Mock location set successfully"))
                } else {
                    updateState { copy(isLoading = false) }
                    emitEffect(
                        LocationViewEffect.ShowError(
                            engine.lastError.value ?: "Failed to set mock location",
                        ),
                    )
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to set mock location", e)
                updateState { copy(isLoading = false) }
                emitEffect(LocationViewEffect.ShowError("Failed to set mock location: ${e.message}"))
            }
        }
    }

    private fun handleClearMockLocation() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                engine.clearMockLocation()
                repository.setMockLocation(null)
                updateState { copy(isLoading = false) }
                emitEffect(LocationViewEffect.ShowSuccess("Mock location cleared"))
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to clear mock location", e)
                updateState { copy(isLoading = false) }
                emitEffect(LocationViewEffect.ShowError("Failed to clear mock location: ${e.message}"))
            }
        }
    }

    private fun handleToggleMockLocation() {
        if (isMockEnabled.value) {
            handleClearMockLocation()
        } else {
            handleSetMockLocationFromInput()
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleSetToCurrentRealLocation() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                val cancellationToken = CancellationTokenSource()
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token,
                ).await()

                if (location != null) {
                    updateState {
                        copy(
                            isLoading = false,
                            latitudeInput = "%.6f".format(location.latitude),
                            longitudeInput = "%.6f".format(location.longitude),
                        )
                    }
                    emitEffect(LocationViewEffect.ShowSuccess("Current location retrieved"))
                } else {
                    updateState { copy(isLoading = false) }
                    emitEffect(
                        LocationViewEffect.ShowError(
                            "Could not get current location. Make sure location is enabled.",
                        ),
                    )
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Location permission not granted", e)
                updateState { copy(isLoading = false) }
                emitEffect(LocationViewEffect.ShowError("Location permission not granted"))
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to get location", e)
                updateState { copy(isLoading = false) }
                emitEffect(LocationViewEffect.ShowError("Failed to get location: ${e.message}"))
            }
        }
    }

    private fun handleSaveCurrentAsPreset(name: String) {
        val state = uiState.value
        val latitude = state.latitudeInput.toDoubleOrNull()
        val longitude = state.longitudeInput.toDoubleOrNull()

        if (latitude == null || longitude == null) {
            emitEffect(LocationViewEffect.ShowError("Please enter valid coordinates first"))
            return
        }

        if (name.isBlank()) {
            emitEffect(LocationViewEffect.ShowError("Please enter a preset name"))
            return
        }

        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            try {
                val preset = LocationPreset(
                    id = "user_${UUID.randomUUID()}",
                    name = name.trim(),
                    location = MockLocation.from(latitude, longitude, name.trim()),
                    isBuiltIn = false,
                )
                repository.savePreset(preset)
                updateState { copy(isLoading = false) }
                emitEffect(LocationViewEffect.ShowSuccess("Preset saved: ${preset.name}"))
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to save preset", e)
                updateState { copy(isLoading = false) }
                emitEffect(LocationViewEffect.ShowError("Failed to save preset: ${e.message}"))
            }
        }
    }

    private fun handleDeletePreset(presetId: String) {
        viewModelScope.launch {
            try {
                repository.deletePreset(presetId)
                emitEffect(LocationViewEffect.ShowSuccess("Preset deleted"))
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to delete preset", e)
                emitEffect(LocationViewEffect.ShowError("Failed to delete preset: ${e.message}"))
            }
        }
    }

    private fun handleMapTapped(latitude: Double, longitude: Double) {
        updateState {
            copy(
                latitudeInput = "%.6f".format(latitude),
                longitudeInput = "%.6f".format(longitude),
            )
        }
    }

    /**
     * Starts continuous real location updates for map display.
     * Call this when the map becomes visible.
     */
    @SuppressLint("MissingPermission")
    private fun startRealLocationUpdates() {
        if (isTrackingLocation) return
        isTrackingLocation = true

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL,
        ).setMinUpdateIntervalMillis(LOCATION_MIN_UPDATE_INTERVAL)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    _realDeviceLocation.value = location
                }
            }
        }
        locationCallback = callback

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                callback,
                Looper.getMainLooper(),
            )
        } catch (e: SecurityException) {
            emitEffect(LocationViewEffect.ShowError("Location permission not granted"))
            isTrackingLocation = false
        }
    }

    /**
     * Stops continuous real location updates.
     * Call this when the map is no longer visible.
     */
    private fun stopRealLocationUpdates() {
        if (!isTrackingLocation) return
        isTrackingLocation = false

        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    override fun onCleared() {
        super.onCleared()
        stopRealLocationUpdates()
    }

    private companion object {
        private const val TAG = "LocationViewModel"
        private const val SUBSCRIPTION_TIMEOUT = 5000L
        private const val LOCATION_UPDATE_INTERVAL = 5000L
        private const val LOCATION_MIN_UPDATE_INTERVAL = 2000L
        private const val LAT_RANGE = 90.0
        private const val LON_RANGE = 180.0
    }
}
