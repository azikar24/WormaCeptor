package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.divider.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorSectionHeader
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.filter.FilterActionButtons
import com.azikar24.wormaceptor.feature.viewer.ui.components.filter.FilterHeader
import com.azikar24.wormaceptor.feature.viewer.ui.components.filter.MethodFilterBars
import com.azikar24.wormaceptor.feature.viewer.ui.components.filter.StatusFilterBars
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionListViewState
import kotlinx.collections.immutable.ImmutableMap

/** Bottom sheet UI for filtering network transactions by HTTP method and status code range. */
@Composable
fun FilterBottomSheetContent(
    state: TransactionListViewState,
    onEvent: (TransactionListViewEvent) -> Unit,
    filteredCount: Int,
    totalCount: Int,
    methodCounts: ImmutableMap<String, Int>,
    statusCounts: ImmutableMap<IntRange, Int>,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    val filtersActive = state.draftFilterMethods.isNotEmpty() ||
        state.draftFilterStatusRanges.isNotEmpty() ||
        state.draftFilterQuery.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        FilterHeader(
            filteredCount = filteredCount,
            totalCount = totalCount,
            filtersActive = filtersActive,
        )

        WormaCeptorDivider(style = DividerStyle.Subtle)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(WormaCeptorTokens.Spacing.lg),
        ) {
            WormaCeptorSearchBar(
                query = state.draftFilterQuery,
                onQueryChange = {
                    onEvent(TransactionListViewEvent.Filter.DraftQueryChanged(it))
                },
                placeholder = stringResource(R.string.viewer_filter_search_placeholder),
                onSearch = { focusManager.clearFocus() },
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))

            WormaCeptorSectionHeader(
                title = stringResource(R.string.viewer_filter_http_method),
                icon = Icons.Outlined.Code,
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

            MethodFilterBars(
                methodCounts = methodCounts,
                selectedMethods = state.draftFilterMethods,
                onMethodToggled = {
                    onEvent(TransactionListViewEvent.Filter.DraftMethodToggled(it))
                },
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))

            WormaCeptorSectionHeader(
                title = stringResource(R.string.viewer_filter_status_code),
                icon = Icons.Outlined.DataUsage,
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

            StatusFilterBars(
                statusCounts = statusCounts,
                selectedRanges = state.draftFilterStatusRanges,
                onStatusToggled = {
                    onEvent(TransactionListViewEvent.Filter.DraftStatusRangeToggled(it))
                },
            )

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))
        }

        FilterActionButtons(
            filtersActive = filtersActive,
            onClearAll = { onEvent(TransactionListViewEvent.Filter.DraftCleared) },
            onApply = {
                focusManager.clearFocus()
                onEvent(TransactionListViewEvent.Filter.Applied)
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FilterBottomSheetContentPreview() {
    WormaCeptorTheme {
        FilterBottomSheetContent(
            state = TransactionListViewState(
                draftFilterQuery = "",
                draftFilterMethods = setOf("GET"),
                draftFilterStatusRanges = setOf(200..299),
            ),
            onEvent = {},
            filteredCount = 42,
            totalCount = 100,
            methodCounts = kotlinx.collections.immutable.persistentMapOf(
                "GET" to 30,
                "POST" to 25,
                "PUT" to 10,
                "DELETE" to 5,
                "PATCH" to 2,
            ),
            statusCounts = kotlinx.collections.immutable.persistentMapOf(
                200..299 to 50,
                300..399 to 10,
                400..499 to 15,
                500..599 to 3,
            ),
        )
    }
}
