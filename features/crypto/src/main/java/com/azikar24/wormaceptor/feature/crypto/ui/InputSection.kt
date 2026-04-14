package com.azikar24.wormaceptor.feature.crypto.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorContainer
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorTextField
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.domain.entities.CryptoConfig
import com.azikar24.wormaceptor.feature.crypto.R
import com.azikar24.wormaceptor.feature.crypto.vm.CryptoViewEvent

@Composable
internal fun InputSection(
    inputText: String,
    isProcessing: Boolean,
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    WormaCeptorContainer(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(WormaCeptorTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            Text(
                stringResource(R.string.crypto_input),
                style = WormaCeptorTokens.Typography.sectionHeader,
            )
            WormaCeptorTextField(
                value = inputText,
                onValueChange = { onEvent(CryptoViewEvent.Input.UpdateText(it)) },
                label = { Text(stringResource(R.string.crypto_input_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WormaCeptorTokens.ComponentSize.textAreaHeight),
                maxLines = 5,
            )
            EncryptDecryptButtons(
                inputText = inputText,
                isProcessing = isProcessing,
                config = config,
                onEvent = onEvent,
            )
        }
    }
}

@Composable
private fun EncryptDecryptButtons(
    inputText: String,
    isProcessing: Boolean,
    config: CryptoConfig,
    onEvent: (CryptoViewEvent) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        WormaCeptorButton(
            text = stringResource(R.string.crypto_encrypt),
            onClick = { onEvent(CryptoViewEvent.Operation.Encrypt) },
            enabled = !isProcessing && inputText.isNotBlank() && config.key.isNotBlank(),
            loading = isProcessing,
            containerColor = WormaCeptorTokens.Colors.Crypto.encrypt,
            modifier = Modifier.weight(1f),
        )
        WormaCeptorButton(
            text = stringResource(R.string.crypto_decrypt),
            onClick = { onEvent(CryptoViewEvent.Operation.Decrypt) },
            enabled = !isProcessing && inputText.isNotBlank() && config.key.isNotBlank(),
            loading = isProcessing,
            containerColor = WormaCeptorTokens.Colors.Crypto.decrypt,
            modifier = Modifier.weight(1f),
        )
    }
}
