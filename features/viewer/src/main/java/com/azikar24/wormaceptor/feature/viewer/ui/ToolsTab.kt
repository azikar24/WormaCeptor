package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.data.FavoritesRepository
import com.azikar24.wormaceptor.feature.viewer.ui.components.tools.CategoryHelper
import com.azikar24.wormaceptor.feature.viewer.ui.components.tools.EmptyToolsState
import com.azikar24.wormaceptor.feature.viewer.ui.components.tools.FavoritesStrip
import com.azikar24.wormaceptor.feature.viewer.ui.components.tools.PerformanceOverlayToggle
import com.azikar24.wormaceptor.feature.viewer.ui.components.tools.ToolCategorySection
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewEvent
import com.azikar24.wormaceptor.feature.viewer.vm.HomeViewState

/**
 * Tools tab composable that displays all available tools organized by category.
 * Features a refined developer console aesthetic with search, favorites strip,
 * and collapsible category sections.
 *
 * @param onNavigate Callback when a tool is selected
 * @param onShowMessage Callback to show a snackbar message
 * @param modifier Modifier for this composable
 */
@Suppress("LongMethod")
@Composable
fun ToolsTab(
    state: HomeViewState,
    onEvent: (HomeViewEvent) -> Unit,
    onNavigate: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val searchActive = state.toolsSearchActive
    val searchQuery = state.toolsSearchQuery
    val collapsedCategories = state.collapsedToolCategories
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository.getInstance(context) }
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(Unit) { favoritesRepository.setDefaultsIfNeeded() }

    val favorites by favoritesRepository.favorites.collectAsState()

    val enabledFeatures = remember { WormaCeptorApi.getEnabledFeatures() }

    val filteredCategories = remember(enabledFeatures) {
        ToolCategories.allCategories.map { category ->
            category.copy(tools = category.tools.filter { it.feature in enabledFeatures })
        }.filter { it.tools.isNotEmpty() }
    }

    val searchFilteredCategories = remember(searchQuery, filteredCategories) {
        if (searchQuery.isBlank()) {
            filteredCategories
        } else {
            filteredCategories.map { category ->
                category.copy(
                    tools = category.tools.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    },
                )
            }.filter { it.tools.isNotEmpty() }
        }
    }

    val filteredFavorites = remember(favorites, enabledFeatures, searchQuery) {
        favorites.filter { feature ->
            feature in enabledFeatures &&
                (
                    searchQuery.isBlank() || ToolCategories.getToolByFeature(feature)?.name?.contains(
                        searchQuery,
                        ignoreCase = true,
                    ) == true
                    )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WormaCeptorTokens.semantic().background),
    ) {
        // Animated search bar (toggle is in parent TopAppBar)
        AnimatedVisibility(
            visible = searchActive,
            enter = WormaCeptorTokens.Animations.expandFadeIn,
            exit = WormaCeptorTokens.Animations.shrinkFadeOut,
        ) {
            WormaCeptorSearchBar(
                query = searchQuery,
                onQueryChange = { onEvent(HomeViewEvent.ToolsSearchQueryChanged(it)) },
                placeholder = stringResource(R.string.viewer_tools_search_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorTokens.Spacing.lg)
                    .padding(top = WormaCeptorTokens.Spacing.sm),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WormaCeptorTokens.Spacing.sm,
                bottom = WormaCeptorTokens.Spacing.xxl + navigationBarPadding,
            ),
        ) {
            // Favorites horizontal strip
            if (filteredFavorites.isNotEmpty()) {
                item(key = "favorites_section") {
                    FavoritesStrip(
                        favorites = filteredFavorites.mapNotNull { ToolCategories.getToolByFeature(it) },
                        onToolClick = onNavigate,
                        onToolLongClick = { tool ->
                            favoritesRepository.toggleFavorite(tool.feature)
                        },
                        modifier = Modifier.padding(bottom = WormaCeptorTokens.Spacing.lg),
                    )
                }
            }

            // Category sections
            items(
                items = searchFilteredCategories,
                key = { it.name },
            ) { category ->
                ToolCategorySection(
                    categoryName = category.name,
                    categoryColor = CategoryHelper.forCategory(category.name),
                    categoryIcon = CategoryHelper.iconForCategory(category.name),
                    tools = category.tools,
                    isCollapsed = category.name in collapsedCategories,
                    onToggleCollapse = { onEvent(HomeViewEvent.ToolCategoryCollapseToggled(category.name)) },
                    onToolClick = onNavigate,
                    onToolLongClick = { tool ->
                        val added = favoritesRepository.toggleFavorite(tool.feature)
                        if (!added && favorites.size >= FavoritesRepository.MAX_FAVORITES) {
                            onShowMessage(
                                context.getString(
                                    R.string.viewer_tools_max_favorites,
                                    FavoritesRepository.MAX_FAVORITES,
                                ),
                            )
                        }
                    },
                    favorites = favorites,
                    headerContent = if (category.name == "Performance") {
                        {
                            PerformanceOverlayToggle(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = WormaCeptorTokens.Spacing.sm),
                            )
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.lg,
                        vertical = WormaCeptorTokens.Spacing.xs,
                    ),
                )
            }

            // Empty state
            if (searchFilteredCategories.isEmpty() && filteredFavorites.isEmpty()) {
                item {
                    EmptyToolsState(
                        searchQuery = searchQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorTokens.Spacing.xxl),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToolsTabPreview() {
    WormaCeptorTheme {
        ToolsTab(
            state = HomeViewState(),
            onEvent = {},
            onNavigate = {},
            onShowMessage = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
