package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.azikar24.wormaceptor.api.Feature
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.core.engine.PerformanceOverlayEngine
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorToolTile
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.data.FavoritesRepository
import org.koin.java.KoinJavaComponent.get

private const val GridColumns = 3

/**
 * Category helper for icons and color access.
 */
private object CategoryHelper {
    fun forCategory(name: String): Color = when (name.lowercase()) {
        "inspection" -> WormaCeptorColors.Category.Inspection
        "performance" -> WormaCeptorColors.Category.Performance
        "network" -> WormaCeptorColors.Category.Network
        "simulation" -> WormaCeptorColors.Category.Simulation
        "core" -> WormaCeptorColors.Category.Core
        "favorites" -> WormaCeptorColors.Category.Favorites
        else -> WormaCeptorColors.Category.Fallback
    }

    fun iconForCategory(name: String): ImageVector = when (name.lowercase()) {
        "inspection" -> Icons.Default.Explore
        "performance" -> Icons.Default.Speed
        "network" -> Icons.Default.Cable
        "simulation" -> Icons.Default.LocationOn
        "core" -> Icons.Default.Code
        else -> Icons.Default.BugReport
    }
}

/**
 * Tools tab composable that displays all available tools organized by category.
 * Features a refined developer console aesthetic with search, favorites strip,
 * and collapsible category sections.
 *
 * @param onNavigate Callback when a tool is selected
 * @param onShowMessage Callback to show a snackbar message
 * @param searchActive Whether the search bar is currently visible
 * @param searchQuery Current search query text
 * @param onSearchQueryChanged Callback when search query changes
 * @param modifier Modifier for this composable
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
fun ToolsTab(
    onNavigate: (String) -> Unit,
    onShowMessage: (String) -> Unit,
    searchActive: Boolean,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    collapsedCategories: Set<String>,
    onToggleCollapse: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Animated search bar (toggle is in parent TopAppBar)
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
                placeholder = stringResource(R.string.viewer_tools_search_placeholder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                    .padding(top = WormaCeptorDesignSystem.Spacing.sm),
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = WormaCeptorDesignSystem.Spacing.sm,
                bottom = WormaCeptorDesignSystem.Spacing.xxl + navigationBarPadding,
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
                        modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.lg),
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
                    onToggleCollapse = { onToggleCollapse(category.name) },
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
                                    .padding(top = WormaCeptorDesignSystem.Spacing.sm),
                            )
                        }
                    } else {
                        null
                    },
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.lg,
                        vertical = WormaCeptorDesignSystem.Spacing.xs,
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
                            .padding(WormaCeptorDesignSystem.Spacing.xxl),
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoritesStrip(
    favorites: List<ToolItem>,
    onToolClick: (String) -> Unit,
    onToolLongClick: (ToolItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = WormaCeptorColors.Category.Favorites,
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Text(
                text = stringResource(R.string.viewer_tools_quick_access),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        // Horizontal scrolling favorites
        LazyRow(
            contentPadding = PaddingValues(horizontal = WormaCeptorDesignSystem.Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            items(
                items = favorites,
                key = { tool -> "fav_${tool.feature}" },
            ) { tool ->
                WormaCeptorToolTile(
                    label = tool.name,
                    icon = tool.icon,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = { onToolClick(tool.route) },
                    onLongClick = { onToolLongClick(tool) },
                    modifier = Modifier.width(116.dp),
                )
            }
        }
    }
}

@Suppress("LongMethod", "LongParameterList")
@Composable
private fun ToolCategorySection(
    categoryName: String,
    categoryColor: Color,
    categoryIcon: ImageVector,
    tools: List<ToolItem>,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onToolClick: (String) -> Unit,
    onToolLongClick: (ToolItem) -> Unit,
    favorites: Set<Feature>,
    modifier: Modifier = Modifier,
    headerContent: (@Composable () -> Unit)? = null,
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 180f,
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
        label = "collapse_rotation",
    )

    val headerBackground by animateColorAsState(
        targetValue = if (isCollapsed) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)
        } else {
            categoryColor.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle)
        },
        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast),
        label = "header_bg",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal)),
    ) {
        // Category header
        Surface(
            onClick = onToggleCollapse,
            modifier = Modifier
                .fillMaxWidth()
                .clip(WormaCeptorDesignSystem.Shapes.card),
            color = headerBackground,
            shape = WormaCeptorDesignSystem.Shapes.card,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.md,
                        vertical = WormaCeptorDesignSystem.Spacing.sm,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category accent dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = categoryColor,
                                shape = CircleShape,
                            ),
                    )

                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = null,
                        tint = categoryColor.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                    )

                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

                    // Tool count badge
                    Surface(
                        shape = WormaCeptorDesignSystem.Shapes.badge,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = "${tools.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.xs,
                                vertical = 2.dp,
                            ),
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isCollapsed) {
                        stringResource(
                            R.string.viewer_body_expand,
                        )
                    } else {
                        stringResource(R.string.viewer_body_collapse)
                    },
                    modifier = Modifier
                        .size(WormaCeptorDesignSystem.IconSize.md)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Tools grid
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = expandVertically(
                animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
            ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
            exit = shrinkVertically(
                animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast),
            ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.fast)),
        ) {
            Column {
                if (headerContent != null) {
                    headerContent()
                }

                val spacing = WormaCeptorDesignSystem.Spacing.sm
                val columns = GridColumns

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WormaCeptorDesignSystem.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(spacing),
                ) {
                    tools.chunked(columns).forEach { rowTools ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing),
                        ) {
                            rowTools.forEach { tool ->
                                WormaCeptorToolTile(
                                    label = tool.name,
                                    icon = tool.icon,
                                    accentColor = categoryColor,
                                    onClick = { onToolClick(tool.route) },
                                    onLongClick = { onToolLongClick(tool) },
                                    isFavorite = tool.feature in favorites,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            // Fill empty slots in last row
                            repeat(columns - rowTools.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyToolsState(
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(R.string.viewer_tools_search_placeholder),
            modifier = Modifier
                .size(WormaCeptorDesignSystem.IconSize.xxxl)
                .alpha(WormaCeptorDesignSystem.Alpha.strong),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

        Text(
            text = if (searchQuery.isNotEmpty()) {
                stringResource(R.string.viewer_tools_no_tools_found, searchQuery)
            } else {
                stringResource(R.string.viewer_tools_no_tools_available)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (searchQuery.isNotEmpty()) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = stringResource(R.string.viewer_tools_try_different_search),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Performance overlay toggle - single on/off switch for all metrics.
 */
@Composable
private fun PerformanceOverlayToggle(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var canDrawOverlays by remember { mutableStateOf(WormaCeptorApi.canShowFloatingButton(context)) }

    val performanceOverlayEngine = remember { get<PerformanceOverlayEngine>(PerformanceOverlayEngine::class.java) }
    val overlayState by performanceOverlayEngine.state.collectAsState()
    val isOverlayEnabled = overlayState.isOverlayEnabled

    // Load saved metric toggle states (FPS, Memory, CPU) from preferences
    LaunchedEffect(Unit) {
        performanceOverlayEngine.loadSavedMetricStates()
    }

    // Re-check permission when returning from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canDrawOverlays = WormaCeptorApi.canShowFloatingButton(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val backgroundColor = when {
        !canDrawOverlays -> MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = WormaCeptorDesignSystem.Alpha.moderate,
        )
        isOverlayEnabled -> WormaCeptorColors.Category.Performance.copy(alpha = WormaCeptorDesignSystem.Alpha.soft)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.bold)
    }
    val borderColor = when {
        !canDrawOverlays -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium)
        isOverlayEnabled -> WormaCeptorColors.Category.Performance.copy(alpha = WormaCeptorDesignSystem.Alpha.strong)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)
    }

    Surface(
        onClick = {
            if (canDrawOverlays) {
                (context as? ComponentActivity)?.let { activity ->
                    performanceOverlayEngine.toggleOverlayWithAllMetrics(activity)
                }
            } else {
                WormaCeptorApi.getOverlayPermissionIntent(context)?.let { intent ->
                    context.startActivity(intent)
                }
            }
        },
        modifier = modifier,
        shape = WormaCeptorDesignSystem.Shapes.cardLarge,
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (isOverlayEnabled && canDrawOverlays) {
                            WormaCeptorColors.Category.Performance.copy(alpha = WormaCeptorDesignSystem.Alpha.medium)
                        } else {
                            WormaCeptorColors.Category.Performance.copy(alpha = WormaCeptorDesignSystem.Alpha.soft)
                        },
                        shape = WormaCeptorDesignSystem.Shapes.card,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                    tint = if (isOverlayEnabled && canDrawOverlays) {
                        WormaCeptorColors.Category.Performance
                    } else {
                        WormaCeptorColors.Category.Performance.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy)
                    },
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.viewer_tools_performance_overlay),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = if (isOverlayEnabled && canDrawOverlays) {
                        stringResource(R.string.viewer_tools_overlay_showing)
                    } else {
                        stringResource(R.string.viewer_tools_overlay_tap_enable)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (!canDrawOverlays) {
                Surface(
                    shape = WormaCeptorDesignSystem.Shapes.button,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Text(
                        text = stringResource(R.string.viewer_tools_overlay_grant),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.sm,
                            vertical = WormaCeptorDesignSystem.Spacing.xs,
                        ),
                    )
                }
            } else {
                // On/Off indicator
                Surface(
                    shape = WormaCeptorDesignSystem.Shapes.button,
                    color = if (isOverlayEnabled) {
                        WormaCeptorColors.Category.Performance.copy(alpha = WormaCeptorDesignSystem.Alpha.medium)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Text(
                        text = if (isOverlayEnabled) {
                            stringResource(
                                R.string.viewer_tools_overlay_on,
                            )
                        } else {
                            stringResource(R.string.viewer_tools_overlay_off)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverlayEnabled) {
                            WormaCeptorColors.Category.Performance
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.sm,
                            vertical = WormaCeptorDesignSystem.Spacing.xs,
                        ),
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
            onNavigate = {},
            onShowMessage = {},
            searchActive = false,
            searchQuery = "",
            onSearchQueryChanged = {},
            collapsedCategories = emptySet(),
            onToggleCollapse = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
