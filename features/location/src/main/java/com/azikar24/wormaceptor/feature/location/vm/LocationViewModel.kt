package com.azikar24.wormaceptor.feature.location.vm

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class LocationViewModel(
    private val repository: LocationSimulatorRepository,
    private val engine: LocationSimulatorEngine,
    context: Context,
) : ViewModel() {

    private companion object {
        private const val TAG = "LocationViewModel"
    }

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Real device location tracking
    private val _realDeviceLocation = MutableStateFlow<Location?>(null)
    val realDeviceLocation: StateFlow<Location?> = _realDeviceLocation.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var isTrackingLocation = false

    // Coordinate input fields
    private val _latitudeInput = MutableStateFlow("")
    val latitudeInput: StateFlow<String> = _latitudeInput.asStateFlow()

    private val _longitudeInput = MutableStateFlow("")
    val longitudeInput: StateFlow<String> = _longitudeInput.asStateFlow()

    // Search query for presets
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Success message for feedback
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Mock location availability
    private val _isMockLocationAvailable = MutableStateFlow(engine.isMockLocationAvailable())
    val isMockLocationAvailable: StateFlow<Boolean> = _isMockLocationAvailable.asStateFlow()

    // Current mock location state
    val currentMockLocation: StateFlow<MockLocation?> = repository.getCurrentMockLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Mock location enabled state
    val isMockEnabled: StateFlow<Boolean> = engine.isEnabled

    // Engine error state
    val engineError: StateFlow<String?> = engine.lastError

    // All presets (filtered by search)
    val presets: StateFlow<ImmutableList<LocationPreset>> = combine(
        repository.getPresets(),
        _searchQuery,
    ) { presets, query ->
        presets.filter { preset ->
            query.isBlank() ||
                preset.name.contains(query, ignoreCase = true) ||
                preset.location.name?.contains(query, ignoreCase = true) == true
        }.toImmutableList()
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    // Validation state for input
    val isInputValid: StateFlow<Boolean> = combine(
        _latitudeInput,
        _longitudeInput,
    ) { lat, lon ->
        val latitude = lat.toDoubleOrNull()
        val longitude = lon.toDoubleOrNull()
        latitude != null && longitude != null &&
            latitude in -90.0..90.0 && longitude in -180.0..180.0
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun onLatitudeChanged(value: String) {
        _latitudeInput.value = value
        _errorMessage.value = null
    }

    fun onLongitudeChanged(value: String) {
        _longitudeInput.value = value
        _errorMessage.value = null
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Sets a mock location from the current input fields.
     */
    fun setMockLocationFromInput() {
        val latitude = _latitudeInput.value.toDoubleOrNull()
        val longitude = _longitudeInput.value.toDoubleOrNull()

        if (latitude == null || longitude == null) {
            _errorMessage.value = "Please enter valid coordinates"
            return
        }

        val mockLocation = MockLocation.from(latitude, longitude)
        setMockLocation(mockLocation)
    }

    /**
     * Sets a mock location from a preset.
     */
    fun setMockLocationFromPreset(preset: LocationPreset) {
        setMockLocation(preset.location)
        // Update input fields to reflect the preset
        _latitudeInput.value = preset.location.latitude.toString()
        _longitudeInput.value = preset.location.longitude.toString()
    }

    /**
     * Sets the mock location.
     */
    fun setMockLocation(location: MockLocation) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.setMockLocation(location)
                if (engine.lastError.value == null) {
                    _successMessage.value = "Mock location set successfully"
                } else {
                    _errorMessage.value = engine.lastError.value
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to set mock location", e)
                _errorMessage.value = "Failed to set mock location: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Clears the current mock location.
     */
    fun clearMockLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.setMockLocation(null)
                _successMessage.value = "Mock location cleared"
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to clear mock location", e)
                _errorMessage.value = "Failed to clear mock location: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggles mock location on/off.
     */
    fun toggleMockLocation() {
        if (isMockEnabled.value) {
            clearMockLocation()
        } else {
            setMockLocationFromInput()
        }
    }

    /**
     * Gets the current real device location and sets it in the input fields.
     */
    @SuppressLint("MissingPermission")
    fun setToCurrentRealLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val cancellationToken = CancellationTokenSource()
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationToken.token,
                ).await()

                if (location != null) {
                    _latitudeInput.value = "%.6f".format(location.latitude)
                    _longitudeInput.value = "%.6f".format(location.longitude)
                    _successMessage.value = "Current location retrieved"
                } else {
                    _errorMessage.value = "Could not get current location. Make sure location is enabled."
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Location permission not granted", e)
                _errorMessage.value = "Location permission not granted"
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to get location", e)
                _errorMessage.value = "Failed to get location: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Saves the current input as a new preset.
     */
    fun saveCurrentAsPreset(name: String) {
        val latitude = _latitudeInput.value.toDoubleOrNull()
        val longitude = _longitudeInput.value.toDoubleOrNull()

        if (latitude == null || longitude == null) {
            _errorMessage.value = "Please enter valid coordinates first"
            return
        }

        if (name.isBlank()) {
            _errorMessage.value = "Please enter a preset name"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val preset = LocationPreset(
                    id = "user_${UUID.randomUUID()}",
                    name = name.trim(),
                    location = MockLocation.from(latitude, longitude, name.trim()),
                    isBuiltIn = false,
                )
                repository.savePreset(preset)
                _successMessage.value = "Preset saved: ${preset.name}"
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to save preset", e)
                _errorMessage.value = "Failed to save preset: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a user-created preset.
     */
    fun deletePreset(presetId: String) {
        viewModelScope.launch {
            try {
                repository.deletePreset(presetId)
                _successMessage.value = "Preset deleted"
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                Log.w(TAG, "Failed to delete preset", e)
                _errorMessage.value = "Failed to delete preset: ${e.message}"
            }
        }
    }

    /**
     * Clears the error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Clears the success message.
     */
    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    /**
     * Refreshes the mock location availability state.
     * Call this when returning from settings to check if mock locations were enabled.
     */
    fun refreshMockLocationAvailability() {
        _isMockLocationAvailable.value = engine.isMockLocationAvailable()
    }

    /**
     * Starts continuous real location updates for map display.
     * Call this when the map becomes visible.
     */
    @SuppressLint("MissingPermission")
    fun startRealLocationUpdates() {
        if (isTrackingLocation) return
        isTrackingLocation = true

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L, // Update every 5 seconds
        ).setMinUpdateIntervalMillis(2000L)
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
            _errorMessage.value = "Location permission not granted"
            isTrackingLocation = false
        }
    }

    /**
     * Stops continuous real location updates.
     * Call this when the map is no longer visible.
     */
    fun stopRealLocationUpdates() {
        if (!isTrackingLocation) return
        isTrackingLocation = false

        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
        locationCallback = null
    }

    /**
     * Updates the mock location from map tap coordinates.
     */
    fun setMockLocationFromCoordinates(latitude: Double, longitude: Double) {
        _latitudeInput.value = "%.6f".format(latitude)
        _longitudeInput.value = "%.6f".format(longitude)
    }

    override fun onCleared() {
        super.onCleared()
        stopRealLocationUpdates()
    }
}
