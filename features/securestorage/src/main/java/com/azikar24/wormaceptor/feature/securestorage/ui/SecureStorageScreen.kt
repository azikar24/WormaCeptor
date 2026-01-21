/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.securestorage.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import com.azikar24.wormaceptor.feature.securestorage.ui.theme.secureStorageColors
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val scope = rememberCoroutineScope()

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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = colors.primary,
                        )
                        Text(
                            text = "Secure Storage",
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
                    IconButton(onClick = onRefresh, enabled = !isLoading) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                            )
                        }
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

            // Summary cards
            SummarySection(
                summary = summary,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Type filter chips
            TypeFilterChips(
                selectedType = selectedType,
                onTypeSelected = onTypeSelected,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Error message
            error?.let { errorMessage ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            // Entries list
            if (entries.isEmpty() && !isLoading) {
                EmptyState(
                    hasFilters = selectedType != null || searchQuery.isNotBlank(),
                    colors = colors,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp,
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
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
    modifier: Modifier = Modifier,
) {
    TextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier = modifier,
        placeholder = {
            Text(
                text = "Search keys and values...",
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
private fun SummarySection(
    summary: SecureStorageSummary,
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SummaryCard(
            count = summary.encryptedPrefsCount,
            label = "Prefs",
            icon = Icons.Default.EnhancedEncryption,
            color = colors.encryptedPrefs,
            colors = colors,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            count = summary.keystoreAliasCount,
            label = "KeyStore",
            icon = Icons.Default.Key,
            color = colors.keystore,
            colors = colors,
            modifier = Modifier.weight(1f),
        )
        SummaryCard(
            count = summary.dataStoreFileCount,
            label = "DataStore",
            icon = Icons.Default.DataObject,
            color = colors.datastore,
            colors = colors,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryCard(
    count: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text("All") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.chipBackgroundSelected,
            ),
        )
        StorageType.entries.forEach { type ->
            val (icon, label, color) = when (type) {
                StorageType.ENCRYPTED_SHARED_PREFS -> Triple(Icons.Default.EnhancedEncryption, "Prefs", colors.encryptedPrefs)
                StorageType.KEYSTORE -> Triple(Icons.Default.Key, "KeyStore", colors.keystore)
                StorageType.DATASTORE -> Triple(Icons.Default.DataObject, "DataStore", colors.datastore)
            }
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(if (selectedType == type) null else type) },
                label = { Text(label) },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color.copy(alpha = 0.2f),
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.cardBackground,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Type indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (entry.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = null,
                    tint = if (entry.isEncrypted) colors.encrypted else colors.unencrypted,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

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
                        text = formatTimestamp(timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.labelSecondary,
                    )
                }
            }

            // Type badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = typeColor.copy(alpha = 0.15f),
            ) {
                Text(
                    text = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> "Prefs"
                        StorageType.KEYSTORE -> "Key"
                        StorageType.DATASTORE -> "DS"
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
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
                Icon(
                    imageVector = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> Icons.Default.EnhancedEncryption
                        StorageType.KEYSTORE -> Icons.Default.Key
                        StorageType.DATASTORE -> Icons.Default.DataObject
                    },
                    contentDescription = null,
                    tint = typeColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column {
                Text(
                    text = when (entry.storageType) {
                        StorageType.ENCRYPTED_SHARED_PREFS -> "Encrypted SharedPreferences"
                        StorageType.KEYSTORE -> "Android KeyStore"
                        StorageType.DATASTORE -> "DataStore"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.labelPrimary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (entry.isEncrypted) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (entry.isEncrypted) colors.encrypted else colors.unencrypted,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = if (entry.isEncrypted) "Encrypted" else "Not encrypted",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.isEncrypted) colors.encrypted else colors.unencrypted,
                    )
                }
            }
        }

        // Key section
        DetailSection(
            label = "Key",
            value = entry.key,
            colors = colors,
        )

        // Value section
        DetailSection(
            label = "Value",
            value = entry.value,
            colors = colors,
        )

        // Timestamp
        entry.lastModified?.let { timestamp ->
            DetailSection(
                label = "Last Modified",
                value = formatTimestampFull(timestamp),
                colors = colors,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
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
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.labelSecondary,
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = colors.searchBackground,
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = colors.valuePrimary,
            )
        }
    }
}

@Composable
private fun EmptyState(
    hasFilters: Boolean,
    colors: com.azikar24.wormaceptor.feature.securestorage.ui.theme.SecureStorageColors,
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
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                tint = colors.labelSecondary,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = if (hasFilters) "No matching entries" else "No secure storage found",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.labelSecondary,
            )
            Text(
                text = if (hasFilters) {
                    "Try adjusting your filters"
                } else {
                    "This app has no EncryptedSharedPreferences,\nKeyStore aliases, or DataStore files"
                },
                style = MaterialTheme.typography.bodySmall,
                color = colors.valueSecondary,
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.US)
    return sdf.format(Date(timestamp))
}

private fun formatTimestampFull(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMMM d, yyyy 'at' HH:mm:ss", Locale.US)
    return sdf.format(Date(timestamp))
}
