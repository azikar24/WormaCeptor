/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.core.engine

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.SystemClock
import com.azikar24.wormaceptor.domain.entities.MockLocation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
 */
class LocationSimulatorEngine(private val context: Context) {

    private val locationManager: LocationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private val _currentMockLocation = MutableStateFlow<MockLocation?>(null)
    val currentMockLocation: StateFlow<MockLocation?> = _currentMockLocation.asStateFlow()

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private var isProviderAdded = false

    /**
     * Sets a mock location for both GPS and Network providers.
     *
     * @param mockLocation The mock location to set
     * @return true if successful, false if mock locations are not enabled or permission denied
     */
    @SuppressLint("MissingPermission")
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

            // Create Android Location from MockLocation
            val gpsLocation = createAndroidLocation(mockLocation, LocationManager.GPS_PROVIDER)
            val networkLocation = createAndroidLocation(mockLocation, LocationManager.NETWORK_PROVIDER)

            // Set mock location for GPS provider
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, gpsLocation)

            // Set mock location for Network provider
            locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, networkLocation)

            _currentMockLocation.value = mockLocation
            _isEnabled.value = true
            _lastError.value = null
            true
        } catch (e: SecurityException) {
            _lastError.value = "Mock locations not enabled. Enable in Developer Options and select this app as mock location app."
            false
        } catch (e: IllegalArgumentException) {
            _lastError.value = "Failed to set mock location: ${e.message}"
            false
        } catch (e: Exception) {
            _lastError.value = "Unexpected error: ${e.message}"
            false
        }
    }

    /**
     * Clears the mock location and restores normal location behavior.
     */
    @SuppressLint("MissingPermission")
    fun clearMockLocation() {
        try {
            if (isProviderAdded) {
                removeTestProviders()
            }
            _currentMockLocation.value = null
            _isEnabled.value = false
            _lastError.value = null
        } catch (e: Exception) {
            _lastError.value = "Failed to clear mock location: ${e.message}"
        }
    }

    /**
     * Toggles mock location on or off with the given location.
     */
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
        } catch (e: SecurityException) {
            false
        } catch (e: IllegalArgumentException) {
            // Provider might already exist, which is fine
            try {
                locationManager.removeTestProvider(TEST_PROVIDER_CHECK)
            } catch (_: Exception) { }
            true
        }
    }

    @SuppressLint("MissingPermission")
    private fun addTestProviders() {
        try {
            // Add GPS test provider
            addTestProvider(LocationManager.GPS_PROVIDER)
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)

            // Add Network test provider
            addTestProvider(LocationManager.NETWORK_PROVIDER)
            locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true)

            isProviderAdded = true
        } catch (e: Exception) {
            throw e
        }
    }

    private fun addTestProvider(provider: String) {
        try {
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
        } catch (e: IllegalArgumentException) {
            // Provider might already exist, remove and re-add
            try {
                locationManager.removeTestProvider(provider)
                addTestProvider(provider)
            } catch (_: Exception) { }
        }
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

    private fun createAndroidLocation(mockLocation: MockLocation, provider: String): Location {
        return Location(provider).apply {
            latitude = mockLocation.latitude
            longitude = mockLocation.longitude
            altitude = mockLocation.altitude
            accuracy = mockLocation.accuracy
            speed = mockLocation.speed
            bearing = mockLocation.bearing
            time = mockLocation.timestamp
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

    companion object {
        private const val TEST_PROVIDER_CHECK = "wormaceptor_test_check"
    }
}
