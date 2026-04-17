package com.azikar24.wormaceptor.feature.pushsimulator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.divider.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.theme.tokens.TokenAlpha
import com.azikar24.wormaceptor.core.ui.theme.tokens.ToolColors
import com.azikar24.wormaceptor.domain.entities.NotificationAction
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.feature.pushsimulator.R

@Composable
internal fun NotificationPreview(
    title: String,
    body: String,
    priority: NotificationPriority,
    channelName: String?,
    actions: List<NotificationAction>,
) {
    val priorityColor = ToolColors.PushSimulator.Priority.forPriority(priority.name)
    val isHighPriority = priority == NotificationPriority.HIGH || priority == NotificationPriority.MAX

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.pushsimulator_preview),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                channelName?.let { name ->
                    Surface(
                        shape = WormaCeptorTokens.Shapes.chip,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = TokenAlpha.SUBTLE),
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(
                                horizontal = WormaCeptorTokens.Spacing.sm,
                                vertical = WormaCeptorTokens.Spacing.xxs,
                            ),
                        )
                    }
                }
            }
            PriorityIndicator(priority = priority, color = priorityColor)
        }

        WormaCeptorCard(
            modifier = Modifier.fillMaxWidth(),
            style = CardStyle.Outlined,
        ) {
            Column(
                modifier = Modifier.padding(WormaCeptorTokens.Spacing.md),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                ) {
                    Box {
                        Box(
                            modifier = Modifier
                                .size(WormaCeptorTokens.TouchTarget.minimum)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    shape = WormaCeptorTokens.Shapes.card,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (isHighPriority) {
                                    Icons.Default.NotificationsActive
                                } else {
                                    Icons.Default.Notifications
                                },
                                contentDescription = stringResource(R.string.pushsimulator_preview),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(WormaCeptorTokens.IconSize.lg),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(WormaCeptorTokens.Spacing.md)
                                .background(
                                    color = priorityColor,
                                    shape = CircleShape,
                                )
                                .padding(WormaCeptorTokens.ComponentSize.dotInset),
                        )
                    }

                    Spacer(modifier = Modifier.width(WormaCeptorTokens.Spacing.md))

                    Column(modifier = Modifier.weight(1f)) {
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
                                text = stringResource(R.string.pushsimulator_preview_time_now),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xxs))

                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = actions.isNotEmpty(),
                    enter = fadeIn() + scaleIn(initialScale = 0.95f),
                    exit = fadeOut() + scaleOut(targetScale = 0.95f),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))
                        WormaCeptorDivider(style = DividerStyle.Section)
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
                        WormaCeptorScrollableRow(
                            contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.md),
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
                        ) {
                            actions.forEach { action ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        WormaCeptorTokens.Spacing.xs,
                                    ),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(WormaCeptorTokens.IconSize.xs),
                                        tint = ToolColors.PushSimulator.Template.action,
                                    )
                                    Text(
                                        text = action.title.uppercase(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = ToolColors.PushSimulator.Template.action,
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
private fun PriorityIndicator(
    priority: NotificationPriority,
    color: Color,
) {
    Surface(
        shape = WormaCeptorTokens.Shapes.chip,
        color = color.copy(alpha = WormaCeptorTokens.Alpha.SUBTLE),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorTokens.Spacing.sm,
                vertical = WormaCeptorTokens.Spacing.xxs,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(WormaCeptorTokens.ComponentSize.dot)
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

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun NotificationPreviewDefaultPreview() {
    WormaCeptorTheme {
        Surface {
            NotificationPreview(
                title = "New Message",
                body = "You have a new notification from the system",
                priority = NotificationPriority.DEFAULT,
                channelName = "General",
                actions = listOf(
                    NotificationAction(title = "Reply", actionId = "reply"),
                    NotificationAction(title = "Dismiss", actionId = "dismiss"),
                ),
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun NotificationPreviewHighPriorityPreview() {
    WormaCeptorTheme {
        Surface {
            NotificationPreview(
                title = "Urgent Alert",
                body = "Critical system alert requires your attention",
                priority = NotificationPriority.HIGH,
                channelName = "Alerts",
                actions = emptyList(),
            )
        }
    }
}
