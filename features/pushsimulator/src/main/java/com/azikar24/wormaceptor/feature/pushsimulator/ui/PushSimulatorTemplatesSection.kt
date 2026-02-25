package com.azikar24.wormaceptor.feature.pushsimulator.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorEmptyState
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.domain.entities.NotificationTemplate
import com.azikar24.wormaceptor.feature.pushsimulator.R
import com.azikar24.wormaceptor.feature.pushsimulator.ui.theme.PushSimulatorDesignSystem

@Composable
internal fun SectionHeader(
    text: String,
    count: Int = 0,
) {
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
            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
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
internal fun EmptyTemplatesCard() {
    WormaCeptorEmptyState(
        title = stringResource(R.string.pushsimulator_templates_empty_title),
        subtitle = stringResource(R.string.pushsimulator_templates_empty_description),
        icon = Icons.Default.Save,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TemplateCard(
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
                                text = stringResource(R.string.pushsimulator_template_preset),
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
                        modifier = Modifier.size(WormaCeptorDesignSystem.TouchTarget.minimum),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(
                                R.string.pushsimulator_template_delete,
                                template.name,
                            ),
                            tint = MaterialTheme.colorScheme.error.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
                            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
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
                    Text(stringResource(R.string.pushsimulator_template_load))
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
                        modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text(stringResource(R.string.pushsimulator_send))
                }
            }
        }
    }
}
