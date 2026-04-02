package com.azikar24.wormaceptor.feature.preferences.vm

import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Consolidated UI state for the SharedPreferences Inspector screens.
 *
 * @property fileSearchQuery Current search query for filtering the preference file list.
 * @property selectedFileName Name of the currently selected preference file, or null if none selected.
 * @property itemSearchQuery Current search query for filtering items within the selected file.
 * @property typeFilter Active type filter name, or null to show all types.
 * @property isLoading Whether a write or delete operation is in progress.
 * @property preferenceFiles Filtered and sorted list of discovered SharedPreferences files.
 * @property preferenceItems Filtered preference items for the currently selected file.
 * @property availableTypes Distinct value type names available in the selected file, for filter chips.
 * @property totalItemCount Total number of preference entries in the selected file, before filtering.
 */
data class PreferencesViewState(
    val fileSearchQuery: String = "",
    val selectedFileName: String? = null,
    val itemSearchQuery: String = "",
    val typeFilter: String? = null,
    val isLoading: Boolean = false,
    val preferenceFiles: ImmutableList<PreferenceFile> = persistentListOf(),
    val preferenceItems: ImmutableList<PreferenceItem> = persistentListOf(),
    val availableTypes: ImmutableList<String> = persistentListOf(),
    val totalItemCount: Int = 0,
)
