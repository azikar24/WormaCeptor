package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.ui.util.formatDurationAvg
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt

@Composable
fun MetricsCard(transactions: ImmutableList<TransactionSummary>, modifier: Modifier = Modifier) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "expand_icon_rotation",
    )

    if (transactions.isEmpty()) return

    // Calculate metrics
    val totalRequests = transactions.size
    val avgDuration = transactions.mapNotNull { it.tookMs }.average().takeIf { !it.isNaN() } ?: 0.0
    val successCount = transactions.count { (it.code ?: 0) in 200..299 }
    val successRate = (successCount.toDouble() / totalRequests * 100).takeIf { !it.isNaN() } ?: 0.0

    // Response time distribution
    val fastCount = transactions.count { (it.tookMs ?: 0) < 100 }
    val mediumCount = transactions.count { (it.tookMs ?: 0) in 100..500 }
    val slowCount = transactions.count { (it.tookMs ?: 0) > 500 }

    // Status code breakdown
    val status2xx = transactions.count { (it.code ?: 0) in 200..299 }
    val status3xx = transactions.count { (it.code ?: 0) in 300..399 }
    val status4xx = transactions.count { (it.code ?: 0) in 400..499 }
    val status5xx = transactions.count { (it.code ?: 0) in 500..599 }

    val methodBreakdown = transactions.groupBy { it.method }
        .mapValues { it.value.size }
        .toList()
        .sortedByDescending { it.second }

    val surfaceColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = WormaCeptorDesignSystem.Spacing.sm, vertical = WormaCeptorDesignSystem.Spacing.md),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor,
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.medium),
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                ) { isExpanded = !isExpanded }
                .padding(WormaCeptorDesignSystem.Spacing.xl),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.viewer_metrics_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { heading() },
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) {
                        stringResource(
                            R.string.viewer_body_collapse,
                        )
                    } else {
                        stringResource(R.string.viewer_body_expand)
                    },
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

            // Always visible summary with enhanced visuals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MetricItem(
                    label = stringResource(R.string.viewer_metrics_success),
                    value = stringResource(R.string.viewer_metrics_success_value, successRate),
                    color = when {
                        successRate.toFloat() >= 90 -> WormaCeptorColors.Chart.Fast
                        successRate.toFloat() >= 70 -> WormaCeptorColors.Chart.Medium
                        else -> WormaCeptorColors.Chart.Slow
                    },
                    modifier = Modifier.weight(1f),
                )

                MetricItem(
                    label = stringResource(R.string.viewer_metrics_total),
                    value = totalRequests.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )

                MetricItem(
                    label = stringResource(R.string.viewer_metrics_avg_time),
                    value = formatDurationAvg(avgDuration),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }

            // Expandable details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(300),
                ) + fadeIn(
                    animationSpec = tween(300),
                ),
                exit = shrinkVertically(
                    animationSpec = tween(300),
                ) + fadeOut(
                    animationSpec = tween(300),
                ),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))
                    WormaCeptorDivider(style = DividerStyle.Subtle)
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

                    // Response Time Distribution
                    SectionHeader(
                        icon = Icons.Outlined.Speed,
                        title = stringResource(R.string.viewer_metrics_response_time_distribution),
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

                    DistributionBar(
                        label = stringResource(R.string.viewer_metrics_fast),
                        count = fastCount,
                        total = totalRequests,
                        color = WormaCeptorColors.Chart.Fast,
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

                    DistributionBar(
                        label = stringResource(R.string.viewer_metrics_medium),
                        count = mediumCount,
                        total = totalRequests,
                        color = WormaCeptorColors.Chart.Medium,
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

                    DistributionBar(
                        label = stringResource(R.string.viewer_metrics_slow),
                        count = slowCount,
                        total = totalRequests,
                        color = WormaCeptorColors.Chart.Slow,
                    )

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xl))

                    // Status Code Breakdown
                    SectionHeader(
                        icon = Icons.Outlined.DataUsage,
                        title = stringResource(R.string.viewer_metrics_status_code_breakdown),
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

                    if (status2xx > 0) {
                        StatusCodeBar(
                            label = stringResource(R.string.viewer_metrics_status_2xx_success),
                            count = status2xx,
                            total = totalRequests,
                            color = WormaCeptorColors.Chart.Success2xx,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                    }

                    if (status3xx > 0) {
                        StatusCodeBar(
                            label = stringResource(R.string.viewer_metrics_status_3xx_redirect),
                            count = status3xx,
                            total = totalRequests,
                            color = WormaCeptorColors.Chart.Redirect3xx,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                    }

                    if (status4xx > 0) {
                        StatusCodeBar(
                            label = stringResource(R.string.viewer_metrics_status_4xx_client_error),
                            count = status4xx,
                            total = totalRequests,
                            color = WormaCeptorColors.Chart.ClientError4xx,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                    }

                    if (status5xx > 0) {
                        StatusCodeBar(
                            label = stringResource(R.string.viewer_metrics_status_5xx_server_error),
                            count = status5xx,
                            total = totalRequests,
                            color = WormaCeptorColors.Chart.ServerError5xx,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                    }

                    if (methodBreakdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.lg))

                        Text(
                            text = stringResource(R.string.viewer_metrics_requests_by_method),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

                        methodBreakdown.forEach { (method, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(
                                                    alpha = WormaCeptorDesignSystem.Alpha.heavy,
                                                ),
                                            ),
                                    )
                                    Text(
                                        text = method,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.semantics { heading() },
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DistributionBar(label: String, count: Int, total: Int, color: Color) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat()) * 100f else 0f
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 600),
        label = "bar_animation",
    )

    Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = "$count (${animatedPercentage.roundToInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs))
                    .background(color),
            )
        }
    }
}

@Composable
private fun StatusCodeBar(label: String, count: Int, total: Int, color: Color) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat()) * 100f else 0f
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 600),
        label = "status_bar_animation",
    )

    Column(verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                text = "$count (${animatedPercentage.roundToInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.xs))
                    .background(color),
            )
        }
    }
}
