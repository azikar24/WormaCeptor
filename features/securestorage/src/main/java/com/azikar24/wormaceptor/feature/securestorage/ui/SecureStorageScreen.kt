package com.azikar24.wormaceptor.feature.securestorage.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.dialog.WormaCeptorBottomSheet
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry
import com.azikar24.wormaceptor.domain.entities.SecureStorageEntry.StorageType
import com.azikar24.wormaceptor.domain.entities.SecureStorageSummary
import com.azikar24.wormaceptor.feature.securestorage.R
import com.azikar24.wormaceptor.feature.securestorage.ui.components.EntryCard
import com.azikar24.wormaceptor.feature.securestorage.ui.components.EntryDetailContent
import com.azikar24.wormaceptor.feature.securestorage.ui.components.SummarySection
import com.azikar24.wormaceptor.feature.securestorage.ui.components.TypeFilterChips
import com.azikar24.wormaceptor.feature.securestorage.vm.SecureStorageViewEvent
import com.azikar24.wormaceptor.feature.securestorage.vm.SecureStorageViewState
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureStorageScreen(
    state: SecureStorageViewState,
    onEvent: (SecureStorageViewEvent) -> Unit,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier,
        topBar = {
            SecureStorageTopAppBar(
                state = state,
                searchActive = searchActive,
                onSearchToggle = {
                    searchActive = !searchActive
                    if (!searchActive) onEvent(SecureStorageViewEvent.UpdateSearchQuery(""))
                },
                onEvent = onEvent,
                onBack = onBack,
            )
        },
    ) { paddingValues ->
        SecureStorageContent(
            state = state,
            onEvent = onEvent,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
        )

        state.selectedEntry?.let { entry ->
            WormaCeptorBottomSheet(
                onDismissRequest = { onEvent(SecureStorageViewEvent.DismissDetail) },
                sheetState = sheetState,
            ) {
                EntryDetailContent(entry = entry)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecureStorageTopAppBar(
    state: SecureStorageViewState,
    searchActive: Boolean,
    onSearchToggle: () -> Unit,
    onEvent: (SecureStorageViewEvent) -> Unit,
    onBack: (() -> Unit)?,
) {
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
                IconButton(onClick = onSearchToggle) {
                    Icon(
                        imageVector = if (searchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = stringResource(R.string.securestorage_search),
                    )
                }
                IconButton(
                    onClick = { onEvent(SecureStorageViewEvent.Refresh) },
                    enabled = !state.isLoading,
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
                            strokeWidth = WormaCeptorTokens.BorderWidth.thick,
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
                animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
            ) + fadeIn(animationSpec = tween(WormaCeptorTokens.Animation.NORMAL)),
            exit = shrinkVertically(
                animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
            ) + fadeOut(animationSpec = tween(WormaCeptorTokens.Animation.NORMAL)),
        ) {
            WormaCeptorSearchBar(
                query = state.searchQuery,
                onQueryChange = { onEvent(SecureStorageViewEvent.UpdateSearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorTokens.Spacing.lg)
                    .padding(top = WormaCeptorTokens.Spacing.sm),
                placeholder = stringResource(R.string.securestorage_search_placeholder),
            )
        }
    }
}

@Composable
private fun SecureStorageContent(
    state: SecureStorageViewState,
    onEvent: (SecureStorageViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SummarySection(
            summary = state.summary,
            keystoreAccessible = state.keystoreAccessible,
            encryptedPrefsAccessible = state.encryptedPrefsAccessible,
            lastRefreshTime = state.lastRefreshTime,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WormaCeptorTokens.Spacing.lg)
                .padding(top = WormaCeptorTokens.Spacing.sm),
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        TypeFilterChips(
            selectedType = state.selectedType,
            onTypeSelected = { onEvent(SecureStorageViewEvent.SelectType(it)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = WormaCeptorTokens.Spacing.lg),
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

        state.error?.let { errorMessage ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = WormaCeptorTokens.Spacing.lg),
                shape = WormaCeptorTokens.Shapes.card,
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        if (state.filteredEntries.isEmpty() && !state.isLoading) {
            WormaCeptorEmptyState(
                title = if (state.selectedType != null || state.searchQuery.isNotBlank()) {
                    stringResource(R.string.securestorage_empty_no_matches)
                } else {
                    stringResource(R.string.securestorage_empty_no_storage)
                },
                modifier = Modifier.fillMaxSize(),
                subtitle = if (state.selectedType != null || state.searchQuery.isNotBlank()) {
                    stringResource(R.string.securestorage_empty_adjust_filters)
                } else {
                    stringResource(R.string.securestorage_empty_description)
                },
                icon = Icons.Default.Storage,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                contentPadding = PaddingValues(
                    start = WormaCeptorTokens.Spacing.lg,
                    end = WormaCeptorTokens.Spacing.lg,
                    bottom = WormaCeptorTokens.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                ),
            ) {
                items(state.filteredEntries, key = { it.key }) { entry ->
                    EntryCard(
                        entry = entry,
                        onClick = { onEvent(SecureStorageViewEvent.SelectEntry(entry)) },
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SecureStorageScreenPreview() {
    WormaCeptorTheme {
        SecureStorageScreen(
            state = SecureStorageViewState(
                filteredEntries = persistentListOf(
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
                keystoreAccessible = true,
                encryptedPrefsAccessible = true,
                lastRefreshTime = System.currentTimeMillis(),
            ),
            onEvent = {},
            onBack = {},
        )
    }
}
