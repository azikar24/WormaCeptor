package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.TransactionStatus
import com.azikar24.wormaceptor.domain.entities.TransactionSummary
import com.azikar24.wormaceptor.feature.viewer.ui.theme.WormaCeptorColors
import java.text.SimpleDateFormat
import java.util.*

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
        LazyColumn(modifier = modifier.fillMaxSize()) {
            if (header != null) {
                item {
                    header()
                }
            }
            items(transactions, key = { it.id }) { transaction ->
                TransactionItem(transaction, onClick = { onItemClick(transaction) })
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
            text = if (hasActiveFilters) "No transactions match filters" else "No transactions captured",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasActiveFilters) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onClearFilters) {
                Text("Clear Filters")
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: TransactionSummary,
    onClick: () -> Unit
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status indicator bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .background(statusColor, shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                MethodBadge(transaction.method)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = transaction.path,
                    fontSize = 14.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row {
                Text(
                    text = transaction.host,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = transaction.code?.toString() ?: "?",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = statusColor
            )
            Text(
                text = "${transaction.tookMs ?: "?"}ms",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MethodBadge(method: String) {
    Surface(
        color = methodColor(method).copy(alpha = 0.2f),
        contentColor = methodColor(method),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    ) {
        Text(
            text = method.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
