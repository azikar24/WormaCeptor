package com.azikar24.wormaceptor.core.ui.components

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/** Standard-sized floating action button styled with the WormaCeptor design system. */
@Composable
fun WormaCeptorFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: ImageVector = Icons.Default.Add,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = WormaCeptorTokens.Shapes.fab,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = WormaCeptorTokens.Elevation.fab,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = iconModifier,
        )
    }
}

/** Small-sized floating action button styled with the WormaCeptor design system. */
@Composable
fun WormaCeptorSmallFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    icon: ImageVector = Icons.Default.Add,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = WormaCeptorTokens.Elevation.fab,
        ),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

/** Extended floating action button with text and optional icon, styled with the WormaCeptor design system. */
@Composable
fun WormaCeptorExtendedFAB(
    onClick: () -> Unit,
    text: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = WormaCeptorTokens.Shapes.fab,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.primary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = WormaCeptorTokens.Elevation.fab,
        ),
    ) {
        icon?.invoke()
        text()
    }
}

// region Previews

@Preview(name = "FAB - Light")
@Composable
private fun FABLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorFAB(
                onClick = {},
                contentDescription = "Add",
            )
        }
    }
}

@Preview(name = "FAB - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FABDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorFAB(
                onClick = {},
                contentDescription = "Add",
            )
        }
    }
}

@Preview(name = "SmallFAB - Light")
@Composable
private fun SmallFABLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorSmallFAB(
                onClick = {},
                icon = Icons.Default.Delete,
                contentDescription = "Delete",
            )
        }
    }
}

@Preview(name = "SmallFAB - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SmallFABDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorSmallFAB(
                onClick = {},
                icon = Icons.Default.Delete,
                contentDescription = "Delete",
            )
        }
    }
}

@Preview(name = "ExtendedFAB - Light")
@Composable
private fun ExtendedFABLightPreview() {
    WormaCeptorTheme {
        Surface {
            WormaCeptorExtendedFAB(
                onClick = {},
                text = { Text("Share Logs") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

@Preview(name = "ExtendedFAB - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ExtendedFABDarkPreview() {
    WormaCeptorTheme(darkTheme = true) {
        Surface {
            WormaCeptorExtendedFAB(
                onClick = {},
                text = { Text("Share Logs") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                    )
                },
            )
        }
    }
}

// endregion
