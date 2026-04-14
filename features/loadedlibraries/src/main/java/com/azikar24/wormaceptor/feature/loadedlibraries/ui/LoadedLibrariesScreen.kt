package com.azikar24.wormaceptor.feature.loadedlibraries.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorBottomSheet
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.LibrarySummary
import com.azikar24.wormaceptor.domain.entities.LoadedLibrary
import com.azikar24.wormaceptor.feature.loadedlibraries.R
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.components.FilterSection
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.components.LibraryCard
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.components.LibraryDetailContent
import com.azikar24.wormaceptor.feature.loadedlibraries.ui.components.SummarySection
import com.azikar24.wormaceptor.feature.loadedlibraries.vm.LoadedLibrariesViewEvent
import com.azikar24.wormaceptor.feature.loadedlibraries.vm.LoadedLibrariesViewState
import kotlinx.collections.immutable.persistentListOf

/** Displays the loaded libraries list with filtering, search, and detail views. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoadedLibrariesScreen(
    state: LoadedLibrariesViewState,
    onEvent: (LoadedLibrariesViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = WormaCeptorTokens.Colors.LoadedLibraries.scheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            LibrariesTopBar(
                searchActive = state.searchActive,
                searchQuery = state.searchQuery,
                isLoading = state.isLoading,
                onBack = onBack,
                onToggleSearch = { onEvent(LoadedLibrariesViewEvent.ToggleSearch) },
                onSearchQueryChange = { onEvent(LoadedLibrariesViewEvent.SetSearchQuery(it)) },
                onRefresh = { onEvent(LoadedLibrariesViewEvent.Refresh) },
            )
        },
    ) { paddingValues ->
        LibrariesBody(
            state = state,
            colors = colors,
            onEvent = onEvent,
            modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding(),
        )

        state.selectedLibrary?.let { lib ->
            WormaCeptorBottomSheet(
                onDismissRequest = { onEvent(LoadedLibrariesViewEvent.DismissDetail) },
                sheetState = sheetState,
            ) {
                LibraryDetailContent(lib, colors)
            }
        }
    }
}

@Composable
private fun LibrariesBody(
    state: LoadedLibrariesViewState,
    colors: ToolColors.LoadedLibraries.Scheme,
    onEvent: (LoadedLibrariesViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SummarySection(
            state.summary,
            colors,
            Modifier
                .fillMaxWidth()
                .padding(horizontal = WormaCeptorTokens.Spacing.lg)
                .padding(top = WormaCeptorTokens.Spacing.sm),
        )
        Spacer(Modifier.height(WormaCeptorTokens.Spacing.sm))
        FilterSection(
            state.selectedType,
            state.showSystemLibs,
            { onEvent(LoadedLibrariesViewEvent.SetSelectedType(it)) },
            { onEvent(LoadedLibrariesViewEvent.SetShowSystemLibs(it)) },
            colors,
            Modifier.fillMaxWidth().padding(horizontal = WormaCeptorTokens.Spacing.lg),
        )
        Spacer(Modifier.height(WormaCeptorTokens.Spacing.sm))

        ErrorBanner(state.error)
        LibrariesListOrEmpty(state, colors, onEvent)
    }
}

@Suppress("LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibrariesTopBar(
    searchActive: Boolean,
    searchQuery: String,
    isLoading: Boolean,
    onBack: (() -> Unit)?,
    onToggleSearch: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Text(stringResource(R.string.loadedlibraries_title), fontWeight = FontWeight.SemiBold)
            },
            navigationIcon = {
                onBack?.let {
                    IconButton(onClick = it) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.loadedlibraries_back),
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onToggleSearch) {
                    Icon(
                        if (searchActive) Icons.Default.Close else Icons.Default.Search,
                        stringResource(R.string.loadedlibraries_search),
                    )
                }
                IconButton(onClick = onRefresh, enabled = !isLoading) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(WormaCeptorTokens.Spacing.xl),
                            strokeWidth = WormaCeptorTokens.BorderWidth.thick,
                        )
                    } else {
                        Icon(Icons.Default.Refresh, stringResource(R.string.loadedlibraries_refresh))
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )
        AnimatedVisibility(
            visible = searchActive,
            enter = expandVertically(
                animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
            ) + fadeIn(animationSpec = tween(WormaCeptorTokens.Animation.NORMAL)),
            exit = shrinkVertically(
                animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
            ) + fadeOut(animationSpec = tween(WormaCeptorTokens.Animation.NORMAL)),
        ) {
            WormaCeptorSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorTokens.Spacing.lg)
                    .padding(top = WormaCeptorTokens.Spacing.sm),
                placeholder = stringResource(R.string.loadedlibraries_search_placeholder),
            )
        }
    }
}

@Composable
private fun ErrorBanner(error: String?) {
    error?.let {
        Surface(
            Modifier.fillMaxWidth().padding(horizontal = WormaCeptorTokens.Spacing.lg),
            WormaCeptorTokens.Shapes.card,
            MaterialTheme.colorScheme.errorContainer,
        ) {
            Text(
                it,
                Modifier.padding(WormaCeptorTokens.Spacing.md),
                MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun LibrariesListOrEmpty(
    state: LoadedLibrariesViewState,
    colors: ToolColors.LoadedLibraries.Scheme,
    onEvent: (LoadedLibrariesViewEvent) -> Unit,
) {
    if (state.filteredLibraries.isEmpty() && !state.isLoading) {
        WormaCeptorEmptyState(
            title = stringResource(R.string.loadedlibraries_empty),
            modifier = Modifier.fillMaxSize(),
            icon = Icons.Default.Extension,
        )
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            contentPadding = PaddingValues(
                start = WormaCeptorTokens.Spacing.lg,
                end = WormaCeptorTokens.Spacing.lg,
                bottom = WormaCeptorTokens.Spacing.lg +
                    WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
            ),
        ) {
            items(state.filteredLibraries, key = { it.path }) { lib ->
                LibraryCard(
                    lib,
                    { onEvent(LoadedLibrariesViewEvent.SelectLibrary(lib)) },
                    colors,
                )
            }
        }
    }
}

@Suppress("UnusedPrivateMember", "MagicNumber")
@Preview(showBackground = true)
@Composable
private fun LoadedLibrariesScreenPreview() {
    WormaCeptorTheme {
        LoadedLibrariesScreen(
            state = LoadedLibrariesViewState(
                filteredLibraries = persistentListOf(
                    LoadedLibrary(
                        name = "libc.so",
                        path = "/system/lib64/libc.so",
                        type = LoadedLibrary.LibraryType.NATIVE_SO,
                        size = 1_200_000L,
                        loadAddress = "0x7f8a000000",
                        version = null,
                        isSystemLibrary = true,
                    ),
                    LoadedLibrary(
                        name = "classes.dex",
                        path = "/data/app/com.example/base.apk!classes.dex",
                        type = LoadedLibrary.LibraryType.DEX,
                        size = 4_500_000L,
                        loadAddress = null,
                        version = null,
                        isSystemLibrary = false,
                    ),
                    LoadedLibrary(
                        name = "okhttp.jar",
                        path = "/data/app/com.example/lib/okhttp.jar",
                        type = LoadedLibrary.LibraryType.JAR,
                        size = 800_000L,
                        loadAddress = null,
                        version = "4.12.0",
                        isSystemLibrary = false,
                    ),
                ),
                summary = LibrarySummary(
                    totalLibraries = 3,
                    nativeSoCount = 1,
                    dexCount = 1,
                    jarCount = 1,
                    totalSizeBytes = 6_500_000L,
                    systemLibraryCount = 1,
                    appLibraryCount = 2,
                ),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
