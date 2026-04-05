package com.azikar24.wormaceptorapp.screens

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.preference.PreferenceManager
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptorapp.R
import com.azikar24.wormaceptorapp.screens.location.LocationTestViewEffect
import com.azikar24.wormaceptorapp.screens.location.LocationTestViewEvent
import com.azikar24.wormaceptorapp.screens.location.LocationTestViewModel
import com.azikar24.wormaceptorapp.screens.location.LocationTestViewState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun LocationTestScreen(
    viewModel: LocationTestViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.sendEvent(LocationTestViewEvent.PermissionResult)
    }

    // Initialize OSMDroid configuration
    DisposableEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
        onDispose { }
    }

    // Forward lifecycle events to ViewModel
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.sendEvent(LocationTestViewEvent.ScreenResumed)
                Lifecycle.Event.ON_PAUSE -> viewModel.sendEvent(LocationTestViewEvent.ScreenPaused)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                LocationTestViewEffect.RequestLocationPermission -> {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                        ),
                    )
                }
                LocationTestViewEffect.OpenMockLocationTool -> {
                    val intent = android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        "wormaceptor://tools/location".toUri(),
                    )
                    context.startActivity(intent)
                }
            }
        },
    ) { state, onEvent ->
        LocationTestScreenContent(
            state = state,
            onBack = onBack,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationTestScreenContent(
    state: LocationTestViewState,
    onBack: () -> Unit,
    onEvent: (LocationTestViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                            text = if (state.isLocationMocked) "Mock Active" else "Real Location",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isLocationMocked) {
                                WormaCeptorTokens.Colors.Location.enabled
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
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
                .padding(WormaCeptorTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            CurrentLocationMap(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            LocationInfoSection(
                location = state.currentLocation,
                hasPermission = state.hasLocationPermission,
                isMocked = state.isLocationMocked,
                modifier = Modifier.fillMaxWidth(),
            )

            WormaCeptorButton(
                text = "Open Mock Location Tool",
                onClick = { onEvent(LocationTestViewEvent.OpenMockLocationTool) },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.LocationOn, null) },
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))
        }
    }
}

@Composable
private fun LocationInfoSection(
    location: GeoPoint?,
    hasPermission: Boolean,
    isMocked: Boolean,
    modifier: Modifier = Modifier,
) {
    when {
        location != null -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (isMocked) "Mocked Location" else "Real Location",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isMocked) {
                        WormaCeptorTokens.Colors.Location.enabled
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    text = stringResource(R.string.location_coordinate_format, location.latitude, location.longitude),
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        !hasPermission -> {
            Text(
                text = "Location permission required",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.then(Modifier.padding(start = WormaCeptorTokens.Spacing.md)),
            )
        }
        else -> {
            Text(
                text = "Waiting for location...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = modifier.then(Modifier.padding(start = WormaCeptorTokens.Spacing.md)),
            )
        }
    }
}

@Composable
private fun CurrentLocationMap(
    state: LocationTestViewState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = androidx.compose.runtime.remember {
        createMapView(context, state)
    }

    val borderWidth by animateDpAsState(
        targetValue = if (state.isLocationMocked) {
            WormaCeptorTokens.BorderWidth.bold
        } else {
            WormaCeptorTokens.BorderWidth.regular
        },
        label = "borderWidth",
    )
    val borderColor by animateColorAsState(
        targetValue = if (state.isLocationMocked) WormaCeptorTokens.Colors.Location.enabled else Color.Transparent,
        label = "borderColor",
    )

    DisposableEffect(Unit) {
        mapView.onResume()
        onDispose {
            mapView.onPause()
            mapView.onDetach()
        }
    }

    LaunchedEffect(state.currentLocation, state.isLocationMocked) {
        updateMapMarker(mapView, context, state.currentLocation, state.isLocationMocked)
    }

    LaunchedEffect(state.mapCenter, state.mapZoom) {
        mapView.controller.animateTo(state.mapCenter)
        mapView.controller.setZoom(state.mapZoom)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(WormaCeptorTokens.Radius.lg))
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(WormaCeptorTokens.Radius.lg),
            ),
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

private fun createMapView(
    context: Context,
    state: LocationTestViewState,
): MapView {
    return MapView(context).apply {
        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)
        controller.setZoom(state.mapZoom)
        controller.setCenter(state.mapCenter)
        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
        minZoomLevel = state.mapZoomMin
        maxZoomLevel = state.mapZoomMax
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
            snippet = context.getString(R.string.location_coordinate_format, it.latitude, it.longitude)
            icon = ContextCompat.getDrawable(context, android.R.drawable.ic_menu_mylocation)
        }
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}
