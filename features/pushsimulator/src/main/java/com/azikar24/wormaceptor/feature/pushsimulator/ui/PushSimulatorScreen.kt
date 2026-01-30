/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.pushsimulator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
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
fun PushSimulatorScreen(viewModel: PushSimulatorViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
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
            contentPadding = PaddingValues(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
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
                    isTitleEmpty = uiState.title.isBlank(),
                )
            }

            // Templates Section
            item {
                SectionHeader(text = "Templates", count = templates.size)
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
                Text(
                    text = "Notification permission is required to send test notifications. " +
                        "Please grant the permission in app settings.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(onClick = { showPermissionDialog = false }) {
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
    val selectedChannel = channels.find { it.id == uiState.selectedChannelId }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = WormaCeptorDesignSystem.Shapes.card,
        border = BorderStroke(
            width = WormaCeptorDesignSystem.BorderWidth.regular,
            color = MaterialTheme.colorScheme.outlineVariant
                .copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            // Enhanced Preview Header
            NotificationPreview(
                title = uiState.title.ifBlank { "Notification Title" },
                body = uiState.body.ifBlank { "Notification message will appear here" },
                priority = uiState.priority,
                channelName = selectedChannel?.name,
                actions = uiState.actions,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
                color = MaterialTheme.colorScheme.outlineVariant
                    .copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
            )

            // Title Input with character count
            OutlinedTextFieldWithCounter(
                value = uiState.title,
                onValueChange = onTitleChange,
                label = "Title",
                placeholder = "Enter notification title",
                singleLine = true,
                maxChars = TitleMaxChars,
            )

            // Body Input with character count
            OutlinedTextFieldWithCounter(
                value = uiState.body,
                onValueChange = onBodyChange,
                label = "Message",
                placeholder = "Enter notification message",
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                maxChars = BodyMaxChars,
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

private const val TitleMaxChars = 50
private const val BodyMaxChars = 200

// NotificationManager importance levels
private const val ImportanceUrgent = 4
private const val ImportanceHigh = 3
private const val ImportanceDefault = 2
private const val ImportanceLow = 1

@Composable
private fun OutlinedTextFieldWithCounter(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    singleLine: Boolean,
    maxChars: Int,
    minLines: Int = 1,
    maxLines: Int = 1,
) {
    val charCount = value.length
    val isOverLimit = charCount > maxChars
    val charCountColor by animateColorAsState(
        targetValue = when {
            isOverLimit -> MaterialTheme.colorScheme.error
            charCount > maxChars * 0.8f -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 150),
        label = "charCountColor",
    )

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            maxLines = if (singleLine) 1 else maxLines,
            modifier = Modifier.fillMaxWidth(),
            shape = WormaCeptorDesignSystem.Shapes.button,
            isError = isOverLimit,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "$charCount / $maxChars",
                        style = MaterialTheme.typography.labelSmall,
                        color = charCountColor,
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isOverLimit) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            ),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NotificationPreview(
    title: String,
    body: String,
    priority: NotificationPriority,
    channelName: String?,
    actions: List<com.azikar24.wormaceptor.domain.entities.NotificationAction>,
) {
    val priorityColor = PushSimulatorDesignSystem.PriorityColors.forPriority(priority.name)
    val isHighPriority = priority == NotificationPriority.HIGH || priority == NotificationPriority.MAX

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        // Preview Label with Priority Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                channelName?.let { name ->
                    Surface(
                        shape = WormaCeptorDesignSystem.Shapes.chip,
                        color = MaterialTheme.colorScheme.primary.asSubtleBackground(),
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                vertical = WormaCeptorDesignSystem.Spacing.xxs,
                            ),
                        )
                    }
                }
            }
            PriorityIndicator(priority = priority, color = priorityColor)
        }

        // Notification Card Preview
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = WormaCeptorDesignSystem.Shapes.card,
            color = MaterialTheme.colorScheme.surfaceVariant
                .copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
            border = BorderStroke(
                width = WormaCeptorDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant
                    .copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
            ),
        ) {
            Column(
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    // App Icon with Priority Indicator
                    Box {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = WormaCeptorDesignSystem.Shapes.card,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (isHighPriority) {
                                    Icons.Default.NotificationsActive
                                } else {
                                    Icons.Default.Notifications
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        // Priority dot indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(12.dp)
                                .background(
                                    color = priorityColor,
                                    shape = CircleShape,
                                )
                                .padding(1.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

                    Column(modifier = Modifier.weight(1f)) {
                        // Title with time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "now",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))

                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                // Action Buttons Preview
                AnimatedVisibility(
                    visible = actions.isNotEmpty(),
                    enter = fadeIn() + scaleIn(initialScale = 0.95f),
                    exit = fadeOut() + scaleOut(targetScale = 0.95f),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                        ) {
                            actions.forEach { action ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        WormaCeptorDesignSystem.Spacing.xs,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = PushSimulatorDesignSystem.TemplateColors.action,
                                    )
                                    Text(
                                        text = action.title.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = PushSimulatorDesignSystem.TemplateColors.action,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityIndicator(priority: NotificationPriority, color: Color) {
    Surface(
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = color.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(color = color, shape = CircleShape),
            )
            Text(
                text = priority.name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = color,
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
    val dropdownRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "dropdownRotation",
    )

    Column {
        Text(
            text = "Channel",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        Box {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true }
                    .semantics {
                        role = Role.DropdownList
                        contentDescription = "Select notification channel"
                    },
                shape = WormaCeptorDesignSystem.Shapes.button,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (expanded) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(WormaCeptorDesignSystem.Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = selectedChannel?.name ?: "Select channel",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedChannel != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        selectedChannel?.let { channel ->
                            ImportanceBadge(importance = channel.importance)
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.rotate(dropdownRotation),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                channels.forEach { channel ->
                    val isSelected = channel.id == selectedChannelId
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(
                                            WormaCeptorDesignSystem.Spacing.sm,
                                        ),
                                    ) {
                                        Text(
                                            text = channel.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) {
                                                FontWeight.SemiBold
                                            } else {
                                                FontWeight.Normal
                                            },
                                        )
                                        ImportanceBadge(importance = channel.importance)
                                    }
                                    channel.description?.let { desc ->
                                        Text(
                                            text = desc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Circle,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(8.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        },
                        onClick = {
                            onChannelSelected(channel.id)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            } else {
                                Color.Transparent
                            },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
private fun ImportanceBadge(importance: Int) {
    val (label, color) = when (importance) {
        ImportanceUrgent -> "Urgent" to PushSimulatorDesignSystem.PriorityColors.max
        ImportanceHigh -> "High" to PushSimulatorDesignSystem.PriorityColors.high
        ImportanceDefault -> "Default" to PushSimulatorDesignSystem.PriorityColors.default
        ImportanceLow -> "Low" to PushSimulatorDesignSystem.PriorityColors.low
        else -> "Min" to MaterialTheme.colorScheme.outline
    }

    Surface(
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = color.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.xs,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
        )
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

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            NotificationPriority.entries.forEach { priority ->
                val priorityColor = PushSimulatorDesignSystem.PriorityColors.forPriority(priority.name)
                val isSelected = selectedPriority == priority

                FilterChip(
                    selected = isSelected,
                    onClick = { onPrioritySelected(priority) },
                    label = {
                        Text(
                            text = priority.name,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = priorityColor,
                                    shape = CircleShape,
                                ),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = priorityColor
                            .copy(alpha = WormaCeptorDesignSystem.Alpha.light),
                        selectedLabelColor = priorityColor,
                        selectedLeadingIconColor = priorityColor,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline
                            .copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
                        selectedBorderColor = priorityColor
                            .copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
                    ),
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
    val remainingSlots = 3 - actions.size

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Action Buttons",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${actions.size}/3",
                style = MaterialTheme.typography.labelSmall,
                color = if (remainingSlots == 0) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

        // Current actions with animation
        AnimatedVisibility(
            visible = actions.isNotEmpty(),
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.sm),
            ) {
                actions.forEach { action ->
                    InputChip(
                        selected = true,
                        onClick = { },
                        label = {
                            Text(
                                text = action.title,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove ${action.title}",
                                modifier = Modifier
                                    .size(InputChipDefaults.IconSize)
                                    .clip(CircleShape)
                                    .clickable { onRemoveAction(action.actionId) }
                                    .padding(2.dp),
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = PushSimulatorDesignSystem.TemplateColors.action
                                .copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
                            selectedLabelColor = PushSimulatorDesignSystem.TemplateColors.action,
                            selectedLeadingIconColor = PushSimulatorDesignSystem.TemplateColors.action,
                            selectedTrailingIconColor = PushSimulatorDesignSystem.TemplateColors.action
                                .copy(alpha = WormaCeptorDesignSystem.Alpha.strong),
                        ),
                        border = InputChipDefaults.inputChipBorder(
                            enabled = true,
                            selected = true,
                            borderColor = Color.Transparent,
                            selectedBorderColor = PushSimulatorDesignSystem.TemplateColors.action
                                .copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
                        ),
                    )
                }
            }
        }

        // Add new action
        AnimatedVisibility(
            visible = remainingSlots > 0,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                OutlinedTextField(
                    value = newActionTitle,
                    onValueChange = onNewActionTitleChange,
                    placeholder = {
                        Text(
                            text = if (actions.isEmpty()) {
                                "e.g., Open, Reply, Dismiss"
                            } else {
                                "Add another action"
                            },
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = WormaCeptorDesignSystem.Shapes.button,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )

                val canAdd by remember(newActionTitle) {
                    derivedStateOf { newActionTitle.isNotBlank() }
                }

                Surface(
                    onClick = {
                        if (canAdd) {
                            onAddAction(newActionTitle)
                        }
                    },
                    enabled = canAdd,
                    shape = CircleShape,
                    color = if (canAdd) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add action",
                            tint = if (canAdd) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(onSendClick: () -> Unit, onSaveClick: () -> Unit, isTitleEmpty: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        OutlinedButton(
            onClick = onSaveClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            contentPadding = PaddingValues(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.sm,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Text("Save Template")
        }

        Button(
            onClick = onSendClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            contentPadding = PaddingValues(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.sm,
            ),
            enabled = !isTitleEmpty,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Text("Send")
        }
    }
}

@Composable
private fun SectionHeader(text: String, count: Int = 0) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
    ) {
        Icon(
            imageVector = Icons.Default.Save,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        if (count > 0) {
            Surface(
                shape = WormaCeptorDesignSystem.Shapes.chip,
                color = MaterialTheme.colorScheme.primary.asSubtleBackground(),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs,
                    ),
                )
            }
        }
    }
}

@Composable
private fun EmptyTemplatesCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = WormaCeptorDesignSystem.Shapes.card,
        color = MaterialTheme.colorScheme.surfaceVariant
            .copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
        border = BorderStroke(
            width = WormaCeptorDesignSystem.BorderWidth.regular,
            color = MaterialTheme.colorScheme.outlineVariant
                .copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant
                            .copy(alpha = WormaCeptorDesignSystem.Alpha.strong),
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                        .copy(alpha = WormaCeptorDesignSystem.Alpha.strong),
                )
            }
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))
            Text(
                text = "No templates saved",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
            Text(
                text = "Save your notification configuration as a template for quick reuse",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TemplateCard(
    template: NotificationTemplate,
    onLoad: () -> Unit,
    onSend: () -> Unit,
    onDelete: () -> Unit,
) {
    val isPreset = template.id.startsWith("preset_")
    val priorityColor = PushSimulatorDesignSystem.PriorityColors
        .forPriority(template.notification.priority.name)
    val actionCount = template.notification.actions.size

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = WormaCeptorDesignSystem.Shapes.card,
        border = BorderStroke(
            width = WormaCeptorDesignSystem.BorderWidth.regular,
            color = if (isPreset) {
                PushSimulatorDesignSystem.TemplateColors.preset
                    .copy(alpha = WormaCeptorDesignSystem.Alpha.medium)
            } else {
                MaterialTheme.colorScheme.outlineVariant
                    .copy(alpha = WormaCeptorDesignSystem.Alpha.medium)
            },
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
        ) {
            // Header with template name, badges, and metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))

                    Text(
                        text = template.notification.title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Badges row
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                ) {
                    // Priority badge
                    Surface(
                        shape = WormaCeptorDesignSystem.Shapes.chip,
                        color = priorityColor.copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                vertical = WormaCeptorDesignSystem.Spacing.xxs,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(
                                WormaCeptorDesignSystem.Spacing.xxs,
                            ),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(color = priorityColor, shape = CircleShape),
                            )
                            Text(
                                text = template.notification.priority.name.take(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityColor,
                            )
                        }
                    }

                    // Action count badge
                    if (actionCount > 0) {
                        Surface(
                            shape = WormaCeptorDesignSystem.Shapes.chip,
                            color = PushSimulatorDesignSystem.TemplateColors.action
                                .copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
                        ) {
                            Row(
                                modifier = Modifier.padding(
                                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                    vertical = WormaCeptorDesignSystem.Spacing.xxs,
                                ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(
                                    WormaCeptorDesignSystem.Spacing.xxs,
                                ),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = PushSimulatorDesignSystem.TemplateColors.action,
                                )
                                Text(
                                    text = actionCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PushSimulatorDesignSystem.TemplateColors.action,
                                )
                            }
                        }
                    }

                    // Preset badge
                    if (isPreset) {
                        Surface(
                            shape = WormaCeptorDesignSystem.Shapes.chip,
                            color = PushSimulatorDesignSystem.TemplateColors.preset
                                .copy(alpha = WormaCeptorDesignSystem.Alpha.subtle),
                        ) {
                            Text(
                                text = "Preset",
                                style = MaterialTheme.typography.labelSmall,
                                color = PushSimulatorDesignSystem.TemplateColors.preset,
                                modifier = Modifier.padding(
                                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                                    vertical = WormaCeptorDesignSystem.Spacing.xxs,
                                ),
                            )
                        }
                    }
                }
            }

            // Body preview
            if (template.notification.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                Text(
                    text = template.notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    WormaCeptorDesignSystem.Spacing.sm,
                    Alignment.End,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isPreset) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete ${template.name}",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                OutlinedButton(
                    onClick = onLoad,
                    contentPadding = PaddingValues(
                        horizontal = WormaCeptorDesignSystem.Spacing.md,
                        vertical = WormaCeptorDesignSystem.Spacing.sm,
                    ),
                ) {
                    Text("Load")
                }

                Button(
                    onClick = onSend,
                    contentPadding = PaddingValues(
                        horizontal = WormaCeptorDesignSystem.Spacing.md,
                        vertical = WormaCeptorDesignSystem.Spacing.sm,
                    ),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text("Send")
                }
            }
        }
    }
}

@Composable
private fun SaveTemplateDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var templateName by remember { mutableStateOf("") }
    val isValid by remember(templateName) { derivedStateOf { templateName.isNotBlank() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save as Template") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                Text(
                    text = "Give your notification a name for quick access later.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text("Template name") },
                    placeholder = { Text("e.g., Welcome Message") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = WormaCeptorDesignSystem.Shapes.textField,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(templateName) },
                enabled = isValid,
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
