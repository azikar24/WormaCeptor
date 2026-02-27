package com.azikar24.wormaceptor.feature.location.vm

import com.azikar24.wormaceptor.domain.entities.LocationPreset
import com.azikar24.wormaceptor.domain.entities.MockLocation

/**
 * User-initiated events for the Location Simulation feature.
 */
sealed class LocationViewEvent {
    /**
     * Latitude input text changed.
     *
     * @property value The new latitude text.
     */
    data class LatitudeChanged(val value: String) : LocationViewEvent()

    /**
     * Longitude input text changed.
     *
     * @property value The new longitude text.
     */
    data class LongitudeChanged(val value: String) : LocationViewEvent()

    /**
     * Search query for presets changed.
     *
     * @property query The new search text.
     */
    data class SearchQueryChanged(val query: String) : LocationViewEvent()

    /** User requested to set mock location from the current input fields. */
    data object SetMockLocationFromInput : LocationViewEvent()

    /**
     * User selected a preset to apply.
     *
     * @property preset The location preset to apply.
     */
    data class SetMockLocationFromPreset(val preset: LocationPreset) : LocationViewEvent()

    /**
     * User requested to set a specific mock location.
     *
     * @property location The mock location to set.
     */
    data class SetMockLocation(val location: MockLocation) : LocationViewEvent()

    /** User requested to clear the current mock location. */
    data object ClearMockLocation : LocationViewEvent()

    /** User toggled mock location on/off. */
    data object ToggleMockLocation : LocationViewEvent()

    /** User requested to use the device's current real location. */
    data object SetToCurrentRealLocation : LocationViewEvent()

    /**
     * User requested to save current input as a new preset.
     *
     * @property name The display name for the new preset.
     */
    data class SaveCurrentAsPreset(val name: String) : LocationViewEvent()

    /**
     * User requested to delete a preset.
     *
     * @property presetId The unique identifier of the preset to delete.
     */
    data class DeletePreset(val presetId: String) : LocationViewEvent()

    /**
     * User tapped a location on the map.
     *
     * @property latitude The tapped latitude coordinate.
     * @property longitude The tapped longitude coordinate.
     */
    data class MapTapped(val latitude: Double, val longitude: Double) : LocationViewEvent()

    /** Refresh mock location availability (e.g., after returning from settings). */
    data object RefreshMockLocationAvailability : LocationViewEvent()

    /** Start continuous real location updates for the map. */
    data object StartRealLocationUpdates : LocationViewEvent()

    /** Stop continuous real location updates. */
    data object StopRealLocationUpdates : LocationViewEvent()

    /** User toggled the map expanded/collapsed state. */
    data object ToggleMapExpanded : LocationViewEvent()
}
