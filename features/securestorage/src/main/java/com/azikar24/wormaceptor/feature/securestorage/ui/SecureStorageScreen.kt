package com.azikar24.wormaceptor.feature.securestorage.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.formatDateShort
import com.azikar24.wormaceptor.core.ui.util.formatTimestampFull
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import com.azikar24.wormaceptor.feature.securestorage.R
import com.azikar24.wormaceptor.feature.securestorage.ui.theme.secureStorageColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * Main screen for the Secure Storage Viewer.
 *
 * Features:
 * - Summary cards showing counts by storage type
 * - Type filter chips
 * - Search functionality
 * - Entry list with detail sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureStorageScreen(
    entries: ImmutableList<SecureStorageEntry>,
    summary: SecureStorageSummary,
    isLoading: Boolean,
    error: String?,
    selectedType: StorageType?,
    searchQuery: String,
    selectedEntry: SecureStorageEntry?,
    keystoreAccessible: Boolean,
    encryptedPrefsAccessible: Boolean,
    lastRefreshTime: Long?,
    onTypeSelected: (StorageType?) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onEntrySelected: (SecureStorageEntry) -> Unit,
    onDismissDetail: () -> Unit,
    onRefresh: () -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = secureStorageColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.securestorage_title),
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        onBack?.let { back ->
                            IconButton(onClick = back) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.securestorage_back),
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            searchActive = !searchActive
                            if (!searchActive) onSearchQueryChanged("")
                        }) {
                            Icon(
                                imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = stringResource(R.string.securestorage_search),
                            )
                        }
                        IconButton(onClick = onRefresh, enabled = !isLoading) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.securestorage_refresh),
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                AnimatedVisibility(
                    visible = searchActive,
                    enter = expandVertically(
                        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
                    ) + fadeIn(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal)),
                    exit = shrinkVertically(
                        animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal),
                    ) + fadeOut(animationSpec = tween(WormaCeptorDesignSystem.AnimationDuration.normal)),
                ) {
                    WormaCeptorSearchBar(
                        query = searchQuery,
                        onQueryChange = onSearchQueryChanged,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                            .padding(top = WormaCeptorDesignSystem.Spacing.sm),
                        placeholder = stringResource(R.string.securestorage_search_placeholder),
                    )
                }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Summary cards with integrated status
            SummarySection(
                summary = summary,
                keystoreAccessible = keystoreAccessible,
                encryptedPrefsAccessible = encryptedPrefsAccessible,
                lastRefreshTime = lastRefreshTime,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg)
                    .padding(top = WormaCeptorDesignSystem.Spacing.sm),
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Type filter chips
            TypeFilterChips(
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Error message
            error?.let { errorMessage ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg),
                    shape = WormaCeptorDesignSystem.Shapes.card,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // Entries list
            if (entries.isEmpty() && !isLoading) {
                WormaCeptorEmptyState(
                    title = if (selectedType != null || searchQuery.isNotBlank()) {
                        stringResource(R.string.securestorage_empty_no_matches)
                    } else {
                        stringResource(R.string.securestorage_empty_no_storage)
                    },
                    modifier = Modifier.fillMaxSize(),
                    subtitle = if (selectedType != null || searchQuery.isNotBlank()) {
                        stringResource(R.string.securestorage_empty_adjust_filters)
                    } else {
                        stringResource(R.string.securestorage_empty_description)
                    },
                    icon = Icons.Default.Storage,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    contentPadding = PaddingValues(
                        start = WormaCeptorDesignSystem.Spacing.lg,
                        end = WormaCeptorDesignSystem.Spacing.lg,
                        bottom = WormaCeptorDesignSystem.Spacing.lg,
                    ),
                ) {
                    items(entries, key = { it.key }) { entry ->
                        EntryCard(
                            entry = entry,
                            onClick = { onEntrySelected(entry) },
                            colors = colors,
                        )
                    }
                }
            }
        }

        // Detail sheet
        selectedEntry?.let { entry ->
            ModalBottomSheet(
                onDismissRequest = onDismissDetail,
                sheetState = sheetState,
            ) {
                EntryDetailContent(
                    entry = entry,
                    colors = colors,
                    modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
                )
            }
        }
    }
}

@Composable
private fun SummarySection(
    summary: SecureStorageSummary,
    keystoreAccessible: Boolean,
    encryptedPrefsAccessible: Boolean,
    lastRefreshTime: Long?,
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        val accessibleText = stringResource(R.string.securestorage_status_accessible)
        val notAccessibleText = stringResource(R.string.securestorage_status_not_accessible)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            SummaryCard(
                count = summary.encryptedPrefsCount,
                label = stringResource(R.string.securestorage_summary_prefs),
                icon = Icons.Default.EnhancedEncryption,
                color = colors.encryptedPrefs,
                isAccessible = encryptedPrefsAccessible,
                accessibleText = accessibleText,
                notAccessibleText = notAccessibleText,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                count = summary.keystoreAliasCount,
                label = stringResource(R.string.securestorage_summary_keystore),
                icon = Icons.Default.Key,
                color = colors.keystore,
                isAccessible = keystoreAccessible,
                accessibleText = accessibleText,
                notAccessibleText = notAccessibleText,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
            SummaryCard(
                count = summary.dataStoreFileCount,
                label = stringResource(R.string.securestorage_summary_datastore),
                icon = Icons.Default.DataObject,
                color = colors.datastore,
                isAccessible = true, // DataStore is always accessible if files exist
                accessibleText = accessibleText,
                notAccessibleText = notAccessibleText,
                colors = colors,
                modifier = Modifier.weight(1f),
            )
        }

        // Last refresh time
        lastRefreshTime?.let { time ->
            Text(
                text = stringResource(R.string.securestorage_last_scanned, formatDateShort(time)),
                style = MaterialTheme.typography.labelSmall,
                color = colors.labelSecondary,
                modifier = Modifier.padding(start = WormaCeptorDesignSystem.Spacing.xs),
            )
        }
    }
}

@Composable
private fun SummaryCard(
    count: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    isAccessible: Boolean,
    accessibleText: String,
    notAccessibleText: String,
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Icon with status indicator
            Box {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
                // Small status dot in corner
                Icon(
                    imageVector = if (isAccessible) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = if (isAccessible) accessibleText else notAccessibleText,
                    tint = if (isAccessible) colors.encrypted else colors.unencrypted,
                    modifier = Modifier
                        .size(10.dp)
                        .align(Alignment.TopEnd),
                )
            }
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.labelSecondary,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TypeFilterChips(
    selectedType: StorageType?,
    onTypeSelected: (StorageType?) -> Unit,
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text(stringResource(R.string.securestorage_filter_all)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.chipBackgroundSelected,
            ),
        )
        StorageType.entries.forEach { type ->
            val (icon, color) = when (type) {
                StorageType.ENCRYPTED_SHARED_PREFS -> Pair(
                    Icons.Default.EnhancedEncryption,
                    colors.encryptedPrefs,
                )
                StorageType.KEYSTORE -> Pair(Icons.Default.Key, colors.keystore)
                StorageType.DATASTORE -> Pair(Icons.Default.DataObject, colors.datastore)
            }
            val label = when (type) {
                StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(R.string.securestorage_filter_prefs)
                StorageType.KEYSTORE -> stringResource(R.string.securestorage_filter_keystore)
                StorageType.DATASTORE -> stringResource(R.string.securestorage_filter_datastore)
            }
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(if (selectedType == type) null else type) },
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
                    selectedLabelColor = color,
                    selectedLeadingIconColor = color,
                ),
            )
        }
    }
}

@Composable
private fun EntryCard(
    entry: SecureStorageEntry,
    onClick: () -> Unit,
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
    modifier: Modifier = Modifier,
) {
    val typeColor = when (entry.storageType) {
        StorageType.ENCRYPTED_SHARED_PREFS -> colors.encryptedPrefs
        StorageType.KEYSTORE -> colors.keystore
        StorageType.DATASTORE -> colors.datastore
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Type indicator
            val encryptionState = if (entry.isEncrypted) {
                stringResource(R.string.securestorage_detail_encrypted)
            } else {
                stringResource(R.string.securestorage_detail_not_encrypted)
            }
            Box(
                modifier = Modifier
                    .size(WormaCeptorDesignSystem.TouchTarget.minimum)
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md))
                    .background(typeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light))
                    .semantics { stateDescription = encryptionState },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (entry.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = encryptionState,
                    tint = if (entry.isEncrypted) colors.encrypted else colors.unencrypted,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = entry.key,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    color = colors.labelPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = entry.value.take(100),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.valueSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                entry.lastModified?.let { timestamp ->
                    Text(
                        text = formatDateShort(timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.labelSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            // Type badge
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                color = typeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
            ) {
                Text(
                    text = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(R.string.securestorage_badge_prefs)
                        StorageType.KEYSTORE -> stringResource(R.string.securestorage_badge_keystore)
                        StorageType.DATASTORE -> stringResource(R.string.securestorage_badge_datastore)
                    },
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.xs + WormaCeptorDesignSystem.Spacing.xxs,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = typeColor,
                )
            }
        }
    }
}

@Composable
private fun EntryDetailContent(
    entry: SecureStorageEntry,
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
    modifier: Modifier = Modifier,
) {
    val typeColor = when (entry.storageType) {
        StorageType.ENCRYPTED_SHARED_PREFS -> colors.encryptedPrefs
        StorageType.KEYSTORE -> colors.keystore
        StorageType.DATASTORE -> colors.datastore
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(WormaCeptorDesignSystem.Spacing.xxxl)
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.lg))
                    .background(typeColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> Icons.Default.EnhancedEncryption
                        StorageType.KEYSTORE -> Icons.Default.Key
                        StorageType.DATASTORE -> Icons.Default.DataObject
                    },
                    contentDescription = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(
                            R.string.securestorage_detail_encrypted_prefs,
                        )
                        StorageType.KEYSTORE -> stringResource(R.string.securestorage_detail_android_keystore)
                        StorageType.DATASTORE -> stringResource(R.string.securestorage_detail_datastore)
                    },
                    tint = typeColor,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                )
            }
            Column {
                Text(
                    text = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> stringResource(
                            R.string.securestorage_detail_encrypted_prefs,
                        )
                        StorageType.KEYSTORE -> stringResource(R.string.securestorage_detail_android_keystore)
                        StorageType.DATASTORE -> stringResource(R.string.securestorage_detail_datastore)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.labelPrimary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (entry.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (entry.isEncrypted) {
                            stringResource(R.string.securestorage_detail_encrypted)
                        } else {
                            stringResource(R.string.securestorage_detail_not_encrypted)
                        },
                        tint = if (entry.isEncrypted) colors.encrypted else colors.unencrypted,
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xs),
                    )
                    Text(
                        text = if (entry.isEncrypted) {
                            stringResource(R.string.securestorage_detail_encrypted)
                        } else {
                            stringResource(R.string.securestorage_detail_not_encrypted)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.isEncrypted) colors.encrypted else colors.unencrypted,
                    )
                }
            }
        }

        // Key section
        DetailSection(
            label = stringResource(R.string.securestorage_detail_label_key),
            value = entry.key,
            colors = colors,
        )

        // Value section
        DetailSection(
            label = stringResource(R.string.securestorage_detail_label_value),
            value = entry.value,
            colors = colors,
        )

        // Timestamp
        entry.lastModified?.let { timestamp ->
            DetailSection(
                label = stringResource(R.string.securestorage_detail_label_last_modified),
                value = formatTimestampFull(timestamp),
                colors = colors,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
    }
}

@Composable
private fun DetailSection(
    label: String,
    value: String,
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.labelSecondary,
            modifier = Modifier.semantics { heading() },
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = WormaCeptorDesignSystem.Shapes.card,
            color = colors.searchBackground,
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = colors.valuePrimary,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SecureStorageScreenPreview() {
    WormaCeptorTheme {
        SecureStorageScreen(
            entries = persistentListOf(
                SecureStorageEntry(
                    key = "user_token",
                    value = "eyJhbGciOiJIUzI1NiJ9.test",
                    storageType = StorageType.ENCRYPTED_SHARED_PREFS,
                    isEncrypted = true,
                    lastModified = System.currentTimeMillis() - 3_600_000L,
                ),
                SecureStorageEntry(
                    key = "test_signing_key",
                    value = "AES-256",
                    storageType = StorageType.KEYSTORE,
                    isEncrypted = true,
                    lastModified = null,
                ),
            ),
            summary = SecureStorageSummary(
                encryptedPrefsCount = 5,
                keystoreAliasCount = 3,
                dataStoreFileCount = 1,
            ),
            isLoading = false,
            error = null,
            selectedType = null,
            searchQuery = "",
            selectedEntry = null,
            keystoreAccessible = true,
            encryptedPrefsAccessible = true,
            lastRefreshTime = System.currentTimeMillis(),
            onTypeSelected = {},
            onSearchQueryChanged = {},
            onEntrySelected = {},
            onDismissDetail = {},
            onRefresh = {},
            onBack = {},
        )
    }
}
