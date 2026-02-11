package com.azikar24.wormaceptor.feature.preferences.ui

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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.PreferenceFile
import com.azikar24.wormaceptor.feature.preferences.R
import kotlinx.collections.immutable.ImmutableList

/**
 * Screen displaying a list of SharedPreferences files.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesListScreen(
    files: ImmutableList<PreferenceFile>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onFileClick: (PreferenceFile) -> Unit,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.preferences_list_title)) },
                navigationIcon = {
                    onNavigateBack?.let { callback ->
                        IconButton(onClick = callback) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.preferences_back),
                            )
                        }
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Search bar
            WormaCeptorSearchBar(
                query = searchQuery,
                onQueryChange = onSearchQueryChanged,
                placeholder = stringResource(R.string.preferences_search_placeholder),
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
            )

            if (files.isEmpty()) {
                WormaCeptorEmptyState(
                    title = if (searchQuery.isNotBlank()) {
                        stringResource(R.string.preferences_empty_no_matches)
                    } else {
                        stringResource(R.string.preferences_empty_no_files)
                    },
                    modifier = Modifier.fillMaxSize(),
                    subtitle = if (searchQuery.isNotBlank()) {
                        stringResource(R.string.preferences_empty_try_different_search)
                    } else {
                        stringResource(R.string.preferences_empty_files_will_appear)
                    },
                    icon = Icons.Default.Settings,
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
                    items(files, key = { it.name }) { file ->
                        PreferenceFileItem(
                            file = file,
                            onClick = { onFileClick(file) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PreferenceFileItem(file: PreferenceFile, onClick: () -> Unit, modifier: Modifier = Modifier) {
    WormaCeptorContainer(
        onClick = onClick,
        style = ContainerStyle.Outlined,
        backgroundColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // File icon
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
                modifier = Modifier.size(WormaCeptorDesignSystem.TouchTarget.minimum),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = stringResource(R.string.preferences_list_title),
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                val itemCountLabel = if (file.itemCount == 1) {
                    stringResource(R.string.preferences_item_count_singular)
                } else {
                    stringResource(R.string.preferences_item_count_plural)
                }
                Text(
                    text = "${file.itemCount} $itemCountLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Chevron indicator
            Text(
                text = ">",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.bold),
            )
        }
    }
}
