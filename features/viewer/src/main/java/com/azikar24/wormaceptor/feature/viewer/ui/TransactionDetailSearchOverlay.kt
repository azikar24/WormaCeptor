package com.azikar24.wormaceptor.feature.viewer.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.viewer.R
import com.azikar24.wormaceptor.feature.viewer.vm.TransactionDetailViewEvent

/**
 * Floating search match navigation overlay shown at the bottom-end of the screen.
 */
@Composable
internal fun TransactionDetailSearchOverlay(
    visible: Boolean,
    matchCount: Int,
    currentMatchIndex: Int,
    onEvent: (TransactionDetailViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(WormaCeptorTokens.Animation.FAST),
        ) + scaleIn(animationSpec = tween(WormaCeptorTokens.Animation.FAST)),
        exit = fadeOut(
            animationSpec = tween(WormaCeptorTokens.Animation.ULTRA_FAST),
        ) + scaleOut(animationSpec = tween(WormaCeptorTokens.Animation.ULTRA_FAST)),
        modifier = modifier,
    ) {
        Surface(
            shape = WormaCeptorTokens.Shapes.pill,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(WormaCeptorTokens.Elevation.md),
            shadowElevation = WormaCeptorTokens.Elevation.lg,
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = WormaCeptorTokens.Spacing.md,
                    vertical = WormaCeptorTokens.Spacing.sm,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (matchCount > 0) {
                    Text(
                        text = "${currentMatchIndex + 1}/$matchCount",
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(end = WormaCeptorTokens.Spacing.xs),
                    )
                    IconButton(
                        onClick = { onEvent(TransactionDetailViewEvent.Search.NavigateToPrevious) },
                        modifier = Modifier.size(WormaCeptorTokens.ComponentSize.iconButtonSmall),
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            stringResource(R.string.viewer_search_previous_match),
                        )
                    }
                    IconButton(
                        onClick = { onEvent(TransactionDetailViewEvent.Search.NavigateToNext) },
                        modifier = Modifier.size(WormaCeptorTokens.ComponentSize.iconButtonSmall),
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            stringResource(R.string.viewer_search_next_match),
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.viewer_search_no_matches),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
