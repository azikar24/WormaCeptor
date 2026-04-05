package com.azikar24.wormaceptor.feature.fps.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.fps.R

@Composable
internal fun DroppedFramesCard(
    droppedFrames: Int,
    jankFrames: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(WormaCeptorTokens.Radius.lg),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.lg),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Dropped frames
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.fps_dropped_frames),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))

                Text(
                    text = droppedFrames.toString(),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                    ),
                    color = if (droppedFrames > 0) WormaCeptorTokens.Colors.Fps.warning else MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = stringResource(R.string.fps_threshold_dropped),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    ),
                )
            }

            // Divider
            Box(
                modifier = Modifier
                    .width(WormaCeptorTokens.BorderWidth.regular)
                    .height(WormaCeptorTokens.Spacing.xxxl)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = WormaCeptorTokens.Alpha.MODERATE)),
            )

            // Jank frames
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
                ) {
                    Text(
                        text = stringResource(R.string.fps_jank_frames),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    AnimatedVisibility(
                        visible = jankFrames > 0,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = stringResource(R.string.fps_jank_detected),
                            modifier = Modifier.size(WormaCeptorTokens.IconSize.sm),
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
                    color = if (jankFrames > 0) WormaCeptorTokens.Colors.Fps.critical else MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = stringResource(R.string.fps_threshold_jank),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = WormaCeptorTokens.Alpha.HEAVY,
                    ),
                )
            }
        }
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
