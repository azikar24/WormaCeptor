/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.pushsimulator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.feature.pushsimulator.ui.theme.PushSimulatorDesignSystem
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorEvent
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorUiState
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewModel
import kotlinx.coroutines.launch

/**
 * Main screen for the Push Notification Simulator.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushSimulatorScreen(
    viewModel: PushSimulatorViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val channels by viewModel.channels.collectAsState()
    val event by viewModel.events.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showSaveDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            viewModel.sendNotification()
        } else {
            scope.launch {
                snackbarHostState.showSnackbar("Notification permission denied")
            }
        }
    }

    // Handle events
    LaunchedEffect(event) {
        when (event) {
            is PushSimulatorEvent.NotificationSent -> {
                snackbarHostState.showSnackbar("Notification sent")
                viewModel.clearEvent()
            }
            is PushSimulatorEvent.TemplateSaved -> {
                snackbarHostState.showSnackbar("Template saved")
                viewModel.clearEvent()
            }
            is PushSimulatorEvent.TemplateDeleted -> {
                snackbarHostState.showSnackbar("Template deleted")
                viewModel.clearEvent()
            }
            is PushSimulatorEvent.TemplateLoaded -> {
                snackbarHostState.showSnackbar("Loaded: ${(event as PushSimulatorEvent.TemplateLoaded).name}")
                viewModel.clearEvent()
            }
            is PushSimulatorEvent.PermissionRequired -> {
                showPermissionDialog = true
                viewModel.clearEvent()
            }
            is PushSimulatorEvent.Error -> {
                snackbarHostState.showSnackbar((event as PushSimulatorEvent.Error).message)
                viewModel.clearEvent()
            }
            null -> {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Push Simulator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearForm() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear form",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(PushSimulatorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(PushSimulatorDesignSystem.Spacing.lg),
        ) {
            // Notification Form
            item {
                NotificationFormCard(
                    uiState = uiState,
                    channels = channels,
                    onTitleChange = viewModel::updateTitle,
                    onBodyChange = viewModel::updateBody,
                    onChannelChange = viewModel::updateChannelId,
                    onPriorityChange = viewModel::updatePriority,
                    onNewActionTitleChange = viewModel::updateNewActionTitle,
                    onAddAction = viewModel::addAction,
                    onRemoveAction = viewModel::removeAction,
                )
            }

            // Action Buttons
            item {
                ActionButtonsRow(
                    onSendClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val permission = Manifest.permission.POST_NOTIFICATIONS
                            when {
                                ContextCompat.checkSelfPermission(context, permission) ==
                                    PackageManager.PERMISSION_GRANTED -> {
                                    viewModel.sendNotification()
                                }
                                else -> {
                                    permissionLauncher.launch(permission)
                                }
                            }
                        } else {
                            viewModel.sendNotification()
                        }
                    },
                    onSaveClick = { showSaveDialog = true },
                )
            }

            // Templates Section
            item {
                SectionHeader(text = "Templates")
            }

            if (templates.isEmpty()) {
                item {
                    EmptyTemplatesCard()
                }
            } else {
                items(templates, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onLoad = { viewModel.loadTemplate(template) },
                        onSend = { viewModel.sendFromTemplate(template) },
                        onDelete = { viewModel.deleteTemplate(template.id) },
                    )
                }
            }
        }
    }

    // Save Template Dialog
    if (showSaveDialog) {
        SaveTemplateDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name ->
                viewModel.saveAsTemplate(name)
                showSaveDialog = false
            },
        )
    }

    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required") },
            text = {
                Text("Notification permission is required to send test notifications. Please grant the permission in app settings.")
            },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("OK")
                }
            },
        )
    }
}

@Composable
private fun NotificationFormCard(
    uiState: PushSimulatorUiState,
    channels: List<NotificationChannelInfo>,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onChannelChange: (String) -> Unit,
    onPriorityChange: (NotificationPriority) -> Unit,
    onNewActionTitleChange: (String) -> Unit,
    onAddAction: (String) -> Unit,
    onRemoveAction: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = PushSimulatorDesignSystem.Shapes.card,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PushSimulatorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(PushSimulatorDesignSystem.Spacing.md),
        ) {
            // Preview Header
            NotificationPreview(
                title = uiState.title.ifBlank { "Notification Title" },
                body = uiState.body.ifBlank { "Notification message will appear here" },
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = PushSimulatorDesignSystem.Spacing.sm))

            // Title Input
            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = { Text("Title") },
                placeholder = { Text("Enter notification title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = PushSimulatorDesignSystem.Shapes.textField,
            )

            // Body Input
            OutlinedTextField(
                value = uiState.body,
                onValueChange = onBodyChange,
                label = { Text("Message") },
                placeholder = { Text("Enter notification message") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = PushSimulatorDesignSystem.Shapes.textField,
            )

            // Channel Selector
            ChannelSelector(
                selectedChannelId = uiState.selectedChannelId,
                channels = channels,
                onChannelSelected = onChannelChange,
            )

            // Priority Selector
            PrioritySelector(
                selectedPriority = uiState.priority,
                onPrioritySelected = onPriorityChange,
            )

            // Action Buttons Section
            ActionButtonsSection(
                actions = uiState.actions,
                newActionTitle = uiState.newActionTitle,
                onNewActionTitleChange = onNewActionTitleChange,
                onAddAction = onAddAction,
                onRemoveAction = onRemoveAction,
            )
        }
    }
}

@Composable
private fun NotificationPreview(
    title: String,
    body: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = PushSimulatorDesignSystem.Shapes.card,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(modifier = Modifier.width(PushSimulatorDesignSystem.Spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ChannelSelector(
    selectedChannelId: String,
    channels: List<NotificationChannelInfo>,
    onChannelSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedChannel = channels.find { it.id == selectedChannelId }

    Column {
        Text(
            text = "Channel",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(PushSimulatorDesignSystem.Spacing.xs))

        Box {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                shape = PushSimulatorDesignSystem.Shapes.textField,
            ) {
                Text(
                    text = selectedChannel?.name ?: "Select channel",
                    modifier = Modifier.padding(PushSimulatorDesignSystem.Spacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                channels.forEach { channel ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                channel.description?.let { desc ->
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        },
                        onClick = {
                            onChannelSelected(channel.id)
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrioritySelector(
    selectedPriority: NotificationPriority,
    onPrioritySelected: (NotificationPriority) -> Unit,
) {
    Column {
        Text(
            text = "Priority",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(PushSimulatorDesignSystem.Spacing.xs))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(PushSimulatorDesignSystem.Spacing.sm),
        ) {
            NotificationPriority.entries.forEach { priority ->
                FilterChip(
                    selected = selectedPriority == priority,
                    onClick = { onPrioritySelected(priority) },
                    label = { Text(priority.name) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionButtonsSection(
    actions: List<com.azikar24.wormaceptor.domain.entities.NotificationAction>,
    newActionTitle: String,
    onNewActionTitleChange: (String) -> Unit,
    onAddAction: (String) -> Unit,
    onRemoveAction: (String) -> Unit,
) {
    Column {
        Text(
            text = "Action Buttons (up to 3)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(PushSimulatorDesignSystem.Spacing.xs))

        // Current actions
        if (actions.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(PushSimulatorDesignSystem.Spacing.sm),
                modifier = Modifier.padding(bottom = PushSimulatorDesignSystem.Spacing.sm),
            ) {
                actions.forEach { action ->
                    InputChip(
                        selected = false,
                        onClick = { },
                        label = { Text(action.title) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove action",
                                modifier = Modifier
                                    .size(InputChipDefaults.IconSize)
                                    .clickable { onRemoveAction(action.actionId) },
                            )
                        },
                    )
                }
            }
        }

        // Add new action
        if (actions.size < 3) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PushSimulatorDesignSystem.Spacing.sm),
            ) {
                OutlinedTextField(
                    value = newActionTitle,
                    onValueChange = onNewActionTitleChange,
                    placeholder = { Text("Action title") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = PushSimulatorDesignSystem.Shapes.textField,
                )

                IconButton(
                    onClick = {
                        if (newActionTitle.isNotBlank()) {
                            onAddAction(newActionTitle)
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add action",
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onSendClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PushSimulatorDesignSystem.Spacing.md),
    ) {
        OutlinedButton(
            onClick = onSaveClick,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(PushSimulatorDesignSystem.Spacing.sm))
            Text("Save Template")
        }

        Button(
            onClick = onSendClick,
            modifier = Modifier.weight(1f),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(PushSimulatorDesignSystem.Spacing.sm))
            Text("Send")
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = PushSimulatorDesignSystem.Spacing.sm),
    )
}

@Composable
private fun EmptyTemplatesCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PushSimulatorDesignSystem.Shapes.card,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PushSimulatorDesignSystem.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(PushSimulatorDesignSystem.Spacing.md))
            Text(
                text = "No templates saved",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "Save your notification configuration as a template for quick reuse",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun TemplateCard(
    template: NotificationTemplate,
    onLoad: () -> Unit,
    onSend: () -> Unit,
    onDelete: () -> Unit,
) {
    val isPreset = template.id.startsWith("preset_")

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = PushSimulatorDesignSystem.Shapes.card,
        border = BorderStroke(
            width = 1.dp,
            color = if (isPreset) {
                PushSimulatorDesignSystem.TemplateColors.preset.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PushSimulatorDesignSystem.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = template.notification.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                if (isPreset) {
                    Surface(
                        shape = PushSimulatorDesignSystem.Shapes.chip,
                        color = PushSimulatorDesignSystem.TemplateColors.preset.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = "Preset",
                            style = MaterialTheme.typography.labelSmall,
                            color = PushSimulatorDesignSystem.TemplateColors.preset,
                            modifier = Modifier.padding(
                                horizontal = PushSimulatorDesignSystem.Spacing.sm,
                                vertical = PushSimulatorDesignSystem.Spacing.xs,
                            ),
                        )
                    }
                }
            }

            if (template.notification.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(PushSimulatorDesignSystem.Spacing.xs))
                Text(
                    text = template.notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(PushSimulatorDesignSystem.Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (!isPreset) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }

                TextButton(onClick = onLoad) {
                    Text("Load")
                }

                Button(
                    onClick = onSend,
                    contentPadding = PaddingValues(
                        horizontal = PushSimulatorDesignSystem.Spacing.md,
                        vertical = PushSimulatorDesignSystem.Spacing.sm,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(PushSimulatorDesignSystem.Spacing.xs))
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun SaveTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var templateName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save as Template") },
        text = {
            OutlinedTextField(
                value = templateName,
                onValueChange = { templateName = it },
                label = { Text("Template name") },
                placeholder = { Text("My notification template") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                onClick = { onSave(templateName) },
                enabled = templateName.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
