package com.azikar24.wormaceptor.feature.viewer.vm

/** All user-initiated actions dispatched from the home screen UI. */
sealed class HomeViewEvent {
    /**
     * User selected a different tab (Transactions / Crashes / Tools).
     *
     * @property index Zero-based index of the newly selected tab.
     */
    data class TabSelected(val index: Int) : HomeViewEvent()

    /**
     * Overflow menu visibility changed.
     *
     * @property visible Whether the menu should be visible.
     */
    data class OverflowMenuVisibilityChanged(val visible: Boolean) : HomeViewEvent()

    /**
     * Tools tab search bar active state changed.
     *
     * @property active Whether the search bar should be active. When set to false, the search query is also cleared.
     */
    data class ToolsSearchActiveChanged(val active: Boolean) : HomeViewEvent()

    /**
     * Tools tab search query text changed.
     *
     * @property query The current text in the tools search field.
     */
    data class ToolsSearchQueryChanged(val query: String) : HomeViewEvent()

    /**
     * User toggled collapse/expand on a tool category.
     *
     * @property category The category name that was toggled.
     */
    data class ToolCategoryCollapseToggled(val category: String) : HomeViewEvent()

    /**
     * Display a snackbar message to the user.
     *
     * @property message The text to show in the snackbar.
     */
    data class ShowMessage(val message: String) : HomeViewEvent()
}
