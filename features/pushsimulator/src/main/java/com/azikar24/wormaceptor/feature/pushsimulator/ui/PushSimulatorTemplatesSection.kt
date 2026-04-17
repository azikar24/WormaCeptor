package com.azikar24.wormaceptor.feature.pushsimulator.ui

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.components.state.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.domain.entities.SimulatedNotification
import com.azikar24.wormaceptor.feature.pushsimulator.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun SectionHeader(
    text: String,
    count: Int = 0,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = WormaCeptorTokens.Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Icon(
            imageVector = Icons.Default.Save,
            contentDescription = null,
            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() },
        )
        if (count > 0) {
            Surface(
                shape = WormaCeptorTokens.Shapes.chip,
                color = MaterialTheme.colorScheme.primary.copy(alpha = TokenAlpha.SUBTLE),
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.sm,
                        vertical = WormaCeptorTokens.Spacing.xxs,
                    ),
                )
            }
        }
    }
}

@Composable
internal fun EmptyTemplatesCard() {
    WormaCeptorEmptyState(
        title = stringResource(R.string.pushsimulator_templates_empty_title),
        subtitle = stringResource(R.string.pushsimulator_templates_empty_description),
        icon = Icons.Default.Save,
    )
}

@Composable
internal fun TemplatesRow(
    templates: ImmutableList<NotificationTemplate>,
    onLoad: (NotificationTemplate) -> Unit,
    onSend: (NotificationTemplate) -> Unit,
    onDelete: (NotificationTemplate) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(
            text = stringResource(R.string.pushsimulator_templates_header),
            count = templates.size,
        )

        if (templates.isEmpty()) {
            EmptyTemplatesCard()
        } else {
            WormaCeptorScrollableRow(
                contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
            ) {
                templates.forEach { template ->
                    TemplateCard(
                        template = template,
                        onLoad = { onLoad(template) },
                        onSend = { onSend(template) },
                        onDelete = { onDelete(template) },
                    )
                }
            }
        }
    }
}

@Composable
internal fun TemplateCard(
    template: NotificationTemplate,
    onLoad: () -> Unit,
    onSend: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPreset = template.id.startsWith("preset_")
    val priorityColor = ToolColors.PushSimulator.Priority
        .forPriority(template.notification.priority.name)
    val actionCount = template.notification.actions.size

    WormaCeptorCard(
        modifier = modifier.width(WormaCeptorTokens.ComponentSize.templateCardWidth),
        onClick = onLoad,
        style = CardStyle.Outlined,
        borderColor = if (isPreset) {
            ToolColors.PushSimulator.Template.preset
                .copy(alpha = WormaCeptorTokens.Alpha.MEDIUM)
        } else {
            null
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = WormaCeptorTokens.Shapes.chip,
                    color = priorityColor.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorTokens.Spacing.sm,
                            vertical = WormaCeptorTokens.Spacing.xxs,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(
                            WormaCeptorTokens.Spacing.xxs,
                        ),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(WormaCeptorTokens.ComponentSize.dot)
                                .background(color = priorityColor, shape = CircleShape),
                        )
                        Text(
                            text = template.notification.priority.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                        )
                    }
                }

                if (actionCount > 0) {
                    Surface(
                        shape = WormaCeptorTokens.Shapes.chip,
                        color = ToolColors.PushSimulator.Template.action
                            .copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorTokens.Spacing.sm,
                                vertical = WormaCeptorTokens.Spacing.xxs,
                            ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(
                                WormaCeptorTokens.Spacing.xxs,
                            ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.TouchApp,
                                contentDescription = null,
                                modifier = Modifier.size(WormaCeptorTokens.IconSize.xxs),
                                tint = ToolColors.PushSimulator.Template.action,
                            )
                            Text(
                                text = actionCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = ToolColors.PushSimulator.Template.action,
                            )
                        }
                    }
                }

                if (isPreset) {
                    Surface(
                        shape = WormaCeptorTokens.Shapes.chip,
                        color = ToolColors.PushSimulator.Template.preset
                            .copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
                    ) {
                        Text(
                            text = stringResource(R.string.pushsimulator_template_preset),
                            style = MaterialTheme.typography.labelSmall,
                            color = ToolColors.PushSimulator.Template.preset,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorTokens.Spacing.sm,
                                vertical = WormaCeptorTokens.Spacing.xxs,
                            ),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

            Text(
                text = template.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = template.notification.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (template.notification.body.isNotBlank()) {
                Text(
                    text = template.notification.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isPreset) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(WormaCeptorTokens.TouchTarget.minimum),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(
                                R.string.pushsimulator_template_delete,
                                template.name,
                            ),
                            tint = MaterialTheme.colorScheme.error
                                .copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.md),
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    onClick = onSend,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(WormaCeptorTokens.TouchTarget.minimum),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.pushsimulator_send),
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
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
private fun SectionHeaderPreview() {
    WormaCeptorTheme {
        SectionHeader(text = "Saved Templates", count = 3)
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun TemplateCardPreview() {
    WormaCeptorTheme {
        TemplateCard(
            template = NotificationTemplate(
                id = "user_1",
                name = "Welcome Message",
                notification = SimulatedNotification(
                    id = "1",
                    title = "Welcome",
                    body = "Thanks for installing the app!",
                    channelId = "general",
                    priority = NotificationPriority.DEFAULT,
                    actions = listOf(
                        NotificationAction(title = "Open", actionId = "open"),
                    ),
                ),
            ),
            onLoad = {},
            onSend = {},
            onDelete = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun TemplateCardPresetPreview() {
    WormaCeptorTheme {
        TemplateCard(
            template = NotificationTemplate(
                id = "preset_1",
                name = "High Priority Alert",
                notification = SimulatedNotification(
                    id = "2",
                    title = "Alert",
                    body = "System alert with high priority",
                    channelId = "alerts",
                    priority = NotificationPriority.HIGH,
                ),
            ),
            onLoad = {},
            onSend = {},
            onDelete = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TemplatesRowPreview() {
    WormaCeptorTheme {
        Surface {
            TemplatesRow(
                templates = listOf(
                    NotificationTemplate(
                        id = "preset_1",
                        name = "High Priority Alert",
                        notification = SimulatedNotification(
                            id = "1",
                            title = "Alert",
                            body = "System alert with high priority",
                            channelId = "alerts",
                            priority = NotificationPriority.HIGH,
                        ),
                    ),
                    NotificationTemplate(
                        id = "user_1",
                        name = "Welcome Message",
                        notification = SimulatedNotification(
                            id = "2",
                            title = "Welcome",
                            body = "Thanks for installing the app!",
                            channelId = "general",
                            priority = NotificationPriority.DEFAULT,
                            actions = listOf(
                                NotificationAction(title = "Open", actionId = "open"),
                            ),
                        ),
                    ),
                ).toImmutableList(),
                onLoad = {},
                onSend = {},
                onDelete = {},
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true, widthDp = 420)
@Composable
private fun TemplatesRowEmptyPreview() {
    WormaCeptorTheme {
        Surface {
            TemplatesRow(
                templates = persistentListOf(),
                onLoad = {},
                onSend = {},
                onDelete = {},
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun EmptyTemplatesCardPreview() {
    WormaCeptorTheme {
        EmptyTemplatesCard()
    }
}
