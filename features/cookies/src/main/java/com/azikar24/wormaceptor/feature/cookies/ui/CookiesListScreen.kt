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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.domain.entities.CookieDomain
import com.azikar24.wormaceptor.domain.entities.CookieInfo
import com.azikar24.wormaceptor.feature.cookies.ui.theme.CookiesDesignSystem
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
    onBack: (() -> Unit)? = null,
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
                    .padding(WormaCeptorDesignSystem.Spacing.md),
                singleLine = true,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
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
                        horizontal = WormaCeptorDesignSystem.Spacing.md,
                        vertical = WormaCeptorDesignSystem.Spacing.xs,
                    ),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
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
            text = {
                Text(
                    "Are you sure you want to delete all $totalCookieCount cookies from $totalDomainCount domains? This action cannot be undone.",
                )
            },
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
        WormaCeptorContainer(
            onClick = onToggle,
            style = ContainerStyle.Outlined,
            backgroundColor = CookiesDesignSystem.CookieColors.domain.asSubtleBackground(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Domain icon
                Surface(
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
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

                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = domain.domain,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                    Text(
                        text = "${domain.cookieCount} ${if (domain.cookieCount == 1) "cookie" else "cookies"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                IconButton(
                    onClick = onDeleteDomain,
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
        }

        // Expanded cookies list
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .padding(start = WormaCeptorDesignSystem.Spacing.xl)
                    .padding(top = WormaCeptorDesignSystem.Spacing.xs),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
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
private fun CookieItem(cookie: CookieInfo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val statusColor = CookiesDesignSystem.CookieColors.forExpirationStatus(cookie.expirationStatus)

    WormaCeptorContainer(
        onClick = onClick,
        style = ContainerStyle.Outlined,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Status indicator
            Box(
                modifier = Modifier
                    .width(WormaCeptorDesignSystem.BorderWidth.thick)
                    .height(32.dp)
                    .background(
                        statusColor,
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                    ),
            )

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

            // Cookie icon
            Icon(
                imageVector = Icons.Default.Cookie,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

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

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))

            // Attribute badges
            Row(horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xxs)) {
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

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))

            // Status badge
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                contentColor = statusColor,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
            ) {
                Text(
                    text = cookie.expirationStatus,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                )
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))

            // Chevron
            Text(
                text = ">",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
        }
    }
}

@Composable
private fun AttributeBadge(text: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
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
private fun EmptyCookiesState(hasSearchQuery: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
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

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

        Text(
            text = if (hasSearchQuery) "No matches found" else "No cookies",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

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
