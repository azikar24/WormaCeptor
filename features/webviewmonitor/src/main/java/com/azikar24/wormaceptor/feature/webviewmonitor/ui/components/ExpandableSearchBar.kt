package com.azikar24.wormaceptor.feature.webviewmonitor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.components.input.WormaCeptorSearchBar
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.webviewmonitor.R

@Composable
internal fun ExpandableSearchBar(
    visible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(
            animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
        ) + fadeIn(animationSpec = tween(WormaCeptorTokens.Animation.NORMAL)),
        exit = shrinkVertically(
            animationSpec = tween(WormaCeptorTokens.Animation.NORMAL),
        ) + fadeOut(animationSpec = tween(WormaCeptorTokens.Animation.NORMAL)),
    ) {
        WormaCeptorSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = WormaCeptorTokens.Spacing.lg,
                    vertical = WormaCeptorTokens.Spacing.sm,
                ),
            placeholder = stringResource(R.string.webviewmonitor_search_placeholder),
        )
    }
}
