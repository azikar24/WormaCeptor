/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptorapp

import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptorapp.wormaceptorui.theme.WormaCeptorMainTheme
import kotlinx.coroutines.launch
import java.security.KeyStore
import javax.crypto.KeyGenerator

private val SecureGreen = Color(0xFF4CAF50)

/**
 * Test activity for the Secure Storage Viewer feature.
 * Provides a custom UI to test secure storage with EncryptedSharedPreferences and KeyStore.
 */
class SecureStorageTestActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WormaCeptorMainTheme {
                SecureStorageTestScreen(
                    onBack = { finish() },
                )
            }
        }
    }
}

private data class EncryptedPrefEntry(
    val key: String,
    val value: String,
    val type: String,
)

private data class KeyStoreEntry(
    val alias: String,
    val algorithm: String,
    val keySize: Int?,
    val creationDate: String?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SecureStorageTestScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var encryptedPrefs by remember { mutableStateOf<List<EncryptedPrefEntry>>(emptyList()) }
    var keyStoreEntries by remember { mutableStateOf<List<KeyStoreEntry>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val pagerState = rememberPagerState(pageCount = { 2 })

    fun refreshEncryptedPrefs() {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                "test_encrypted_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            encryptedPrefs = prefs.all.map { (key, value) ->
                val type = when (value) {
                    is String -> "String"
                    is Int -> "Int"
                    is Long -> "Long"
                    is Float -> "Float"
                    is Boolean -> "Boolean"
                    is Set<*> -> "StringSet"
                    else -> "Unknown"
                }
                EncryptedPrefEntry(
                    key = key,
                    value = value?.toString() ?: "null",
                    type = type,
                )
            }.sortedBy { it.key }
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Failed to read encrypted prefs: ${e.message}"
            encryptedPrefs = emptyList()
        }
    }

    fun refreshKeyStore() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            keyStoreEntries = keyStore.aliases().toList().map { alias ->
                val entry = keyStore.getEntry(alias, null)
                val algorithm = when (entry) {
                    is KeyStore.SecretKeyEntry -> entry.secretKey?.algorithm ?: "Unknown"
                    is KeyStore.PrivateKeyEntry -> entry.privateKey?.algorithm ?: "Unknown"
                    else -> "Unknown"
                }

                KeyStoreEntry(
                    alias = alias,
                    algorithm = algorithm,
                    keySize = null, // KeyStore doesn't easily expose key size
                    creationDate = keyStore.getCreationDate(alias)?.toString(),
                )
            }.sortedBy { it.alias }
            errorMessage = null
        } catch (e: Exception) {
            errorMessage = "Failed to read keystore: ${e.message}"
            keyStoreEntries = emptyList()
        }
    }

    fun setupTestData() {
        // Set up EncryptedSharedPreferences
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            val prefs = EncryptedSharedPreferences.create(
                context,
                "test_encrypted_prefs",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            prefs.edit().apply {
                putString("user_token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test")
                putString("refresh_token", "refresh_xyz_123456")
                putString("api_key", "sk-test-1234567890abcdef")
                putString("user_email", "user@example.com")
                putBoolean("is_premium", true)
                putInt("login_count", 42)
                putLong("last_login", System.currentTimeMillis())
                apply()
            }
        } catch (e: Exception) {
            errorMessage = "Failed to create encrypted prefs: ${e.message}"
        }

        // Set up KeyStore aliases
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val keyAliases = listOf(
                "test_encryption_key",
                "test_signing_key",
                "test_auth_key",
            )

            keyAliases.forEach { alias ->
                if (!keyStore.containsAlias(alias)) {
                    val keyGenerator = KeyGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_AES,
                        "AndroidKeyStore",
                    )

                    val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()

                    keyGenerator.init(keyGenParameterSpec)
                    keyGenerator.generateKey()
                }
            }
        } catch (e: Exception) {
            errorMessage = "Failed to create keystore keys: ${e.message}"
        }

        refreshEncryptedPrefs()
        refreshKeyStore()
    }

    // Initial load
    LaunchedEffect(Unit) {
        refreshEncryptedPrefs()
        refreshKeyStore()
    }

    val totalEntries = encryptedPrefs.size + keyStoreEntries.size

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
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = SecureGreen,
                        )
                        Column {
                            Text(
                                text = "Secure Storage Test",
                                fontWeight = FontWeight.SemiBold,
                            )
                            if (totalEntries > 0) {
                                Text(
                                    text = "$totalEntries entr${if (totalEntries != 1) "ies" else "y"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SecureGreen,
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
                    IconButton(onClick = {
                        refreshEncryptedPrefs()
                        refreshKeyStore()
                    }) {
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
                .padding(padding),
        ) {
            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WormaCeptorDesignSystem.Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Button(
                    onClick = { setupTestData() },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text("Add Test Data")
                }

                OutlinedButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                    Text("Add Entry")
                }
            }

            // Error message
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = WormaCeptorDesignSystem.Spacing.md),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336).copy(alpha = 0.1f),
                    ),
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.md),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFF44336),
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = pagerState.currentPage,
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text("Encrypted Prefs")
                            if (encryptedPrefs.isNotEmpty()) {
                                Surface(
                                    shape = CircleShape,
                                    color = SecureGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        Text(
                                            text = encryptedPrefs.size.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SecureGreen,
                                        )
                                    }
                                }
                            }
                        }
                    },
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.xs),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Text("KeyStore")
                            if (keyStoreEntries.isNotEmpty()) {
                                Surface(
                                    shape = CircleShape,
                                    color = SecureGreen.copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp),
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        Text(
                                            text = keyStoreEntries.size.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = SecureGreen,
                                        )
                                    }
                                }
                            }
                        }
                    },
                )
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> EncryptedPrefsSection(entries = encryptedPrefs)
                    1 -> KeyStoreSection(entries = keyStoreEntries)
                }
            }
        }
    }

    // Add entry dialog
    if (showAddDialog) {
        AddSecureEntryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { key, value ->
                try {
                    val masterKey = MasterKey.Builder(context)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build()

                    val prefs = EncryptedSharedPreferences.create(
                        context,
                        "test_encrypted_prefs",
                        masterKey,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
                    )

                    prefs.edit().putString(key, value).apply()
                    refreshEncryptedPrefs()
                    showAddDialog = false
                } catch (e: Exception) {
                    errorMessage = "Failed to add entry: ${e.message}"
                }
            },
        )
    }
}

@Composable
private fun EncryptedPrefsSection(entries: List<EncryptedPrefEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Lock,
            title = "No encrypted preferences",
            subtitle = "Tap 'Add Test Data' to create some entries",
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(WormaCeptorDesignSystem.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            items(entries, key = { it.key }) { entry ->
                EncryptedPrefItem(
                    entry = entry,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun EncryptedPrefItem(entry: EncryptedPrefEntry, modifier: Modifier = Modifier) {
    var isValueVisible by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
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
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Text(
                        text = entry.key,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = getTypeColor(entry.type).copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = entry.type,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = getTypeColor(entry.type),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xs))
                Text(
                    text = if (isValueVisible) entry.value else maskValue(entry.value),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = { isValueVisible = !isValueVisible }) {
                Icon(
                    imageVector = if (isValueVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isValueVisible) "Hide value" else "Show value",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun KeyStoreSection(entries: List<KeyStoreEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Key,
            title = "No keystore entries",
            subtitle = "Tap 'Add Test Data' to create some keys",
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(WormaCeptorDesignSystem.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
        ) {
            items(entries, key = { it.alias }) { entry ->
                KeyStoreItem(
                    entry = entry,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun KeyStoreItem(entry: KeyStoreEntry, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.md),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorDesignSystem.Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                color = SecureGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = SecureGreen,
                    )
                }
            }

            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.alias,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.xxs))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = entry.algorithm,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    entry.keySize?.let { size ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                text = "${size}-bit",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
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
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.md))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddSecureEntryDialog(
    onDismiss: () -> Unit,
    onAdd: (key: String, value: String) -> Unit,
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Encrypted Entry") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
            ) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Key") },
                    placeholder = { Text("my_secret_key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value") },
                    placeholder = { Text("secret_value") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm),
                )

                Text(
                    text = "This value will be encrypted using AES-256-GCM",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(key, value) },
                enabled = key.isNotBlank() && value.isNotBlank(),
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

private fun maskValue(value: String): String {
    return if (value.length <= 8) {
        "*".repeat(value.length)
    } else {
        value.take(4) + "*".repeat(minOf(8, value.length - 8)) + value.takeLast(4)
    }
}

private fun getTypeColor(type: String): Color {
    return when (type) {
        "String" -> Color(0xFF2196F3)
        "Int" -> Color(0xFF4CAF50)
        "Long" -> Color(0xFF9C27B0)
        "Float" -> Color(0xFFFF9800)
        "Boolean" -> Color(0xFFE91E63)
        "StringSet" -> Color(0xFF00BCD4)
        else -> Color(0xFF757575)
    }
}
