package com.azikar24.wormaceptor.feature.preferences.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.list.WormaCeptorListItem
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorListSkeleton
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorLoadableContent
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.feature.preferences.R
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewEvent
import com.azikar24.wormaceptor.feature.preferences.vm.PreferencesViewState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesListScreen(
    state: PreferencesViewState,
    onEvent: (PreferencesViewEvent) -> Unit,
    onFileClick: (PreferenceFile) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            PreferencesListTopBar(
                searchActive = state.isFileSearchActive,
                onToggleSearch = { onEvent(PreferencesViewEvent.List.SearchToggled) },
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier,
    ) { padding ->
        PreferencesListBody(
            state = state,
            onEvent = onEvent,
            onFileClick = onFileClick,
            searchActive = state.isFileSearchActive,
            modifier = Modifier.fillMaxSize().padding(padding).imePadding(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreferencesListTopBar(
    searchActive: Boolean,
    onToggleSearch: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.preferences_list_title)) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.preferences_back),
                )
            }
        },
        actions = {
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (searchActive) {
                        stringResource(R.string.preferences_close_search)
                    } else {
                        stringResource(R.string.preferences_search)
                    },
                )
            }
        },
    )
}

@Composable
private fun PreferencesListBody(
    state: PreferencesViewState,
    onEvent: (PreferencesViewEvent) -> Unit,
    onFileClick: (PreferenceFile) -> Unit,
    searchActive: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AnimatedVisibility(
            visible = searchActive,
            enter = WormaCeptorTokens.Animations.expandFadeIn,
            exit = WormaCeptorTokens.Animations.shrinkFadeOut,
        ) {
            WormaCeptorSearchBar(
                query = state.fileSearchQuery,
                onQueryChange = { onEvent(PreferencesViewEvent.List.SearchQueryChanged(it)) },
                placeholder = stringResource(R.string.preferences_search_placeholder),
                modifier = Modifier.padding(
                    start = WormaCeptorTokens.Spacing.md,
                    end = WormaCeptorTokens.Spacing.md,
                    top = WormaCeptorTokens.Spacing.sm,
                ),
            )
        }

        WormaCeptorLoadableContent(
            isLoading = state.isFilesLoading,
            isEmpty = state.preferenceFiles.isEmpty(),
            loading = { WormaCeptorListSkeleton(modifier = Modifier.fillMaxSize()) },
            empty = { PreferencesEmptyState(state.fileSearchQuery) },
            modifier = Modifier.fillMaxSize(),
        ) {
            PreferencesFilesList(
                files = state.preferenceFiles,
                onFileClick = onFileClick,
            )
        }
    }
}

@Composable
private fun PreferencesEmptyState(searchQuery: String) {
    WormaCeptorEmptyState(
        title = if (searchQuery.isNotBlank()) {
            stringResource(R.string.preferences_empty_no_matches)
        } else {
            stringResource(R.string.preferences_empty_no_files)
        },
        modifier = Modifier.fillMaxSize(),
        subtitle = if (searchQuery.isNotBlank()) {
            stringResource(R.string.preferences_empty_try_different_search)
        } else {
            stringResource(R.string.preferences_empty_files_will_appear)
        },
        icon = Icons.Default.Settings,
    )
}

@Composable
private fun PreferencesFilesList(
    files: ImmutableList<PreferenceFile>,
    onFileClick: (PreferenceFile) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = WormaCeptorTokens.Spacing.md,
            end = WormaCeptorTokens.Spacing.md,
            top = WormaCeptorTokens.Spacing.sm,
            bottom = WormaCeptorTokens.Spacing.xs +
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        items(files, key = { it.name }) { file ->
            PreferenceFileItem(
                file = file,
                onClick = { onFileClick(file) },
                modifier = Modifier.animateItem(),
            )
        }
    }
}

@Composable
private fun PreferenceFileItem(
    file: PreferenceFile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemCountLabel = if (file.itemCount == 1) {
        stringResource(R.string.preferences_item_count_singular)
    } else {
        stringResource(R.string.preferences_item_count_plural)
    }
    WormaCeptorListItem(
        headline = file.name,
        supporting = "${file.itemCount} $itemCountLabel",
        leadingContent = { PreferenceFileIcon() },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = WormaCeptorTokens.Alpha.BOLD,
                ),
            )
        },
        onClick = onClick,
        modifier = modifier
            .clip(WormaCeptorTokens.Shapes.card)
            .background(MaterialTheme.colorScheme.surface),
    )
}

@Composable
private fun PreferenceFileIcon() {
    Surface(
        shape = WormaCeptorTokens.Shapes.button,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = WormaCeptorTokens.Alpha.MODERATE),
        modifier = Modifier
            .padding(top = WormaCeptorTokens.Spacing.xxs)
            .size(WormaCeptorTokens.TouchTarget.minimum),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize(),
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = stringResource(R.string.preferences_list_title),
                modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun PreferencesListScreenPreview() {
    WormaCeptorTheme {
        PreferencesListScreen(
            state = PreferencesViewState(
                preferenceFiles = persistentListOf(
                    PreferenceFile(name = "app_preferences", itemCount = 12),
                    PreferenceFile(name = "user_settings", itemCount = 5),
                    PreferenceFile(name = "cache_config", itemCount = 3),
                ),
            ),
            onEvent = {},
            onFileClick = {},
            onNavigateBack = {},
        )
    }
}
