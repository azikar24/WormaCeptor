package com.azikar24.wormaceptor.core.engine

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock
import com.azikar24.wormaceptor.domain.entities.MockLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Engine that manages mock location simulation using Android's mock location provider API.
 *
 * Requirements:
 * - The app must be set as the mock location app in Developer Options
 * - Requires ACCESS_FINE_LOCATION permission
 * - User must enable "Allow mock locations" in developer settings
 *
 * This engine sets mock locations for both GPS_PROVIDER and NETWORK_PROVIDER
 * to ensure consistent behavior across apps using different location providers.
 *
 * The engine continuously pushes the mock location at regular intervals to prevent
 * Android from resetting to real location.
 */
class LocationSimulatorEngine(private val context: Context) {

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var updateJob: Job? = null

    private val _currentMockLocation = MutableStateFlow<MockLocation?>(null)

    /** The currently active mock location, or null if not mocking. */
    val currentMockLocation: StateFlow<MockLocation?> = _currentMockLocation.asStateFlow()

    private val _isEnabled = MutableStateFlow(false)

    /** Whether mock location simulation is currently active. */
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)

    /** The most recent error message from a failed mock location operation. */
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private var isProviderAdded = false

    /**
     * Sets a mock location for both GPS and Network providers.
     * Starts continuous location updates to maintain the mock.
     *
     * @param mockLocation The mock location to set
     * @return true if successful, false if mock locations are not enabled or permission denied
     */
    @SuppressLint("MissingPermission")
    @Synchronized
    fun setLocation(mockLocation: MockLocation): Boolean {
        if (!mockLocation.isValid()) {
            _lastError.value = "Invalid location coordinates"
            return false
        }

        return try {
            // Ensure test providers are added
            if (!isProviderAdded) {
                addTestProviders()
            }

            // Push the location once immediately
            pushLocation(mockLocation)

            _currentMockLocation.value = mockLocation
            _isEnabled.value = true
            _lastError.value = null

            // Start continuous updates
            startContinuousUpdates()

            true
        } catch (_: SecurityException) {
            // Reset state if we failed
            stopContinuousUpdates()
            isProviderAdded = false
            _isEnabled.value = false
            _lastError.value = "Mock locations not enabled. Enable in Developer Options and select this app as mock location app."
            false
        } catch (e: IllegalArgumentException) {
            // Try to recover by re-adding providers
            stopContinuousUpdates()
            isProviderAdded = false
            _lastError.value = "Failed to set mock location: ${e.message}"
            false
        } catch (e: Exception) {
            _lastError.value = "Unexpected error: ${e.message}"
            false
        }
    }

    /**
     * Pushes the location to both providers without managing state.
     */
    @SuppressLint("MissingPermission")
    private fun pushLocation(mockLocation: MockLocation) {
        // Create Android Location from MockLocation with fresh timestamps
        val gpsLocation = createAndroidLocation(mockLocation, LocationManager.GPS_PROVIDER)
        val networkLocation = createAndroidLocation(mockLocation, LocationManager.NETWORK_PROVIDER)

        // Set mock location for GPS provider
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, gpsLocation)

        // Set mock location for Network provider
        locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, networkLocation)
    }

    /**
     * Starts continuous location updates to maintain the mock location.
     */
    private fun startContinuousUpdates() {
        // Cancel any existing job
        updateJob?.cancel()

        updateJob = scope.launch {
            while (isActive && _isEnabled.value) {
                val location = _currentMockLocation.value
                if (location != null && isProviderAdded) {
                    try {
                        pushLocation(location)
                    } catch (e: Exception) {
                        // If pushing fails, try to recover
                        if (e is SecurityException) {
                            // Mock locations were disabled, stop
                            clearMockLocation()
                            break
                        }
                    }
                }
                // Push every 500ms to ensure stability
                delay(LOCATION_UPDATE_INTERVAL_MS)
            }
        }
    }

    /**
     * Stops continuous location updates.
     */
    private fun stopContinuousUpdates() {
        updateJob?.cancel()
        updateJob = null
    }

    /**
     * Clears the mock location and restores normal location behavior.
     */
    @SuppressLint("MissingPermission")
    @Synchronized
    fun clearMockLocation() {
        // Stop continuous updates first
        stopContinuousUpdates()

        try {
            if (isProviderAdded) {
                removeTestProviders()
            }
        } catch (_: Exception) {
            // Log but don't fail - we still want to clear the state
        } finally {
            // Always reset state even if provider removal fails
            _currentMockLocation.value = null
            _isEnabled.value = false
            _lastError.value = null
            isProviderAdded = false
        }
    }

    /**
     * Toggles mock location on or off with the given location.
     */
    @Synchronized
    fun toggle(mockLocation: MockLocation?): Boolean {
        return if (_isEnabled.value) {
            clearMockLocation()
            true
        } else if (mockLocation != null) {
            setLocation(mockLocation)
        } else {
            false
        }
    }

    /**
     * Checks if mock locations are available on this device.
     * This checks if the app can be set as a mock location provider.
     */
    fun isMockLocationAvailable(): Boolean {
        return try {
            // Try to add a test provider to check if mock locations are enabled
            locationManager.addTestProvider(
                TEST_PROVIDER_CHECK,
                false, // requiresNetwork
                false, // requiresSatellite
                false, // requiresCell
                false, // hasMonetaryCost
                true, // supportsAltitude
                true, // supportsSpeed
                true, // supportsBearing
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ProviderProperties.POWER_USAGE_LOW
                } else {
                    @Suppress("DEPRECATION")
                    android.location.Criteria.POWER_LOW
                },
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ProviderProperties.ACCURACY_FINE
                } else {
                    @Suppress("DEPRECATION")
                    android.location.Criteria.ACCURACY_FINE
                },
            )
            locationManager.removeTestProvider(TEST_PROVIDER_CHECK)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            // Provider might already exist, which is fine
            try {
                locationManager.removeTestProvider(TEST_PROVIDER_CHECK)
            } catch (_: Exception) { }
            true
        }
    }

    @SuppressLint("MissingPermission")
    private fun addTestProviders() {
        // Add GPS test provider
        addTestProvider(LocationManager.GPS_PROVIDER)
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)

        // Add Network test provider
        addTestProvider(LocationManager.NETWORK_PROVIDER)
        locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true)

        isProviderAdded = true
    }

    private fun addTestProvider(provider: String) {
        // First try to remove any existing test provider
        try {
            locationManager.removeTestProvider(provider)
        } catch (_: Exception) {
            // Provider didn't exist, which is fine
        }

        // Now add the test provider
        locationManager.addTestProvider(
            provider,
            false, // requiresNetwork
            false, // requiresSatellite
            false, // requiresCell
            false, // hasMonetaryCost
            true, // supportsAltitude
            true, // supportsSpeed
            true, // supportsBearing
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ProviderProperties.POWER_USAGE_LOW
            } else {
                @Suppress("DEPRECATION")
                android.location.Criteria.POWER_LOW
            },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ProviderProperties.ACCURACY_FINE
            } else {
                @Suppress("DEPRECATION")
                android.location.Criteria.ACCURACY_FINE
            },
        )
    }

    @SuppressLint("MissingPermission")
    private fun removeTestProviders() {
        try {
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) { }

        try {
            locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
            locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) { }

        isProviderAdded = false
    }

    private fun createAndroidLocation(
        mockLocation: MockLocation,
        provider: String,
    ): Location {
        return Location(provider).apply {
            latitude = mockLocation.latitude
            longitude = mockLocation.longitude
            altitude = mockLocation.altitude
            accuracy = mockLocation.accuracy
            speed = mockLocation.speed
            bearing = mockLocation.bearing
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                bearingAccuracyDegrees = 0.1f
                verticalAccuracyMeters = mockLocation.accuracy
                speedAccuracyMetersPerSecond = 0.1f
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                isMock = true
            }
        }
    }

    /** Provider names and update interval constants. */
    companion object {
        private const val TEST_PROVIDER_CHECK = "wormaceptor_test_check"
        private const val LOCATION_UPDATE_INTERVAL_MS = 500L
    }
}
