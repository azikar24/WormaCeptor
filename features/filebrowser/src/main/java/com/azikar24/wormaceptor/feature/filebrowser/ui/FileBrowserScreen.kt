package com.azikar24.wormaceptor.feature.filebrowser.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.feature.filebrowser.R
import com.azikar24.wormaceptor.feature.filebrowser.ui.components.BreadcrumbBar
import com.azikar24.wormaceptor.feature.filebrowser.ui.components.FileListItem
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewEvent
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewState
import com.azikar24.wormaceptor.feature.filebrowser.vm.SortMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    state: FileBrowserViewState,
    onEvent: (FileBrowserViewEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackBarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var searchActive by rememberSaveable { mutableStateOf(false) }
    val currentOnEvent by rememberUpdatedState(onEvent)

    BackHandler {
        currentOnEvent(FileBrowserViewEvent.NavigateBack)
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackBarHostState.showSnackbar(it)
            currentOnEvent(FileBrowserViewEvent.ClearError)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            FileBrowserTopBar(
                state = state,
                onEvent = onEvent,
                searchActive = searchActive,
                onSearchActiveChange = { active ->
                    searchActive = active
                    if (!active) onEvent(FileBrowserViewEvent.SearchQueryChanged(""))
                },
                showSortMenu = showSortMenu,
                onShowSortMenuChange = { showSortMenu = it },
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = modifier,
    ) { padding ->
        FileBrowserBody(
            state = state,
            onEvent = onEvent,
            padding = padding,
        )
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileBrowserTopBar(
    state: FileBrowserViewState,
    onEvent: (FileBrowserViewEvent) -> Unit,
    searchActive: Boolean,
    onSearchActiveChange: (Boolean) -> Unit,
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
) {
    Column {
        TopAppBar(
            title = { Text(stringResource(R.string.filebrowser_title)) },
            navigationIcon = {
                IconButton(
                    onClick = { onEvent(FileBrowserViewEvent.NavigateBack) },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.filebrowser_back),
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { onSearchActiveChange(!searchActive) },
                ) {
                    Icon(
                        imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = stringResource(R.string.filebrowser_search),
                    )
                }

                SortMenuButton(
                    showSortMenu = showSortMenu,
                    onShowSortMenuChange = onShowSortMenuChange,
                    onEvent = onEvent,
                )
            },
        )

        AnimatedVisibility(
            visible = searchActive,
            enter = WormaCeptorTokens.Animations.expandFadeIn,
            exit = WormaCeptorTokens.Animations.shrinkFadeOut,
        ) {
            WormaCeptorSearchBar(
                query = state.searchQuery,
                onQueryChange = { onEvent(FileBrowserViewEvent.SearchQueryChanged(it)) },
                placeholder = stringResource(R.string.filebrowser_search_placeholder),
                onSearch = { onSearchActiveChange(false) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorTokens.Spacing.lg)
                    .padding(vertical = WormaCeptorTokens.Spacing.sm),
            )
        }

        BreadcrumbBar(
            isAtRoot = state.currentPath == null,
            navigationStack = state.navigationStack,
            onRootClick = { onEvent(FileBrowserViewEvent.LoadRootDirectories) },
            onBreadcrumbClick = { onEvent(FileBrowserViewEvent.NavigateToBreadcrumb(it)) },
        )

        WormaCeptorDivider()
    }
}

@Composable
private fun SortMenuButton(
    showSortMenu: Boolean,
    onShowSortMenuChange: (Boolean) -> Unit,
    onEvent: (FileBrowserViewEvent) -> Unit,
) {
    Box {
        IconButton(onClick = { onShowSortMenuChange(true) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = stringResource(R.string.filebrowser_sort),
            )
        }

        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { onShowSortMenuChange(false) },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filebrowser_sort_name)) },
                onClick = {
                    onEvent(FileBrowserViewEvent.SetSortMode(SortMode.NAME))
                    onShowSortMenuChange(false)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filebrowser_sort_size)) },
                onClick = {
                    onEvent(FileBrowserViewEvent.SetSortMode(SortMode.SIZE))
                    onShowSortMenuChange(false)
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.filebrowser_sort_date)) },
                onClick = {
                    onEvent(FileBrowserViewEvent.SetSortMode(SortMode.DATE))
                    onShowSortMenuChange(false)
                },
            )
        }
    }
}

@Composable
private fun FileBrowserBody(
    state: FileBrowserViewState,
    onEvent: (FileBrowserViewEvent) -> Unit,
    padding: PaddingValues,
) {
    Box(modifier = Modifier.imePadding()) {
        when {
            state.isLoading -> LoadingIndicator(padding)
            state.filteredFiles.isEmpty() -> WormaCeptorEmptyState(
                title = stringResource(R.string.filebrowser_no_files_found),
                icon = Icons.Default.Folder,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            else -> FileList(
                files = state.filteredFiles,
                onEvent = onEvent,
                padding = padding,
            )
        }
    }
}

@Composable
private fun LoadingIndicator(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FileList(
    files: ImmutableList<FileEntry>,
    onEvent: (FileBrowserViewEvent) -> Unit,
    padding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(
            top = WormaCeptorTokens.Spacing.lg,
            bottom = WormaCeptorTokens.Spacing.lg +
                WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding(),
        ),
    ) {
        items(
            items = files,
            key = { it.path },
        ) { file ->
            FileListItem(
                file = file,
                onClick = { onEvent(FileBrowserViewEvent.FileClicked(file)) },
                onLongClick = { onEvent(FileBrowserViewEvent.FileLongClicked(file)) },
            )
            WormaCeptorDivider()
        }
    }
}

@SuppressLint("SdCardPath")
@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun FileBrowserScreenPreview() {
    WormaCeptorTheme {
        FileBrowserScreen(
            state = FileBrowserViewState(
                currentPath = "/data/data/com.example/files",
                navigationStack = persistentListOf("files"),
                filteredFiles = persistentListOf(
                    FileEntry(
                        name = "config",
                        path = "/data/data/com.example/files/config",
                        isDirectory = true,
                        sizeBytes = 4_096L,
                        lastModified = 1_700_000_000_000L,
                        permissions = "rwxr-xr-x",
                    ),
                    FileEntry(
                        name = "app.log",
                        path = "/data/data/com.example/files/app.log",
                        isDirectory = false,
                        sizeBytes = 25_600L,
                        lastModified = 1_700_001_000_000L,
                        permissions = "rw-r--r--",
                    ),
                    FileEntry(
                        name = "settings.json",
                        path = "/data/data/com.example/files/settings.json",
                        isDirectory = false,
                        sizeBytes = 1_024L,
                        lastModified = 1_700_002_000_000L,
                        permissions = "rw-r--r--",
                    ),
                ),
                searchQuery = "",
                isLoading = false,
                error = null,
            ),
            onEvent = {},
        )
    }
}
