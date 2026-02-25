package com.azikar24.wormaceptor.feature.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.domain.contracts.FeatureConfig
import com.azikar24.wormaceptor.feature.settings.R
import com.azikar24.wormaceptor.feature.settings.vm.SettingsViewModel

/**
 * Screen for managing feature toggles.
 * Allows users to show/hide tabs and menu items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureToggleScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val config by viewModel.featureConfig.collectAsState()

    FeatureToggleScreenContent(
        config = config,
        onBack = onBack,
        onResetDefaults = viewModel::resetToDefaults,
        onToggleNetworkTab = viewModel::toggleNetworkTab,
        onToggleCrashesTab = viewModel::toggleCrashesTab,
        onTogglePreferences = viewModel::togglePreferences,
        onToggleConsoleLogs = viewModel::toggleConsoleLogs,
        onToggleDeviceInfo = viewModel::toggleDeviceInfo,
        onToggleSqliteBrowser = viewModel::toggleSqliteBrowser,
        onToggleFileBrowser = viewModel::toggleFileBrowser,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeatureToggleScreenContent(
    config: FeatureConfig,
    onBack: () -> Unit,
    onResetDefaults: () -> Unit,
    onToggleNetworkTab: () -> Unit,
    onToggleCrashesTab: () -> Unit,
    onTogglePreferences: () -> Unit,
    onToggleConsoleLogs: () -> Unit,
    onToggleDeviceInfo: () -> Unit,
    onToggleSqliteBrowser: () -> Unit,
    onToggleFileBrowser: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
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
                    IconButton(onClick = onResetDefaults) {
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
                    onCheckedChange = { onToggleNetworkTab() },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_crashes_tab),
                    description = stringResource(R.string.settings_crashes_tab_description),
                    checked = config.showCrashesTab,
                    onCheckedChange = { onToggleCrashesTab() },
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
                    onCheckedChange = { onTogglePreferences() },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_console_logs_title),
                    description = stringResource(R.string.settings_console_logs_description),
                    checked = config.showConsoleLogs,
                    onCheckedChange = { onToggleConsoleLogs() },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_device_info_title),
                    description = stringResource(R.string.settings_device_info_description),
                    checked = config.showDeviceInfo,
                    onCheckedChange = { onToggleDeviceInfo() },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_sqlite_browser_title),
                    description = stringResource(R.string.settings_sqlite_browser_description),
                    checked = config.showSqliteBrowser,
                    onCheckedChange = { onToggleSqliteBrowser() },
                )
            }

            item {
                FeatureToggleItem(
                    title = stringResource(R.string.settings_file_browser_title),
                    description = stringResource(R.string.settings_file_browser_description),
                    checked = config.showFileBrowser,
                    onCheckedChange = { onToggleFileBrowser() },
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
            onResetDefaults = {},
            onToggleNetworkTab = {},
            onToggleCrashesTab = {},
            onTogglePreferences = {},
            onToggleConsoleLogs = {},
            onToggleDeviceInfo = {},
            onToggleSqliteBrowser = {},
            onToggleFileBrowser = {},
        )
    }
}
