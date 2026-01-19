package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import kotlin.math.roundToInt

@Composable
fun MetricsCard(
    transactions: List<TransactionSummary>,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val rotationAngle by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "expand_icon_rotation"
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
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            surfaceColor.copy(alpha = 0.97f),
            surfaceColor.copy(alpha = 1f)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) { isExpanded = !isExpanded }
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Performance Metrics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.rotate(rotationAngle),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Always visible summary with enhanced visuals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularSuccessMetric(
                        label = "Success",
                        percentage = successRate.toFloat()
                    )
                }

                MetricItem(
                    label = "Total",
                    value = totalRequests.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                MetricItem(
                    label = "Avg Time",
                    value = "${avgDuration.toInt()}ms",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Expandable details
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(300)
                ) + fadeIn(
                    animationSpec = tween(300)
                ),
                exit = shrinkVertically(
                    animationSpec = tween(300)
                ) + fadeOut(
                    animationSpec = tween(300)
                )
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Response Time Distribution
                    SectionHeader(
                        icon = Icons.Outlined.Speed,
                        title = "Response Time Distribution"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DistributionBar(
                        label = "Fast (<100ms)",
                        count = fastCount,
                        total = totalRequests,
                        color = Color(0xFF10B981)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DistributionBar(
                        label = "Medium (100-500ms)",
                        count = mediumCount,
                        total = totalRequests,
                        color = Color(0xFFF59E0B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    DistributionBar(
                        label = "Slow (>500ms)",
                        count = slowCount,
                        total = totalRequests,
                        color = Color(0xFFEF4444)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status Code Breakdown
                    SectionHeader(
                        icon = Icons.Outlined.DataUsage,
                        title = "Status Code Breakdown"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (status2xx > 0) {
                        StatusCodeBar(
                            label = "2xx Success",
                            count = status2xx,
                            total = totalRequests,
                            color = Color(0xFF10B981)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (status3xx > 0) {
                        StatusCodeBar(
                            label = "3xx Redirect",
                            count = status3xx,
                            total = totalRequests,
                            color = Color(0xFF3B82F6)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (status4xx > 0) {
                        StatusCodeBar(
                            label = "4xx Client Error",
                            count = status4xx,
                            total = totalRequests,
                            color = Color(0xFFF59E0B)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (status5xx > 0) {
                        StatusCodeBar(
                            label = "5xx Server Error",
                            count = status5xx,
                            total = totalRequests,
                            color = Color(0xFFEF4444)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (methodBreakdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Requests by Method",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        methodBreakdown.forEach { (method, count) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                    )
                                    Text(
                                        text = method,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = count.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.8f))
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CircularSuccessMetric(
    label: String,
    percentage: Float
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 1000),
        label = "percentage_animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(64.dp)
        ) {
            // Background circle
            Canvas(modifier = Modifier.size(64.dp)) {
                drawArc(
                    color = Color.Gray.copy(alpha = 0.1f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Progress circle
            val color = when {
                animatedPercentage >= 90 -> Color(0xFF10B981)
                animatedPercentage >= 70 -> Color(0xFFF59E0B)
                else -> Color(0xFFEF4444)
            }

            Canvas(modifier = Modifier.size(64.dp)) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = (animatedPercentage / 100f) * 360f,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Text(
                text = "${animatedPercentage.roundToInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DistributionBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat()) * 100f else 0f
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 600),
        label = "bar_animation"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$count (${animatedPercentage.roundToInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.8f),
                                color
                            )
                        )
                    )
            )
        }
    }
}

@Composable
private fun StatusCodeBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total.toFloat()) * 100f else 0f
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(durationMillis = 600),
        label = "status_bar_animation"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "$count (${animatedPercentage.roundToInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                color.copy(alpha = 0.8f),
                                color
                            )
                        )
                    )
            )
        }
    }
}
