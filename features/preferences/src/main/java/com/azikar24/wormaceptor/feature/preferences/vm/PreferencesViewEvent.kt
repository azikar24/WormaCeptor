package com.azikar24.wormaceptor.feature.preferences.vm

import com.azikar24.wormaceptor.domain.entities.PreferenceItem
import com.azikar24.wormaceptor.domain.entities.PreferenceValue

sealed class PreferencesViewEvent {

    sealed class List : PreferencesViewEvent() {
        data object SearchToggled : List()
        data class SearchQueryChanged(val query: String) : List()
        data class Selected(val fileName: String) : List()
        data object SelectionCleared : List()
        data object BackPressed : List()
    }

    sealed class Detail : PreferencesViewEvent() {
        data class SearchQueryChanged(val query: String) : Detail()
        data class TypeFilterChanged(val typeName: String?) : Detail()
        data object FiltersCleared : Detail()
        data class PreferenceSet(val key: String, val value: PreferenceValue) : Detail()
        data class PreferenceDeleted(val key: String) : Detail()
        data object FileCleared : Detail()
        data class PreferenceCreated(val key: String, val value: PreferenceValue) : Detail()
        data class EditSheetOpened(val item: PreferenceItem?) : Detail()
        data object EditSheetDismissed : Detail()
        data class DeleteConfirmShown(val key: String) : Detail()
        data object DeleteConfirmDismissed : Detail()
        data object ClearConfirmShown : Detail()
        data object ClearConfirmDismissed : Detail()
    }

    sealed class Editor : PreferencesViewEvent() {
        data class KeyChanged(val value: String) : Editor()
        data class TypeSelected(val type: String) : Editor()
        data class TypeDropdownExpandedChanged(val expanded: Boolean) : Editor()
        data class StringValueChanged(val value: String) : Editor()
        data class IntValueChanged(val value: String) : Editor()
        data class LongValueChanged(val value: String) : Editor()
        data class FloatValueChanged(val value: String) : Editor()
        data class BooleanValueChanged(val value: Boolean) : Editor()
        data class NewStringSetItemChanged(val value: String) : Editor()
        data object AddStringSetItem : Editor()
        data class RemoveStringSetItem(val index: Int) : Editor()
        data object SaveRequested : Editor()
        data object DeleteRequested : Editor()
    }
}
