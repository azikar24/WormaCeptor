/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.FileEntry
import com.azikar24.wormaceptor.feature.filebrowser.ui.components.BreadcrumbBar
import com.azikar24.wormaceptor.feature.filebrowser.ui.components.FileListItem
import com.azikar24.wormaceptor.feature.filebrowser.vm.FileBrowserViewModel
import kotlinx.collections.immutable.ImmutableList

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
    onSortModeChanged: (FileBrowserViewModel.SortMode) -> Unit,
    onNavigateBack: () -> Boolean,
    onExitBrowser: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showSortMenu by remember { mutableStateOf(false) }
    var searchActive by remember { mutableStateOf(false) }

    // Handle system back button: navigate up directory or exit if at home
    BackHandler {
        if (!onNavigateBack()) {
            onExitBrowser()
        }
    }

    // Show error as snackbar
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("File Browser") },
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

                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort",
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Name") },
                                    onClick = {
                                        onSortModeChanged(FileBrowserViewModel.SortMode.NAME)
                                        showSortMenu = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Size") },
                                    onClick = {
                                        onSortModeChanged(FileBrowserViewModel.SortMode.SIZE)
                                        showSortMenu = false
                                    },
                                )
                                DropdownMenuItem(
                                    text = { Text("Date") },
                                    onClick = {
                                        onSortModeChanged(FileBrowserViewModel.SortMode.DATE)
                                        showSortMenu = false
                                    },
                                )
                            }
                        }
                    },
                )

                // Search bar
                if (searchActive) {
                    WormaCeptorSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChanged,
                        placeholder = "Search files...",
                        onSearch = { searchActive = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                            .padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
                    )
                }

                // Breadcrumb navigation
                BreadcrumbBar(
                    currentPath = currentPath,
                    navigationStack = navigationStack,
                    onBreadcrumbClick = onNavigateToBreadcrumb,
                )

                HorizontalDivider()
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp),
                        )
                        Text(
                            text = "No files found",
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
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
