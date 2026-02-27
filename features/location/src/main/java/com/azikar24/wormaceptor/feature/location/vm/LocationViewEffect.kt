package com.azikar24.wormaceptor.feature.location.vm

/**
 * One-time side effects for the Location Simulation feature.
 */
sealed class LocationViewEffect {
    /**
     * Show an error message to the user.
     *
     * @property message The error text to display.
     */
    data class ShowError(val message: String) : LocationViewEffect()

    /**
     * Show a success message to the user.
     *
     * @property message The success text to display.
     */
    data class ShowSuccess(val message: String) : LocationViewEffect()
}
