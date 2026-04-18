package com.azikar24.wormaceptor.feature.fps.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.card.CardStyle
import com.azikar24.wormaceptor.core.ui.components.card.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.components.divider.DividerOrientation
import com.azikar24.wormaceptor.core.ui.components.divider.DividerStyle
import com.azikar24.wormaceptor.core.ui.components.divider.WormaCeptorDivider
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.fps.R

@Composable
internal fun DroppedFramesCard(
    droppedFrames: Int,
    jankFrames: Int,
    modifier: Modifier = Modifier,
) {
    WormaCeptorCard(
        modifier = modifier,
        style = CardStyle.Outlined,
        shape = WormaCeptorTokens.Shapes.cardLarge,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DroppedFramesColumn(droppedFrames = droppedFrames)

            WormaCeptorDivider(
                modifier = Modifier.height(WormaCeptorTokens.Spacing.xxxl),
                style = DividerStyle.Subtle,
                orientation = DividerOrientation.Vertical,
            )

            JankFramesColumn(jankFrames = jankFrames)
        }
    }
}

@Composable
private fun DroppedFramesColumn(droppedFrames: Int) {
    val hasDroppedFrames = droppedFrames > 0
    val droppedColor = if (hasDroppedFrames) {
        WormaCeptorTokens.Colors.Fps.warning
    } else {
        WormaCeptorTokens.semantic().textPrimary
    }
    val label = stringResource(R.string.fps_dropped_frames)

    Column(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "$droppedFrames $label"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = WormaCeptorTokens.semantic().textSecondary,
        )

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

        Text(
            text = droppedFrames.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            color = droppedColor,
        )

        Text(
            text = stringResource(R.string.fps_threshold_dropped),
            style = MaterialTheme.typography.labelSmall,
            color = WormaCeptorTokens.semantic().textSecondary.copy(
                alpha = WormaCeptorTokens.Alpha.HEAVY,
            ),
        )
    }
}

@Composable
private fun JankFramesColumn(jankFrames: Int) {
    val hasJankFrames = jankFrames > 0
    val jankColor = if (hasJankFrames) {
        WormaCeptorTokens.Colors.Fps.critical
    } else {
        WormaCeptorTokens.semantic().textPrimary
    }
    val label = stringResource(R.string.fps_jank_frames)

    Column(
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "$jankFrames $label"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                WormaCeptorTokens.Spacing.xs,
            ),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = WormaCeptorTokens.semantic().textSecondary,
            )

            AnimatedVisibility(
                visible = hasJankFrames,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(
                        WormaCeptorTokens.IconSize.sm,
                    ),
                    tint = WormaCeptorTokens.Colors.Fps.jankIndicator(),
                )
            }
        }

        Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

        Text(
            text = jankFrames.toString(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            color = jankColor,
        )

        Text(
            text = stringResource(R.string.fps_threshold_jank),
            style = MaterialTheme.typography.labelSmall,
            color = WormaCeptorTokens.semantic().textSecondary.copy(
                alpha = WormaCeptorTokens.Alpha.HEAVY,
            ),
        )
    }
}

@Preview(name = "DroppedFramesCard - Light")
@Composable
private fun DroppedFramesCardPreview() {
    WormaCeptorTheme {
        DroppedFramesCard(
            droppedFrames = 7,
            jankFrames = 3,
        )
    }
}

@Preview(name = "DroppedFramesCard - Mixed")
@Composable
private fun DroppedFramesCardMixedPreview() {
    WormaCeptorTheme {
        DroppedFramesCard(
            droppedFrames = 5,
            jankFrames = 0,
        )
    }
}

@Preview(name = "DroppedFramesCard - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DroppedFramesCardDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        DroppedFramesCard(
            droppedFrames = 0,
            jankFrames = 0,
        )
    }
}
