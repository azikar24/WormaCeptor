/*
 * Copyright AziKar24 2025.
 */

package com.azikar24.wormaceptor.feature.crypto

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.CryptoOperation
import com.azikar24.wormaceptor.domain.entities.CryptoPreset
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.domain.entities.KeyFormat
import com.azikar24.wormaceptor.domain.entities.PaddingScheme
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Entry point for the Response Encryption/Decryption feature.
 * Provides factory methods and composable entry point.
 */
object CryptoFeature {
    /**
     * Creates a CryptoEngine instance.
     */
    fun createEngine(): CryptoEngine = CryptoEngine()

    /**
     * Creates a CryptoViewModel factory for use with viewModel().
     */
    fun createViewModelFactory(engine: CryptoEngine): CryptoViewModelFactory = CryptoViewModelFactory(engine)
}

/**
 * ViewModel for the Crypto feature.
 */
class CryptoViewModel(private val engine: CryptoEngine) : ViewModel() {
    val config: StateFlow<CryptoConfig> = engine.config
    val currentResult: StateFlow<CryptoResult?> = engine.currentResult
    val history: StateFlow<List<CryptoResult>> = engine.history
    val isProcessing: StateFlow<Boolean> = engine.isProcessing
    val error: StateFlow<String?> = engine.error

    var inputText by mutableStateOf("")
        private set

    fun updateInputText(text: String) {
        inputText = text
    }

    fun setAlgorithm(algorithm: CryptoAlgorithm) = engine.setAlgorithm(algorithm)
    fun setMode(mode: CipherMode) = engine.setMode(mode)
    fun setPadding(padding: PaddingScheme) = engine.updateConfig { copy(padding = padding) }
    fun setKey(key: String) = engine.setKey(key)
    fun setIv(iv: String) = engine.setIv(iv)
    fun setKeyFormat(format: KeyFormat) = engine.setKeyFormat(format)

    fun applyPreset(preset: CryptoPreset) {
        engine.setConfig(preset.config)
    }

    fun generateKey(): String {
        val key = engine.generateKey()
        engine.setKey(key)
        return key
    }

    fun generateIv(): String {
        val iv = engine.generateIv()
        engine.setIv(iv)
        return iv
    }

    fun encrypt() {
        if (inputText.isNotBlank()) {
            engine.encrypt(inputText)
        }
    }

    fun decrypt() {
        if (inputText.isNotBlank()) {
            engine.decrypt(inputText)
        }
    }

    fun clearResult() = engine.clearResult()
    fun clearHistory() = engine.clearHistory()
    fun removeFromHistory(id: String) = engine.removeFromHistory(id)

    fun loadFromHistory(result: CryptoResult) {
        inputText = result.input
    }
}

/**
 * Factory for creating CryptoViewModel instances.
 */
class CryptoViewModelFactory(
    private val engine: CryptoEngine,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CryptoViewModel::class.java)) {
            return CryptoViewModel(engine) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

/**
 * Main composable for the Response Encryption/Decryption feature.
 *
 * @param engine Optional CryptoEngine instance. If provided, allows sharing state with
 *               CryptoHistoryScreen. If null, creates its own engine instance.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CryptoTool(
    modifier: Modifier = Modifier,
    engine: CryptoEngine? = null,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToHistory: (() -> Unit)? = null,
) {
    val cryptoEngine = engine ?: remember { CryptoFeature.createEngine() }
    val factory = remember(cryptoEngine) { CryptoFeature.createViewModelFactory(cryptoEngine) }
    val viewModel: CryptoViewModel = viewModel(factory = factory)

    val config by viewModel.config.collectAsState()
    val currentResult by viewModel.currentResult.collectAsState()
    val history by viewModel.history.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showKeyPassword by remember { mutableStateOf(false) }
    var showIvPassword by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.Security, null, tint = Color(0xFF673AB7))
                        Text("Crypto Tool", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    if (history.isNotEmpty() && onNavigateToHistory != null) {
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(
                                Icons.Default.History,
                                "History",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Presets
            WormaCeptorContainer(
                style = ContainerStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                ) {
                    Text("Presets", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        CryptoPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = config.algorithm == preset.config.algorithm &&
                                    config.mode == preset.config.mode,
                                onClick = { viewModel.applyPreset(preset) },
                                label = { Text(preset.displayName) },
                            )
                        }
                    }
                }
            }

            // Algorithm & Mode Selection
            WormaCeptorContainer(
                style = ContainerStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                ) {
                    Text("Algorithm", fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        CryptoAlgorithm.entries.filter { it != CryptoAlgorithm.RSA }.forEach { algorithm ->
                            FilterChip(
                                selected = config.algorithm == algorithm,
                                onClick = { viewModel.setAlgorithm(algorithm) },
                                label = { Text(algorithm.displayName) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                    Text("Mode", fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        CipherMode.entries.forEach { mode ->
                            FilterChip(
                                selected = config.mode == mode,
                                onClick = { viewModel.setMode(mode) },
                                label = { Text(mode.displayName) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                    Text("Padding", fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        PaddingScheme.entries.forEach { padding ->
                            FilterChip(
                                selected = config.padding == padding,
                                onClick = { viewModel.setPadding(padding) },
                                label = { Text(padding.displayName) },
                            )
                        }
                    }
                }
            }

            // Key & IV Input
            WormaCeptorContainer(
                style = ContainerStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                ) {
                    Text("Key Format", fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        KeyFormat.entries.forEach { format ->
                            FilterChip(
                                selected = config.keyFormat == format,
                                onClick = { viewModel.setKeyFormat(format) },
                                label = { Text(format.displayName) },
                            )
                        }
                    }

                    // Key input
                    OutlinedTextField(
                        value = config.key,
                        onValueChange = { viewModel.setKey(it) },
                        label = { Text("Key (${config.algorithm.keyLengthBits / 8} bytes)") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showKeyPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { showKeyPassword = !showKeyPassword }) {
                                    Icon(
                                        if (showKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showKeyPassword) "Hide key" else "Show key",
                                    )
                                }
                                IconButton(onClick = {
                                    viewModel.generateKey()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Key generated")
                                    }
                                }) {
                                    Icon(Icons.Default.Refresh, "Generate key")
                                }
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Key, null) },
                        singleLine = true,
                    )

                    // IV input (only show if mode requires it)
                    AnimatedVisibility(
                        visible = config.mode.requiresIv,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        OutlinedTextField(
                            value = config.iv,
                            onValueChange = { viewModel.setIv(it) },
                            label = { Text("IV (Initialization Vector)") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showIvPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { showIvPassword = !showIvPassword }) {
                                        Icon(
                                            if (showIvPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showIvPassword) "Hide IV" else "Show IV",
                                        )
                                    }
                                    IconButton(onClick = {
                                        viewModel.generateIv()
                                        scope.launch {
                                            snackbarHostState.showSnackbar("IV generated")
                                        }
                                    }) {
                                        Icon(Icons.Default.Refresh, "Generate IV")
                                    }
                                }
                            },
                            singleLine = true,
                        )
                    }
                }
            }

            // Input Text Area
            WormaCeptorContainer(
                style = ContainerStyle.Outlined,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                ) {
                    Text("Input", fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = viewModel.inputText,
                        onValueChange = { viewModel.updateInputText(it) },
                        label = { Text("Enter text to encrypt/decrypt") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        maxLines = 5,
                    )

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                    ) {
                        Button(
                            onClick = { viewModel.encrypt() },
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing && viewModel.inputText.isNotBlank() && config.key.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                            Text("Encrypt")
                        }

                        Button(
                            onClick = { viewModel.decrypt() },
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing && viewModel.inputText.isNotBlank() && config.key.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688)),
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                            Text("Decrypt")
                        }
                    }
                }
            }

            // Result Display
            AnimatedVisibility(
                visible = currentResult != null || error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                currentResult?.let { result ->
                    ResultCard(
                        result = result,
                        onCopy = { text ->
                            copyToClipboard(context, text)
                            scope.launch {
                                snackbarHostState.showSnackbar("Copied to clipboard")
                            }
                        },
                        onClear = { viewModel.clearResult() },
                        onUseAsInput = { text ->
                            viewModel.updateInputText(text)
                            scope.launch {
                                snackbarHostState.showSnackbar("Output loaded as input")
                            }
                        },
                    )
                } ?: error?.let { errorMessage ->
                    ErrorCard(
                        message = errorMessage,
                        onDismiss = { viewModel.clearResult() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    result: CryptoResult,
    onCopy: (String) -> Unit,
    onClear: () -> Unit,
    onUseAsInput: (String) -> Unit,
) {
    val isSuccess = result.success
    val accentColor = if (isSuccess) {
        if (result.operation == CryptoOperation.ENCRYPT) Color(0xFF673AB7) else Color(0xFF009688)
    } else {
        Color(0xFFF44336)
    }

    WormaCeptorContainer(
        style = ContainerStyle.Outlined,
        backgroundColor = accentColor.copy(alpha = 0.1f),
        borderColor = accentColor.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    Icon(
                        if (isSuccess) Icons.Default.Check else Icons.Default.Error,
                        null,
                        tint = accentColor,
                    )
                    Text(
                        "${result.operation.displayName} ${if (isSuccess) "Success" else "Failed"}",
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                    )
                }
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, "Clear result", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            val outputText = result.output
            if (isSuccess && outputText != null) {
                Text(
                    "Output:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(WormaCeptorDesignSystem.CornerRadius.sm))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(WormaCeptorDesignSystem.Spacing.md),
                ) {
                    Text(
                        outputText,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                ) {
                    OutlinedButton(
                        onClick = { onCopy(outputText) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                        Text("Copy")
                    }
                    OutlinedButton(
                        onClick = { onUseAsInput(outputText) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                        Text("Use as Input")
                    }
                }
            } else if (!isSuccess) {
                Text(
                    result.errorMessage ?: "Unknown error",
                    color = accentColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Text(
                "${result.algorithm.displayName}/${result.mode.displayName} | ${result.durationMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onDismiss: () -> Unit) {
    val errorColor = Color(0xFFF44336)
    WormaCeptorContainer(
        style = ContainerStyle.Outlined,
        backgroundColor = errorColor.copy(alpha = 0.1f),
        borderColor = errorColor.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Default.Error, null, tint = errorColor)
                Text(
                    message,
                    color = errorColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Delete, "Dismiss", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HistoryItem(result: CryptoResult, onLoad: () -> Unit, onRemove: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val accentColor = if (result.success) {
        if (result.operation == CryptoOperation.ENCRYPT) Color(0xFF673AB7) else Color(0xFF009688)
    } else {
        Color(0xFFF44336)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onLoad() }
            .padding(vertical = WormaCeptorDesignSystem.Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                Icon(
                    if (result.operation == CryptoOperation.ENCRYPT) Icons.Default.Lock else Icons.Default.LockOpen,
                    null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    "${result.operation.displayName} - ${result.algorithm.displayName}/${result.mode.displayName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            Text(
                result.input.take(50) + if (result.input.length > 50) "..." else "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace,
            )
            Text(
                "${dateFormat.format(
                    Date(result.timestamp),
                )} | ${if (result.success) "Success" else "Failed"} | ${result.durationMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, "Remove", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Crypto Output", text)
    clipboard.setPrimaryClip(clip)
}

/**
 * History screen for viewing past crypto operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CryptoHistoryScreen(
    engine: CryptoEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onLoadResult: (CryptoResult) -> Unit,
) {
    val factory = remember { CryptoFeature.createViewModelFactory(engine) }
    val viewModel: CryptoViewModel = viewModel(factory = factory)
    val history by viewModel.history.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Default.History, null, tint = Color(0xFF673AB7))
                        Text("Crypto History", fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(
                                Icons.Default.Delete,
                                "Clear all",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
                ) {
                    Icon(
                        Icons.Default.History,
                        null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    )
                    Text(
                        "No history yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        "Your encryption and decryption operations will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(WormaCeptorDesignSystem.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
            ) {
                history.forEachIndexed { index, result ->
                    HistoryItem(
                        result = result,
                        onLoad = {
                            onLoadResult(result)
                            scope.launch {
                                snackbarHostState.showSnackbar("Loaded to crypto tool")
                            }
                        },
                        onRemove = { viewModel.removeFromHistory(result.id) },
                    )
                    if (index < history.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}
