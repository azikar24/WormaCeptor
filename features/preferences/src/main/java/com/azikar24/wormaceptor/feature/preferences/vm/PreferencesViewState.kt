package com.azikar24.wormaceptor.feature.preferences.vm

import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class PreferencesViewState(
    val isFileSearchActive: Boolean = false,
    val fileSearchQuery: String = "",
    val selectedFileName: String? = null,
    val itemSearchQuery: String = "",
    val typeFilter: String? = null,
    val isLoading: Boolean = false,
    val isFilesLoading: Boolean = true,
    val isItemsLoading: Boolean = false,
    val preferenceFiles: ImmutableList<PreferenceFile> = persistentListOf(),
    val preferenceItems: ImmutableList<PreferenceItem> = persistentListOf(),
    val availableTypes: ImmutableList<String> = persistentListOf(),
    val totalItemCount: Int = 0,
    val editingItem: PreferenceItem? = null,
    val showEditSheet: Boolean = false,
    val editor: PreferenceEditorState = PreferenceEditorState(),
    val showDeleteConfirmKey: String? = null,
    val showClearConfirmDialog: Boolean = false,
)
