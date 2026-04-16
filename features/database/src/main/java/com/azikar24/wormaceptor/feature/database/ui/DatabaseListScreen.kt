package com.azikar24.wormaceptor.feature.database.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.list.WormaCeptorListItem
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.feature.database.R
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewEvent
import com.azikar24.wormaceptor.feature.database.vm.DatabaseViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseListScreen(
    state: DatabaseViewState,
    onEvent: (DatabaseViewEvent) -> Unit,
    onDatabaseClick: (DatabaseInfo) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            DatabaseListTopBar(
                searchActive = state.isDatabaseSearchActive,
                searchQuery = state.databaseSearchQuery,
                onToggleSearch = { onEvent(DatabaseViewEvent.List.ToggleSearch) },
                onSearchQueryChange = { onEvent(DatabaseViewEvent.List.SearchQueryChanged(it)) },
                onRefresh = { onEvent(DatabaseViewEvent.List.Load) },
                onBack = onBack,
            )
        },
        modifier = modifier,
    ) { padding ->
        DatabaseListBody(
            state = state,
            onDatabaseClick = onDatabaseClick,
            modifier = Modifier.padding(padding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatabaseListTopBar(
    searchActive: Boolean,
    searchQuery: String,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

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
                IconButton(
                    onClick = {
                        if (searchActive) keyboardController?.hide()
                        onToggleSearch()
                    },
                ) {
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
            enter = WormaCeptorTokens.Animations.expandFadeIn,
            exit = WormaCeptorTokens.Animations.shrinkFadeOut,
        ) {
            WormaCeptorSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                placeholder = stringResource(R.string.database_list_search_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorTokens.Spacing.lg)
                    .padding(vertical = WormaCeptorTokens.Spacing.sm),
            )
        }
    }
}

@Composable
private fun DatabaseListBody(
    state: DatabaseViewState,
    onDatabaseClick: (DatabaseInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().imePadding()) {
        val error = state.databasesError
        if (error != null) {
            DatabaseErrorState(error = error)
        } else {
            WormaCeptorLoadableContent(
                isLoading = state.isDatabasesLoading,
                isEmpty = state.databases.isEmpty(),
                loading = { WormaCeptorListSkeleton(modifier = Modifier.fillMaxSize()) },
                empty = { DatabaseEmptyState() },
                content = {
                    DatabaseLoadedList(
                        databases = state.databases,
                        onDatabaseClick = onDatabaseClick,
                    )
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DatabaseErrorState(
    error: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
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
private fun DatabaseEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = stringResource(R.string.database_list_title),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(WormaCeptorTokens.Spacing.xxxl),
            )
            Text(
                text = stringResource(R.string.database_list_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DatabaseLoadedList(
    databases: ImmutableList<DatabaseInfo>,
    onDatabaseClick: (DatabaseInfo) -> Unit,
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

@Composable
private fun DatabaseListItem(
    database: DatabaseInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WormaCeptorListItem(
        headline = database.name,
        supporting = stringResource(R.string.database_list_tables_count, database.tableCount) +
            " · " + formatBytes(database.sizeBytes),
        leadingContent = {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        onClick = onClick,
        modifier = modifier,
    )
}

@SuppressLint("SdCardPath")
@Preview(showBackground = true)
@Composable
private fun DatabaseListScreenPreview() {
    WormaCeptorTheme {
        DatabaseListScreen(
            state = DatabaseViewState(
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
            ),
            onEvent = {},
            onDatabaseClick = {},
            onBack = {},
        )
    }
}
