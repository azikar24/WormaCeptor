/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp

import android.os.Bundle
import android.webkit.CookieManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Https
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme

private val CookieOrange = Color(0xFFFF9800)

/**
 * Test activity for the Cookies Manager feature.
 * Provides a custom UI to test cookie management with CookieManager directly.
 */
class CookiesTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WormaCeptorMainTheme {
                CookiesTestScreen(
                    onBack = { finish() },
                )
            }
        }
    }
}

private data class ParsedCookie(
    val name: String,
    val value: String,
    val domain: String,
    val path: String?,
    val isSecure: Boolean,
    val isHttpOnly: Boolean,
    val maxAge: Long?,
    val rawCookie: String,
)

private data class DomainCookies(
    val domain: String,
    val cookies: List<ParsedCookie>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CookiesTestScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val cookieManager = remember { CookieManager.getInstance() }
    var domainCookies by remember { mutableStateOf<List<DomainCookies>>(emptyList()) }
    val expandedDomains = remember { mutableStateMapOf<String, Boolean>() }
    var selectedCookie by remember { mutableStateOf<ParsedCookie?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val testDomains = listOf(
        "https://example.com",
        "https://api.example.com",
        "https://analytics.example.com",
        "https://shop.example.com",
        "https://cdn.example.com",
    )

    fun refreshCookies() {
        domainCookies = testDomains.mapNotNull { url ->
            val domain = url.removePrefix("https://").removePrefix("http://")
            val cookieString = cookieManager.getCookie(url)
            if (cookieString.isNullOrBlank()) {
                null
            } else {
                val cookies = parseCookies(cookieString, domain)
                if (cookies.isNotEmpty()) {
                    DomainCookies(domain = domain, cookies = cookies)
                } else {
                    null
                }
            }
        }
    }

    fun setupTestCookies() {
        cookieManager.setAcceptCookie(true)

        val testCookies = listOf(
            "https://example.com" to "session_id=abc123; Path=/",
            "https://example.com" to "user_pref=dark_mode; Path=/; Secure",
            "https://api.example.com" to "auth_token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9; Path=/; Secure; HttpOnly",
            "https://api.example.com" to "refresh_token=xyz789; Path=/api/auth; Secure; HttpOnly",
            "https://analytics.example.com" to "_ga=GA1.2.123456789.1234567890; Path=/; Max-Age=63072000",
            "https://analytics.example.com" to "_gid=GA1.2.987654321.1234567890; Path=/; Max-Age=86400",
            "https://shop.example.com" to "cart_id=cart_12345; Path=/",
            "https://shop.example.com" to "currency=USD; Path=/",
            "https://shop.example.com" to "language=en; Path=/; Secure",
            "https://cdn.example.com" to "cache_version=v2; Path=/; Max-Age=31536000",
        )

        testCookies.forEach { (url, cookie) ->
            cookieManager.setCookie(url, cookie)
        }
        cookieManager.flush()
        refreshCookies()
    }

    fun clearAllCookies() {
        cookieManager.removeAllCookies(null)
        cookieManager.flush()
        refreshCookies()
    }

    // Initial load
    remember {
        refreshCookies()
        true
    }

    val totalCookies = domainCookies.sumOf { it.cookies.size }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cookie,
                            contentDescription = null,
                            tint = CookieOrange,
                        )
                        Column {
                            Text(
                                text = "Cookies Test",
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (totalCookies > 0) {
                                Text(
                                    text = "$totalCookies cookie${if (totalCookies != 1) "s" else ""} in ${domainCookies.size} domain${if (domainCookies.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CookieOrange,
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { refreshCookies() }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Button(
                    onClick = { setupTestCookies() },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text("Add Test Cookies")
                }

                OutlinedButton(
                    onClick = { clearAllCookies() },
                    modifier = Modifier.weight(1f),
                    enabled = totalCookies > 0,
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text("Clear All")
                }
            }

            OutlinedButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                Text("Add Custom Cookie")
            }

            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

            // Cookie list
            if (domainCookies.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(64.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cookie,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))
                        Text(
                            text = "No cookies found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Tap 'Add Test Cookies' to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    domainCookies.forEach { domainData ->
                        item(key = "header_${domainData.domain}") {
                            DomainHeader(
                                domain = domainData.domain,
                                cookieCount = domainData.cookies.size,
                                isExpanded = expandedDomains[domainData.domain] ?: true,
                                onToggle = {
                                    expandedDomains[domainData.domain] = !(expandedDomains[domainData.domain] ?: true)
                                },
                            )
                        }

                        if (expandedDomains[domainData.domain] != false) {
                            items(
                                items = domainData.cookies,
                                key = { "${domainData.domain}_${it.name}" },
                            ) { cookie ->
                                CookieListItem(
                                    cookie = cookie,
                                    onClick = { selectedCookie = cookie },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Cookie detail sheet
    selectedCookie?.let { cookie ->
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { selectedCookie = null },
            sheetState = sheetState,
        ) {
            CookieDetailSheet(
                cookie = cookie,
                onDismiss = { selectedCookie = null },
            )
        }
    }

    // Add cookie dialog
    if (showAddDialog) {
        AddCookieDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { domain, name, value, isSecure ->
                val url = "https://$domain"
                val cookieString = buildString {
                    append("$name=$value; Path=/")
                    if (isSecure) append("; Secure")
                }
                cookieManager.setCookie(url, cookieString)
                cookieManager.flush()
                refreshCookies()
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun DomainHeader(
    domain: String,
    cookieCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(
            containerColor = CookieOrange.copy(alpha = 0.1f),
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = CookieOrange,
                )
                Text(
                    text = domain,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = CookieOrange.copy(alpha = 0.2f),
                ) {
                    Text(
                        text = cookieCount.toString(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CookieOrange,
                    )
                }
            }
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CookieListItem(
    cookie: ParsedCookie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = WormaCeptorDesignSystem.Spacing.lg)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                ) {
                    Text(
                        text = cookie.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (cookie.isSecure) {
                        Icon(
                            imageVector = Icons.Default.Https,
                            contentDescription = "Secure",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF4CAF50),
                        )
                    }
                    if (cookie.isHttpOnly) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "HttpOnly",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF2196F3),
                        )
                    }
                }
                Text(
                    text = cookie.value.take(40) + if (cookie.value.length > 40) "..." else "",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun CookieDetailSheet(
    cookie: ParsedCookie,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(WormaCeptorDesignSystem.Spacing.lg)
            .padding(bottom = WormaCeptorDesignSystem.Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.Cookie,
                contentDescription = null,
                tint = CookieOrange,
            )
            Text(
                text = "Cookie Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))

        DetailItem(label = "Name", value = cookie.name)
        DetailItem(label = "Value", value = cookie.value, isMonospace = true)
        DetailItem(label = "Domain", value = cookie.domain)
        cookie.path?.let { DetailItem(label = "Path", value = it) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            FlagChip(label = "Secure", isEnabled = cookie.isSecure)
            FlagChip(label = "HttpOnly", isEnabled = cookie.isHttpOnly)
        }

        cookie.maxAge?.let {
            DetailItem(label = "Max-Age", value = formatDuration(it))
        }

        Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))

        Text(
            text = "Raw Cookie",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ) {
            Text(
                text = cookie.rawCookie,
                modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun FlagChip(label: String, isEnabled: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
        color = if (isEnabled) {
            Color(0xFF4CAF50).copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        if (isEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        CircleShape,
                    ),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddCookieDialog(
    onDismiss: () -> Unit,
    onAdd: (domain: String, name: String, value: String, isSecure: Boolean) -> Unit,
) {
    var domain by remember { mutableStateOf("example.com") }
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var isSecure by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Cookie") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                OutlinedTextField(
                    value = domain,
                    onValueChange = { domain = it },
                    label = { Text("Domain") },
                    placeholder = { Text("example.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Cookie Name") },
                    placeholder = { Text("my_cookie") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Cookie Value") },
                    placeholder = { Text("cookie_value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isSecure = !isSecure },
                ) {
                    Checkbox(
                        checked = isSecure,
                        onCheckedChange = { isSecure = it },
                    )
                    Text("Secure cookie (HTTPS only)")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(domain, name, value, isSecure) },
                enabled = domain.isNotBlank() && name.isNotBlank() && value.isNotBlank(),
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun parseCookies(cookieString: String, domain: String): List<ParsedCookie> {
    return cookieString.split(";").mapNotNull { part ->
        val trimmed = part.trim()
        if (trimmed.isEmpty() || !trimmed.contains("=")) return@mapNotNull null

        val firstEquals = trimmed.indexOf("=")
        val name = trimmed.substring(0, firstEquals).trim()
        val value = trimmed.substring(firstEquals + 1).trim()

        // Skip cookie attributes that look like cookies
        if (name.lowercase() in listOf("path", "domain", "max-age", "expires", "secure", "httponly", "samesite")) {
            return@mapNotNull null
        }

        ParsedCookie(
            name = name,
            value = value,
            domain = domain,
            path = null,
            isSecure = cookieString.lowercase().contains("secure"),
            isHttpOnly = cookieString.lowercase().contains("httponly"),
            maxAge = null,
            rawCookie = trimmed,
        )
    }
}

private fun formatDuration(seconds: Long): String {
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m"
        seconds < 86400 -> "${seconds / 3600}h"
        else -> "${seconds / 86400}d"
    }
}
