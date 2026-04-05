package com.azikar24.wormaceptorapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptorapp.screens.securestorage.AddSecureEntryDialog
import com.azikar24.wormaceptorapp.screens.securestorage.EncryptedPrefEntry
import com.azikar24.wormaceptorapp.screens.securestorage.EncryptedPrefsSection
import com.azikar24.wormaceptorapp.screens.securestorage.KeyStoreEntry
import com.azikar24.wormaceptorapp.screens.securestorage.KeyStoreSection
import com.azikar24.wormaceptorapp.screens.securestorage.SecureStorageTestViewEffect
import com.azikar24.wormaceptorapp.screens.securestorage.SecureStorageTestViewEvent
import com.azikar24.wormaceptorapp.screens.securestorage.SecureStorageTestViewModel
import com.azikar24.wormaceptorapp.screens.securestorage.SecureStorageTestViewState
import com.azikar24.wormaceptorapp.screens.securestorage.SpeedDialFab
import kotlinx.coroutines.launch

@Composable
fun SecureStorageTestScreen(
    viewModel: SecureStorageTestViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is SecureStorageTestViewEffect.ShowError -> {
                    scope.launch { snackbarHostState.showSnackbar(effect.message) }
                }
            }
        },
    ) { state, onEvent ->
        SecureStorageTestScreenContent(
            state = state,
            onBack = onBack,
            onEvent = onEvent,
            snackbarHostState = snackbarHostState,
            modifier = modifier,
        )
    }
}

@Composable
fun SecureStorageTestScreen(
    state: SecureStorageTestViewState,
    onBack: () -> Unit,
    onEvent: (SecureStorageTestViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    SecureStorageTestScreenContent(
        state = state,
        onBack = onBack,
        onEvent = onEvent,
        snackbarHostState = remember { SnackbarHostState() },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecureStorageTestScreenContent(
    state: SecureStorageTestViewState,
    onBack: () -> Unit,
    onEvent: (SecureStorageTestViewEvent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val totalEntries = state.encryptedPrefs.size + state.keyStoreEntries.size
    val pagerState = rememberPagerState(pageCount = { 2 })

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Secure Storage Test",
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (totalEntries > 0) {
                            Text(
                                text = "$totalEntries entr${if (totalEntries != 1) "ies" else "y"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = WormaCeptorTokens.Colors.SecureStorage.encrypted,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            SpeedDialFab(
                expanded = state.isFabExpanded,
                onExpandedChange = { onEvent(SecureStorageTestViewEvent.FabExpandedChanged(it)) },
                onAddTestData = {
                    onEvent(SecureStorageTestViewEvent.SetupTestData)
                    onEvent(SecureStorageTestViewEvent.FabExpandedChanged(false))
                },
                onAddEntry = {
                    onEvent(SecureStorageTestViewEvent.ShowAddDialog)
                    onEvent(SecureStorageTestViewEvent.FabExpandedChanged(false))
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SecureStorageTabRow(
                currentPage = pagerState.currentPage,
                encryptedPrefsCount = state.encryptedPrefs.size,
                keyStoreEntriesCount = state.keyStoreEntries.size,
                onSelectTab = { page -> scope.launch { pagerState.animateScrollToPage(page) } },
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> EncryptedPrefsSection(entries = state.encryptedPrefs)
                    1 -> KeyStoreSection(entries = state.keyStoreEntries)
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddSecureEntryDialog(
            onDismiss = { onEvent(SecureStorageTestViewEvent.DismissAddDialog) },
            onAdd = { key, value -> onEvent(SecureStorageTestViewEvent.AddEntry(key, value)) },
        )
    }
}

@Composable
private fun SecureStorageTabRow(
    currentPage: Int,
    encryptedPrefsCount: Int,
    keyStoreEntriesCount: Int,
    onSelectTab: (Int) -> Unit,
) {
    TabRow(selectedTabIndex = currentPage) {
        Tab(
            selected = currentPage == 0,
            onClick = { onSelectTab(0) },
            text = {
                TabContent(
                    icon = Icons.Default.Lock,
                    label = "Encrypted Prefs",
                    count = encryptedPrefsCount,
                )
            },
        )
        Tab(
            selected = currentPage == 1,
            onClick = { onSelectTab(1) },
            text = {
                TabContent(
                    icon = Icons.Default.Key,
                    label = "KeyStore",
                    count = keyStoreEntriesCount,
                )
            },
        )
    }
}

@Composable
private fun TabContent(
    icon: ImageVector,
    label: String,
    count: Int,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        Text(label)
        if (count > 0) {
            Surface(
                shape = CircleShape,
                color = WormaCeptorTokens.Colors.SecureStorage.encrypted.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                modifier = Modifier.size(20.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WormaCeptorTokens.Colors.SecureStorage.encrypted,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SecureStorageTestScreenEmptyPreview() {
    WormaCeptorTheme {
        SecureStorageTestScreen(
            state = SecureStorageTestViewState(),
            onBack = {},
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecureStorageTestScreenWithDataPreview() {
    WormaCeptorTheme {
        SecureStorageTestScreen(
            state = SecureStorageTestViewState(
                encryptedPrefs = listOf(
                    EncryptedPrefEntry(key = "auth_token", value = "eyJhbGciOiJIUzI1NiJ9.test", type = "String"),
                    EncryptedPrefEntry(key = "login_count", value = "42", type = "Int"),
                    EncryptedPrefEntry(key = "is_premium", value = "true", type = "Boolean"),
                ),
                keyStoreEntries = listOf(
                    KeyStoreEntry(
                        alias = "test_encryption_key",
                        algorithm = "AES",
                        keySize = 256,
                        creationDate = "2026-04-05",
                    ),
                    KeyStoreEntry(
                        alias = "test_signing_key",
                        algorithm = "AES",
                        keySize = 256,
                        creationDate = "2026-04-05",
                    ),
                ),
            ),
            onBack = {},
            onEvent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecureStorageTestScreenDialogPreview() {
    WormaCeptorTheme {
        SecureStorageTestScreen(
            state = SecureStorageTestViewState(showAddDialog = true),
            onBack = {},
            onEvent = {},
        )
    }
}
