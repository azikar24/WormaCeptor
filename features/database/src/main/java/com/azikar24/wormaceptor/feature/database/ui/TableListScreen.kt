package com.azikar24.wormaceptor.feature.database.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.list.WormaCeptorListItem
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.TableInfo
import com.azikar24.wormaceptor.feature.database.R
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewEvent
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableListScreen(
    state: DatabaseViewState,
    onEvent: (DatabaseViewEvent) -> Unit,
    onTableClick: (TableInfo) -> Unit,
    onQueryClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TableListTopBar(
                state = state,
                onEvent = onEvent,
                onQueryClick = onQueryClick,
                onBack = onBack,
            )
        },
        modifier = modifier,
    ) { padding ->
        TableListBody(
            state = state,
            onTableClick = onTableClick,
            modifier = Modifier.padding(padding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableListTopBar(
    state: DatabaseViewState,
    onEvent: (DatabaseViewEvent) -> Unit,
    onQueryClick: () -> Unit,
    onBack: () -> Unit,
) {
    Column {
        WormaCeptorTopBar(
            title = state.selectedDatabaseName ?: "",
            subtitle = stringResource(R.string.database_table_list_tables_count, state.tables.size),
            onBack = onBack,
            backContentDescription = stringResource(R.string.database_table_list_back),
            actions = {
                IconButton(onClick = { onEvent(DatabaseViewEvent.Tables.ToggleSearch) }) {
                    Icon(
                        imageVector = if (state.isTableSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = stringResource(R.string.database_table_list_search),
                    )
                }
                IconButton(onClick = onQueryClick) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = stringResource(R.string.database_table_list_sql_query),
                    )
                }
            },
        )

        if (state.isTableSearchActive) {
            WormaCeptorSearchBar(
                query = state.tableSearchQuery,
                onQueryChange = { onEvent(DatabaseViewEvent.Tables.SearchQueryChanged(it)) },
                placeholder = stringResource(R.string.database_table_list_search_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorTokens.Spacing.lg)
                    .padding(vertical = WormaCeptorTokens.Spacing.sm),
            )
        }
    }
}

@Composable
private fun TableListBody(
    state: DatabaseViewState,
    onTableClick: (TableInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().imePadding()) {
        val error = state.tablesError
        if (error != null) {
            TableErrorState(error = error)
        } else {
            WormaCeptorLoadableContent(
                isLoading = state.isTablesLoading,
                isEmpty = state.tables.isEmpty(),
                loading = { WormaCeptorListSkeleton(modifier = Modifier.fillMaxSize()) },
                empty = { TableEmptyState() },
                content = {
                    TableLoadedList(
                        tables = state.tables,
                        onTableClick = onTableClick,
                    )
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun TableErrorState(
    error: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun TableEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().navigationBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = stringResource(R.string.database_table_list_empty),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(WormaCeptorTokens.Spacing.xxxl),
            )
            Text(
                text = stringResource(R.string.database_table_list_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TableLoadedList(
    tables: ImmutableList<TableInfo>,
    onTableClick: (TableInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = WormaCeptorTokens.Spacing.lg +
                WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding(),
        ),
    ) {
        items(
            items = tables,
            key = { it.name },
        ) { table ->
            TableListItem(
                table = table,
                onClick = { onTableClick(table) },
            )
            WormaCeptorDivider()
        }
    }
}

@Composable
private fun TableListItem(
    table: TableInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorListItem(
        headline = table.name,
        supporting = stringResource(R.string.database_table_list_rows_count, table.rowCount) +
            " · " + stringResource(R.string.database_table_list_columns_count, table.columnCount),
        leadingContent = {
            Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        },
        onClick = onClick,
        modifier = modifier,
    )
}

@Preview(showBackground = true, name = "Loaded")
@Composable
private fun TableListScreenPreview() {
    WormaCeptorTheme {
        TableListScreen(
            state = DatabaseViewState(
                selectedDatabaseName = "app_database.db",
                tables = persistentListOf(
                    TableInfo(name = "users", rowCount = 150L, columnCount = 6),
                    TableInfo(name = "transactions", rowCount = 1024L, columnCount = 12),
                    TableInfo(name = "settings", rowCount = 8L, columnCount = 3),
                ),
            ),
            onEvent = {},
            onTableClick = {},
            onQueryClick = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun TableListScreenLoadingPreview() {
    WormaCeptorTheme {
        TableListScreen(
            state = DatabaseViewState(
                selectedDatabaseName = "app_database.db",
                isTablesLoading = true,
            ),
            onEvent = {},
            onTableClick = {},
            onQueryClick = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun TableListScreenErrorPreview() {
    WormaCeptorTheme {
        TableListScreen(
            state = DatabaseViewState(
                selectedDatabaseName = "app_database.db",
                tablesError = "Failed to load tables",
            ),
            onEvent = {},
            onTableClick = {},
            onQueryClick = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun TableListScreenEmptyPreview() {
    WormaCeptorTheme {
        TableListScreen(
            state = DatabaseViewState(
                selectedDatabaseName = "app_database.db",
            ),
            onEvent = {},
            onTableClick = {},
            onQueryClick = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Search Active")
@Composable
private fun TableListScreenSearchPreview() {
    WormaCeptorTheme {
        TableListScreen(
            state = DatabaseViewState(
                selectedDatabaseName = "app_database.db",
                isTableSearchActive = true,
                tableSearchQuery = "user",
                tables = persistentListOf(
                    TableInfo(name = "users", rowCount = 150L, columnCount = 6),
                ),
            ),
            onEvent = {},
            onTableClick = {},
            onQueryClick = {},
            onBack = {},
        )
    }
}
