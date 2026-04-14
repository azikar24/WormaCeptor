package com.azikar24.wormaceptor.feature.database.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.divider.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.feature.database.R
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewEvent
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private val QueryInputMinHeight = 100.dp
private val QueryInputMaxHeight = 200.dp

/**
 * Screen for executing SQL queries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryScreen(
    state: DatabaseViewState,
    onEvent: (DatabaseViewEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            QueryTopBar(
                sqlQuery = state.sqlQuery,
                isExecuting = state.isQueryExecuting,
                onClear = { onEvent(DatabaseViewEvent.Query.Clear) },
                onExecute = { onEvent(DatabaseViewEvent.Query.Execute) },
                onBack = onBack,
            )
        },
        modifier = modifier,
    ) { padding ->
        QueryBody(
            state = state,
            onEvent = onEvent,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .imePadding(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QueryTopBar(
    sqlQuery: String,
    isExecuting: Boolean,
    onClear: () -> Unit,
    onExecute: () -> Unit,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.database_query_title)) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.database_query_back),
                )
            }
        },
        actions = {
            if (sqlQuery.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.database_query_clear),
                    )
                }
            }
            IconButton(
                onClick = onExecute,
                enabled = sqlQuery.isNotBlank() && !isExecuting,
            ) {
                if (isExecuting) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(WormaCeptorTokens.Spacing.sm),
                        strokeWidth = WormaCeptorTokens.Spacing.xxs,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.database_query_execute),
                    )
                }
            }
        },
    )
}

@Composable
private fun QueryBody(
    state: DatabaseViewState,
    onEvent: (DatabaseViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        QueryInputField(
            sqlQuery = state.sqlQuery,
            onQueryChange = { onEvent(DatabaseViewEvent.Query.SqlChanged(it)) },
        )

        WormaCeptorDivider()

        QueryResults(
            databaseName = state.selectedDatabaseName ?: "",
            queryResult = state.queryExecutionResult,
            queryHistory = state.queryHistory,
            onSelectHistory = { onEvent(DatabaseViewEvent.Query.HistorySelected(it)) },
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
        )
    }
}

@Composable
private fun QueryInputField(
    sqlQuery: String,
    onQueryChange: (String) -> Unit,
) {
    BasicTextField(
        value = sqlQuery,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = QueryInputMinHeight, max = QueryInputMaxHeight)
            .background(
                MaterialTheme.colorScheme.surfaceVariant
                    .copy(alpha = WormaCeptorTokens.Alpha.BOLD),
            )
            .padding(WormaCeptorTokens.Spacing.md),
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Ascii,
            imeAction = ImeAction.None,
        ),
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (sqlQuery.isEmpty()) {
                    Text(
                        text = stringResource(R.string.database_query_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun QueryResults(
    databaseName: String,
    queryResult: QueryResult?,
    queryHistory: ImmutableList<String>,
    onSelectHistory: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        queryResult != null -> {
            QueryResultView(
                result = queryResult,
                modifier = modifier,
            )
        }

        queryHistory.isNotEmpty() -> {
            QueryHistoryView(
                history = queryHistory,
                onSelectQuery = onSelectHistory,
                modifier = modifier,
            )
        }

        else -> {
            QueryEmptyState(
                databaseName = databaseName,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun QueryEmptyState(
    databaseName: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.database_query_instruction),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.database_query_database_label, databaseName),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QueryResultView(
    result: QueryResult,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()

    val error = result.error
    if (error != null) {
        Box(
            modifier = modifier.padding(WormaCeptorTokens.Spacing.lg),
            contentAlignment = Alignment.TopStart,
        ) {
            Text(
                text = stringResource(R.string.database_query_error, error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    } else {
        Column(modifier = modifier) {
            Text(
                text = stringResource(R.string.database_query_rows_returned, result.rowCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScrollState),
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
                                    .widthIn(min = 100.dp, max = 200.dp)
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
                    val nullValue = stringResource(R.string.database_query_null_value)
                    Row(
                        modifier = Modifier.padding(vertical = WormaCeptorTokens.Spacing.xs),
                    ) {
                        row.forEach { cell ->
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 100.dp, max = 200.dp)
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
            }
        }
    }
}

@Composable
private fun QueryHistoryView(
    history: ImmutableList<String>,
    onSelectQuery: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reversedHistory = remember(history) { history.reversed() }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.database_query_history_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
        )

        LazyColumn {
            itemsIndexed(
                items = reversedHistory,
                key = { index, _ -> "history_$index" },
            ) { _, query ->
                ListItem(
                    modifier = Modifier.clickable { onSelectQuery(query) },
                    headlineContent = {
                        Text(
                            text = query,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
                WormaCeptorDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QueryScreenPreview() {
    WormaCeptorTheme {
        QueryScreen(
            state = DatabaseViewState(
                selectedDatabaseName = "app_database.db",
                sqlQuery = "SELECT * FROM users WHERE active = 1",
                queryHistory = persistentListOf(
                    "SELECT * FROM users",
                    "SELECT COUNT(*) FROM transactions",
                ),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
