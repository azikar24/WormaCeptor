/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.cookies.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azikar24.wormaceptor.domain.entities.CookieInfo
import com.azikar24.wormaceptor.feature.cookies.ui.theme.CookiesDesignSystem
import com.azikar24.wormaceptor.feature.cookies.ui.theme.asSubtleBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Screen displaying detailed information about a single cookie.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookieDetailScreen(cookie: CookieInfo, onBack: () -> Unit, onDelete: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = cookie.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = cookie.domain,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete cookie",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        modifier = modifier,
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(CookiesDesignSystem.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(CookiesDesignSystem.Spacing.md),
        ) {
            // Status card
            item {
                StatusCard(cookie = cookie)
            }

            // Value card
            item {
                ValueCard(
                    value = cookie.value,
                    onCopy = {
                        copyToClipboard(context, "Cookie Value", cookie.value)
                    },
                )
            }

            // Attributes section
            item {
                AttributesSection(cookie = cookie)
            }

            // Security section
            item {
                SecuritySection(cookie = cookie)
            }

            // Expiration section
            item {
                ExpirationSection(cookie = cookie)
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Cookie") },
            text = { Text("Are you sure you want to delete \"${cookie.name}\" from ${cookie.domain}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun StatusCard(cookie: CookieInfo, modifier: Modifier = Modifier) {
    val statusColor = CookiesDesignSystem.CookieColors.forExpirationStatus(cookie.expirationStatus)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CookiesDesignSystem.CornerRadius.md))
            .border(
                width = CookiesDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .background(
                color = statusColor.asSubtleBackground(),
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .padding(CookiesDesignSystem.Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.sm),
            color = statusColor.copy(alpha = 0.15f),
            modifier = Modifier.size(48.dp),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize(),
            ) {
                Icon(
                    imageVector = Icons.Default.Cookie,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = statusColor,
                )
            }
        }

        Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.lg))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = cookie.expirationStatus,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = statusColor,
            )
            Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.xxs))
            Text(
                text = when (cookie.expirationStatus) {
                    "Session" -> "Cookie will be deleted when browser closes"
                    "Expired" -> "Cookie has expired and may be deleted"
                    "Valid" -> "Cookie is active and valid"
                    else -> ""
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ValueCard(value: String, onCopy: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CookiesDesignSystem.CornerRadius.md))
            .border(
                width = CookiesDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .padding(CookiesDesignSystem.Spacing.lg),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Value",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy value",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.sm))

        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.sm),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = value.ifEmpty { "(empty)" },
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                color = if (value.isEmpty()) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(CookiesDesignSystem.Spacing.md),
            )
        }
    }
}

@Composable
private fun AttributesSection(cookie: CookieInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CookiesDesignSystem.CornerRadius.md))
            .border(
                width = CookiesDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .padding(CookiesDesignSystem.Spacing.lg),
    ) {
        SectionHeader(
            icon = Icons.Default.Info,
            title = "Attributes",
        )

        Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.md))

        AttributeRow(
            icon = Icons.Default.Language,
            label = "Domain",
            value = cookie.domain,
        )

        Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.sm))

        AttributeRow(
            icon = Icons.Default.Info,
            label = "Path",
            value = cookie.path,
        )

        cookie.sameSite?.let { sameSite ->
            Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.sm))

            AttributeRow(
                icon = Icons.Default.Security,
                label = "SameSite",
                value = sameSite.name.lowercase().replaceFirstChar { it.uppercase() },
                valueColor = when (sameSite) {
                    CookieInfo.SameSite.STRICT -> CookiesDesignSystem.CookieColors.sameSiteStrict
                    CookieInfo.SameSite.LAX -> CookiesDesignSystem.CookieColors.sameSiteLax
                    CookieInfo.SameSite.NONE -> CookiesDesignSystem.CookieColors.sameSiteNone
                },
            )
        }
    }
}

@Composable
private fun SecuritySection(cookie: CookieInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CookiesDesignSystem.CornerRadius.md))
            .border(
                width = CookiesDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .padding(CookiesDesignSystem.Spacing.lg),
    ) {
        SectionHeader(
            icon = Icons.Default.Lock,
            title = "Security",
        )

        Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.md))

        SecurityAttribute(
            label = "Secure",
            isEnabled = cookie.isSecure,
            description = if (cookie.isSecure) {
                "Only sent over HTTPS connections"
            } else {
                "Sent over both HTTP and HTTPS"
            },
        )

        Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.md))

        SecurityAttribute(
            label = "HttpOnly",
            isEnabled = cookie.isHttpOnly,
            description = if (cookie.isHttpOnly) {
                "Not accessible via JavaScript"
            } else {
                "Accessible via JavaScript"
            },
        )
    }
}

@Composable
private fun ExpirationSection(cookie: CookieInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CookiesDesignSystem.CornerRadius.md))
            .border(
                width = CookiesDesignSystem.BorderWidth.regular,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.md),
            )
            .padding(CookiesDesignSystem.Spacing.lg),
    ) {
        SectionHeader(
            icon = Icons.Default.Schedule,
            title = "Expiration",
        )

        Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.md))

        if (cookie.isSessionCookie) {
            Text(
                text = "Session Cookie",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = CookiesDesignSystem.CookieColors.session,
            )
            Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.xxs))
            Text(
                text = "This cookie will be deleted when you close your browser",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }
            val expiresDate = cookie.expiresAt?.let { Date(it) }

            AttributeRow(
                icon = Icons.Default.Schedule,
                label = "Expires",
                value = expiresDate?.let { dateFormat.format(it) } ?: "Unknown",
                valueColor = if (cookie.isExpired) {
                    CookiesDesignSystem.CookieColors.expired
                } else {
                    null
                },
            )

            val expiresAt = cookie.expiresAt
            if (!cookie.isExpired && expiresAt != null) {
                Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.sm))

                val remainingTime = expiresAt - System.currentTimeMillis()
                val remainingText = formatRemainingTime(remainingTime)

                Text(
                    text = "Expires in $remainingText",
                    style = MaterialTheme.typography.bodySmall,
                    color = CookiesDesignSystem.CookieColors.valid,
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.sm))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AttributeRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.sm))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
            ),
            fontWeight = FontWeight.Medium,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SecurityAttribute(label: String, isEnabled: Boolean, description: String, modifier: Modifier = Modifier) {
    val color = if (isEnabled) {
        CookiesDesignSystem.CookieColors.secure
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            color = color.copy(alpha = 0.15f),
            contentColor = color,
            shape = RoundedCornerShape(CookiesDesignSystem.CornerRadius.xs),
        ) {
            Text(
                text = if (isEnabled) "Yes" else "No",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = CookiesDesignSystem.Spacing.sm,
                    vertical = CookiesDesignSystem.Spacing.xxs,
                ),
            )
        }

        Spacer(modifier = Modifier.width(CookiesDesignSystem.Spacing.md))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(CookiesDesignSystem.Spacing.xxs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun formatRemainingTime(millis: Long): String {
    if (millis <= 0) return "0 seconds"

    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days ${if (days == 1L) "day" else "days"}"
        hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"}"
        minutes > 0 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"}"
        else -> "$seconds ${if (seconds == 1L) "second" else "seconds"}"
    }
}
