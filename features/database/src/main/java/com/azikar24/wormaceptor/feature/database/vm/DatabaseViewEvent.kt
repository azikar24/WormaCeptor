package com.azikar24.wormaceptor.feature.database.vm

sealed class DatabaseViewEvent {

    sealed class List : DatabaseViewEvent() {
        data object Load : List()
        data class SearchQueryChanged(val query: String) : List()
        data object ToggleSearch : List()
        data class Selected(val name: String) : List()
        data object SelectionCleared : List()
    }

    sealed class Tables : DatabaseViewEvent() {
        data class SearchQueryChanged(val query: String) : Tables()
        data object ToggleSearch : Tables()
        data class Selected(val name: String) : Tables()
        data object SelectionCleared : Tables()
    }

    sealed class Data : DatabaseViewEvent() {
        data object ToggleSchema : Data()
        data object NextPage : Data()
        data object PreviousPage : Data()
    }

    sealed class Query : DatabaseViewEvent() {
        data class SqlChanged(val query: String) : Query()
        data object Execute : Query()
        data object Clear : Query()
        data class HistorySelected(val query: String) : Query()
        data class PrefilledRequested(val tableName: String, val queryType: String) : Query()
    }
}
