/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.viewer.ui.components.quickactions

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.theme.asSubtleBackground
import kotlinx.coroutines.delay

/**
 * A transaction list item with selection support.
 *
 * Design features:
 * - Checkbox appears on left when in selection mode
 * - Long-press triggers haptic feedback and shows context menu
 * - Scale animation on press for tactile feedback
 * - Selected state shows filled background and checkmark
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SelectableTransactionItem(
    transaction: TransactionSummary,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    showContextMenu: Boolean = false,
    onDismissContextMenu: () -> Unit = {},
    onCopyUrl: () -> Unit = {},
    onShare: () -> Unit = {},
    onCopyAsCurl: () -> Unit = {},
    onReplay: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val hapticFeedback = LocalHapticFeedback.current
    val view = LocalView.current

    // Status color based on transaction state
    val statusColor = remember(transaction.status, transaction.code) {
        when (transaction.status) {
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
    }

    // Press state tracking for animations
    var isLongPressing by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Scale animation for press feedback
    val scale by animateFloatAsState(
        targetValue = when {
            isLongPressing -> 0.96f
            isPressed -> 0.98f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "itemScale"
    )

    // Selection background color animation
    val selectionBackgroundAlpha by animateFloatAsState(
        targetValue = if (isSelected) 0.15f else 0f,
        animationSpec = tween(200),
        label = "selectionBg"
    )

    // Border animation for selection
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
        },
        animationSpec = tween(200),
        label = "borderColor"
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
                    width = if (isSelected) WormaCeptorDesignSystem.BorderWidth.regular else WormaCeptorDesignSystem.BorderWidth.thin,
                    color = borderColor,
                    shape = WormaCeptorDesignSystem.Shapes.card
                )
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary.copy(alpha = selectionBackgroundAlpha)
                    } else {
                        statusColor.asSubtleBackground()
                    },
                    shape = WormaCeptorDesignSystem.Shapes.card
                )
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = {
                        isLongPressing = true
                        // Perform haptic feedback
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        onLongClick()
                        isLongPressing = false
                    }
                )
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection checkbox (animated visibility)
            AnimatedVisibility(
                visible = isSelectionMode,
                enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.8f, animationSpec = tween(150)),
                exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.8f, animationSpec = tween(100))
            ) {
                Row {
                    SelectionCheckbox(
                        isSelected = isSelected,
                        accentColor = MaterialTheme.colorScheme.primary
                    )
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

            // Main content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)
                ) {
                    MethodBadge(transaction.method)
                    Text(
                        text = truncateStart(transaction.path, 40),
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

            // Status code and timing
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

        // Context menu positioned near the item
        TransactionContextMenu(
            expanded = showContextMenu,
            onDismiss = onDismissContextMenu,
            onCopyUrl = onCopyUrl,
            onShare = onShare,
            onCopyAsCurl = onCopyAsCurl,
            onReplay = onReplay,
            onDelete = onDelete
        )
    }
}

/**
 * Custom selection checkbox with animation.
 *
 * Design: Circular checkbox that fills with accent color when selected.
 * Checkmark icon animates in when selected.
 */
@Composable
fun SelectionCheckbox(
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Int = 24
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "checkboxScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.Transparent,
        animationSpec = tween(150),
        label = "checkboxBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else accentColor.copy(alpha = 0.5f),
        animationSpec = tween(150),
        label = "checkboxBorder"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = WormaCeptorDesignSystem.BorderWidth.thick,
                color = borderColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(tween(100)) + scaleIn(initialScale = 0.5f, animationSpec = tween(150)),
            exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.5f, animationSpec = tween(100))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size((size * 0.6f).dp)
            )
        }
    }
}

/**
 * Square checkbox variant for different UI contexts.
 */
@Composable
fun SquareSelectionCheckbox(
    isSelected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Int = 22
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.92f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "squareCheckboxScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else Color.Transparent,
        animationSpec = tween(150),
        label = "squareCheckboxBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else accentColor.copy(alpha = 0.4f),
        animationSpec = tween(150),
        label = "squareCheckboxBorder"
    )

    Box(
        modifier = modifier
            .size(size.dp)
            .scale(scale)
            .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs))
            .background(backgroundColor)
            .border(
                width = WormaCeptorDesignSystem.BorderWidth.regular,
                color = borderColor,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs)
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn(tween(80)) + scaleIn(initialScale = 0.4f, animationSpec = tween(120)),
            exit = fadeOut(tween(80)) + scaleOut(targetScale = 0.4f, animationSpec = tween(80))
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size((size * 0.65f).dp)
            )
        }
    }
}

@Composable
private fun MethodBadge(method: String) {
    val color = methodColor(method)
    Surface(
        color = color.copy(alpha = 0.15f),
        contentColor = color,
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

private fun truncateStart(text: String, maxLength: Int): String {
    return if (text.length > maxLength) {
        "..." + text.takeLast(maxLength - 3)
    } else {
        text
    }
}
