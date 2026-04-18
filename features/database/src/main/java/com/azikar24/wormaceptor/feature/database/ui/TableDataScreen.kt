package com.azikar24.wormaceptor.feature.database.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.appbar.WormaCeptorTopBar
import com.azikar24.wormaceptor.core.ui.components.divider.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.feature.database.R
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewEvent
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private const val PageSize = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDataScreen(
    state: DatabaseViewState,
    onEvent: (DatabaseViewEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TableDataTopBar(
                tableName = state.selectedTableName ?: "",
                currentPage = state.currentPage,
                showSchema = state.showSchema,
                hasNextPage = state.queryResult?.rowCount == PageSize,
                onToggleSchema = { onEvent(DatabaseViewEvent.Data.ToggleSchema) },
                onPrevious = { onEvent(DatabaseViewEvent.Data.PreviousPage) },
                onNext = { onEvent(DatabaseViewEvent.Data.NextPage) },
                onBack = onBack,
            )
        },
        modifier = modifier,
    ) { padding ->
        TableDataBody(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding(),
        )
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TableDataTopBar(
    tableName: String,
    currentPage: Int,
    showSchema: Boolean,
    hasNextPage: Boolean,
    onToggleSchema: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    WormaCeptorTopBar(
        title = tableName,
        subtitle = stringResource(R.string.database_table_data_page, currentPage + 1),
        onBack = onBack,
        backContentDescription = stringResource(R.string.database_table_data_back),
        actions = {
            IconButton(onClick = onToggleSchema) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = stringResource(R.string.database_table_data_schema),
                    tint = if (showSchema) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                )
            }
            IconButton(
                onClick = onPrevious,
                enabled = currentPage > 0,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = stringResource(R.string.database_table_data_previous),
                )
            }
            IconButton(
                onClick = onNext,
                enabled = hasNextPage,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = stringResource(R.string.database_table_data_next),
                )
            }
        },
    )
}

@Composable
private fun TableDataBody(
    state: DatabaseViewState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when {
            state.isDataLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.queryResult?.error != null -> {
                Text(
                    text = state.queryResult.error ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            state.showSchema -> {
                SchemaView(
                    schema = state.tableSchema,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            state.queryResult != null -> {
                DataTable(
                    result = state.queryResult,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun SchemaView(
    schema: ImmutableList<ColumnInfo>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(
            items = schema,
            key = { it.name },
        ) { column ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WormaCeptorTokens.Spacing.lg,
                        vertical = WormaCeptorTokens.Spacing.md,
                    ),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = column.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        if (column.isPrimaryKey) {
                            Text(
                                text = stringResource(R.string.database_table_data_pk),
                                style = MaterialTheme.typography.labelSmall,
                                color = WormaCeptorTokens.Colors.Database.primaryKey,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                    ) {
                        Text(
                            text = column.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = WormaCeptorTokens.Colors.Database.forDataType(column.type),
                        )
                        if (column.isNullable) {
                            Text(
                                text = stringResource(R.string.database_table_data_nullable),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            WormaCeptorDivider()
        }
    }
}

@Composable
private fun DataTable(
    result: QueryResult,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()

    LazyColumn(
        modifier = modifier.horizontalScroll(horizontalScrollState),
    ) {
        // Header row
        item {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(vertical = WormaCeptorTokens.Spacing.xs),
            ) {
                result.columns.forEach { column ->
                    Box(
                        modifier = Modifier
                            .widthIn(
                                min = WormaCeptorTokens.ComponentSize.tableCellMinWidth,
                                max = WormaCeptorTokens.ComponentSize.tableCellMaxWidth,
                            )
                            .padding(horizontal = WormaCeptorTokens.Spacing.sm),
                    ) {
                        Text(
                            text = column,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            WormaCeptorDivider(style = DividerStyle.Thick)
        }

        // Data rows
        itemsIndexed(
            items = result.rows,
            key = { index, _ -> "row_$index" },
        ) { _, row ->
            Row(
                modifier = Modifier.padding(vertical = WormaCeptorTokens.Spacing.xs),
            ) {
                val nullValue = stringResource(R.string.database_table_data_null_value)
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .widthIn(
                                min = WormaCeptorTokens.ComponentSize.tableCellMinWidth,
                                max = WormaCeptorTokens.ComponentSize.tableCellMaxWidth,
                            )
                            .padding(horizontal = WormaCeptorTokens.Spacing.sm),
                    ) {
                        Text(
                            text = cell?.toString() ?: nullValue,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = if (cell == null) {
                                WormaCeptorTokens.Colors.Database.nullValue
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            WormaCeptorDivider()
        }

        // Empty state
        if (result.rows.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WormaCeptorTokens.Spacing.xl),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.database_table_data_no_data),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TableDataScreenPreview() {
    WormaCeptorTheme {
        TableDataScreen(
            state = DatabaseViewState(
                selectedTableName = "users",
                queryResult = QueryResult(
                    columns = listOf("id", "name", "email"),
                    rows = listOf(
                        listOf(1, "Alice", "alice@example.com"),
                        listOf(2, "Bob", "bob@example.com"),
                    ),
                    rowCount = 2,
                ),
                tableSchema = persistentListOf(
                    ColumnInfo(name = "id", type = "INTEGER", isPrimaryKey = true, isNullable = false),
                    ColumnInfo(name = "name", type = "TEXT", isPrimaryKey = false, isNullable = false),
                    ColumnInfo(name = "email", type = "TEXT", isPrimaryKey = false, isNullable = true),
                ),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
