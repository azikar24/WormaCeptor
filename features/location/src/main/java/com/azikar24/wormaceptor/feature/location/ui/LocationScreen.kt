package com.azikar24.wormaceptor.feature.location.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.azikar24.wormaceptor.feature.location.R
import com.azikar24.wormaceptor.feature.location.vm.LocationViewEvent
import com.azikar24.wormaceptor.feature.location.vm.LocationViewState
import kotlinx.collections.immutable.persistentListOf
import org.osmdroid.util.GeoPoint

/**
 * Main screen for the Location Simulation feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    state: LocationViewState,
    onEvent: (LocationViewEvent) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showSavePresetDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                    ) {
                        Text(
                            text = stringResource(R.string.location_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (state.isMockEnabled) {
                            Surface(
                                shape = RoundedCornerShape(WormaCeptorTokens.Radius.xs),
                                color = WormaCeptorTokens.Colors.Location.enabled.copy(
                                    alpha = WormaCeptorTokens.Alpha.SOFT,
                                ),
                            ) {
                                Text(
                                    text = stringResource(R.string.location_status_active),
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp,
                                        vertical = WormaCeptorTokens.Spacing.xxs,
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = WormaCeptorTokens.Colors.Location.enabled,
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.location_back),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = WormaCeptorTokens.Spacing.lg,
                    top = WormaCeptorTokens.Spacing.lg,
                    end = WormaCeptorTokens.Spacing.lg,
                    bottom = WormaCeptorTokens.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.lg),
            ) {
                // Warning banner if mock locations not available
                if (!state.isMockLocationAvailable) {
                    item {
                        MockLocationWarningBanner()
                    }
                }

                // Current mock location status card
                item {
                    MockLocationStatusCard(
                        currentMockLocation = state.currentMockLocation,
                        isMockEnabled = state.isMockEnabled,
                        onToggle = {
                            if (state.isMockEnabled) {
                                onEvent(LocationViewEvent.ClearMockLocation)
                            } else {
                                onEvent(LocationViewEvent.SetMockLocationFromInput)
                            }
                        },
                        isEnabled = state.isMockLocationAvailable,
                        isInputValid = state.isInputValid,
                    )
                }

                // Collapsible map visualization
                item {
                    CollapsibleMapSection(
                        isExpanded = state.isMapExpanded,
                        onToggle = { onEvent(LocationViewEvent.ToggleMapExpanded) },
                        realLocation = state.realDeviceLocation,
                        mockLocation = state.currentMockLocation?.let {
                            GeoPoint(it.latitude, it.longitude)
                        },
                        isMockActive = state.isMockEnabled,
                        onMapTap = { geoPoint ->
                            onEvent(LocationViewEvent.MapTapped(geoPoint.latitude, geoPoint.longitude))
                        },
                    )
                }

                // Coordinate input section
                item {
                    CoordinateInputCard(
                        latitudeInput = state.latitudeInput,
                        longitudeInput = state.longitudeInput,
                        isInputValid = state.isInputValid,
                        isLoading = state.isLoading,
                        isMockEnabled = state.isMockEnabled,
                        isMockLocationAvailable = state.isMockLocationAvailable,
                        currentMockLatitude = state.currentMockLocation?.latitude,
                        currentMockLongitude = state.currentMockLocation?.longitude,
                        onLatitudeChanged = { onEvent(LocationViewEvent.LatitudeChanged(it)) },
                        onLongitudeChanged = { onEvent(LocationViewEvent.LongitudeChanged(it)) },
                        onSetMockLocation = { onEvent(LocationViewEvent.SetMockLocationFromInput) },
                        onSetToCurrentLocation = { onEvent(LocationViewEvent.SetToCurrentRealLocation) },
                        onSaveAsPreset = { showSavePresetDialog = true },
                    )
                }

                // Presets section header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.location_presets_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.semantics { heading() },
                        )
                        Text(
                            text = stringResource(R.string.location_presets_count, state.presets.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Search presets
                item {
                    WormaCeptorSearchBar(
                        query = state.searchQuery,
                        onQueryChange = { onEvent(LocationViewEvent.SearchQueryChanged(it)) },
                        placeholder = stringResource(R.string.location_search_placeholder),
                    )
                }

                // Preset items
                items(state.presets, key = { it.id }) { preset ->
                    PresetItem(
                        preset = preset,
                        isSelected = state.currentMockLocation?.let {
                            it.latitude == preset.location.latitude &&
                                it.longitude == preset.location.longitude
                        } == true,
                        onClick = { onEvent(LocationViewEvent.SetMockLocationFromPreset(preset)) },
                        onDelete = if (!preset.isBuiltIn) {
                            { onEvent(LocationViewEvent.DeletePreset(preset.id)) }
                        } else {
                            null
                        },
                        modifier = Modifier.animateItem(),
                    )
                }

                // Empty state for presets
                if (state.presets.isEmpty()) {
                    item {
                        EmptyPresetsState(hasSearchQuery = state.searchQuery.isNotBlank())
                    }
                }
            }

            // Loading overlay
            AnimatedVisibility(
                visible = state.isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center),
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Save preset dialog
    if (showSavePresetDialog) {
        SavePresetDialog(
            onDismiss = { showSavePresetDialog = false },
            onSave = { name ->
                onEvent(LocationViewEvent.SaveCurrentAsPreset(name))
                showSavePresetDialog = false
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationScreenPreview() {
    WormaCeptorTheme {
        LocationScreen(
            state = LocationViewState(
                latitudeInput = "40.7128",
                longitudeInput = "-74.0060",
                isMockLocationAvailable = true,
                isInputValid = true,
                presets = persistentListOf(
                    LocationPreset(
                        id = "1",
                        name = "New York City",
                        location = MockLocation.from(40.7128, -74.0060, "New York City"),
                        isBuiltIn = true,
                    ),
                    LocationPreset(
                        id = "2",
                        name = "London",
                        location = MockLocation.from(51.5074, -0.1278, "London"),
                        isBuiltIn = true,
                    ),
                ),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
