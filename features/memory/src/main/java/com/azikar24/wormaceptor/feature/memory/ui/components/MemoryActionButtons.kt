package com.azikar24.wormaceptor.feature.memory.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.memory.R

@Composable
internal fun MemoryActionButtons(
    onForceGc: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        WormaCeptorButton(
            text = stringResource(R.string.memory_force_gc),
            onClick = onForceGc,
            variant = ButtonVariant.Secondary,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = null,
                    modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
                )
            },
        )
    }
}

@Preview(name = "MemoryActionButtons - Light")
@Composable
private fun MemoryActionButtonsPreview() {
    WormaCeptorTheme {
        MemoryActionButtons(onForceGc = {})
    }
}
