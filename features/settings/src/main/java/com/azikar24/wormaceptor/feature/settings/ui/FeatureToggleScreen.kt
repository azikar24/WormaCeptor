package com.azikar24.wormaceptor.feature.settings.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.contracts.FeatureConfig
import com.azikar24.wormaceptor.feature.settings.R
import com.azikar24.wormaceptor.feature.settings.vm.SettingsViewEvent
import com.azikar24.wormaceptor.feature.settings.vm.SettingsViewModel

/**
 * Screen for managing feature toggles.
 * Allows users to show/hide tabs and menu items.
 */
@Composable
fun FeatureToggleScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BaseScreen(viewModel) { state, onEvent ->
        FeatureToggleScreenContent(
            config = state.featureConfig,
            onBack = onBack,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeatureToggleScreenContent(
    config: FeatureConfig,
    onBack: () -> Unit,
    onEvent: (SettingsViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(SettingsViewEvent.ResetToDefaults) }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = stringResource(R.string.settings_reset_defaults),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
            ),
        ) {
            // Tabs Section
            item {
                SectionHeader(text = stringResource(R.string.settings_tabs_title))
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_network_tab),
                    description = stringResource(R.string.settings_network_tab_description),
                    checked = config.showNetworkTab,
                    onCheckedChange = { onEvent(SettingsViewEvent.ToggleNetworkTab) },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_crashes_tab),
                    description = stringResource(R.string.settings_crashes_tab_description),
                    checked = config.showCrashesTab,
                    onCheckedChange = { onEvent(SettingsViewEvent.ToggleCrashesTab) },
                )
            }

            item {
                WormaCeptorDivider(modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.sm))
            }

            // Tools Section
            item {
                SectionHeader(text = stringResource(R.string.settings_tools_title))
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_preferences_title),
                    description = stringResource(R.string.settings_preferences_description),
                    checked = config.showPreferences,
                    onCheckedChange = { onEvent(SettingsViewEvent.TogglePreferences) },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_console_logs_title),
                    description = stringResource(R.string.settings_console_logs_description),
                    checked = config.showConsoleLogs,
                    onCheckedChange = { onEvent(SettingsViewEvent.ToggleConsoleLogs) },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_device_info_title),
                    description = stringResource(R.string.settings_device_info_description),
                    checked = config.showDeviceInfo,
                    onCheckedChange = { onEvent(SettingsViewEvent.ToggleDeviceInfo) },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_sqlite_browser_title),
                    description = stringResource(R.string.settings_sqlite_browser_description),
                    checked = config.showSqliteBrowser,
                    onCheckedChange = { onEvent(SettingsViewEvent.ToggleSqliteBrowser) },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_file_browser_title),
                    description = stringResource(R.string.settings_file_browser_description),
                    checked = config.showFileBrowser,
                    onCheckedChange = { onEvent(SettingsViewEvent.ToggleFileBrowser) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = WormaCeptorDesignSystem.Spacing.lg, vertical = WormaCeptorDesignSystem.Spacing.md),
    )
}

@Composable
private fun FeatureToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        supportingContent = {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun FeatureToggleScreenPreview() {
    WormaCeptorTheme {
        FeatureToggleScreenContent(
            config = FeatureConfig(
                showNetworkTab = true,
                showCrashesTab = true,
                showPreferences = false,
                showConsoleLogs = true,
                showDeviceInfo = true,
                showSqliteBrowser = false,
                showFileBrowser = true,
            ),
            onBack = {},
            onEvent = {},
        )
    }
}
