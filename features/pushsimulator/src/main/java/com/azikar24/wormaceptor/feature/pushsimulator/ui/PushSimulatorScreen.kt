package com.azikar24.wormaceptor.feature.pushsimulator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import com.azikar24.wormaceptor.common.presentation.BaseScreen
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.feature.pushsimulator.R
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewEffect
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewEvent
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewModel
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewState
import kotlinx.coroutines.launch

/**
 * Main screen for the Push Notification Simulator.
 */
@Composable
fun PushSimulatorScreen(viewModel: PushSimulatorViewModel, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showSaveDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) {
            viewModel.sendEvent(PushSimulatorViewEvent.SendNotification)
        } else {
            val message = context.getString(R.string.pushsimulator_notification_permission_denied)
            scope.launch {
                snackBarHostState.showSnackbar(message)
            }
        }
    }

    // String resources for snackbar messages
    val notificationSentMessage = stringResource(R.string.pushsimulator_notification_sent)
    val templateSavedMessage = stringResource(R.string.pushsimulator_template_saved)
    val templateDeletedMessage = stringResource(R.string.pushsimulator_template_deleted)

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is PushSimulatorViewEffect.NotificationSent -> {
                    scope.launch { snackBarHostState.showSnackbar(notificationSentMessage) }
                }
                is PushSimulatorViewEffect.TemplateSaved -> {
                    scope.launch { snackBarHostState.showSnackbar(templateSavedMessage) }
                }
                is PushSimulatorViewEffect.TemplateDeleted -> {
                    scope.launch { snackBarHostState.showSnackbar(templateDeletedMessage) }
                }
                is PushSimulatorViewEffect.TemplateLoaded -> {
                    scope.launch {
                        snackBarHostState.showSnackbar(
                            context.getString(R.string.pushsimulator_template_loaded, effect.name),
                        )
                    }
                }
                is PushSimulatorViewEffect.PermissionRequired -> {
                    showPermissionDialog = true
                }
                is PushSimulatorViewEffect.Error -> {
                    scope.launch { snackBarHostState.showSnackbar(effect.message) }
                }
            }
        },
    ) { state, onEvent ->
        PushSimulatorScreenContent(
            state = state,
            snackBarHostState = snackBarHostState,
            showSaveDialog = showSaveDialog,
            showPermissionDialog = showPermissionDialog,
            onBack = onBack,
            onEvent = onEvent,
            onSendClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val permission = Manifest.permission.POST_NOTIFICATIONS
                    if (ContextCompat.checkSelfPermission(context, permission) ==
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        onEvent(PushSimulatorViewEvent.SendNotification)
                    } else {
                        permissionLauncher.launch(permission)
                    }
                } else {
                    onEvent(PushSimulatorViewEvent.SendNotification)
                }
            },
            onSaveClick = { showSaveDialog = true },
            onSaveTemplate = { name ->
                onEvent(PushSimulatorViewEvent.SaveAsTemplate(name))
                showSaveDialog = false
            },
            onDismissSaveDialog = { showSaveDialog = false },
            onDismissPermissionDialog = { showPermissionDialog = false },
            modifier = modifier,
        )
    }
}

@Suppress("LongParameterList", "LongMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PushSimulatorScreenContent(
    state: PushSimulatorViewState,
    snackBarHostState: SnackbarHostState,
    showSaveDialog: Boolean,
    showPermissionDialog: Boolean,
    onBack: () -> Unit,
    onEvent: (PushSimulatorViewEvent) -> Unit,
    onSendClick: () -> Unit,
    onSaveClick: () -> Unit,
    onSaveTemplate: (String) -> Unit,
    onDismissSaveDialog: () -> Unit,
    onDismissPermissionDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.pushsimulator_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.pushsimulator_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(PushSimulatorViewEvent.ClearForm) }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.pushsimulator_clear_form),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
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
                    state = state,
                    channels = state.channels,
                    previewTitlePlaceholder = stringResource(R.string.pushsimulator_preview_title_placeholder),
                    previewBodyPlaceholder = stringResource(R.string.pushsimulator_preview_body_placeholder),
                    onTitleChange = { onEvent(PushSimulatorViewEvent.UpdateTitle(it)) },
                    onBodyChange = { onEvent(PushSimulatorViewEvent.UpdateBody(it)) },
                    onChannelChange = { onEvent(PushSimulatorViewEvent.UpdateChannelId(it)) },
                    onPriorityChange = { onEvent(PushSimulatorViewEvent.UpdatePriority(it)) },
                    onNewActionTitleChange = { onEvent(PushSimulatorViewEvent.UpdateNewActionTitle(it)) },
                    onAddAction = { onEvent(PushSimulatorViewEvent.AddAction(it)) },
                    onRemoveAction = { onEvent(PushSimulatorViewEvent.RemoveAction(it)) },
                )
            }

            // Action Buttons
            item {
                ActionButtonsRow(
                    onSendClick = onSendClick,
                    onSaveClick = onSaveClick,
                    isTitleEmpty = state.title.isBlank(),
                )
            }

            // Templates Section
            item {
                SectionHeader(
                    text = stringResource(R.string.pushsimulator_templates_header),
                    count = state.templates.size,
                )
            }

            if (state.templates.isEmpty()) {
                item {
                    EmptyTemplatesCard()
                }
            } else {
                items(state.templates, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        onLoad = { onEvent(PushSimulatorViewEvent.LoadTemplate(template)) },
                        onSend = { onEvent(PushSimulatorViewEvent.SendFromTemplate(template)) },
                        onDelete = { onEvent(PushSimulatorViewEvent.DeleteTemplate(template.id)) },
                    )
                }
            }
        }
    }

    // Save Template Dialog
    if (showSaveDialog) {
        SaveTemplateDialog(
            onDismiss = onDismissSaveDialog,
            onSave = onSaveTemplate,
        )
    }

    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = onDismissPermissionDialog,
            title = { Text(stringResource(R.string.pushsimulator_permission_title)) },
            text = {
                Text(
                    text = stringResource(R.string.pushsimulator_permission_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                Button(onClick = onDismissPermissionDialog) {
                    Text(stringResource(R.string.pushsimulator_ok))
                }
            },
        )
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
                .height(WormaCeptorDesignSystem.Spacing.xxxl),
            contentPadding = PaddingValues(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.sm,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Text(stringResource(R.string.pushsimulator_save_template))
        }

        Button(
            onClick = onSendClick,
            modifier = Modifier
                .weight(1f)
                .height(WormaCeptorDesignSystem.Spacing.xxxl),
            contentPadding = PaddingValues(
                horizontal = WormaCeptorDesignSystem.Spacing.lg,
                vertical = WormaCeptorDesignSystem.Spacing.sm,
            ),
            enabled = !isTitleEmpty,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            )
            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
            Text(stringResource(R.string.pushsimulator_send))
        }
    }
}

@Composable
private fun SaveTemplateDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var templateName by remember { mutableStateOf("") }
    val isValid by remember(templateName) { derivedStateOf { templateName.isNotBlank() } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pushsimulator_dialog_save_title)) },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                Text(
                    text = stringResource(R.string.pushsimulator_dialog_save_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = templateName,
                    onValueChange = { templateName = it },
                    label = { Text(stringResource(R.string.pushsimulator_dialog_template_name)) },
                    placeholder = { Text(stringResource(R.string.pushsimulator_dialog_template_placeholder)) },
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
                Text(stringResource(R.string.pushsimulator_dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.pushsimulator_dialog_cancel))
            }
        },
    )
}

@Suppress("UnusedPrivateMember")
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun PushSimulatorScreenContentPreview() {
    WormaCeptorTheme {
        PushSimulatorScreenContent(
            state = PushSimulatorViewState(title = "Test Notification", body = "This is a test"),
            snackBarHostState = remember { SnackbarHostState() },
            showSaveDialog = false,
            showPermissionDialog = false,
            onBack = {},
            onEvent = {},
            onSendClick = {},
            onSaveClick = {},
            onSaveTemplate = {},
            onDismissSaveDialog = {},
            onDismissPermissionDialog = {},
        )
    }
}
