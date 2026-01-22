/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.settings.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.feature.settings.vm.SettingsViewModel

/**
 * Screen for managing feature toggles.
 * Allows users to show/hide tabs and menu items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureToggleScreen(viewModel: SettingsViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val config by viewModel.featureConfig.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Feature Toggles") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::resetToDefaults) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset to defaults",
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
                SectionHeader(text = "Tabs")
            }

            item {
                FeatureToggleItem(
                    title = "Network Tab",
                    description = "Show network transactions",
                    checked = config.showNetworkTab,
                    onCheckedChange = { viewModel.toggleNetworkTab() },
                )
            }

            item {
                FeatureToggleItem(
                    title = "Crashes Tab",
                    description = "Show crash reports",
                    checked = config.showCrashesTab,
                    onCheckedChange = { viewModel.toggleCrashesTab() },
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // Tools Section
            item {
                SectionHeader(text = "Tools")
            }

            item {
                FeatureToggleItem(
                    title = "SharedPreferences",
                    description = "Inspect and edit SharedPreferences files",
                    checked = config.showPreferences,
                    onCheckedChange = { viewModel.togglePreferences() },
                )
            }

            item {
                FeatureToggleItem(
                    title = "Console Logs",
                    description = "View app console logs",
                    checked = config.showConsoleLogs,
                    onCheckedChange = { viewModel.toggleConsoleLogs() },
                )
            }

            item {
                FeatureToggleItem(
                    title = "Device Info",
                    description = "View device information",
                    checked = config.showDeviceInfo,
                    onCheckedChange = { viewModel.toggleDeviceInfo() },
                )
            }

            item {
                FeatureToggleItem(
                    title = "SQLite Browser",
                    description = "Browse and query SQLite databases",
                    checked = config.showSqliteBrowser,
                    onCheckedChange = { viewModel.toggleSqliteBrowser() },
                )
            }

            item {
                FeatureToggleItem(
                    title = "File Browser",
                    description = "Browse app internal files",
                    checked = config.showFileBrowser,
                    onCheckedChange = { viewModel.toggleFileBrowser() },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
