package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.components.TextWithStartEllipsis
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewEvent

/**
 * Combined header for the transaction detail screen: top app bar + tab row.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionDetailHeader(
    title: String,
    showSearch: Boolean,
    showMenu: Boolean,
    currentTabHasContent: Boolean,
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onEvent: (TransactionDetailViewEvent) -> Unit,
    onBack: () -> Unit,
    onNavigatePrev: () -> Unit,
    onNavigateNext: () -> Unit,
    canNavigatePrev: Boolean,
    canNavigateNext: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SwipeableTopBar(
            onSwipeLeft = onNavigateNext,
            onSwipeRight = onNavigatePrev,
            canSwipeLeft = canNavigateNext,
            canSwipeRight = canNavigatePrev,
        ) {
            TopAppBar(
                title = {
                    TextWithStartEllipsis(
                        text = title,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.viewer_transaction_detail_back),
                        )
                    }
                },
                actions = {
                    SearchAction(
                        showSearch = showSearch,
                        currentTabHasContent = currentTabHasContent,
                        selectedTabIndex = selectedTabIndex,
                        onEvent = onEvent,
                    )

                    MenuAction(
                        showMenu = showMenu,
                        onEvent = onEvent,
                    )
                },
            )
        }

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = tabTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selectedTabIndex == index) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchAction(
    showSearch: Boolean,
    currentTabHasContent: Boolean,
    selectedTabIndex: Int,
    onEvent: (TransactionDetailViewEvent) -> Unit,
) {
    if (showSearch) {
        IconButton(
            onClick = { onEvent(TransactionDetailViewEvent.Search.VisibilityChanged(false)) },
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.viewer_transaction_detail_close_search),
            )
        }
    } else if (selectedTabIndex > 0 && currentTabHasContent) {
        IconButton(
            onClick = { onEvent(TransactionDetailViewEvent.Search.VisibilityChanged(true)) },
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.viewer_transaction_detail_search),
            )
        }
    }
}

@Composable
private fun MenuAction(
    showMenu: Boolean,
    onEvent: (TransactionDetailViewEvent) -> Unit,
) {
    IconButton(
        onClick = { onEvent(TransactionDetailViewEvent.Menu.VisibilityChanged(true)) },
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.viewer_transaction_detail_options),
        )
    }

    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { onEvent(TransactionDetailViewEvent.Menu.VisibilityChanged(false)) },
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.viewer_transaction_detail_copy_as_text)) },
            onClick = { onEvent(TransactionDetailViewEvent.Menu.CopyAsText) },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.viewer_transaction_detail_copy_as_curl)) },
            onClick = { onEvent(TransactionDetailViewEvent.Menu.CopyAsCurl) },
        )
        WormaCeptorDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.viewer_transaction_detail_share_as_json)) },
            onClick = { onEvent(TransactionDetailViewEvent.Menu.ShareAsJson) },
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.viewer_transaction_detail_share_as_har)) },
            onClick = { onEvent(TransactionDetailViewEvent.Menu.ShareAsHar) },
        )
    }
}
