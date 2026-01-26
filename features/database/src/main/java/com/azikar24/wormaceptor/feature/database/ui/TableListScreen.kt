/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.database.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.TableInfo
import kotlinx.collections.immutable.ImmutableList

/**
 * Screen displaying list of tables in a database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableListScreen(
    databaseName: String,
    tables: ImmutableList<TableInfo>,
    searchQuery: String,
    isLoading: Boolean,
    error: String?,
    onSearchQueryChanged: (String) -> Unit,
    onTableClick: (TableInfo) -> Unit,
    onQueryClick: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column {
                            Text(databaseName)
                            Text(
                                text = "${tables.size} tables",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { searchActive = !searchActive }) {
                            Icon(
                                imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Search",
                            )
                        }
                        IconButton(onClick = onQueryClick) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "SQL Query",
                            )
                        }
                    },
                )

                if (searchActive) {
                    WormaCeptorSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChanged,
                        placeholder = "Search tables...",
                        onSearch = { searchActive = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                            .padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
                    )
                }
            }
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

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }

            tables.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            text = "No tables found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    items(
                        items = tables,
                        key = { it.name },
                    ) { table ->
                        TableListItem(
                            table = table,
                            onClick = { onTableClick(table) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun TableListItem(table: TableInfo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = table.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        supportingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                Text(
                    text = "${table.rowCount} rows",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${table.columnCount} columns",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
        },
    )
}
