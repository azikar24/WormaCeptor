/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cookies.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.CookieDomain
import com.azikar24.wormaceptor.domain.entities.CookieInfo
import com.azikar24.wormaceptor.feature.cookies.ui.theme.CookiesDesignSystem
import com.azikar24.wormaceptor.feature.cookies.ui.theme.asSubtleBackground
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet

/**
 * Screen displaying a list of cookies grouped by domain.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookiesListScreen(
    domains: ImmutableList<CookieDomain>,
    expandedDomains: ImmutableSet<String>,
    searchQuery: String,
    totalCookieCount: Int,
    totalDomainCount: Int,
    onSearchQueryChanged: (String) -> Unit,
    onToggleDomain: (String) -> Unit,
    onExpandAll: () -> Unit,
    onCollapseAll: () -> Unit,
    onCookieClick: (CookieInfo) -> Unit,
    onDeleteDomain: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showClearAllDialog by remember { mutableStateOf(false) }
    var showDeleteDomainDialog by remember { mutableStateOf<String?>(null) }
    var showMoreMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Cookies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "$totalCookieCount cookies in $totalDomainCount domains",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    if (domains.isNotEmpty()) {
                        IconButton(onClick = {
                            if (expandedDomains.size == domains.size) {
                                onCollapseAll()
                            } else {
                                onExpandAll()
                            }
                        }) {
                            Icon(
                                imageVector = if (expandedDomains.size == domains.size) {
                                    Icons.Default.UnfoldLess
                                } else {
                                    Icons.Default.UnfoldMore
                                },
                                contentDescription = if (expandedDomains.size == domains.size) {
                                    "Collapse all"
                                } else {
                                    "Expand all"
                                },
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear all cookies",
                            )
                        }
                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Clear All Cookies") },
                                onClick = {
                                    showMoreMenu = false
                                    showClearAllDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.DeleteSweep, contentDescription = null)
                                },
                                enabled = domains.isNotEmpty(),
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = { Text("Search cookies...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CookiesDesignSystem.Spacing.md),
                singleLine = true,
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
            )

            if (domains.isEmpty()) {
                EmptyCookiesState(
                    hasSearchQuery = searchQuery.isNotBlank(),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        horizontal = CookiesDesignSystem.Spacing.md,
                        vertical = CookiesDesignSystem.Spacing.xs,
                    ),
                    verticalArrangement = Arrangement.spacedBy(CookiesDesignSystem.Spacing.sm),
                ) {
                    items(domains, key = { it.domain }) { domain ->
                        DomainSection(
                            domain = domain,
                            isExpanded = expandedDomains.contains(domain.domain),
                            onToggle = { onToggleDomain(domain.domain) },
                            onCookieClick = onCookieClick,
                            onDeleteDomain = { showDeleteDomainDialog = domain.domain },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    // Clear all confirmation dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All Cookies") },
            text = { Text("Are you sure you want to delete all $totalCookieCount cookies from $totalDomainCount domains? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearAllDialog = false
                        onClearAll()
                    },
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    // Delete domain confirmation dialog
    showDeleteDomainDialog?.let { domain ->
        val cookieCount = domains.find { it.domain == domain }?.cookieCount ?: 0
        AlertDialog(
            onDismissRequest = { showDeleteDomainDialog = null },
            title = { Text("Delete Domain Cookies") },
            text = { Text("Are you sure you want to delete all $cookieCount cookies for $domain?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDomainDialog = null
                        onDeleteDomain(domain)
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDomainDialog = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun DomainSection(
    domain: CookieDomain,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCookieClick: (CookieInfo) -> Unit,
    onDeleteDomain: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "expandRotation",
    )

    Column(modifier = modifier) {
        // Domain header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CookiesDesignSystem.CornerRadius.md))
                .border(
                    width = CookiesDesignSystem.BorderWidth.regular,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
                )
                .background(
                    color = CookiesDesignSystem.CookieColors.domain.asSubtleBackground(),
                    shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
                )
                .clickable(onClick = onToggle)
                .padding(CookiesDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Domain icon
            Surface(
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.sm),
                color = CookiesDesignSystem.CookieColors.domain.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = CookiesDesignSystem.CookieColors.domain,
                    )
                }
            }

            Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = domain.domain,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.xxs))
                Text(
                    text = "${domain.cookieCount} ${if (domain.cookieCount == 1) "cookie" else "cookies"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = onDeleteDomain,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete all cookies for this domain",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error,
                )
            }

            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.rotate(rotationAngle),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Expanded cookies list
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = CookiesDesignSystem.Spacing.xl)
                    .padding(top = CookiesDesignSystem.Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(CookiesDesignSystem.Spacing.xs),
            ) {
                domain.cookies.forEach { cookie ->
                    CookieItem(
                        cookie = cookie,
                        onClick = { onCookieClick(cookie) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CookieItem(
    cookie: CookieInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "cookieItemScale",
    )

    val statusColor = CookiesDesignSystem.CookieColors.forExpirationStatus(cookie.expirationStatus)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(CookiesDesignSystem.CornerRadius.sm))
            .border(
                width = CookiesDesignSystem.BorderWidth.thin,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.sm),
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.sm),
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(CookiesDesignSystem.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .width(CookiesDesignSystem.BorderWidth.thick)
                .height(32.dp)
                .background(
                    statusColor,
                    shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.xs),
                ),
        )

        Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.sm))

        // Cookie icon
        Icon(
            imageVector = Icons.Default.Cookie,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.sm))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cookie.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = cookie.value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.sm))

        // Attribute badges
        Row(horizontalArrangement = Arrangement.spacedBy(CookiesDesignSystem.Spacing.xxs)) {
            if (cookie.isSecure) {
                AttributeBadge(
                    text = "S",
                    color = CookiesDesignSystem.CookieColors.secure,
                )
            }
            if (cookie.isHttpOnly) {
                AttributeBadge(
                    text = "H",
                    color = CookiesDesignSystem.CookieColors.httpOnly,
                )
            }
        }

        Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.xs))

        // Status badge
        Surface(
            color = statusColor.copy(alpha = 0.15f),
            contentColor = statusColor,
            shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.xs),
        ) {
            Text(
                text = cookie.expirationStatus,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(
                    horizontal = CookiesDesignSystem.Spacing.sm,
                    vertical = CookiesDesignSystem.Spacing.xxs,
                ),
            )
        }

        Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.xs))

        // Chevron
        Text(
            text = ">",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
        )
    }
}

@Composable
private fun AttributeBadge(
    text: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.xs),
        modifier = modifier.size(20.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun EmptyCookiesState(
    hasSearchQuery: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.lg),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Cookie,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.lg))

        Text(
            text = if (hasSearchQuery) "No matches found" else "No cookies",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.xs))

        Text(
            text = if (hasSearchQuery) {
                "Try a different search term"
            } else {
                "Cookies will appear here when stored"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
