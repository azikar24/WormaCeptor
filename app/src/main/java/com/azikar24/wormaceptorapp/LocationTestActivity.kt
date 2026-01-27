/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.azikar24.wormaceptor.core.engine.LocationSimulatorEngine
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay

private val LocationBlue = Color(0xFF2196F3)
private val LocationGreen = Color(0xFF4CAF50)

/**
 * Test activity for the Location Simulator feature.
 * Provides a custom UI to test mock location with a map and preset locations.
 */
class LocationTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize osmdroid configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName

        enableEdgeToEdge()
        setContent {
            WormaCeptorMainTheme {
                LocationTestScreen(
                    context = this,
                    onBack = { finish() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
    }
}

private data class LocationPreset(
    val name: String,
    val location: MockLocation,
    val flag: String,
)

private val presetLocations = listOf(
    LocationPreset("New York", MockLocation.from(40.7128, -74.0060, "New York"), "US"),
    LocationPreset("London", MockLocation.from(51.5074, -0.1278, "London"), "UK"),
    LocationPreset("Tokyo", MockLocation.from(35.6762, 139.6503, "Tokyo"), "JP"),
    LocationPreset("Sydney", MockLocation.from(-33.8688, 151.2093, "Sydney"), "AU"),
    LocationPreset("Paris", MockLocation.from(48.8566, 2.3522, "Paris"), "FR"),
    LocationPreset("San Francisco", MockLocation.from(37.7749, -122.4194, "San Francisco"), "US"),
    LocationPreset("Dubai", MockLocation.from(25.2048, 55.2708, "Dubai"), "AE"),
    LocationPreset("Singapore", MockLocation.from(1.3521, 103.8198, "Singapore"), "SG"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationTestScreen(
    context: Context,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val engine = remember { LocationSimulatorEngine(context) }
    val currentMockLocation by engine.currentMockLocation.collectAsState()
    val isEnabled by engine.isEnabled.collectAsState()
    val lastError by engine.lastError.collectAsState()

    var selectedLocation by remember { mutableStateOf<MockLocation?>(null) }
    var latitudeInput by remember { mutableStateOf("") }
    var longitudeInput by remember { mutableStateOf("") }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var isMockAvailable by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        hasLocationPermission = permissions.values.all { it }
        if (hasLocationPermission) {
            isMockAvailable = engine.isMockLocationAvailable()
        }
    }

    LaunchedEffect(Unit) {
        hasLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        } else {
            isMockAvailable = engine.isMockLocationAvailable()
        }
    }

    fun setMockLocation(location: MockLocation) {
        selectedLocation = location
        latitudeInput = "%.6f".format(location.latitude)
        longitudeInput = "%.6f".format(location.longitude)
    }

    fun applyMockLocation() {
        val lat = latitudeInput.toDoubleOrNull()
        val lng = longitudeInput.toDoubleOrNull()
        if (lat != null && lng != null) {
            val location = MockLocation.from(lat, lng)
            engine.setLocation(location)
            selectedLocation = location
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (isEnabled) LocationGreen else LocationBlue,
                        )
                        Column {
                            Text(
                                text = "Location Test",
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = if (isEnabled) "Mock Active" else "Mock Inactive",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isEnabled) LocationGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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
                .verticalScroll(rememberScrollState())
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            // Status card
            LocationStatusCard(
                isEnabled = isEnabled,
                currentLocation = currentMockLocation,
                hasPermission = hasLocationPermission,
                isMockAvailable = isMockAvailable,
                error = lastError,
            )

            // Map
            LocationTestMap(
                mockLocation = selectedLocation?.let { GeoPoint(it.latitude, it.longitude) },
                isMockActive = isEnabled,
                onMapTap = { geoPoint ->
                    val location = MockLocation.from(geoPoint.latitude, geoPoint.longitude)
                    setMockLocation(location)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
            )

            // Map hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "Tap on map to select coordinates",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Preset locations
            Text(
                text = "Preset Locations",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                presetLocations.forEach { preset ->
                    val isSelected = selectedLocation?.latitude == preset.location.latitude &&
                        selectedLocation?.longitude == preset.location.longitude

                    FilterChip(
                        selected = isSelected,
                        onClick = { setMockLocation(preset.location) },
                        label = { Text(preset.name) },
                        leadingIcon = {
                            Text(
                                text = getFlagEmoji(preset.flag),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LocationBlue.copy(alpha = 0.15f),
                            selectedLabelColor = LocationBlue,
                        ),
                    )
                }
            }

            // Custom coordinates
            Text(
                text = "Custom Coordinates",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                OutlinedTextField(
                    value = latitudeInput,
                    onValueChange = { latitudeInput = it },
                    label = { Text("Latitude") },
                    placeholder = { Text("-90 to 90") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )

                OutlinedTextField(
                    value = longitudeInput,
                    onValueChange = { longitudeInput = it },
                    label = { Text("Longitude") },
                    placeholder = { Text("-180 to 180") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Button(
                    onClick = { applyMockLocation() },
                    modifier = Modifier.weight(1f),
                    enabled = hasLocationPermission &&
                        isMockAvailable &&
                        latitudeInput.toDoubleOrNull() != null &&
                        longitudeInput.toDoubleOrNull() != null,
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text("Set Mock Location")
                }

                OutlinedButton(
                    onClick = { engine.clearMockLocation() },
                    modifier = Modifier.weight(1f),
                    enabled = isEnabled,
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text("Clear Mock")
                }
            }

            // Current mock location display
            if (currentMockLocation != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = LocationGreen.copy(alpha = 0.1f),
                    ),
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorDesignSystem.Spacing.md),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        ) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = LocationGreen,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "Active Mock Location",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = LocationGreen,
                            )
                        }

                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

                        currentMockLocation?.let { loc ->
                            Text(
                                text = "%.6f, %.6f".format(loc.latitude, loc.longitude),
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            loc.name?.let { name ->
                                Text(
                                    text = name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
        }
    }
}

@Composable
private fun LocationStatusCard(
    isEnabled: Boolean,
    currentLocation: MockLocation?,
    hasPermission: Boolean,
    isMockAvailable: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
) {
    val statusColor = when {
        !hasPermission -> Color(0xFFFF9800)
        !isMockAvailable -> Color(0xFFF44336)
        isEnabled -> LocationGreen
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val statusText = when {
        !hasPermission -> "Location permission required"
        !isMockAvailable -> "Mock locations not enabled"
        isEnabled -> "Mock location active"
        else -> "Ready to mock location"
    }

    val statusDescription = when {
        !hasPermission -> "Grant location permission to use mock locations"
        !isMockAvailable -> "Enable 'Allow mock locations' in Developer Options and select this app"
        isEnabled -> currentLocation?.formatCoordinates() ?: "Location set"
        else -> "Select a location to start mocking"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f),
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(statusColor, CircleShape),
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

            Text(
                text = statusDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            error?.let { err ->
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFF44336),
                    )
                    Text(
                        text = err,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFF44336),
                    )
                }
            }
        }
    }
}

@Composable
private fun LocationTestMap(
    mockLocation: GeoPoint?,
    isMockActive: Boolean,
    onMapTap: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = remember { createMapView(context) }

    // Animate border based on mock active state
    val borderWidth by animateDpAsState(
        targetValue = if (isMockActive) 3.dp else 1.dp,
        label = "borderWidth",
    )
    val borderColor by animateColorAsState(
        targetValue = if (isMockActive) LocationGreen else Color.Transparent,
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
    LaunchedEffect(mockLocation, isMockActive) {
        updateMapMarkers(
            mapView = mapView,
            context = context,
            mockLocation = mockLocation,
            isMockActive = isMockActive,
        )

        mockLocation?.let {
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
        controller.setZoom(4.0)
        // Default center on the world
        controller.setCenter(GeoPoint(20.0, 0.0))
        // Disable built-in zoom controls
        zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)
        // Set minimum and maximum zoom
        minZoomLevel = 2.0
        maxZoomLevel = 19.0
    }
}

private fun updateMapMarkers(
    mapView: MapView,
    context: Context,
    mockLocation: GeoPoint?,
    isMockActive: Boolean,
) {
    // Remove existing markers
    mapView.overlays.removeAll { it is Marker }

    // Add mock location marker
    mockLocation?.let { location ->
        val marker = Marker(mapView).apply {
            position = location
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = if (isMockActive) "Mock Location (Active)" else "Mock Location"
            snippet = "%.6f, %.6f".format(location.latitude, location.longitude)
            // Use default marker icon
            icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
        }
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}

/**
 * Custom overlay to handle map tap events.
 */
private class MapTapOverlay(
    private val onTap: (GeoPoint) -> Unit,
) : Overlay() {

    override fun onSingleTapConfirmed(e: android.view.MotionEvent?, mapView: MapView?): Boolean {
        if (e == null || mapView == null) return false

        val projection = mapView.projection
        val geoPoint = projection.fromPixels(e.x.toInt(), e.y.toInt()) as GeoPoint
        onTap(geoPoint)
        return true
    }
}

private fun getFlagEmoji(countryCode: String): String {
    return when (countryCode.uppercase()) {
        "US" -> "\uD83C\uDDFA\uD83C\uDDF8"
        "UK" -> "\uD83C\uDDEC\uD83C\uDDE7"
        "JP" -> "\uD83C\uDDEF\uD83C\uDDF5"
        "AU" -> "\uD83C\uDDE6\uD83C\uDDFA"
        "FR" -> "\uD83C\uDDEB\uD83C\uDDF7"
        "AE" -> "\uD83C\uDDE6\uD83C\uDDEA"
        "SG" -> "\uD83C\uDDF8\uD83C\uDDEC"
        else -> "\uD83C\uDFF3\uFE0F"
    }
}
