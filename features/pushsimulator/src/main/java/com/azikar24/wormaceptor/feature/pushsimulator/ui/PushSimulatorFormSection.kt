package com.azikar24.wormaceptor.feature.pushsimulator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.badge.WormaCeptorStatusBadge
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.divider.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationChannelInfo
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.feature.pushsimulator.R
import com.azikar24.wormaceptor.feature.pushsimulator.vm.PushSimulatorViewState

@Composable
internal fun NotificationFormCard(
    state: PushSimulatorViewState,
    channels: List<NotificationChannelInfo>,
    previewTitlePlaceholder: String,
    previewBodyPlaceholder: String,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onChannelChange: (String) -> Unit,
    onPriorityChange: (NotificationPriority) -> Unit,
    onNewActionTitleChange: (String) -> Unit,
    onAddAction: (String) -> Unit,
    onRemoveAction: (String) -> Unit,
) {
    val selectedChannel = channels.find { it.id == state.selectedChannelId }

    WormaCeptorCard(
        modifier = Modifier.fillMaxWidth(),
        style = CardStyle.Outlined,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            NotificationPreview(
                title = state.title.ifBlank { previewTitlePlaceholder },
                body = state.body.ifBlank { previewBodyPlaceholder },
                priority = state.priority,
                channelName = selectedChannel?.name,
                actions = state.actions,
            )

            WormaCeptorDivider(
                modifier = Modifier.padding(vertical = WormaCeptorTokens.Spacing.sm),
                style = DividerStyle.Subtle,
            )

            OutlinedTextFieldWithCounter(
                value = state.title,
                onValueChange = onTitleChange,
                label = stringResource(R.string.pushsimulator_field_title),
                placeholder = stringResource(R.string.pushsimulator_field_title_placeholder),
                singleLine = true,
                maxChars = PushSimulatorConstants.TITLE_MAX_CHARS,
            )

            OutlinedTextFieldWithCounter(
                value = state.body,
                onValueChange = onBodyChange,
                label = stringResource(R.string.pushsimulator_field_message),
                placeholder = stringResource(R.string.pushsimulator_field_message_placeholder),
                singleLine = false,
                minLines = 2,
                maxLines = 4,
                maxChars = PushSimulatorConstants.BODY_MAX_CHARS,
            )

            ChannelSelector(
                selectedChannelId = state.selectedChannelId,
                channels = channels,
                onChannelSelected = onChannelChange,
            )

            PrioritySelector(
                selectedPriority = state.priority,
                onPrioritySelected = onPriorityChange,
            )

            ActionButtonsSection(
                actions = state.actions,
                newActionTitle = state.newActionTitle,
                onNewActionTitleChange = onNewActionTitleChange,
                onAddAction = onAddAction,
                onRemoveAction = onRemoveAction,
            )
        }
    }
}

@Composable
internal fun OutlinedTextFieldWithCounter(
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
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.FAST),
        label = "charCountColor",
    )

    Column {
        WormaCeptorTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            singleLine = singleLine,
            minLines = if (singleLine) 1 else minLines,
            maxLines = if (singleLine) 1 else maxLines,
            modifier = Modifier.fillMaxWidth(),
            isError = isOverLimit,
        )
        Text(
            text = "$charCount / $maxChars",
            style = MaterialTheme.typography.labelSmall,
            color = charCountColor,
            modifier = Modifier
                .align(Alignment.End)
                .padding(top = WormaCeptorTokens.Spacing.xxs),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChannelSelector(
    selectedChannelId: String,
    channels: List<NotificationChannelInfo>,
    onChannelSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedChannel = channels.find { it.id == selectedChannelId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedChannel?.name.orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.pushsimulator_channel_label)) },
            placeholder = { Text(stringResource(R.string.pushsimulator_channel_placeholder)) },
            textStyle = MaterialTheme.typography.bodyMedium,
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                    modifier = Modifier.padding(end = WormaCeptorTokens.Spacing.sm),
                ) {
                    selectedChannel?.let { ImportanceBadge(importance = it.importance) }
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            },
            shape = WormaCeptorTokens.Shapes.textField,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = WormaCeptorTokens.Alpha.BOLD,
                ),
                focusedBorderColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            channels.forEach { channel ->
                val isSelected = channel.id == selectedChannelId
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(
                                        R.string.pushsimulator_channel_selected,
                                    ),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                                )
                            } else {
                                Spacer(modifier = Modifier.size(WormaCeptorTokens.IconSize.sm))
                            }
                            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = channel.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) {
                                        FontWeight.SemiBold
                                    } else {
                                        FontWeight.Normal
                                    },
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    },
                                )
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
                            Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.sm))
                            ImportanceBadge(importance = channel.importance)
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

@Composable
private fun ImportanceBadge(importance: Int) {
    val urgentLabel = stringResource(R.string.pushsimulator_importance_urgent)
    val highLabel = stringResource(R.string.pushsimulator_importance_high)
    val defaultLabel = stringResource(R.string.pushsimulator_importance_default)
    val lowLabel = stringResource(R.string.pushsimulator_importance_low)
    val minLabel = stringResource(R.string.pushsimulator_importance_min)

    val (label, color) = when (importance) {
        PushSimulatorConstants.IMPORTANCE_URGENT -> urgentLabel to ToolColors.PushSimulator.Priority.max
        PushSimulatorConstants.IMPORTANCE_HIGH -> highLabel to ToolColors.PushSimulator.Priority.high
        PushSimulatorConstants.IMPORTANCE_DEFAULT -> defaultLabel to ToolColors.PushSimulator.Priority.default
        PushSimulatorConstants.IMPORTANCE_LOW -> lowLabel to ToolColors.PushSimulator.Priority.low
        else -> minLabel to MaterialTheme.colorScheme.outline
    }

    WormaCeptorStatusBadge(
        text = label,
        containerColor = color.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
        contentColor = color,
    )
}

@Composable
private fun PrioritySelector(
    selectedPriority: NotificationPriority,
    onPrioritySelected: (NotificationPriority) -> Unit,
) {
    Column {
        Text(
            text = stringResource(R.string.pushsimulator_priority_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

        WormaCeptorScrollableRow(
            contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
        ) {
            NotificationPriority.entries.forEach { priority ->
                val priorityColor = ToolColors.PushSimulator.Priority.forPriority(priority.name)
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
                                .size(WormaCeptorTokens.Spacing.sm)
                                .background(
                                    color = priorityColor,
                                    shape = CircleShape,
                                ),
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = priorityColor
                            .copy(alpha = WormaCeptorTokens.Alpha.LIGHT),
                        selectedLabelColor = priorityColor,
                        selectedLeadingIconColor = priorityColor,
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outline
                            .copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                        selectedBorderColor = priorityColor
                            .copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                    ),
                )
            }
        }
    }
}

@Composable
private fun ActionButtonsSection(
    actions: List<NotificationAction>,
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
                text = stringResource(R.string.pushsimulator_actions_label),
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

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

        AnimatedVisibility(
            visible = actions.isNotEmpty(),
            enter = fadeIn() + scaleIn(initialScale = 0.95f),
            exit = fadeOut() + scaleOut(targetScale = 0.95f),
        ) {
            WormaCeptorScrollableRow(
                modifier = Modifier.padding(bottom = WormaCeptorTokens.Spacing.sm),
                contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
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
                                modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(
                                    R.string.pushsimulator_actions_remove,
                                    action.title,
                                ),
                                modifier = Modifier
                                    .size(InputChipDefaults.IconSize)
                                    .clip(CircleShape)
                                    .clickable { onRemoveAction(action.actionId) }
                                    .padding(WormaCeptorTokens.Spacing.xxs),
                            )
                        },
                        colors = InputChipDefaults.inputChipColors(
                            selectedContainerColor = ToolColors.PushSimulator.Template.action
                                .copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
                            selectedLabelColor = ToolColors.PushSimulator.Template.action,
                            selectedLeadingIconColor = ToolColors.PushSimulator.Template.action,
                            selectedTrailingIconColor = ToolColors.PushSimulator.Template.action
                                .copy(alpha = WormaCeptorTokens.Alpha.STRONG),
                        ),
                        border = InputChipDefaults.inputChipBorder(
                            enabled = true,
                            selected = true,
                            borderColor = Color.Transparent,
                            selectedBorderColor = ToolColors.PushSimulator.Template.action
                                .copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
                        ),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = remainingSlots > 0,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                WormaCeptorTextField(
                    value = newActionTitle,
                    onValueChange = onNewActionTitleChange,
                    placeholder = {
                        Text(
                            text = if (actions.isEmpty()) {
                                stringResource(R.string.pushsimulator_actions_placeholder_empty)
                            } else {
                                stringResource(R.string.pushsimulator_actions_placeholder_add)
                            },
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
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
                    modifier = Modifier.size(WormaCeptorTokens.Spacing.xxxl),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.pushsimulator_actions_add),
                            tint = if (canAdd) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                    alpha = WormaCeptorTokens.Alpha.BOLD,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun NotificationFormCardPreview() {
    WormaCeptorTheme {
        NotificationFormCard(
            state = PushSimulatorViewState(
                title = "Test Notification",
                body = "This is the notification body text",
                priority = NotificationPriority.HIGH,
            ),
            channels = listOf(
                NotificationChannelInfo(
                    id = "general",
                    name = "General",
                    description = "General notifications",
                    importance = 3,
                ),
            ),
            previewTitlePlaceholder = "Title",
            previewBodyPlaceholder = "Body",
            onTitleChange = {},
            onBodyChange = {},
            onChannelChange = {},
            onPriorityChange = {},
            onNewActionTitleChange = {},
            onAddAction = {},
            onRemoveAction = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun OutlinedTextFieldWithCounterPreview() {
    WormaCeptorTheme {
        OutlinedTextFieldWithCounter(
            value = "Sample input text",
            onValueChange = {},
            label = "Title",
            placeholder = "Enter title",
            singleLine = true,
            maxChars = PushSimulatorConstants.TITLE_MAX_CHARS,
        )
    }
}
