package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.chip.WormaCeptorChip
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.components.section.WormaCeptorScrollableRow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.domain.entities.KeyFormat
import com.azikar24.wormaceptor.feature.crypto.R
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEvent

private const val BitsPerByte = 8

@Composable
internal fun KeyIvSection(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    var showKeyPassword by remember { mutableStateOf(false) }
    var showIvPassword by remember { mutableStateOf(false) }

    WormaCeptorContainer(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            KeyFormatChips(config, onEvent)
            KeyInput(
                config = config,
                showPassword = showKeyPassword,
                onToggleVisibility = { showKeyPassword = !showKeyPassword },
                onEvent = onEvent,
            )
            IvInput(
                config = config,
                showPassword = showIvPassword,
                onToggleVisibility = { showIvPassword = !showIvPassword },
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun KeyFormatChips(
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    Text(
        stringResource(R.string.crypto_key_format),
        style = WormaCeptorTokens.Typography.sectionHeader,
    )
    WormaCeptorScrollableRow(
        contentPadding = PaddingValues(horizontal = WormaCeptorTokens.Spacing.lg),
    ) {
        KeyFormat.entries.forEach { format ->
            WormaCeptorChip(
                label = format.displayName,
                selected = config.keyFormat == format,
                onClick = { onEvent(CryptoViewEvent.Config.SetKeyFormat(format)) },
            )
        }
    }
}

@Composable
private fun KeyInput(
    config: CryptoConfig,
    showPassword: Boolean,
    onToggleVisibility: () -> Unit,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    WormaCeptorTextField(
        value = config.key,
        onValueChange = { onEvent(CryptoViewEvent.Config.SetKey(it)) },
        label = { Text(stringResource(R.string.crypto_key_label, config.algorithm.keyLengthBits / BitsPerByte)) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (showPassword) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        trailingIcon = {
            Row {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (showPassword) {
                            Icons.Default.VisibilityOff
                        } else {
                            Icons.Default.Visibility
                        },
                        contentDescription = if (showPassword) {
                            stringResource(R.string.crypto_hide_key)
                        } else {
                            stringResource(R.string.crypto_show_key)
                        },
                    )
                }
                IconButton(onClick = { onEvent(CryptoViewEvent.Config.GenerateKey) }) {
                    Icon(Icons.Default.Refresh, stringResource(R.string.crypto_generate_key))
                }
            }
        },
        leadingIcon = { Icon(Icons.Default.Key, null) },
        singleLine = true,
    )
}

@Composable
private fun IvInput(
    config: CryptoConfig,
    showPassword: Boolean,
    onToggleVisibility: () -> Unit,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    val ivLabel = stringResource(R.string.crypto_iv_label)
    val hideIv = stringResource(R.string.crypto_hide_iv)
    val showIv = stringResource(R.string.crypto_show_iv)
    val generateIv = stringResource(R.string.crypto_generate_iv)

    AnimatedVisibility(
        visible = config.mode.requiresIv,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
    ) {
        WormaCeptorTextField(
            value = config.iv,
            onValueChange = { onEvent(CryptoViewEvent.Config.SetIv(it)) },
            label = { Text(ivLabel) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                Row {
                    IconButton(onClick = onToggleVisibility) {
                        Icon(
                            imageVector = if (showPassword) {
                                Icons.Default.VisibilityOff
                            } else {
                                Icons.Default.Visibility
                            },
                            contentDescription = if (showPassword) hideIv else showIv,
                        )
                    }
                    IconButton(onClick = { onEvent(CryptoViewEvent.Config.GenerateIv) }) {
                        Icon(Icons.Default.Refresh, generateIv)
                    }
                }
            },
            singleLine = true,
        )
    }
}
