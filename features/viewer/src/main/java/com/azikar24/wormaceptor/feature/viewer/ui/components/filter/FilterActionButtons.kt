package com.azikar24.wormaceptor.feature.viewer.ui.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.ButtonVariant
import com.azikar24.wormaceptor.core.ui.components.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorButton
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun FilterActionButtons(
    filtersActive: Boolean,
    onClearAll: () -> Unit,
    onApply: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WormaCeptorDivider(style = DividerStyle.Subtle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
        ) {
            WormaCeptorButton(
                text = stringResource(R.string.viewer_filter_clear_all),
                onClick = onClearAll,
                variant = ButtonVariant.Outlined,
                enabled = filtersActive,
                modifier = Modifier.weight(1f),
            )

            WormaCeptorButton(
                text = stringResource(R.string.viewer_filter_done),
                onClick = onApply,
                variant = ButtonVariant.Primary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
