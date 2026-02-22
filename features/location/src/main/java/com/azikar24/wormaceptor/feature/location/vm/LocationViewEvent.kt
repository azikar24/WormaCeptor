package com.azikar24.wormaceptor.feature.location.vm

import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation

/**
 * User-initiated events for the Location Simulation feature.
 */
sealed class LocationViewEvent {
    /** Latitude input text changed. */
    data class LatitudeChanged(val value: String) : LocationViewEvent()

    /** Longitude input text changed. */
    data class LongitudeChanged(val value: String) : LocationViewEvent()

    /** Search query for presets changed. */
    data class SearchQueryChanged(val query: String) : LocationViewEvent()

    /** User requested to set mock location from the current input fields. */
    data object SetMockLocationFromInput : LocationViewEvent()

    /** User selected a preset to apply. */
    data class SetMockLocationFromPreset(val preset: LocationPreset) : LocationViewEvent()

    /** User requested to set a specific mock location. */
    data class SetMockLocation(val location: MockLocation) : LocationViewEvent()

    /** User requested to clear the current mock location. */
    data object ClearMockLocation : LocationViewEvent()

    /** User toggled mock location on/off. */
    data object ToggleMockLocation : LocationViewEvent()

    /** User requested to use the device's current real location. */
    data object SetToCurrentRealLocation : LocationViewEvent()

    /** User requested to save current input as a new preset. */
    data class SaveCurrentAsPreset(val name: String) : LocationViewEvent()

    /** User requested to delete a preset. */
    data class DeletePreset(val presetId: String) : LocationViewEvent()

    /** User tapped a location on the map. */
    data class MapTapped(val latitude: Double, val longitude: Double) : LocationViewEvent()

    /** Refresh mock location availability (e.g., after returning from settings). */
    data object RefreshMockLocationAvailability : LocationViewEvent()

    /** Start continuous real location updates for the map. */
    data object StartRealLocationUpdates : LocationViewEvent()

    /** Stop continuous real location updates. */
    data object StopRealLocationUpdates : LocationViewEvent()
}
