package com.azikar24.wormaceptor.feature.viewer.vm

data class HomeViewState(
    val selectedTabIndex: Int = 0,
    val showOverflowMenu: Boolean = false,
    val toolsSearchActive: Boolean = false,
    val toolsSearchQuery: String = "",
    val collapsedToolCategories: Set<String> = emptySet(),
)
