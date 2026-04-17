package com.azikar24.wormaceptor.feature.location.vm

import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.osmdroid.util.GeoPoint

/**
 * UI state for the Location Simulation feature.
 */
data class LocationViewState(
    /** Current latitude input text. */
    val latitudeInput: String = "",
    /** Current longitude input text. */
    val longitudeInput: String = "",
    /** Search query for filtering presets. */
    val searchQuery: String = "",
    /** Whether a loading operation is in progress. */
    val isLoading: Boolean = false,
    /** Whether mock location provider is available on the device. */
    val isMockLocationAvailable: Boolean = false,
    /** Whether the map section is expanded. */
    val isMapExpanded: Boolean = false,
    /** Filtered location presets. */
    val presets: ImmutableList<LocationPreset> = persistentListOf(),
    /** Current active mock location. */
    val currentMockLocation: MockLocation? = null,
    /** Whether mock location is currently enabled. */
    val isMockEnabled: Boolean = false,
    /** Whether the current coordinate input is valid. */
    val isInputValid: Boolean = false,
    /** Real device location from GPS, converted to GeoPoint for the map. */
    val realDeviceLocation: GeoPoint? = null,
    /** Whether the save-preset dialog is visible. */
    val showSavePresetDialog: Boolean = false,
)
