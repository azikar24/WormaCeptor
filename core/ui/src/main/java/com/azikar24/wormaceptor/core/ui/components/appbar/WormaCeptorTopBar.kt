package com.azikar24.wormaceptor.core.ui.components.appbar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/** Standard top app bar with optional subtitle, leading/trailing title slots, and back button. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WormaCeptorTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    titleLeading: (@Composable () -> Unit)? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Unspecified,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TopBarTitle(
                title = title,
                subtitle = subtitle,
                leading = titleLeading,
                trailing = titleTrailing,
            )
        },
        navigationIcon = {
            TopBarNavigationIcon(
                onBack = onBack,
                backContentDescription = backContentDescription,
                icon = navigationIcon,
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = resolveContainer(containerColor)),
    )
}

/** Slot-based top app bar that lets the caller render the title with full [RowScope] control. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WormaCeptorTopBar(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    backContentDescription: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = Unspecified,
    title: @Composable RowScope.() -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                content = title,
            )
        },
        navigationIcon = {
            TopBarNavigationIcon(
                onBack = onBack,
                backContentDescription = backContentDescription,
                icon = null,
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = resolveContainer(containerColor)),
    )
}

@Composable
private fun resolveContainer(color: Color): Color =
    if (color == Unspecified) WormaCeptorTokens.semantic().surface else color

@Composable
private fun TopBarTitle(
    title: String,
    subtitle: String?,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
) {
    val titleRow: @Composable () -> Unit = {
        if (leading == null && trailing == null) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                leading?.invoke()
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                trailing?.invoke()
            }
        }
    }
    if (subtitle == null) {
        titleRow()
    } else {
        Column {
            titleRow()
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WormaCeptorTokens.semantic().textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TopBarNavigationIcon(
    onBack: (() -> Unit)?,
    backContentDescription: String?,
    icon: (@Composable () -> Unit)?,
) {
    when {
        icon != null -> icon()
        onBack != null -> IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = backContentDescription,
            )
        }
    }
}
