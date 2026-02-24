package com.azikar24.wormaceptor.feature.location.vm

/**
 * One-time side effects for the Location Simulation feature.
 */
sealed class LocationViewEffect {
    /** Show an error message to the user. */
    data class ShowError(val message: String) : LocationViewEffect()

    /** Show a success message to the user. */
    data class ShowSuccess(val message: String) : LocationViewEffect()
}
