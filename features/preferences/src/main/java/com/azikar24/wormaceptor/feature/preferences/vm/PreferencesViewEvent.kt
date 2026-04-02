package com.azikar24.wormaceptor.feature.preferences.vm

import com.azikar24.wormaceptor.domain.entities.PreferenceValue

/**
 * User-initiated events dispatched to [PreferencesViewModel].
 */
sealed class PreferencesViewEvent {
    /**
     * Updates the search query used to filter the preference file list.
     *
     * @property query New search query.
     */
    data class FileSearchQueryChanged(val query: String) : PreferencesViewEvent()

    /**
     * Selects a preference file to display its entries.
     *
     * @property fileName Name of the file to select.
     */
    data class SelectFile(val fileName: String) : PreferencesViewEvent()

    /** Deselects the current file and resets item filters. */
    data object ClearFileSelection : PreferencesViewEvent()

    /**
     * Updates the search query used to filter items within the selected file.
     *
     * @property query New search query.
     */
    data class ItemSearchQueryChanged(val query: String) : PreferencesViewEvent()

    /**
     * Applies a type-based filter to show only items of the given type, or all if null.
     *
     * @property typeName Type name to filter by, or null to show all.
     */
    data class SetTypeFilter(val typeName: String?) : PreferencesViewEvent()

    /** Resets item search query and type filter to their defaults. */
    data object ClearFilters : PreferencesViewEvent()

    /**
     * Writes or updates a preference value in the currently selected file.
     *
     * @property key Preference key.
     * @property value New preference value.
     */
    data class SetPreference(val key: String, val value: PreferenceValue) : PreferencesViewEvent()

    /**
     * Removes a preference entry by key from the currently selected file.
     *
     * @property key Preference key to delete.
     */
    data class DeletePreference(val key: String) : PreferencesViewEvent()

    /** Removes all preference entries from the currently selected file. */
    data object ClearCurrentFile : PreferencesViewEvent()

    /**
     * Creates a new preference item in the current file.
     *
     * @property key Preference key.
     * @property value New preference value.
     */
    data class CreatePreference(val key: String, val value: PreferenceValue) : PreferencesViewEvent()
}
