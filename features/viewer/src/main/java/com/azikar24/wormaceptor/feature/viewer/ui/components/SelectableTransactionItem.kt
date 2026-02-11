package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorMethodBadge
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.util.getStatusColor

/**
 * A transaction list item that supports selection mode.
 * - Normal mode: tap to view details, long-press to show context menu
 * - Selection mode: tap to toggle selection, shows checkbox
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableTransactionItem(
    transaction: TransactionSummary,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCopyUrl: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onCopyAsCurl: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hapticFeedback = LocalHapticFeedback.current
    var showContextMenu by remember { mutableStateOf(false) }

    val statusColor = getStatusColor(transaction.status, transaction.code)

    // Scale animation for press feedback
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isSelected && isSelectionMode) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "itemScale",
    )

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                    vertical = WormaCeptorDesignSystem.Spacing.xs,
                )
                .scale(scale)
                .clip(WormaCeptorDesignSystem.Shapes.card)
                .border(
                    width = if (isSelected) WormaCeptorDesignSystem.BorderWidth.thick else WormaCeptorDesignSystem.BorderWidth.regular,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium)
                    },
                    shape = WormaCeptorDesignSystem.Shapes.card,
                )
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate)
                    } else {
                        statusColor.asSubtleBackground()
                    },
                    shape = WormaCeptorDesignSystem.Shapes.card,
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = {
                        if (isSelectionMode) {
                            onClick()
                        } else {
                            onClick()
                        }
                    },
                    onLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!isSelectionMode) {
                            showContextMenu = true
                        }
                        onLongClick()
                    },
                )
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Transaction content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    WormaCeptorMethodBadge(transaction.method)
                    TextWithStartEllipsis(
                        text = transaction.path,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f, fill = false),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))

                HostChip(transaction.host)
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            // Status code and duration
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = statusColor.asSubtleBackground(),
                    contentColor = statusColor,
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs),
                ) {
                    Text(
                        text = transaction.code?.toString() ?: "?",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.sm,
                            vertical = WormaCeptorDesignSystem.Spacing.xxs,
                        ),
                    )
                }
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                Text(
                    text = formatDuration(transaction.tookMs),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorDesignSystem.Alpha.heavy,
                    ),
                )
            }
        }

        // Context menu
        TransactionContextMenu(
            transaction = transaction,
            expanded = showContextMenu,
            onDismiss = { showContextMenu = false },
            onCopyUrl = onCopyUrl,
            onShare = onShare,
            onDelete = onDelete,
            onCopyAsCurl = onCopyAsCurl,
            offset = DpOffset(
                x = WormaCeptorDesignSystem.Spacing.lg,
                y = (-WormaCeptorDesignSystem.Spacing.sm),
            ),
        )
    }
}

@Composable
private fun HostChip(host: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.prominent),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill),
    ) {
        Text(
            text = host,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xxs,
            ),
        )
    }
}
