package com.azikar24.wormaceptor.feature.database.vm

/**
 * One-time side effects for the Database Browser feature.
 */
sealed class DatabaseViewEffect {

    /**
     * Signals the UI to display an error message.
     *
     * @property message The error text to show.
     */
    data class ShowError(val message: String) : DatabaseViewEffect()
}
