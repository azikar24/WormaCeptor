package com.azikar24.wormaceptor.feature.viewer.vm

import com.azikar24.wormaceptor.domain.contracts.ContentType
import com.azikar24.wormaceptor.domain.entities.ImageMetadata
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.feature.viewer.ui.MatchInfo

/**
 * State for a single body section (request or response).
 * Shared structure avoids duplicating fields between request/response.
 */
data class BodySectionState(
    val parsedBody: String? = null,
    val rawBody: String? = null,
    val rawBodyBytes: ByteArray? = null,
    val isLoading: Boolean = false,
    val parsedContentType: ContentType = ContentType.UNKNOWN,
    val isPrettyMode: Boolean = true,
    val headersExpanded: Boolean = true,
    val bodyExpanded: Boolean = true,
    val isCopying: Boolean = false,
    val matches: List<MatchInfo> = emptyList(),
    val imageMetadata: ImageMetadata? = null,
    val showImageViewer: Boolean = false,
    val showPdfViewer: Boolean = false,
) {
    // ByteArray needs custom equals/hashCode
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BodySectionState) return false
        return parsedBody == other.parsedBody &&
            rawBody == other.rawBody &&
            rawBodyBytes.contentEquals(other.rawBodyBytes) &&
            isLoading == other.isLoading &&
            parsedContentType == other.parsedContentType &&
            isPrettyMode == other.isPrettyMode &&
            headersExpanded == other.headersExpanded &&
            bodyExpanded == other.bodyExpanded &&
            isCopying == other.isCopying &&
            matches == other.matches &&
            imageMetadata == other.imageMetadata &&
            showImageViewer == other.showImageViewer &&
            showPdfViewer == other.showPdfViewer
    }

    override fun hashCode(): Int {
        var result = parsedBody.hashCode()
        result = 31 * result + rawBody.hashCode()
        result = 31 * result + (rawBodyBytes?.contentHashCode() ?: 0)
        result = 31 * result + isLoading.hashCode()
        result = 31 * result + parsedContentType.hashCode()
        result = 31 * result + isPrettyMode.hashCode()
        result = 31 * result + headersExpanded.hashCode()
        result = 31 * result + bodyExpanded.hashCode()
        result = 31 * result + isCopying.hashCode()
        result = 31 * result + matches.hashCode()
        result = 31 * result + imageMetadata.hashCode()
        result = 31 * result + showImageViewer.hashCode()
        result = 31 * result + showPdfViewer.hashCode()
        return result
    }
}

/**
 * Full UI state for the transaction detail screen.
 */
data class TransactionDetailViewState(
    val transaction: NetworkTransaction? = null,
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
