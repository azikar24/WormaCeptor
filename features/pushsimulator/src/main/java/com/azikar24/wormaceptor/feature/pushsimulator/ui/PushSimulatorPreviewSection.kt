package com.azikar24.wormaceptor.feature.pushsimulator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.domain.entities.NotificationPriority
import com.azikar24.wormaceptor.feature.pushsimulator.R
import com.azikar24.wormaceptor.feature.pushsimulator.ui.theme.PushSimulatorDesignSystem

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NotificationPreview(
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
                    text = stringResource(R.string.pushsimulator_preview),
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
                                contentDescription = stringResource(R.string.pushsimulator_preview),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.lg),
                            )
                        }
                        // Priority dot indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(WormaCeptorDesignSystem.Spacing.md)
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
                                text = stringResource(R.string.pushsimulator_preview_time_now),
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
                        WormaCeptorDivider(style = DividerStyle.Section)
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
                                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.xs),
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
internal fun PriorityIndicator(priority: NotificationPriority, color: Color) {
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
