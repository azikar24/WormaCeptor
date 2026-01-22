package com.azikar24.wormaceptor.feature.viewer.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.api.Feature
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.feature.viewer.data.FavoritesRepository
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem

/**
 * Category accent colors for visual differentiation.
 */
private object CategoryColors {
    val inspection = Color(0xFF6366F1) // Indigo
    val performance = Color(0xFFF59E0B) // Amber
    val network = Color(0xFF10B981) // Emerald
    val simulation = Color(0xFF8B5CF6) // Violet
    val visualDebug = Color(0xFFEC4899) // Pink
    val core = Color(0xFF3B82F6) // Blue
    val favorites = Color(0xFFF59E0B) // Gold

    fun forCategory(name: String): Color = when (name.lowercase()) {
        "inspection" -> inspection
        "performance" -> performance
        "network" -> network
        "simulation" -> simulation
        "visual debug" -> visualDebug
        "core" -> core
        "favorites" -> favorites
        else -> Color(0xFF6B7280)
    }

    fun iconForCategory(name: String): ImageVector = when (name.lowercase()) {
        "inspection" -> Icons.Default.Explore
        "performance" -> Icons.Default.Speed
        "network" -> Icons.Default.Cable
        "simulation" -> Icons.Default.LocationOn
        "visual debug" -> Icons.Default.Visibility
        "core" -> Icons.Default.Code
        else -> Icons.Default.BugReport
    }
}

/**
 * Tools tab composable that displays all available tools organized by category.
 * Features a refined developer console aesthetic with search, favorites strip,
 * and collapsible category sections.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToolsTab(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository.getInstance(context) }

    LaunchedEffect(true) { favoritesRepository.setDefaultsIfNeeded() }

    val favorites by favoritesRepository.favorites.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val collapsedCategories = remember { mutableStateMapOf<String, Boolean>() }
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
                (searchQuery.isBlank() || ToolCategories.getToolByFeature(feature)?.name?.contains(
                    searchQuery,
                    ignoreCase = true,
                ) == true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Search Bar
        ToolsSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.lg,
                    vertical = WormaCeptorDesignSystem.Spacing.md,
                ),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = WormaCeptorDesignSystem.Spacing.xxl),
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
                    categoryColor = CategoryColors.forCategory(category.name),
                    categoryIcon = CategoryColors.iconForCategory(category.name),
                    tools = category.tools,
                    isCollapsed = collapsedCategories[category.name] == true,
                    onToggleCollapse = {
                        collapsedCategories[category.name] = !(collapsedCategories[category.name] ?: false)
                    },
                    onToolClick = onNavigate,
                    onToolLongClick = { tool ->
                        val added = favoritesRepository.toggleFavorite(tool.feature)
                        if (!added && favorites.size >= FavoritesRepository.MAX_FAVORITES) {
                            Toast.makeText(
                                context,
                                "Maximum ${FavoritesRepository.MAX_FAVORITES} favorites allowed",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    },
                    favorites = favorites,
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
private fun ToolsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Surface(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp),
            ),
        shape = RoundedCornerShape(14.dp),
        color = surfaceColor,
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search tools...",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp),
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
        )
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
                tint = CategoryColors.favorites,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Text(
                text = "QUICK ACCESS",
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
                FavoriteTile(
                    tool = tool,
                    onClick = { onToolClick(tool.route) },
                    onLongClick = { onToolLongClick(tool) },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteTile(
    tool: ToolItem,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .width(88.dp)
            .height(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                    vertical = WormaCeptorDesignSystem.Spacing.md,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.name,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

            Text(
                text = tool.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 180f,
        animationSpec = tween(250),
        label = "collapse_rotation",
    )

    val headerBackground by animateColorAsState(
        targetValue = if (isCollapsed) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        } else {
            categoryColor.copy(alpha = 0.08f)
        },
        animationSpec = tween(200),
        label = "header_bg",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(250)),
    ) {
        // Category header
        Surface(
            onClick = onToggleCollapse,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            color = headerBackground,
            shape = RoundedCornerShape(10.dp),
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
                        tint = categoryColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp),
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
                        shape = RoundedCornerShape(4.dp),
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
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Tools grid
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = expandVertically(animationSpec = tween(250)) + fadeIn(animationSpec = tween(200)),
            exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(150)),
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = WormaCeptorDesignSystem.Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                tools.forEach { tool ->
                    ToolTile(
                        tool = tool,
                        isFavorite = tool.feature in favorites,
                        categoryColor = categoryColor,
                        onClick = { onToolClick(tool.route) },
                        onLongClick = { onToolLongClick(tool) },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ToolTile(
    tool: ToolItem,
    isFavorite: Boolean,
    categoryColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val tileBackground = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val tileBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)

    Card(
        modifier = modifier
            .width(104.dp)
            .height(116.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                },
            ),
        colors = CardDefaults.cardColors(containerColor = tileBackground),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, tileBorder),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(WormaCeptorDesignSystem.Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Icon with category-colored background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    categoryColor.copy(alpha = 0.15f),
                                    categoryColor.copy(alpha = 0.05f),
                                ),
                            ),
                            shape = RoundedCornerShape(10.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = tool.icon,
                        contentDescription = tool.name,
                        modifier = Modifier.size(22.dp),
                        tint = categoryColor.copy(alpha = 0.9f),
                    )
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 14.sp,
                )
            }

            // Favorite indicator
            if (isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(12.dp),
                    tint = CategoryColors.favorites,
                )
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
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .alpha(0.4f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

        Text(
            text = if (searchQuery.isNotEmpty()) {
                "No tools found for \"$searchQuery\""
            } else {
                "No tools available"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        if (searchQuery.isNotEmpty()) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = "Try a different search term",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
        }
    }
}
