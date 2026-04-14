package com.azikar24.wormaceptor.feature.dependenciesinspector.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Close
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
import com.azikar24.wormaceptor.domain.entities.DependencyCategory
import com.azikar24.wormaceptor.domain.entities.DependencyInfo
import com.azikar24.wormaceptor.domain.entities.DependencySummary
import com.azikar24.wormaceptor.domain.entities.DetectionMethod
import com.azikar24.wormaceptor.feature.dependenciesinspector.R
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components.DependencyCard
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components.DependencyDetailContent
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components.FilterSection
import com.azikar24.wormaceptor.feature.dependenciesinspector.ui.components.SummarySection
import com.azikar24.wormaceptor.feature.dependenciesinspector.vm.DependenciesInspectorViewEvent
import com.azikar24.wormaceptor.feature.dependenciesinspector.vm.DependenciesInspectorViewState
import kotlinx.collections.immutable.persistentListOf

/** Displays the dependencies list with category filtering, search, and detail views. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DependenciesInspectorScreen(
    state: DependenciesInspectorViewState,
    onEvent: (DependenciesInspectorViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = WormaCeptorTokens.Colors.DependenciesInspector.scheme()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            DependenciesTopBar(
                isSearchActive = state.isSearchActive,
                searchQuery = state.searchQuery,
                isLoading = state.isLoading,
                onBack = onBack,
                onEvent = onEvent,
            )
        },
    ) { paddingValues ->
        DependenciesBody(
            state = state,
            colors = colors,
            onEvent = onEvent,
            modifier = Modifier.fillMaxSize().padding(paddingValues).imePadding(),
        )

        state.selectedDependency?.let { dep ->
            WormaCeptorBottomSheet(
                onDismissRequest = { onEvent(DependenciesInspectorViewEvent.DismissDetail) },
                sheetState = sheetState,
            ) {
                DependencyDetailContent(dep, colors)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DependenciesTopBar(
    isSearchActive: Boolean,
    searchQuery: String,
    isLoading: Boolean,
    onBack: (() -> Unit)?,
    onEvent: (DependenciesInspectorViewEvent) -> Unit,
) {
    Column {
        TopAppBar(
            title = {
                Text(
                    stringResource(R.string.dependenciesinspector_title),
                    fontWeight = FontWeight.SemiBold,
                )
            },
            navigationIcon = {
                onBack?.let {
                    IconButton(onClick = it) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.dependenciesinspector_navigation_back),
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { onEvent(DependenciesInspectorViewEvent.ToggleSearch) }) {
                    Icon(
                        if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        stringResource(R.string.dependenciesinspector_action_search),
                    )
                }
                IconButton(
                    onClick = { onEvent(DependenciesInspectorViewEvent.Refresh) },
                    enabled = !isLoading,
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(WormaCeptorTokens.IconSize.lg),
                            strokeWidth = WormaCeptorTokens.BorderWidth.thick,
                        )
                    } else {
                        Icon(
                            Icons.Default.Refresh,
                            stringResource(R.string.dependenciesinspector_action_refresh),
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )
        AnimatedVisibility(
            visible = isSearchActive,
            enter = expandVertically(
                animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
            ) + fadeIn(animationSpec = tween(WormaCeptorTokens.Animation.NORMAL)),
            exit = shrinkVertically(
                animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
            ) + fadeOut(animationSpec = tween(WormaCeptorTokens.Animation.NORMAL)),
        ) {
            WormaCeptorSearchBar(
                query = searchQuery,
                onQueryChange = { onEvent(DependenciesInspectorViewEvent.SetSearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorTokens.Spacing.lg)
                    .padding(top = WormaCeptorTokens.Spacing.sm),
                placeholder = stringResource(R.string.dependenciesinspector_search_placeholder),
            )
        }
    }
}

@Composable
private fun DependenciesBody(
    state: DependenciesInspectorViewState,
    colors: ToolColors.DependenciesInspector.Scheme,
    onEvent: (DependenciesInspectorViewEvent) -> Unit,
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
            selectedCategory = state.selectedCategory,
            summary = state.summary,
            showVersionedOnly = state.showVersionedOnly,
            onSelectCategory = { onEvent(DependenciesInspectorViewEvent.SetSelectedCategory(it)) },
            onToggleVersionedOnly = { onEvent(DependenciesInspectorViewEvent.SetShowVersionedOnly(it)) },
            colors = colors,
            modifier = Modifier.fillMaxWidth().padding(horizontal = WormaCeptorTokens.Spacing.lg),
        )

        Spacer(Modifier.height(WormaCeptorTokens.Spacing.sm))

        ErrorBanner(state.error)
        DependenciesListOrEmpty(state, colors, onEvent)
    }
}

@Composable
private fun ErrorBanner(error: String?) {
    error?.let {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = WormaCeptorTokens.Spacing.lg),
            shape = RoundedCornerShape(WormaCeptorTokens.Radius.md),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
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
private fun DependenciesListOrEmpty(
    state: DependenciesInspectorViewState,
    colors: ToolColors.DependenciesInspector.Scheme,
    onEvent: (DependenciesInspectorViewEvent) -> Unit,
) {
    if (state.filteredDependencies.isEmpty() && !state.isLoading) {
        WormaCeptorEmptyState(
            title = stringResource(R.string.dependenciesinspector_empty_title),
            modifier = Modifier.fillMaxSize(),
            subtitle = stringResource(R.string.dependenciesinspector_empty_subtitle),
            icon = Icons.AutoMirrored.Filled.HelpOutline,
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
            items(
                count = state.filteredDependencies.size,
                key = { state.filteredDependencies[it].packageName },
            ) { index ->
                val dep = state.filteredDependencies[index]
                DependencyCard(
                    dep,
                    { onEvent(DependenciesInspectorViewEvent.SelectDependency(dep)) },
                    colors,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Loaded")
@Composable
private fun DependenciesInspectorScreenPreview() {
    WormaCeptorTheme {
        DependenciesInspectorScreen(
            state = DependenciesInspectorViewState(
                filteredDependencies = persistentListOf(
                    DependencyInfo(
                        name = "OkHttp",
                        groupId = "com.squareup.okhttp3",
                        artifactId = "okhttp",
                        version = "4.12.0",
                        category = DependencyCategory.NETWORKING,
                        detectionMethod = DetectionMethod.VERSION_FIELD,
                        packageName = "okhttp3",
                        isDetected = true,
                        description = "HTTP client for Android and Java",
                        website = "https://square.github.io/okhttp/",
                    ),
                    DependencyInfo(
                        name = "Koin",
                        groupId = "io.insert-koin",
                        artifactId = "koin-core",
                        version = "4.0.0",
                        category = DependencyCategory.DEPENDENCY_INJECTION,
                        detectionMethod = DetectionMethod.CLASS_PRESENCE_ONLY,
                        packageName = "org.koin",
                        isDetected = true,
                        description = "Lightweight dependency injection framework",
                        website = null,
                    ),
                ),
                summary = DependencySummary(
                    totalDetected = 12,
                    withVersion = 8,
                    withoutVersion = 4,
                    byCategory = mapOf(
                        DependencyCategory.NETWORKING to 3,
                        DependencyCategory.DEPENDENCY_INJECTION to 2,
                    ),
                ),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Loading")
@Composable
private fun DependenciesInspectorScreenLoadingPreview() {
    WormaCeptorTheme {
        DependenciesInspectorScreen(
            state = DependenciesInspectorViewState(isLoading = true),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Error")
@Composable
private fun DependenciesInspectorScreenErrorPreview() {
    WormaCeptorTheme {
        DependenciesInspectorScreen(
            state = DependenciesInspectorViewState(error = "Failed to scan dependencies"),
            onEvent = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Empty")
@Composable
private fun DependenciesInspectorScreenEmptyPreview() {
    WormaCeptorTheme {
        DependenciesInspectorScreen(
            state = DependenciesInspectorViewState(),
            onEvent = {},
            onBack = {},
        )
    }
}
