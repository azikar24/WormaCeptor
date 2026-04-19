package com.azikar24.wormaceptorapp.screens.location

import org.osmdroid.util.GeoPoint

data class LocationTestViewState(
    val hasLocationPermission: Boolean = false,
    val currentLocation: GeoPoint? = null,
    val isLocationMocked: Boolean = false,
    val mapZoom: Double = DEFAULT_ZOOM,
    val mapCenter: GeoPoint = DEFAULT_CENTER,
    val mapZoomMin: Double = ZOOM_MIN,
    val mapZoomMax: Double = ZOOM_MAX,
) {
    companion object {
        const val DEFAULT_ZOOM = 4.0
        const val FOCUSED_ZOOM = 15.0
        const val ZOOM_MIN = 2.0
        const val ZOOM_MAX = 19.0
        val DEFAULT_CENTER = GeoPoint(20.0, 0.0)
    }
}
