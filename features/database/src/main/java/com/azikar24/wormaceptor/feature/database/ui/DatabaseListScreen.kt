package com.azikar24.wormaceptor.feature.database.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.feature.database.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Screen displaying list of available databases.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseListScreen(
    databases: ImmutableList<DatabaseInfo>,
    searchQuery: String,
    isLoading: Boolean,
    error: String?,
    onSearchQueryChanged: (String) -> Unit,
    onDatabaseClick: (DatabaseInfo) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var searchActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.database_list_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.database_list_back),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { searchActive = !searchActive }) {
                            Icon(
                                imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = stringResource(R.string.database_list_search),
                            )
                        }
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.database_list_refresh),
                            )
                        }
                    },
                )

                AnimatedVisibility(
                    visible = searchActive,
                    enter = expandVertically(
                        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
                    ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
                    exit = shrinkVertically(
                        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast),
                    ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
                ) {
                    WormaCeptorSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChanged,
                        placeholder = stringResource(R.string.database_list_search_placeholder),
                        onSearch = { searchActive = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                            .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
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

            databases.isEmpty() -> {
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
                            imageVector = Icons.Default.Storage,
                            contentDescription = stringResource(R.string.database_list_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xxxl),
                        )
                        Text(
                            text = stringResource(R.string.database_list_empty),
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
                    contentPadding = PaddingValues(
                        bottom = WormaCeptorDesignSystem.Spacing.lg,
                    ),
                ) {
                    items(
                        items = databases,
                        key = { it.path },
                    ) { database ->
                        DatabaseListItem(
                            database = database,
                            onClick = { onDatabaseClick(database) },
                        )
                        WormaCeptorDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun DatabaseListItem(database: DatabaseInfo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = database.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        supportingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                Text(
                    text = stringResource(R.string.database_list_tables_count, database.tableCount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = formatBytes(database.sizeBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
    )
}

@SuppressLint("SdCardPath")
@Preview(showBackground = true)
@Composable
private fun DatabaseListScreenPreview() {
    WormaCeptorTheme {
        DatabaseListScreen(
            databases = persistentListOf(
                DatabaseInfo(
                    name = "app_database.db",
                    path = "/data/data/com.example/databases/app_database.db",
                    sizeBytes = 524_288L,
                    tableCount = 5,
                ),
                DatabaseInfo(
                    name = "cache.db",
                    path = "/data/data/com.example/databases/cache.db",
                    sizeBytes = 131_072L,
                    tableCount = 2,
                ),
            ),
            searchQuery = "",
            isLoading = false,
            error = null,
            onSearchQueryChanged = {},
            onDatabaseClick = {},
            onRefresh = {},
            onBack = {},
        )
    }
}
