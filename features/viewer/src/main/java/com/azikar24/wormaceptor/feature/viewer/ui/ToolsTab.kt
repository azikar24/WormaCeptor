package com.azikar24.wormaceptor.feature.viewer.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.api.Feature
import com.azikar24.wormaceptor.api.WormaCeptorApi
import com.azikar24.wormaceptor.feature.viewer.data.FavoritesRepository
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem

/**
 * Tools tab composable that displays all available tools organized by category.
 * Supports search, favorites, and collapsible sections.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ToolsTab(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val favoritesRepository = remember { FavoritesRepository.getInstance(context) }

    // Initialize default favorites for new users
    remember { favoritesRepository.setDefaultsIfNeeded() }

    val favorites by favoritesRepository.favorites.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Track collapsed state for each category
    val collapsedCategories = remember { mutableStateMapOf<String, Boolean>() }

    // Filter tools based on enabled features
    val enabledFeatures = remember { WormaCeptorApi.getEnabledFeatures() }

    // Filter categories to only show enabled tools
    val filteredCategories = remember(enabledFeatures) {
        ToolCategories.allCategories.map { category ->
            category.copy(tools = category.tools.filter { it.feature in enabledFeatures })
        }.filter { it.tools.isNotEmpty() }
    }

    // Filter based on search query
    val searchFilteredCategories = remember(searchQuery, filteredCategories) {
        if (searchQuery.isBlank()) {
            filteredCategories
        } else {
            filteredCategories.map { category ->
                category.copy(
                    tools = category.tools.filter {
                        it.name.contains(searchQuery, ignoreCase = true)
                    }
                )
            }.filter { it.tools.isNotEmpty() }
        }
    }

    // Filter favorites to only include enabled features
    val filteredFavorites = remember(favorites, enabledFeatures, searchQuery) {
        favorites.filter { feature ->
            feature in enabledFeatures &&
                (searchQuery.isBlank() || ToolCategories.getToolByFeature(feature)?.name?.contains(searchQuery, ignoreCase = true) == true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
    ) {
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search tools...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md)
        ) {
            // Favorites section
            if (filteredFavorites.isNotEmpty()) {
                item {
                    ToolCategorySection(
                        categoryName = "Favorites",
                        tools = filteredFavorites.mapNotNull { ToolCategories.getToolByFeature(it) },
                        isCollapsed = collapsedCategories["Favorites"] == true,
                        onToggleCollapse = {
                            collapsedCategories["Favorites"] = !(collapsedCategories["Favorites"] ?: false)
                        },
                        onToolClick = onNavigate,
                        onToolLongClick = { tool ->
                            favoritesRepository.toggleFavorite(tool.feature)
                        },
                        favorites = favorites,
                        isFavoritesSection = true
                    )
                }
            }

            // Category sections
            items(searchFilteredCategories) { category ->
                ToolCategorySection(
                    categoryName = category.name,
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
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    favorites = favorites
                )
            }

            // Empty state when no results
            if (searchFilteredCategories.isEmpty() && filteredFavorites.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WormaCeptorDesignSystem.Spacing.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) {
                                "No tools found matching \"$searchQuery\""
                            } else {
                                "No tools available"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToolCategorySection(
    categoryName: String,
    tools: List<ToolItem>,
    isCollapsed: Boolean,
    onToggleCollapse: () -> Unit,
    onToolClick: (String) -> Unit,
    onToolLongClick: (ToolItem) -> Unit,
    favorites: Set<Feature>,
    isFavoritesSection: Boolean = false,
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isCollapsed) 0f else 180f,
        label = "collapse_rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        // Category header
        Surface(
            onClick = onToggleCollapse,
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isFavoritesSection) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                    }
                    Text(
                        text = categoryName.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                    Text(
                        text = "(${tools.size})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isCollapsed) "Expand" else "Collapse",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tools grid
        AnimatedVisibility(
            visible = !isCollapsed,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
            ) {
                tools.forEach { tool ->
                    ToolTile(
                        tool = tool,
                        isFavorite = tool.feature in favorites,
                        onClick = { onToolClick(tool.route) },
                        onLongClick = { onToolLongClick(tool) }
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
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Card(
        modifier = modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongClick()
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WormaCeptorDesignSystem.Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.name,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Favorite indicator
            if (isFavorite) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorite",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
