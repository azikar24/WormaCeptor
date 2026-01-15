package com.azikar24.wormaceptor.feature.viewer.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.theme.asSubtleBackground

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
    onReplay: () -> Unit,
    onCopyAsCurl: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current
    var showContextMenu by remember { mutableStateOf(false) }

    val statusColor = when (transaction.status) {
        TransactionStatus.COMPLETED -> when {
            transaction.code == null -> WormaCeptorColors.StatusAmber
            transaction.code in 200..299 -> WormaCeptorColors.StatusGreen
            transaction.code in 300..399 -> WormaCeptorColors.StatusBlue
            transaction.code in 400..499 -> WormaCeptorColors.StatusAmber
            transaction.code in 500..599 -> WormaCeptorColors.StatusRed
            else -> WormaCeptorColors.StatusGrey
        }
        TransactionStatus.FAILED -> WormaCeptorColors.StatusRed
        TransactionStatus.ACTIVE -> WormaCeptorColors.StatusGrey
    }

    // Scale animation for press feedback
    val interactionSource = remember { MutableInteractionSource() }
    val scale by animateFloatAsState(
        targetValue = if (isSelected && isSelectionMode) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "itemScale"
    )

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorDesignSystem.Spacing.sm,
                    vertical = WormaCeptorDesignSystem.Spacing.xs
                )
                .scale(scale)
                .clip(WormaCeptorDesignSystem.Shapes.card)
                .border(
                    width = if (isSelected) WormaCeptorDesignSystem.BorderWidth.thick else WormaCeptorDesignSystem.BorderWidth.regular,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                    },
                    shape = WormaCeptorDesignSystem.Shapes.card
                )
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    } else {
                        statusColor.asSubtleBackground()
                    },
                    shape = WormaCeptorDesignSystem.Shapes.card
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
                    }
                )
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox (animated)
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row {
                    SelectionCheckbox(isSelected = isSelected)
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))
                }
            }

            // Status indicator bar
            Box(
                modifier = Modifier
                    .width(WormaCeptorDesignSystem.BorderWidth.thick)
                    .height(48.dp)
                    .background(
                        statusColor,
                        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs)
                    )
            )

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            // Transaction content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
                ) {
                    MethodBadge(transaction.method)
                    Text(
                        text = transaction.path,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false),
                        color = MaterialTheme.colorScheme.primary
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
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs)
                ) {
                    Text(
                        text = transaction.code?.toString() ?: "?",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(
                            horizontal = WormaCeptorDesignSystem.Spacing.sm,
                            vertical = WormaCeptorDesignSystem.Spacing.xxs
                        )
                    )
                }
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                Text(
                    text = "${transaction.tookMs ?: "?"}ms",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
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
            onReplay = onReplay,
            onCopyAsCurl = onCopyAsCurl,
            offset = DpOffset(
                x = WormaCeptorDesignSystem.Spacing.lg,
                y = (-WormaCeptorDesignSystem.Spacing.sm)
            )
        )
    }
}

@Composable
private fun SelectionCheckbox(
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val checkboxColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier.size(24.dp),
        shape = CircleShape,
        color = if (isSelected) checkboxColor else Color.Transparent,
        border = if (!isSelected) {
            androidx.compose.foundation.BorderStroke(
                width = WormaCeptorDesignSystem.BorderWidth.regular,
                color = checkboxColor
            )
        } else null
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(4.dp)
                    .size(16.dp)
            )
        }
    }
}

@Composable
private fun MethodBadge(method: String) {
    Surface(
        color = methodColor(method).copy(alpha = 0.15f),
        contentColor = methodColor(method),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs)
    ) {
        Text(
            text = method.uppercase(),
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.xs,
                vertical = WormaCeptorDesignSystem.Spacing.xxs
            )
        )
    }
}

@Composable
private fun HostChip(host: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.pill)
    ) {
        Text(
            text = host,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xxs
            )
        )
    }
}

private fun methodColor(method: String): Color = when (method.uppercase()) {
    "GET" -> WormaCeptorColors.StatusGreen
    "POST" -> WormaCeptorColors.StatusBlue
    "PUT" -> WormaCeptorColors.StatusAmber
    "DELETE" -> WormaCeptorColors.StatusRed
    "PATCH" -> Color(0xFF9C27B0)
    else -> WormaCeptorColors.StatusGrey
}
