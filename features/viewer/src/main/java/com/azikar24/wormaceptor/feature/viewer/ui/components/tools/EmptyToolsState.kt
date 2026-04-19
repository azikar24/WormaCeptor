package com.azikar24.wormaceptor.feature.viewer.ui.components.tools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R

@Composable
internal fun EmptyToolsState(
    searchQuery: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(R.string.viewer_tools_search_placeholder),
            modifier = Modifier
                .size(WormaCeptorTokens.IconSize.xxxl)
                .alpha(WormaCeptorTokens.Alpha.STRONG),
            tint = WormaCeptorTokens.semantic().textSecondary,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.md))

        Text(
            text = if (searchQuery.isNotEmpty()) {
                stringResource(R.string.viewer_tools_no_tools_found, searchQuery)
            } else {
                stringResource(R.string.viewer_tools_no_tools_available)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = WormaCeptorTokens.semantic().textSecondary,
            textAlign = TextAlign.Center,
        )

        if (searchQuery.isNotEmpty()) {
            Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
            Text(
                text = stringResource(R.string.viewer_tools_try_different_search),
                style = MaterialTheme.typography.bodySmall,
                color = WormaCeptorTokens.semantic().textSecondary.copy(alpha = WormaCeptorTokens.Alpha.HEAVY),
                textAlign = TextAlign.Center,
            )
        }
    }
}
