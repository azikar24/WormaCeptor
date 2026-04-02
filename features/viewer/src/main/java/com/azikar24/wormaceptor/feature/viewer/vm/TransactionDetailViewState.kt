package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.entities.NetworkTransaction

/**
 * Full UI state for the transaction detail screen.
 */
internal data class TransactionDetailViewState(
    val transaction: NetworkTransaction? = null,
    val activeTabIndex: Int = 0,
    val showSearch: Boolean = false,
    val searchQuery: String = "",
    val debouncedSearchQuery: String = "",
    val currentMatchIndex: Int = 0,
    val showMenu: Boolean = false,
    val requestState: BodySectionState = BodySectionState(),
    val responseState: BodySectionState = BodySectionState(),
) {
    val requestMatchCount: Int get() = requestState.matches.size
    val responseMatchCount: Int get() = responseState.matches.size
}
