package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.feature.viewer.ui.theme.asSubtleBackground

@Composable
fun TransactionListScreen(
    transactions: List<TransactionSummary>,
    onItemClick: (TransactionSummary) -> Unit,
    hasActiveFilters: Boolean = false,
    onClearFilters: () -> Unit = {},
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null
) {
    if (transactions.isEmpty()) {
        EmptyState(
            hasActiveFilters = hasActiveFilters,
            onClearFilters = onClearFilters,
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = WormaCeptorDesignSystem.Spacing.xs)
        ) {
            if (header != null) {
                item {
                    header()
                }
            }
            items(transactions, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onItemClick(transaction) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (hasActiveFilters) "No matches found" else "No transactions yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
        Text(
            text = if (hasActiveFilters)
                "Try adjusting your filters to see more results"
            else
                "Network requests will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        if (hasActiveFilters) {
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
            Button(
                onClick = onClearFilters,
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm)
            ) {
                Text(
                    text = "Clear Filters",
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorDesignSystem.Spacing.sm,
                        vertical = WormaCeptorDesignSystem.Spacing.xxs
                    )
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: TransactionSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    // Press interaction for scale animation
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "itemScale"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = WormaCeptorDesignSystem.Spacing.sm,
                vertical = WormaCeptorDesignSystem.Spacing.xs
            )
            .scale(scale)
            .clip(WormaCeptorDesignSystem.Shapes.card)
            .border(
                width = WormaCeptorDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                shape = WormaCeptorDesignSystem.Shapes.card
            )
            .background(
                color = statusColor.asSubtleBackground(),
                shape = WormaCeptorDesignSystem.Shapes.card
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(WormaCeptorDesignSystem.Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 2dp left border as status indicator
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
