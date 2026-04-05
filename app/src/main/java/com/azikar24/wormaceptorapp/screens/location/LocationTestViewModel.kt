package com.azikar24.wormaceptorapp.screens.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.azikar24.wormaceptor.common.presentation.BaseViewModel
import org.osmdroid.util.GeoPoint

class LocationTestViewModel(
    private val application: Application,
) : BaseViewModel<LocationTestViewState, LocationTestViewEffect, LocationTestViewEvent>(LocationTestViewState()) {

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    init {
        checkPermission()
    }

    override fun handleEvent(event: LocationTestViewEvent) {
        when (event) {
            LocationTestViewEvent.PermissionResult -> handlePermissionResult()
            LocationTestViewEvent.OpenMockLocationTool -> emitEffect(LocationTestViewEffect.OpenMockLocationTool)
            LocationTestViewEvent.ScreenResumed -> handleResume()
            LocationTestViewEvent.ScreenPaused -> handlePause()
        }
    }

    private fun checkPermission() {
        val granted = ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        updateState { copy(hasLocationPermission = granted) }
    }

    private fun handlePermissionResult() {
        checkPermission()
    }

    private fun handleResume() {
        if (!uiState.value.hasLocationPermission) {
            emitEffect(LocationTestViewEffect.RequestLocationPermission)
        } else {
            startLocationUpdates()
        }
    }

    private fun handlePause() {
        stopLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val manager = application.getSystemService(LocationManager::class.java)
        locationManager = manager

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val point = GeoPoint(location.latitude, location.longitude)
                updateState {
                    copy(
                        currentLocation = point,
                        isLocationMocked = isLocationFromMockProvider(location),
                        mapCenter = point,
                        mapZoom = LocationTestViewState.FOCUSED_ZOOM,
                    )
                }
            }

            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(
                provider: String?,
                status: Int,
                extras: Bundle?,
            ) = Unit
        }
        locationListener = listener

        try {
            requestProviderUpdates(manager, LocationManager.GPS_PROVIDER, listener)
            requestProviderUpdates(manager, LocationManager.NETWORK_PROVIDER, listener)
            requestProviderUpdates(manager, LocationManager.FUSED_PROVIDER, listener)
        } catch (_: SecurityException) {
            // Permission not granted
        } catch (_: IllegalArgumentException) {
            // Provider doesn't exist
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestProviderUpdates(
        manager: LocationManager,
        provider: String,
        listener: LocationListener,
    ) {
        if (!manager.isProviderEnabled(provider)) return

        manager.requestLocationUpdates(
            provider,
            LOCATION_UPDATE_INTERVAL_MS,
            LOCATION_UPDATE_MIN_DISTANCE_M,
            listener,
        )
        manager.getLastKnownLocation(provider)?.let { location ->
            val point = GeoPoint(location.latitude, location.longitude)
            updateState {
                copy(
                    currentLocation = point,
                    isLocationMocked = isLocationFromMockProvider(location),
                    mapCenter = point,
                    mapZoom = LocationTestViewState.FOCUSED_ZOOM,
                )
            }
        }
    }

    private fun stopLocationUpdates() {
        val manager = locationManager ?: return
        val listener = locationListener ?: return
        try {
            manager.removeUpdates(listener)
        } catch (_: SecurityException) {
            // Cleanup — permission may have been revoked
        } catch (_: IllegalStateException) {
            // Listener already unregistered
        }
        locationListener = null
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

    companion object {
        private const val LOCATION_UPDATE_INTERVAL_MS = 1000L
        private const val LOCATION_UPDATE_MIN_DISTANCE_M = 1f

        private fun isLocationFromMockProvider(location: Location): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                location.isMock
            } else {
                @Suppress("DEPRECATION")
                location.isFromMockProvider
            }
        }
    }
}
