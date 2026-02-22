package com.azikar24.wormaceptor.feature.crypto.ui

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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.CryptoOperation
import com.azikar24.wormaceptor.domain.entities.CryptoPreset
import com.azikar24.wormaceptor.domain.entities.CryptoResult
import com.azikar24.wormaceptor.domain.entities.KeyFormat
import com.azikar24.wormaceptor.domain.entities.PaddingScheme
import com.azikar24.wormaceptor.feature.crypto.CryptoFeature
import com.azikar24.wormaceptor.feature.crypto.CryptoViewModel
import com.azikar24.wormaceptor.feature.crypto.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Main composable for the Response Encryption/Decryption feature.
 *
 * @param engine Optional CryptoEngine instance. If provided, allows sharing state with
 *               CryptoHistoryScreen. If null, creates its own engine instance.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CryptoTool(
    engine: CryptoEngine,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    onNavigateToHistory: (() -> Unit)? = null,
) {
    val factory = remember(engine) { CryptoFeature.createViewModelFactory(engine) }
    val viewModel: CryptoViewModel = viewModel(factory = factory)

    val config by viewModel.config.collectAsState()
    val currentResult by viewModel.currentResult.collectAsState()
    val history by viewModel.history.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val copiedMessage = stringResource(R.string.crypto_copied_to_clipboard)
    val outputLoadedMessage = stringResource(R.string.crypto_output_loaded_as_input)
    val clipboardLabel = stringResource(R.string.crypto_clipboard_label)
    val keyGeneratedMessage = stringResource(R.string.crypto_key_generated)
    val ivGeneratedMessage = stringResource(R.string.crypto_iv_generated)

    CryptoToolContent(
        config = config,
        inputText = viewModel.inputText,
        currentResult = currentResult,
        history = history.toImmutableList(),
        isProcessing = isProcessing,
        error = error,
        snackBarHostState = snackBarHostState,
        onNavigateBack = onNavigateBack,
        onNavigateToHistory = onNavigateToHistory,
        onApplyPreset = { viewModel.applyPreset(it) },
        onSetAlgorithm = { viewModel.setAlgorithm(it) },
        onSetMode = { viewModel.setMode(it) },
        onSetPadding = { viewModel.setPadding(it) },
        onSetKeyFormat = { viewModel.setKeyFormat(it) },
        onSetKey = { viewModel.setKey(it) },
        onSetIv = { viewModel.setIv(it) },
        onGenerateKey = {
            viewModel.generateKey()
            scope.launch { snackBarHostState.showSnackbar(keyGeneratedMessage) }
        },
        onGenerateIv = {
            viewModel.generateIv()
            scope.launch { snackBarHostState.showSnackbar(ivGeneratedMessage) }
        },
        onInputTextChange = { viewModel.updateInputText(it) },
        onEncrypt = { viewModel.encrypt() },
        onDecrypt = { viewModel.decrypt() },
        onCopyResult = { text ->
            copyToClipboard(context, clipboardLabel, text)
            scope.launch { snackBarHostState.showSnackbar(copiedMessage) }
        },
        onClearResult = { viewModel.clearResult() },
        onUseAsInput = { text ->
            viewModel.updateInputText(text)
            scope.launch { snackBarHostState.showSnackbar(outputLoadedMessage) }
        },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun CryptoToolContent(
    config: CryptoConfig,
    inputText: String,
    currentResult: CryptoResult?,
    history: ImmutableList<CryptoResult>,
    isProcessing: Boolean,
    error: String?,
    snackBarHostState: SnackbarHostState,
    onNavigateBack: (() -> Unit)?,
    onNavigateToHistory: (() -> Unit)?,
    onApplyPreset: (CryptoPreset) -> Unit,
    onSetAlgorithm: (CryptoAlgorithm) -> Unit,
    onSetMode: (CipherMode) -> Unit,
    onSetPadding: (PaddingScheme) -> Unit,
    onSetKeyFormat: (KeyFormat) -> Unit,
    onSetKey: (String) -> Unit,
    onSetIv: (String) -> Unit,
    onGenerateKey: () -> Unit,
    onGenerateIv: () -> Unit,
    onInputTextChange: (String) -> Unit,
    onEncrypt: () -> Unit,
    onDecrypt: () -> Unit,
    onCopyResult: (String) -> Unit,
    onClearResult: () -> Unit,
    onUseAsInput: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showKeyPassword by remember { mutableStateOf(false) }
    var showIvPassword by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.crypto_title), fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    onNavigateBack?.let {
                        IconButton(onClick = it) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.crypto_back))
                        }
                    }
                },
                actions = {
                    if (history.isNotEmpty() && onNavigateToHistory != null) {
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(
                                Icons.Default.History,
                                stringResource(R.string.crypto_history),
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
                    Text(stringResource(R.string.crypto_presets), fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        CryptoPreset.entries.forEach { preset ->
                            FilterChip(
                                selected = config.algorithm == preset.config.algorithm &&
                                    config.mode == preset.config.mode,
                                onClick = { onApplyPreset(preset) },
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
                    Text(stringResource(R.string.crypto_algorithm), fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        CryptoAlgorithm.entries.filter { it != CryptoAlgorithm.RSA }.forEach { algorithm ->
                            FilterChip(
                                selected = config.algorithm == algorithm,
                                onClick = { onSetAlgorithm(algorithm) },
                                label = { Text(algorithm.displayName) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                    Text(stringResource(R.string.crypto_mode), fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        CipherMode.entries.forEach { mode ->
                            FilterChip(
                                selected = config.mode == mode,
                                onClick = { onSetMode(mode) },
                                label = { Text(mode.displayName) },
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(WormaCeptorDesignSystem.Spacing.sm))
                    Text(stringResource(R.string.crypto_padding), fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        PaddingScheme.entries.forEach { padding ->
                            FilterChip(
                                selected = config.padding == padding,
                                onClick = { onSetPadding(padding) },
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
                    Text(stringResource(R.string.crypto_key_format), fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.sm),
                    ) {
                        KeyFormat.entries.forEach { format ->
                            FilterChip(
                                selected = config.keyFormat == format,
                                onClick = { onSetKeyFormat(format) },
                                label = { Text(format.displayName) },
                            )
                        }
                    }

                    // Key input
                    OutlinedTextField(
                        value = config.key,
                        onValueChange = onSetKey,
                        label = { Text(stringResource(R.string.crypto_key_label, config.algorithm.keyLengthBits / 8)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showKeyPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Row {
                                IconButton(onClick = { showKeyPassword = !showKeyPassword }) {
                                    Icon(
                                        if (showKeyPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showKeyPassword) {
                                            stringResource(
                                                R.string.crypto_hide_key,
                                            )
                                        } else {
                                            stringResource(R.string.crypto_show_key)
                                        },
                                    )
                                }
                                IconButton(onClick = onGenerateKey) {
                                    Icon(Icons.Default.Refresh, stringResource(R.string.crypto_generate_key))
                                }
                            }
                        },
                        leadingIcon = { Icon(Icons.Default.Key, null) },
                        singleLine = true,
                    )

                    // IV input (only show if mode requires it)
                    val ivLabel = stringResource(R.string.crypto_iv_label)
                    val hideIv = stringResource(R.string.crypto_hide_iv)
                    val showIv = stringResource(R.string.crypto_show_iv)
                    val generateIv = stringResource(R.string.crypto_generate_iv)
                    AnimatedVisibility(
                        visible = config.mode.requiresIv,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        OutlinedTextField(
                            value = config.iv,
                            onValueChange = onSetIv,
                            label = { Text(ivLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = if (showIvPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                Row {
                                    IconButton(onClick = { showIvPassword = !showIvPassword }) {
                                        Icon(
                                            if (showIvPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (showIvPassword) hideIv else showIv,
                                        )
                                    }
                                    IconButton(onClick = onGenerateIv) {
                                        Icon(Icons.Default.Refresh, generateIv)
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
                    Text(stringResource(R.string.crypto_input), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputTextChange,
                        label = { Text(stringResource(R.string.crypto_input_hint)) },
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
                            onClick = onEncrypt,
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing && inputText.isNotBlank() && config.key.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WormaCeptorColors.SecureStorage.EncryptedPrefs,
                            ),
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = WormaCeptorDesignSystem.ThemeColors.LightBackground,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                            Text(stringResource(R.string.crypto_encrypt))
                        }

                        Button(
                            onClick = onDecrypt,
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing && inputText.isNotBlank() && config.key.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WormaCeptorColors.SecureStorage.Datastore,
                            ),
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = WormaCeptorDesignSystem.ThemeColors.LightBackground,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.sm))
                            Text(stringResource(R.string.crypto_decrypt))
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
                        onCopy = onCopyResult,
                        onClear = onClearResult,
                        onUseAsInput = onUseAsInput,
                    )
                } ?: error?.let { errorMessage ->
                    ErrorCard(
                        message = errorMessage,
                        onDismiss = onClearResult,
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
    val accentColor = when {
        !isSuccess -> WormaCeptorDesignSystem.ThemeColors.Error
        result.operation == CryptoOperation.ENCRYPT -> WormaCeptorColors.SecureStorage.EncryptedPrefs
        else -> WormaCeptorColors.SecureStorage.Datastore
    }
    val successText = stringResource(R.string.crypto_success)
    val failedText = stringResource(R.string.crypto_failed)
    val unknownErrorText = stringResource(R.string.crypto_unknown_error)

    WormaCeptorContainer(
        style = ContainerStyle.Outlined,
        backgroundColor = accentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        borderColor = accentColor.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
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
                        "${result.operation.displayName} ${if (isSuccess) successText else failedText}",
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor,
                    )
                }
                IconButton(onClick = onClear) {
                    Icon(
                        Icons.Default.Delete,
                        stringResource(R.string.crypto_clear_result),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            val outputText = result.output
            if (isSuccess && outputText != null) {
                Text(
                    stringResource(R.string.crypto_output_label),
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
                        Text(stringResource(R.string.crypto_copy))
                    }
                    OutlinedButton(
                        onClick = { onUseAsInput(outputText) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(WormaCeptorDesignSystem.Spacing.xs))
                        Text(stringResource(R.string.crypto_use_as_input))
                    }
                }
            } else if (!isSuccess) {
                Text(
                    result.errorMessage ?: unknownErrorText,
                    color = accentColor,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Text(
                "${result.algorithm.displayName}/${result.mode.displayName} | ${result.durationMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
            )
        }
    }
}

@Composable
private fun ErrorCard(message: String, onDismiss: () -> Unit) {
    val errorColor = WormaCeptorDesignSystem.ThemeColors.Error
    WormaCeptorContainer(
        style = ContainerStyle.Outlined,
        backgroundColor = errorColor.copy(alpha = WormaCeptorDesignSystem.Alpha.light),
        borderColor = errorColor.copy(alpha = WormaCeptorDesignSystem.Alpha.moderate),
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
                Icon(
                    Icons.Default.Delete,
                    stringResource(R.string.crypto_dismiss),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
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
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.crypto_history_title), fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.crypto_back))
                    }
                },
                actions = {
                    if (history.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(
                                Icons.Default.Delete,
                                stringResource(R.string.crypto_clear_all),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        val loadedMessage = stringResource(R.string.crypto_loaded_to_tool)
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.bold,
                        ),
                    )
                    Text(
                        stringResource(R.string.crypto_no_history),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.crypto_empty_history_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = WormaCeptorDesignSystem.Alpha.heavy,
                        ),
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
                                snackBarHostState.showSnackbar(loadedMessage)
                            }
                        },
                        onRemove = { viewModel.removeFromHistory(result.id) },
                    )
                    if (index < history.lastIndex) {
                        WormaCeptorDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(result: CryptoResult, onLoad: () -> Unit, onRemove: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val accentColor = when {
        !result.success -> WormaCeptorDesignSystem.ThemeColors.Error
        result.operation == CryptoOperation.ENCRYPT -> WormaCeptorColors.SecureStorage.EncryptedPrefs
        else -> WormaCeptorColors.SecureStorage.Datastore
    }
    val successText = stringResource(R.string.crypto_success)
    val failedText = stringResource(R.string.crypto_failed)

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
                )} | ${if (result.success) successText else failedText} | ${result.durationMs}ms",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WormaCeptorDesignSystem.Alpha.heavy),
            )
        }
        IconButton(onClick = onRemove) {
            Icon(
                Icons.Default.Delete,
                stringResource(R.string.crypto_remove),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CryptoToolContentPreview() {
    WormaCeptorTheme {
        CryptoToolContent(
            config = CryptoConfig.default(),
            inputText = "Hello World",
            currentResult = null,
            history = persistentListOf(),
            isProcessing = false,
            error = null,
            snackBarHostState = remember { SnackbarHostState() },
            onNavigateBack = {},
            onNavigateToHistory = null,
            onApplyPreset = {},
            onSetAlgorithm = {},
            onSetMode = {},
            onSetPadding = {},
            onSetKeyFormat = {},
            onSetKey = {},
            onSetIv = {},
            onGenerateKey = {},
            onGenerateIv = {},
            onInputTextChange = {},
            onEncrypt = {},
            onDecrypt = {},
            onCopyResult = {},
            onClearResult = {},
            onUseAsInput = {},
        )
    }
}
