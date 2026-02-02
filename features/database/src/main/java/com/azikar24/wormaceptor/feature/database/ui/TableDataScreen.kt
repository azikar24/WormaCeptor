/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.database.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.feature.database.R
import com.azikar24.wormaceptor.feature.database.ui.theme.DatabaseDesignSystem
import kotlinx.collections.immutable.ImmutableList

/**
 * Screen displaying table data with pagination.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableDataScreen(
    tableName: String,
    queryResult: QueryResult?,
    schema: ImmutableList<ColumnInfo>,
    showSchema: Boolean,
    currentPage: Int,
    isLoading: Boolean,
    onToggleSchema: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(tableName)
                        Text(
                            text = stringResource(R.string.database_table_data_page, currentPage + 1),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.database_table_data_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSchema) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(R.string.database_table_data_schema),
                            tint = if (showSchema) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    IconButton(
                        onClick = onPreviousPage,
                        enabled = currentPage > 0,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                            contentDescription = stringResource(R.string.database_table_data_previous),
                        )
                    }
                    IconButton(
                        onClick = onNextPage,
                        enabled = queryResult?.rowCount == 100,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                            contentDescription = stringResource(R.string.database_table_data_next),
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            queryResult?.error != null -> {
                val errorMessage = queryResult.error ?: ""
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            showSchema -> {
                SchemaView(
                    schema = schema,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }

            queryResult != null -> {
                DataTable(
                    result = queryResult,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                )
            }
        }
    }
}

@Composable
private fun SchemaView(schema: ImmutableList<ColumnInfo>, modifier: Modifier = Modifier) {
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
                        horizontal = WormaCeptorDesignSystem.Spacing.lg,
                        vertical = WormaCeptorDesignSystem.Spacing.md,
                    ),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
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
                                color = DatabaseDesignSystem.DataTypeColors.primaryKey,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Text(
                            text = column.type,
                            style = MaterialTheme.typography.bodySmall,
                            color = DatabaseDesignSystem.DataTypeColors.forType(column.type),
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
            HorizontalDivider()
        }
    }
}

@Composable
private fun DataTable(result: QueryResult, modifier: Modifier = Modifier) {
    val horizontalScrollState = rememberScrollState()

    LazyColumn(
        modifier = modifier.horizontalScroll(horizontalScrollState),
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
            HorizontalDivider(thickness = 2.dp)
        }

        // Data rows
        items(
            items = result.rows,
            key = { it.hashCode() },
        ) { row ->
            Row(
                modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.xs),
            ) {
                val nullValue = stringResource(R.string.database_table_data_null_value)
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .widthIn(min = 100.dp, max = 200.dp)
                            .padding(horizontal = WormaCeptorDesignSystem.Spacing.sm),
                    ) {
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
            HorizontalDivider()
        }

        // Empty state
        if (result.rows.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WormaCeptorDesignSystem.Spacing.xl),
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
