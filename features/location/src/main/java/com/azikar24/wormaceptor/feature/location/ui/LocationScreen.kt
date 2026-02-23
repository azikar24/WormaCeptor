package com.azikar24.wormaceptor.feature.location.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.LaunchedEffect
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
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import com.azikar24.wormaceptor.feature.location.R
import com.azikar24.wormaceptor.feature.location.ui.theme.LocationColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.osmdroid.util.GeoPoint

/**
 * Main screen for the Location Simulation feature.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationScreen(
    latitudeInput: String,
    longitudeInput: String,
    searchQuery: String,
    presets: ImmutableList<LocationPreset>,
    currentMockLocation: MockLocation?,
    isMockEnabled: Boolean,
    isMockLocationAvailable: Boolean,
    isInputValid: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    realDeviceLocation: GeoPoint?,
    onLatitudeChanged: (String) -> Unit,
    onLongitudeChanged: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onSetMockLocation: () -> Unit,
    onClearMockLocation: () -> Unit,
    onSetToCurrentLocation: () -> Unit,
    onPresetClick: (LocationPreset) -> Unit,
    onDeletePreset: (String) -> Unit,
    onSavePreset: (String) -> Unit,
    onClearError: () -> Unit,
    onClearSuccessMessage: () -> Unit,
    onMapTap: (GeoPoint) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showSavePresetDialog by remember { mutableStateOf(false) }

    // Show error as snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackBarHostState.showSnackbar(it)
            onClearError()
        }
    }

    // Show success as snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackBarHostState.showSnackbar(it)
            onClearSuccessMessage()
        }
    }

    var isMapExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Text(
                            text = stringResource(R.string.location_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (isMockEnabled) {
                            Surface(
                                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                                color = LocationColors.enabled.copy(alpha = WormaCeptorDesignSystem.Alpha.soft),
                            ) {
                                Text(
                                    text = stringResource(R.string.location_status_active),
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp,
                                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LocationColors.enabled,
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
                contentPadding = PaddingValues(WormaCeptorDesignSystem.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
            ) {
                // Warning banner if mock locations not available
                if (!isMockLocationAvailable) {
                    item {
                        MockLocationWarningBanner()
                    }
                }

                // Current mock location status card
                item {
                    MockLocationStatusCard(
                        currentMockLocation = currentMockLocation,
                        isMockEnabled = isMockEnabled,
                        onToggle = {
                            if (isMockEnabled) onClearMockLocation() else onSetMockLocation()
                        },
                        isEnabled = isMockLocationAvailable,
                        isInputValid = isInputValid,
                    )
                }

                // Collapsible map visualization
                item {
                    CollapsibleMapSection(
                        isExpanded = isMapExpanded,
                        onToggle = { isMapExpanded = !isMapExpanded },
                        realLocation = realDeviceLocation,
                        mockLocation = currentMockLocation?.let {
                            GeoPoint(it.latitude, it.longitude)
                        },
                        isMockActive = isMockEnabled,
                        onMapTap = onMapTap,
                    )
                }

                // Coordinate input section
                item {
                    CoordinateInputCard(
                        latitudeInput = latitudeInput,
                        longitudeInput = longitudeInput,
                        isInputValid = isInputValid,
                        isLoading = isLoading,
                        isMockEnabled = isMockEnabled,
                        isMockLocationAvailable = isMockLocationAvailable,
                        currentMockLatitude = currentMockLocation?.latitude,
                        currentMockLongitude = currentMockLocation?.longitude,
                        onLatitudeChanged = onLatitudeChanged,
                        onLongitudeChanged = onLongitudeChanged,
                        onSetMockLocation = onSetMockLocation,
                        onSetToCurrentLocation = onSetToCurrentLocation,
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
                            text = stringResource(R.string.location_presets_count, presets.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                // Search presets
                item {
                    WormaCeptorSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChanged,
                        placeholder = stringResource(R.string.location_search_placeholder),
                    )
                }

                // Preset items
                items(presets, key = { it.id }) { preset ->
                    PresetItem(
                        preset = preset,
                        isSelected = currentMockLocation?.let {
                            it.latitude == preset.location.latitude &&
                                it.longitude == preset.location.longitude
                        } == true,
                        onClick = { onPresetClick(preset) },
                        onDelete = if (!preset.isBuiltIn) {
                            { onDeletePreset(preset.id) }
                        } else {
                            null
                        },
                        modifier = Modifier.animateItem(),
                    )
                }

                // Empty state for presets
                if (presets.isEmpty()) {
                    item {
                        EmptyPresetsState(hasSearchQuery = searchQuery.isNotBlank())
                    }
                }
            }

            // Loading overlay
            AnimatedVisibility(
                visible = isLoading,
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
                onSavePreset(name)
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
            latitudeInput = "40.7128",
            longitudeInput = "-74.0060",
            searchQuery = "",
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
            currentMockLocation = null,
            isMockEnabled = false,
            isMockLocationAvailable = true,
            isInputValid = true,
            isLoading = false,
            errorMessage = null,
            successMessage = null,
            realDeviceLocation = null,
            onLatitudeChanged = {},
            onLongitudeChanged = {},
            onSearchQueryChanged = {},
            onSetMockLocation = {},
            onClearMockLocation = {},
            onSetToCurrentLocation = {},
            onPresetClick = {},
            onDeletePreset = {},
            onSavePreset = {},
            onClearError = {},
            onClearSuccessMessage = {},
            onMapTap = {},
            onBack = {},
        )
    }
}
