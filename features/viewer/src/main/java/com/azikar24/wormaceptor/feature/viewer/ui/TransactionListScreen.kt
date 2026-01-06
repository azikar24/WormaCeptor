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
            items(transactions) { transaction ->
                TransactionItem(transaction, onClick = { onItemClick(transaction) })
                Divider()
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
            transaction.code == null -> Color(0xFFFF9800) // Orange for unknown
            transaction.code in 200..299 -> Color(0xFF4CAF50) // Green for success
            transaction.code in 300..399 -> Color(0xFF2196F3) // Blue for redirect
            transaction.code in 400..499 -> Color(0xFFFF9800) // Orange for client error
            transaction.code in 500..599 -> Color(0xFFF44336) // Red for server error
            else -> Color(0xFF9E9E9E) // Gray for other
        }
        TransactionStatus.FAILED -> Color(0xFFF44336) // Red for failed
        TransactionStatus.ACTIVE -> Color(0xFF9E9E9E) // Gray for pending
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
                .background(statusColor)
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
                    modifier = Modifier.weight(1f)
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
        color = methodColor(method).copy(alpha = 0.1f),
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
    "GET" -> Color(0xFF4CAF50)    // Green
    "POST" -> Color(0xFF2196F3)   // Blue
    "PUT" -> Color(0xFFFF9800)    // Orange
    "DELETE" -> Color(0xFFF44336) // Red
    "PATCH" -> Color(0xFF9C27B0)  // Purple
    else -> Color(0xFF9E9E9E)     // Gray
}
