package com.azikar24.wormaceptor.feature.location.vm

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
)
