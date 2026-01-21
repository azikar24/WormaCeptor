/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.filebrowser.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.feature.filebrowser.ui.theme.FileBrowserDesignSystem
import kotlinx.collections.immutable.ImmutableList
import java.io.File

/**
 * Breadcrumb navigation bar showing the current path.
 */
@Composable
fun BreadcrumbBar(
    currentPath: String?,
    navigationStack: ImmutableList<String>,
    onBreadcrumbClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(
                horizontal = FileBrowserDesignSystem.Spacing.lg,
                vertical = FileBrowserDesignSystem.Spacing.sm,
            ),
        horizontalArrangement = Arrangement.spacedBy(FileBrowserDesignSystem.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Root/Home icon
        Icon(
            imageVector = Icons.Default.Home,
            contentDescription = "Root",
            tint = if (currentPath == null) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier
                .clickable { onBreadcrumbClick(-1) }
                .padding(FileBrowserDesignSystem.Spacing.xs),
        )

        // Path segments
        navigationStack.forEachIndexed { index, path ->
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = FileBrowserDesignSystem.Spacing.xxs),
            )

            val fileName = File(path).name
            val isLast = index == navigationStack.lastIndex

            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isLast) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .clickable { onBreadcrumbClick(index) }
                    .padding(FileBrowserDesignSystem.Spacing.xs),
            )
        }
    }
}
