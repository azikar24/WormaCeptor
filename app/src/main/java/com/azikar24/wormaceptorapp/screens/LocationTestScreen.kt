package com.azikar24.wormaceptorapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.preference.PreferenceManager
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val LocationGreen = Color(0xFF4CAF50)

/**
 * Test screen for viewing current location (real or mocked).
 * Provides a simple map view and navigation to the mock location tool.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationTestScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Initialize OSMDroid configuration
    DisposableEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isLocationMocked by remember { mutableStateOf(false) }
    var locationListener by remember { mutableStateOf<LocationListener?>(null) }
    var locationManager by remember { mutableStateOf<LocationManager?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        hasLocationPermission = permissions.values.all { it }
    }

    // Check permission initially
    hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Lifecycle-aware location updates
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, hasLocationPermission) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    } else {
                        startLocationUpdates(
                            context = context,
                            onLocationUpdate = { location ->
                                currentLocation = GeoPoint(location.latitude, location.longitude)
                                isLocationMocked = isLocationFromMockProvider(location)
                            },
                            onListenerCreated = { manager, listener ->
                                locationManager = manager
                                locationListener = listener
                            },
                        )
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    stopLocationUpdates(locationManager, locationListener)
                    locationListener = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            stopLocationUpdates(locationManager, locationListener)
        }
    }

    val displayLocation = currentLocation

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Current Location",
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (isLocationMocked) "Mock Active" else "Real Location",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isLocationMocked) LocationGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            // Map
            CurrentLocationMap(
                location = displayLocation,
                isMockActive = isLocationMocked,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            // Location info
            if (displayLocation != null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (isLocationMocked) "Mocked Location" else "Real Location",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isLocationMocked) LocationGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "%.6f, %.6f".format(displayLocation.latitude, displayLocation.longitude),
                        style = MaterialTheme.typography.bodyLarge,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            } else if (!hasLocationPermission) {
                Text(
                    text = "Location permission required",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            } else {
                Text(
                    text = "Waiting for location...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            // Deep link button to mock location tool
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("wormaceptor://tools/location"))
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                Text("Open Mock Location Tool")
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))
        }
    }
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates(
    context: Context,
    onLocationUpdate: (Location) -> Unit,
    onListenerCreated: (LocationManager, LocationListener) -> Unit,
) {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            onLocationUpdate(location)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(
            provider: String?,
            status: Int,
            extras: Bundle?,
        ) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    onListenerCreated(locationManager, listener)

    try {
        // Try GPS first
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000L,
                1f,
                listener,
            )
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let {
                onLocationUpdate(it)
            }
        }

        // Also try network provider for faster initial fix
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                1000L,
                1f,
                listener,
            )
            // Use network location if no GPS location yet
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let {
                onLocationUpdate(it)
            }
        }

        // Try fused provider if available
        if (locationManager.isProviderEnabled(LocationManager.FUSED_PROVIDER)) {
            locationManager.requestLocationUpdates(
                LocationManager.FUSED_PROVIDER,
                1000L,
                1f,
                listener,
            )
            locationManager.getLastKnownLocation(LocationManager.FUSED_PROVIDER)?.let {
                onLocationUpdate(it)
            }
        }
    } catch (_: SecurityException) {
        // Permission not granted
    } catch (_: IllegalArgumentException) {
        // Provider doesn't exist
    }
}

private fun stopLocationUpdates(
    locationManager: LocationManager?,
    listener: LocationListener?,
) {
    if (locationManager != null && listener != null) {
        try {
            locationManager.removeUpdates(listener)
        } catch (_: Exception) {
            // Ignore cleanup errors
        }
    }
}

/**
 * Checks if the location is from a mock provider.
 */
private fun isLocationFromMockProvider(location: Location): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        location.isMock
    } else {
        @Suppress("DEPRECATION")
        location.isFromMockProvider
    }
}

@Composable
private fun CurrentLocationMap(
    location: GeoPoint?,
    isMockActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = remember { createMapView(context) }

    val borderWidth by animateDpAsState(
        targetValue = if (isMockActive) 3.dp else 1.dp,
        label = "borderWidth",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isMockActive) LocationGreen else Color.Transparent,
        label = "borderColor",
    )

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    // Update marker when location changes
    DisposableEffect(location, isMockActive) {
        updateMapMarker(mapView, context, location, isMockActive)
        location?.let {
            mapView.controller.animateTo(it)
            mapView.controller.setZoom(15.0)
        }
        onDispose {}
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
        )
    }
}

private fun createMapView(context: Context): MapView {
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(4.0)
        controller.setCenter(GeoPoint(20.0, 0.0))
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        minZoomLevel = 2.0
        maxZoomLevel = 19.0
    }
}

private fun updateMapMarker(
    mapView: MapView,
    context: Context,
    location: GeoPoint?,
    isMockActive: Boolean,
) {
    mapView.overlays.removeAll { it is Marker }

    location?.let {
        val marker = Marker(mapView).apply {
            position = it
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = if (isMockActive) "Mock Location" else "Current Location"
            snippet = "%.6f, %.6f".format(it.latitude, it.longitude)
            icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
        }
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}
