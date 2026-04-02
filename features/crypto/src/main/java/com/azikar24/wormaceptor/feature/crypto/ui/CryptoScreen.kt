package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.core.engine.CryptoEngine
import com.azikar24.wormaceptor.core.ui.components.ContainerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorFlowRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorColors
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorDesignSystem
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.util.copyToClipboard
import com.azikar24.wormaceptor.domain.entities.CipherMode
import com.azikar24.wormaceptor.domain.entities.CryptoAlgorithm
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
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
        contentWindowInsets = WindowInsets(0),
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
                .padding(
                    start = WormaCeptorDesignSystem.Spacing.lg,
                    top = WormaCeptorDesignSystem.Spacing.lg,
                    end = WormaCeptorDesignSystem.Spacing.lg,
                    bottom = WormaCeptorDesignSystem.Spacing.lg +
                        WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding(),
                ),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.lg),
        ) {
            // Presets
            PresetsSection(config = config, onApplyPreset = onApplyPreset)

            // Algorithm & Mode Selection
            AlgorithmModeSection(
                config = config,
                onSetAlgorithm = onSetAlgorithm,
                onSetMode = onSetMode,
                onSetPadding = onSetPadding,
            )

            // Key & IV Input
            KeyIvSection(
                config = config,
                showKeyPassword = showKeyPassword,
                onShowKeyPasswordChanged = { showKeyPassword = it },
                showIvPassword = showIvPassword,
                onShowIvPasswordChanged = { showIvPassword = it },
                onSetKeyFormat = onSetKeyFormat,
                onSetKey = onSetKey,
                onSetIv = onSetIv,
                onGenerateKey = onGenerateKey,
                onGenerateIv = onGenerateIv,
            )

            // Input Text Area
            InputSection(
                inputText = inputText,
                isProcessing = isProcessing,
                config = config,
                onInputTextChange = onInputTextChange,
                onEncrypt = onEncrypt,
                onDecrypt = onDecrypt,
            )

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetsSection(
    config: CryptoConfig,
    onApplyPreset: (CryptoPreset) -> Unit,
) {
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlgorithmModeSection(
    config: CryptoConfig,
    onSetAlgorithm: (CryptoAlgorithm) -> Unit,
    onSetMode: (CipherMode) -> Unit,
    onSetPadding: (PaddingScheme) -> Unit,
) {
    WormaCeptorContainer(
        style = ContainerStyle.Outlined,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(stringResource(R.string.crypto_algorithm), fontWeight = FontWeight.SemiBold)
            WormaCeptorFlowRow(
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
            WormaCeptorFlowRow(
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
            WormaCeptorFlowRow(
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
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeyIvSection(
    config: CryptoConfig,
    showKeyPassword: Boolean,
    onShowKeyPasswordChanged: (Boolean) -> Unit,
    showIvPassword: Boolean,
    onShowIvPasswordChanged: (Boolean) -> Unit,
    onSetKeyFormat: (KeyFormat) -> Unit,
    onSetKey: (String) -> Unit,
    onSetIv: (String) -> Unit,
    onGenerateKey: () -> Unit,
    onGenerateIv: () -> Unit,
) {
    WormaCeptorContainer(
        style = ContainerStyle.Outlined,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorDesignSystem.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorDesignSystem.Spacing.md),
        ) {
            Text(stringResource(R.string.crypto_key_format), fontWeight = FontWeight.SemiBold)
            WormaCeptorFlowRow(
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
                visualTransformation = if (showKeyPassword) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    Row {
                        IconButton(onClick = { onShowKeyPasswordChanged(!showKeyPassword) }) {
                            Icon(
                                imageVector = if (showKeyPassword) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (showKeyPassword) {
                                    stringResource(R.string.crypto_hide_key)
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
                    visualTransformation = if (showIvPassword) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        Row {
                            IconButton(onClick = { onShowIvPasswordChanged(!showIvPassword) }) {
                                Icon(
                                    imageVector = if (showIvPassword) {
                                        Icons.Default.VisibilityOff
                                    } else {
                                        Icons.Default.Visibility
                                    },
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
}

@Composable
private fun InputSection(
    inputText: String,
    isProcessing: Boolean,
    config: CryptoConfig,
    onInputTextChange: (String) -> Unit,
    onEncrypt: () -> Unit,
    onDecrypt: () -> Unit,
) {
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
