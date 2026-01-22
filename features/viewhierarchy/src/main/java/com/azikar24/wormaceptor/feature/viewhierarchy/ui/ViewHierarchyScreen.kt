/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewhierarchy.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.ViewNode
import com.azikar24.wormaceptor.feature.viewhierarchy.ui.theme.viewHierarchyColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main screen for the View Hierarchy Inspector.
 *
 * Features:
 * - Tree view of view hierarchy
 * - Search functionality
 * - Node detail view with properties
 * - Expand/collapse controls
 * - View count and depth statistics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewHierarchyScreen(
    rootNode: ViewNode?,
    viewCount: Int,
    maxDepth: Int,
    captureTimestamp: Long,
    selectedNode: ViewNode?,
    searchQuery: String,
    expandedNodeIds: Set<Int>,
    is3DMode: Boolean,
    onSearchQueryChanged: (String) -> Unit,
    onNodeSelected: (ViewNode) -> Unit,
    onDismissDetail: () -> Unit,
    onToggleNodeExpanded: (Int) -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onToggle3DMode: () -> Unit,
    onClear: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = viewHierarchyColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = null,
                            tint = colors.primary,
                        )
                        Text(
                            text = "View Hierarchy",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = {
                    onBack?.let { back ->
                        IconButton(onClick = back) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onExpandAll) {
                        Icon(
                            imageVector = Icons.Default.UnfoldMore,
                            contentDescription = "Expand all",
                        )
                    }
                    IconButton(onClick = onCollapseAll) {
                        Icon(
                            imageVector = Icons.Default.UnfoldLess,
                            contentDescription = "Collapse all",
                        )
                    }
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear hierarchy",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChanged = onSearchQueryChanged,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            // Statistics card
            if (rootNode != null) {
                StatisticsCard(
                    viewCount = viewCount,
                    maxDepth = maxDepth,
                    captureTimestamp = captureTimestamp,
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tree view
            if (rootNode != null) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
                    ),
                ) {
                    item {
                        TreeNode(
                            node = rootNode,
                            expandedNodeIds = expandedNodeIds,
                            searchQuery = searchQuery,
                            onNodeClick = onNodeSelected,
                            onToggleExpand = onToggleNodeExpanded,
                            colors = colors,
                            depth = 0,
                        )
                    }
                }
            } else {
                EmptyState(
                    colors = colors,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Detail sheet
        selectedNode?.let { node ->
            ModalBottomSheet(
                onDismissRequest = onDismissDetail,
                sheetState = sheetState,
            ) {
                NodeDetailContent(
                    node = node,
                    colors = colors,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    colors: com.azikar24.wormaceptor.feature.viewhierarchy.ui.theme.ViewHierarchyColors,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search views by name, ID, or class...",
                color = colors.labelSecondary,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = colors.labelSecondary,
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                IconButton(onClick = { onQueryChanged("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = colors.labelSecondary,
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = colors.searchBackground,
            unfocusedContainerColor = colors.searchBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
    )
}

@Composable
private fun StatisticsCard(
    viewCount: Int,
    maxDepth: Int,
    captureTimestamp: Long,
    colors: com.azikar24.wormaceptor.feature.viewhierarchy.ui.theme.ViewHierarchyColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            StatItem(
                label = "Views",
                value = viewCount.toString(),
                icon = Icons.Default.Layers,
                color = colors.primary,
                colors = colors,
            )
            StatItem(
                label = "Max Depth",
                value = maxDepth.toString(),
                icon = Icons.Default.AccountTree,
                color = colors.layout,
                colors = colors,
            )
            StatItem(
                label = "Captured",
                value = if (captureTimestamp > 0) formatTime(captureTimestamp) else "--:--",
                icon = null,
                color = colors.labelSecondary,
                colors = colors,
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    color: Color,
    colors: com.azikar24.wormaceptor.feature.viewhierarchy.ui.theme.ViewHierarchyColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = color,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.labelSecondary,
        )
    }
}

@Composable
private fun TreeNode(
    node: ViewNode,
    expandedNodeIds: Set<Int>,
    searchQuery: String,
    onNodeClick: (ViewNode) -> Unit,
    onToggleExpand: (Int) -> Unit,
    colors: com.azikar24.wormaceptor.feature.viewhierarchy.ui.theme.ViewHierarchyColors,
    depth: Int,
    modifier: Modifier = Modifier,
) {
    val isExpanded = node.id in expandedNodeIds
    val hasChildren = node.children.isNotEmpty()
    val typeColor = colors.colorForViewType(node.viewType)

    val matchesSearch = searchQuery.isBlank() ||
        node.displayName.contains(searchQuery, ignoreCase = true) ||
        node.className.contains(searchQuery, ignoreCase = true) ||
        node.resourceId?.contains(searchQuery, ignoreCase = true) == true

    Column(modifier = modifier) {
        // Node row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (matchesSearch && searchQuery.isNotBlank()) {
                        colors.selectedNode
                    } else {
                        Color.Transparent
                    },
                )
                .clickable { onNodeClick(node) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Indentation
            Spacer(modifier = Modifier.width((depth * 16).dp))

            // Expand/collapse icon
            if (hasChildren) {
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 90f else 0f,
                    label = "expand_rotation",
                )
                IconButton(
                    onClick = { onToggleExpand(node.id) },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = colors.expandIcon,
                        modifier = Modifier.rotate(rotation),
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(24.dp))
            }

            // Type color indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(typeColor),
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Node info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = node.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = node.simpleClassName,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.className,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Child count badge
            if (hasChildren) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = typeColor.copy(alpha = 0.15f),
                ) {
                    Text(
                        text = node.children.size.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = typeColor,
                    )
                }
            }
        }

        // Children
        AnimatedVisibility(
            visible = isExpanded && hasChildren,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column {
                node.children.forEach { child ->
                    TreeNode(
                        node = child,
                        expandedNodeIds = expandedNodeIds,
                        searchQuery = searchQuery,
                        onNodeClick = onNodeClick,
                        onToggleExpand = onToggleExpand,
                        colors = colors,
                        depth = depth + 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun NodeDetailContent(
    node: ViewNode,
    colors: com.azikar24.wormaceptor.feature.viewhierarchy.ui.theme.ViewHierarchyColors,
    modifier: Modifier = Modifier,
) {
    val typeColor = colors.colorForViewType(node.viewType)

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(typeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(typeColor),
                    )
                }
                Column {
                    Text(
                        text = node.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.labelPrimary,
                    )
                    Text(
                        text = node.className,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = colors.className,
                    )
                }
            }
        }

        // Basic info section
        item {
            DetailSection(
                title = "Basic Info",
                items = listOfNotNull(
                    "Resource ID" to (node.resourceId ?: "none"),
                    "Content Description" to (node.contentDescription ?: "none"),
                    "Visibility" to when (node.visibility) {
                        0 -> "VISIBLE"
                        4 -> "INVISIBLE"
                        8 -> "GONE"
                        else -> "UNKNOWN (${node.visibility})"
                    },
                    "Depth" to node.depth.toString(),
                    "Children" to node.children.size.toString(),
                ),
                colors = colors,
            )
        }

        // Bounds section
        item {
            DetailSection(
                title = "Bounds",
                items = listOf(
                    "Left" to "${node.bounds.left}px",
                    "Top" to "${node.bounds.top}px",
                    "Right" to "${node.bounds.right}px",
                    "Bottom" to "${node.bounds.bottom}px",
                    "Width" to "${node.bounds.width}px",
                    "Height" to "${node.bounds.height}px",
                ),
                colors = colors,
            )
        }

        // Style section
        item {
            DetailSection(
                title = "Style",
                items = listOf(
                    "Alpha" to String.format("%.2f", node.alpha),
                    "Elevation" to "${node.elevation}px",
                ),
                colors = colors,
            )
        }

        // Properties section (if any)
        if (node.properties.isNotEmpty()) {
            item {
                DetailSection(
                    title = "Properties",
                    items = node.properties.toList(),
                    colors = colors,
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    items: List<Pair<String, String>>,
    colors: com.azikar24.wormaceptor.feature.viewhierarchy.ui.theme.ViewHierarchyColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = colors.labelSecondary,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = colors.searchBackground,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.labelSecondary,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = colors.valuePrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    colors: com.azikar24.wormaceptor.feature.viewhierarchy.ui.theme.ViewHierarchyColors,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.AccountTree,
                contentDescription = null,
                tint = colors.labelSecondary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = "No hierarchy captured",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.labelSecondary,
            )
            Text(
                text = "Capture a view hierarchy to inspect it here",
                style = MaterialTheme.typography.bodySmall,
                color = colors.labelSecondary,
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.US)
    return sdf.format(Date(timestamp))
}
