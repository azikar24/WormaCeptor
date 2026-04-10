package com.azikar24.wormaceptor.feature.pushsimulator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.pushsimulator.R

@Composable
internal fun ActionButtonsRow(
    onSendClick: () -> Unit,
    onSaveClick: () -> Unit,
    isTitleEmpty: Boolean = false,
) {
    val saveLabel = stringResource(R.string.pushsimulator_save_template)
    val sendLabel = stringResource(R.string.pushsimulator_send)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        WormaCeptorButton(
            text = saveLabel,
            onClick = onSaveClick,
            modifier = Modifier
                .weight(1f)
                .height(WormaCeptorTokens.Spacing.xxxl),
            variant = ButtonVariant.Outlined,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                )
            },
        )

        WormaCeptorButton(
            text = sendLabel,
            onClick = onSendClick,
            modifier = Modifier
                .weight(1f)
                .height(WormaCeptorTokens.Spacing.xxxl),
            variant = ButtonVariant.Primary,
            enabled = !isTitleEmpty,
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                )
            },
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun ActionButtonsRowPreview() {
    WormaCeptorTheme {
        ActionButtonsRow(
            onSendClick = {},
            onSaveClick = {},
            isTitleEmpty = false,
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun ActionButtonsRowDisabledPreview() {
    WormaCeptorTheme {
        ActionButtonsRow(
            onSendClick = {},
            onSaveClick = {},
            isTitleEmpty = true,
        )
    }
}
