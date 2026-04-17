package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.divider.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.components.metric.WormaCeptorDistributionBar
import com.azikar24.wormaceptor.core.ui.components.metric.WormaCeptorStatItem
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorSectionHeader
import com.azikar24.wormaceptor.core.ui.components.status.WormaCeptorStatusDot
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.core.ui.util.clickableWithoutRipple
import com.azikar24.wormaceptor.core.ui.util.formatDurationAvg
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.R
import kotlinx.collections.immutable.ImmutableList

/** Expandable card showing aggregate network metrics (count, average duration, status breakdown). */
@Composable
fun MetricsCard(
    transactions: ImmutableList<TransactionSummary>,
    modifier: Modifier = Modifier,
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = WormaCeptorTokens.Animation.PAGE),
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

    WormaCeptorCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = WormaCeptorTokens.Spacing.sm, vertical = WormaCeptorTokens.Spacing.md),
        style = CardStyle.Outlined,
        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorTokens.Alpha.MEDIUM),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickableWithoutRipple { isExpanded = !isExpanded }
                .padding(WormaCeptorTokens.Spacing.xl),
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

            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))

            // Always visible summary with enhanced visuals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WormaCeptorStatItem(
                    label = stringResource(R.string.viewer_metrics_success),
                    value = stringResource(R.string.viewer_metrics_success_value, successRate),
                    color = when {
                        successRate.toFloat() >= 90 -> WormaCeptorTokens.Colors.Chart.fast
                        successRate.toFloat() >= 70 -> WormaCeptorTokens.Colors.Chart.medium
                        else -> WormaCeptorTokens.Colors.Chart.slow
                    },
                    modifier = Modifier.weight(1f),
                )

                WormaCeptorStatItem(
                    label = stringResource(R.string.viewer_metrics_total),
                    value = totalRequests.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )

                WormaCeptorStatItem(
                    label = stringResource(R.string.viewer_metrics_avg_time),
                    value = formatDurationAvg(avgDuration),
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }

            // Expandable details
            val expandDuration = WormaCeptorTokens.Animation.PAGE
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(expandDuration),
                ) + fadeIn(
                    animationSpec = tween(expandDuration),
                ),
                exit = shrinkVertically(
                    animationSpec = tween(expandDuration),
                ) + fadeOut(
                    animationSpec = tween(expandDuration),
                ),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))
                    WormaCeptorDivider(style = DividerStyle.Subtle)
                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))

                    // Response Time Distribution
                    WormaCeptorSectionHeader(
                        title = stringResource(R.string.viewer_metrics_response_time_distribution),
                        icon = Icons.Outlined.Speed,
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

                    WormaCeptorDistributionBar(
                        label = stringResource(R.string.viewer_metrics_fast),
                        count = fastCount,
                        total = totalRequests,
                        color = WormaCeptorTokens.Colors.Chart.fast,
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

                    WormaCeptorDistributionBar(
                        label = stringResource(R.string.viewer_metrics_medium),
                        count = mediumCount,
                        total = totalRequests,
                        color = WormaCeptorTokens.Colors.Chart.medium,
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

                    WormaCeptorDistributionBar(
                        label = stringResource(R.string.viewer_metrics_slow),
                        count = slowCount,
                        total = totalRequests,
                        color = WormaCeptorTokens.Colors.Chart.slow,
                    )

                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xl))

                    // Status Code Breakdown
                    WormaCeptorSectionHeader(
                        title = stringResource(R.string.viewer_metrics_status_code_breakdown),
                        icon = Icons.Outlined.DataUsage,
                    )
                    Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

                    if (status2xx > 0) {
                        WormaCeptorDistributionBar(
                            label = stringResource(R.string.viewer_metrics_status_2xx_success),
                            count = status2xx,
                            total = totalRequests,
                            color = WormaCeptorTokens.Colors.Chart.success2xx,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
                    }

                    if (status3xx > 0) {
                        WormaCeptorDistributionBar(
                            label = stringResource(R.string.viewer_metrics_status_3xx_redirect),
                            count = status3xx,
                            total = totalRequests,
                            color = WormaCeptorTokens.Colors.Chart.redirect3xx,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
                    }

                    if (status4xx > 0) {
                        WormaCeptorDistributionBar(
                            label = stringResource(R.string.viewer_metrics_status_4xx_client_error),
                            count = status4xx,
                            total = totalRequests,
                            color = WormaCeptorTokens.Colors.Chart.clientError4xx,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
                    }

                    if (status5xx > 0) {
                        WormaCeptorDistributionBar(
                            label = stringResource(R.string.viewer_metrics_status_5xx_server_error),
                            count = status5xx,
                            total = totalRequests,
                            color = WormaCeptorTokens.Colors.Chart.serverError5xx,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))
                    }

                    if (methodBreakdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.lg))

                        Text(
                            text = stringResource(R.string.viewer_metrics_requests_by_method),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.sm))

                        methodBreakdown.forEach { (method, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = WormaCeptorTokens.Spacing.sm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                                ) {
                                    WormaCeptorStatusDot(
                                        color = MaterialTheme.colorScheme.primary.copy(
                                            alpha = WormaCeptorTokens.Alpha.HEAVY,
                                        ),
                                        size = WormaCeptorTokens.ComponentSize.dot,
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
