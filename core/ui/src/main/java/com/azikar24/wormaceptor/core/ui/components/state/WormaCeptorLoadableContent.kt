package com.azikar24.wormaceptor.core.ui.components.state

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

private const val LoadableCrossfadeMillis = 180
private const val DefaultListSkeletonRowCount = 6
private val DefaultListSkeletonRowHeight = 72.dp

private enum class LoadablePhase { Loading, Empty, Content }

/** Three-phase content container: skeleton → empty → content with a crossfade. */
@Composable
fun WormaCeptorLoadableContent(
    isLoading: Boolean,
    isEmpty: Boolean,
    loading: @Composable () -> Unit,
    empty: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val phase = when {
        isLoading -> LoadablePhase.Loading
        isEmpty -> LoadablePhase.Empty
        else -> LoadablePhase.Content
    }
    AnimatedContent(
        targetState = phase,
        modifier = modifier,
        transitionSpec = {
            fadeIn(tween(LoadableCrossfadeMillis))
                .togetherWith(fadeOut(tween(LoadableCrossfadeMillis)))
        },
        label = "loadable_content",
    ) { current ->
        when (current) {
            LoadablePhase.Loading -> loading()
            LoadablePhase.Empty -> empty()
            LoadablePhase.Content -> content()
        }
    }
}

/** List-shaped skeleton placeholder — a padded column of shimmer rows. */
@Composable
fun WormaCeptorListSkeleton(
    modifier: Modifier = Modifier,
    rowCount: Int = DefaultListSkeletonRowCount,
    rowHeight: Dp = DefaultListSkeletonRowHeight,
    contentPadding: PaddingValues = PaddingValues(
        start = WormaCeptorTokens.Spacing.md,
        end = WormaCeptorTokens.Spacing.md,
        top = WormaCeptorTokens.Spacing.sm,
    ),
    verticalSpacing: Dp = WormaCeptorTokens.Spacing.sm,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
        repeat(rowCount) {
            WormaCeptorSkeleton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight),
            )
        }
    }
}
