/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.location.ui.components

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.location.R
import com.azikar24.wormaceptor.feature.location.ui.theme.LocationColors
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * A composable that displays an OpenStreetMap view with real and mock location markers.
 *
 * @param realLocation The real device location (blue marker)
 * @param mockLocation The mocked location (green marker)
 * @param isMockActive Whether mock location is currently active
 * @param onMapTap Callback when the map is tapped with the GeoPoint
 * @param modifier Modifier for the composable
 */
@Composable
fun LocationMapView(
    realLocation: GeoPoint?,
    mockLocation: GeoPoint?,
    isMockActive: Boolean,
    onMapTap: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = remember { createMapView(context) }

    // Animate border based on mock active state
    val borderWidth by animateDpAsState(
        targetValue = if (isMockActive) WormaCeptorDesignSystem.BorderWidth.thick else WormaCeptorDesignSystem.BorderWidth.regular,
        label = "borderWidth",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isMockActive) LocationColors.enabled else Color.Transparent,
        label = "borderColor",
    )

    // Lifecycle handling
    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // Update markers when locations change
    LaunchedEffect(realLocation, mockLocation, isMockActive) {
        updateMapMarkers(
            mapView = mapView,
            context = context,
            realLocation = realLocation,
            mockLocation = mockLocation,
            isMockActive = isMockActive,
        )

        // Auto-center on mock location if active, otherwise on real location
        val centerPoint = when {
            isMockActive && mockLocation != null -> mockLocation
            mockLocation != null -> mockLocation
            realLocation != null -> realLocation
            else -> null
        }
        centerPoint?.let {
            mapView.controller.animateTo(it)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
            ),
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                // Set up tap listener
                view.overlays.removeAll { it is MapTapOverlay }
                view.overlays.add(MapTapOverlay(onMapTap))
            },
        )
    }
}

private fun createMapView(context: Context): MapView {
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(15.0)
        // Default center (will be updated when locations are available)
        controller.setCenter(GeoPoint(0.0, 0.0))
        // Disable built-in zoom controls
        zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
        // Set minimum and maximum zoom
        minZoomLevel = 3.0
        maxZoomLevel = 19.0
    }
}

private fun updateMapMarkers(
    mapView: MapView,
    context: Context,
    realLocation: GeoPoint?,
    mockLocation: GeoPoint?,
    isMockActive: Boolean,
) {
    // Remove existing markers
    mapView.overlays.removeAll { it is Marker }

    // Add real location marker (blue)
    realLocation?.let { location ->
        val marker = Marker(mapView).apply {
            position = location
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Real Location"
            snippet = formatCoordinates(location)
            icon = createMarkerDrawable(context, isReal = true)
        }
        mapView.overlays.add(marker)
    }

    // Add mock location marker (green)
    mockLocation?.let { location ->
        val marker = Marker(mapView).apply {
            position = location
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = if (isMockActive) "Mock Location (Active)" else "Mock Location"
            snippet = formatCoordinates(location)
            icon = createMarkerDrawable(context, isReal = false)
        }
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}

private fun createMarkerDrawable(context: Context, isReal: Boolean): Drawable? {
    val drawableRes = if (isReal) {
        R.drawable.ic_marker_real
    } else {
        R.drawable.ic_marker_mock
    }
    return ContextCompat.getDrawable(context, drawableRes)
}

private fun formatCoordinates(geoPoint: GeoPoint): String {
    return "%.6f, %.6f".format(geoPoint.latitude, geoPoint.longitude)
}

/**
 * Custom overlay to handle map tap events.
 */
private class MapTapOverlay(
    private val onTap: (GeoPoint) -> Unit,
) : org.osmdroid.views.overlay.Overlay() {

    override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
        if (e == null || mapView == null) return false

        val projection = mapView.projection
        val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
        onTap(geoPoint)
        return true
    }
}

/**
 * Calculate distance between two GeoPoints in meters.
 */
fun calculateDistance(point1: GeoPoint, point2: GeoPoint): Double {
    return point1.distanceToAsDouble(point2)
}

/**
 * Format distance for display.
 */
fun formatDistance(meters: Double): String {
    return when {
        meters < 1000 -> "%.0f m".format(meters)
        else -> "%.2f km".format(meters / 1000)
    }
}
