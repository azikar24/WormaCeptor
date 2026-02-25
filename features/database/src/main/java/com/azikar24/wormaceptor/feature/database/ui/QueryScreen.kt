package com.azikar24.wormaceptor.feature.database.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.feature.database.R
import com.azikar24.wormaceptor.feature.database.ui.theme.DatabaseDesignSystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Screen for executing SQL queries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueryScreen(
    databaseName: String,
    sqlQuery: String,
    queryResult: QueryResult?,
    queryHistory: ImmutableList<String>,
    isExecuting: Boolean,
    onQueryChanged: (String) -> Unit,
    onExecuteQuery: () -> Unit,
    onClearQuery: () -> Unit,
    onSelectFromHistory: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
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
                        IconButton(onClick = onClearQuery) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.database_query_clear),
                            )
                        }
                    }
                    IconButton(
                        onClick = onExecuteQuery,
                        enabled = sqlQuery.isNotBlank() && !isExecuting,
                    ) {
                        if (isExecuting) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.sm),
                                strokeWidth = WormaCeptorDesignSystem.Spacing.xxs,
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
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            // Query input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 200.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.bold),
                    )
                    .padding(WormaCeptorDesignSystem.Spacing.md),
            ) {
                if (sqlQuery.isEmpty()) {
                    Text(
                        text = stringResource(R.string.database_query_placeholder),
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                BasicTextField(
                    value = sqlQuery,
                    onValueChange = onQueryChanged,
                    modifier = Modifier.fillMaxSize(),
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                )
            }

            WormaCeptorDivider()

            // Results or History
            when {
                queryResult != null -> {
                    QueryResultView(
                        result = queryResult,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                    )
                }

                queryHistory.isNotEmpty() -> {
                    QueryHistoryView(
                        history = queryHistory,
                        onSelectQuery = onSelectFromHistory,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
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
            }
        }
    }
}

@Composable
private fun QueryResultView(
    result: QueryResult,
    modifier: Modifier = Modifier,
) {
    val horizontalScrollState = rememberScrollState()

    if (result.error != null) {
        Box(
            modifier = modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            contentAlignment = Alignment.TopStart,
        ) {
            Text(
                text = stringResource(R.string.database_query_error, result.error ?: ""),
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
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
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
                            .padding(vertical = WormaCeptorDesignSystem.Spacing.xs),
                    ) {
                        result.columns.forEach { column ->
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 100.dp, max = 200.dp)
                                    .padding(horizontal = WormaCeptorDesignSystem.Spacing.sm),
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
                items(
                    items = result.rows,
                    key = { it.hashCode() },
                ) { row ->
                    Row(
                        modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.xs),
                    ) {
                        row.forEach { cell ->
                            Box(
                                modifier = Modifier
                                    .widthIn(min = 100.dp, max = 200.dp)
                                    .padding(horizontal = WormaCeptorDesignSystem.Spacing.sm),
                            ) {
                                val nullValue = stringResource(R.string.database_query_null_value)
                                Text(
                                    text = cell?.toString() ?: nullValue,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (cell == null) {
                                        DatabaseDesignSystem.DataTypeColors.nullValue
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
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.database_query_history_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
        )

        LazyColumn {
            items(
                items = history.reversed(),
                key = { it },
            ) { query ->
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
            databaseName = "app_database.db",
            sqlQuery = "SELECT * FROM users WHERE active = 1",
            queryResult = null,
            queryHistory = persistentListOf(
                "SELECT * FROM users",
                "SELECT COUNT(*) FROM transactions",
            ),
            isExecuting = false,
            onQueryChanged = {},
            onExecuteQuery = {},
            onClearQuery = {},
            onSelectFromHistory = {},
            onBack = {},
        )
    }
}
