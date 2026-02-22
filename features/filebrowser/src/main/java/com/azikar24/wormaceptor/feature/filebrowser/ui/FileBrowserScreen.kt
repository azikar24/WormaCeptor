package com.azikar24.wormaceptor.feature.filebrowser.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.feature.filebrowser.R
import com.azikar24.wormaceptor.feature.filebrowser.ui.components.BreadcrumbBar
import com.azikar24.wormaceptor.feature.filebrowser.ui.components.FileListItem
import com.azikar24.wormaceptor.feature.filebrowser.vm.SortMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Main file browser screen showing directory listing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    currentPath: String?,
    navigationStack: ImmutableList<String>,
    filteredFiles: ImmutableList<FileEntry>,
    searchQuery: String,
    isLoading: Boolean,
    error: String?,
    onSearchQueryChanged: (String) -> Unit,
    onNavigateToBreadcrumb: (Int) -> Unit,
    onFileClick: (FileEntry) -> Unit,
    onFileLongClick: (FileEntry) -> Unit,
    onSortModeChanged: (SortMode) -> Unit,
    onNavigateBack: () -> Boolean,
    onExitBrowser: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchActive by rememberSaveable { mutableStateOf(false) }

    // Handle system back button: navigate up directory or exit if at home
    BackHandler {
        if (!onNavigateBack()) {
            onExitBrowser()
        }
    }

    // Show error as snackbar
    LaunchedEffect(error) {
        error?.let {
            snackBarHostState.showSnackbar(it)
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.filebrowser_title)) },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (!onNavigateBack()) {
                                    onExitBrowser()
                                }
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.filebrowser_back),
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                searchActive = !searchActive
                                if (!searchActive) onSearchQueryChanged("")
                            },
                        ) {
                            Icon(
                                imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = stringResource(R.string.filebrowser_search),
                            )
                        }

                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = stringResource(R.string.filebrowser_sort),
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filebrowser_sort_name)) },
                                    onClick = {
                                        onSortModeChanged(SortMode.NAME)
                                        showSortMenu = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filebrowser_sort_size)) },
                                    onClick = {
                                        onSortModeChanged(SortMode.SIZE)
                                        showSortMenu = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.filebrowser_sort_date)) },
                                    onClick = {
                                        onSortModeChanged(SortMode.DATE)
                                        showSortMenu = false
                                    },
                                )
                            }
                        }
                    },
                )

                // Search bar
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
                        placeholder = stringResource(R.string.filebrowser_search_placeholder),
                        onSearch = { searchActive = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                            .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
                    )
                }

                // Breadcrumb navigation
                BreadcrumbBar(
                    currentPath = currentPath,
                    navigationStack = navigationStack,
                    onBreadcrumbClick = onNavigateToBreadcrumb,
                )

                WormaCeptorDivider()
            }
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
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

            filteredFiles.isEmpty() -> {
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
                            imageVector = Icons.Default.Folder,
                            contentDescription = stringResource(R.string.filebrowser_no_files_found),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(WormaCeptorDesignSystem.Spacing.xxxl),
                        )
                        Text(
                            text = stringResource(R.string.filebrowser_no_files_found),
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
                    contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    items(
                        items = filteredFiles,
                        key = { it.path },
                    ) { file ->
                        FileListItem(
                            file = file,
                            onClick = { onFileClick(file) },
                            onLongClick = { onFileLongClick(file) },
                        )
                        WormaCeptorDivider()
                    }
                }
            }
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
            onSearchQueryChanged = {},
            onNavigateToBreadcrumb = {},
            onFileClick = {},
            onFileLongClick = {},
            onSortModeChanged = {},
            onNavigateBack = { false },
            onExitBrowser = {},
            onClearError = {},
        )
    }
}
