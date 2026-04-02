package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.asSubtleBackground
import com.azikar24.wormaceptor.core.ui.util.formatBytes
import com.azikar24.wormaceptor.core.ui.util.formatDuration
import com.azikar24.wormaceptor.domain.entities.NetworkTransaction
import com.azikar24.wormaceptor.domain.entities.Request
import com.azikar24.wormaceptor.domain.entities.Response
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun OverviewTab(
    transaction: NetworkTransaction,
    modifier: Modifier = Modifier,
) {
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = WormaCeptorDesignSystem.Spacing.lg,
                top = WormaCeptorDesignSystem.Spacing.lg,
                end = WormaCeptorDesignSystem.Spacing.lg,
                bottom = WormaCeptorDesignSystem.Spacing.lg + navBarPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
    ) {
        // Status & Timing Card with Timeline
        EnhancedOverviewCard(
            title = stringResource(R.string.viewer_overview_timing),
            icon = Icons.Default.Schedule,
            iconTint = MaterialTheme.colorScheme.tertiary,
        ) {
            DetailRow("URL", transaction.request.url)
            DetailRow("Method", transaction.request.method)
            DetailRow("Status", transaction.status.name)
            DetailRow("Response Code", transaction.response?.code?.toString() ?: "-")
            DetailRow("Duration", formatDuration(transaction.durationMs))
            DetailRow(
                "Timestamp",
                java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault(),
                ).format(java.util.Date(transaction.timestamp)),
            )

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

            // Visual Timeline
            TransactionTimeline(
                durationMs = transaction.durationMs ?: 0,
                hasResponse = transaction.response != null,
            )
        }

        // Security Card with Badge
        EnhancedOverviewCard(
            title = stringResource(R.string.viewer_overview_security),
            icon = Icons.Default.Security,
            iconTint = MaterialTheme.colorScheme.secondary,
        ) {
            DetailRow("Protocol", transaction.response?.protocol ?: "Unknown")

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Enhanced SSL/TLS Badge
            val isSsl = transaction.response?.tlsVersion != null
            SslBadge(
                isSsl = isSsl,
                tlsVersion = transaction.response?.tlsVersion,
            )
        }

        // Data Transfer Card
        EnhancedOverviewCard(
            title = stringResource(R.string.viewer_overview_data_transfer),
            icon = Icons.Default.Storage,
            iconTint = MaterialTheme.colorScheme.primary,
        ) {
            val reqSize = transaction.request.bodySize
            val resSize = transaction.response?.bodySize ?: 0
            val totalSize = reqSize + resSize

            DetailRow("Request Size", formatBytes(reqSize))
            DetailRow("Response Size", formatBytes(resSize))
            DetailRow("Total Transfer", formatBytes(totalSize))
        }

        // Extensions Card - only shown when extensions exist
        if (transaction.extensions.isNotEmpty()) {
            EnhancedOverviewCard(
                title = stringResource(R.string.viewer_overview_extensions),
                icon = Icons.Default.Extension,
                iconTint = MaterialTheme.colorScheme.tertiary,
            ) {
                transaction.extensions.forEach { (key, value) ->
                    DetailRow(key, value)
                }
            }
        }
    }
}

@Composable
private fun TransactionTimeline(
    durationMs: Long,
    hasResponse: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.PROMINENT),
                shape = WormaCeptorDesignSystem.Shapes.card,
            )
            .border(
                width = WormaCeptorDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.MEDIUM),
                shape = WormaCeptorDesignSystem.Shapes.card,
            )
            .padding(WormaCeptorDesignSystem.Spacing.md),
    ) {
        Text(
            text = stringResource(R.string.viewer_overview_timeline_title),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.xs),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Request Phase
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorDesignSystem.Alpha.HEAVY),
                        shape = RoundedCornerShape(
                            topStart = WormaCeptorDesignSystem.CornerRadius.xs,
                            bottomStart = WormaCeptorDesignSystem.CornerRadius.xs,
                        ),
                    ),
            )

            // Processing/Network Phase
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        MaterialTheme.colorScheme.secondary.copy(alpha = WormaCeptorDesignSystem.Alpha.INTENSE),
                    ),
            )

            // Response Phase
            Box(
                modifier = Modifier
                    .weight(0.3f)
                    .height(WormaCeptorDesignSystem.Spacing.sm)
                    .background(
                        if (hasResponse) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = WormaCeptorDesignSystem.Alpha.HEAVY)
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = WormaCeptorDesignSystem.Alpha.STRONG)
                        },
                        shape = RoundedCornerShape(
                            topEnd = WormaCeptorDesignSystem.CornerRadius.xs,
                            bottomEnd = WormaCeptorDesignSystem.CornerRadius.xs,
                        ),
                    ),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WormaCeptorDesignSystem.Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.viewer_overview_timeline_request),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (hasResponse) {
                    stringResource(R.string.viewer_overview_timeline_response)
                } else {
                    stringResource(R.string.viewer_overview_timeline_failed)
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SslBadge(
    isSsl: Boolean,
    tlsVersion: String?,
) {
    Surface(
        shape = WormaCeptorDesignSystem.Shapes.chip,
        color = if (isSsl) {
            MaterialTheme.colorScheme.primary.asSubtleBackground()
        } else {
            MaterialTheme.colorScheme.error.asSubtleBackground()
        },
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            if (isSsl) {
                MaterialTheme.colorScheme.primary.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = WormaCeptorDesignSystem.Alpha.MODERATE)
            },
        ),
        modifier = Modifier.wrapContentSize(),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = WormaCeptorDesignSystem.Spacing.md,
                vertical = WormaCeptorDesignSystem.Spacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
        ) {
            Icon(
                imageVector = if (isSsl) Icons.Default.Lock else Icons.Default.LockOpen,
                contentDescription = if (isSsl) {
                    stringResource(R.string.viewer_overview_secure)
                } else {
                    stringResource(R.string.viewer_overview_insecure)
                },
                tint = if (isSsl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.sm),
            )
            Text(
                text = if (isSsl) {
                    tlsVersion ?: stringResource(R.string.viewer_overview_secure_connection)
                } else {
                    stringResource(R.string.viewer_overview_insecure_connection)
                },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = if (isSsl) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
internal fun EnhancedOverviewCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorDesignSystem.Elevation.sm),
        ),
        border = BorderStroke(
            WormaCeptorDesignSystem.BorderWidth.regular,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.MEDIUM),
        ),
        shape = WormaCeptorDesignSystem.Shapes.card,
    ) {
        Column(modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg)) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                modifier = Modifier.padding(bottom = WormaCeptorDesignSystem.Spacing.md),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(WormaCeptorDesignSystem.IconSize.md),
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OverviewTabPreview() {
    WormaCeptorTheme {
        OverviewTab(
            transaction = NetworkTransaction(
                request = Request(
                    url = "https://api.example.com/users/123",
                    method = "GET",
                    headers = mapOf(
                        "Content-Type" to listOf("application/json"),
                        "Authorization" to listOf("Bearer token123"),
                    ),
                    bodyRef = null,
                ),
                response = Response(
                    code = 200,
                    message = "OK",
                    headers = mapOf(
                        "Content-Type" to listOf("application/json"),
                    ),
                    bodyRef = null,
                    protocol = "HTTP/2",
                    tlsVersion = "TLSv1.3",
                    bodySize = 1024L,
                ),
                durationMs = 142L,
                status = TransactionStatus.COMPLETED,
            ),
        )
    }
}
