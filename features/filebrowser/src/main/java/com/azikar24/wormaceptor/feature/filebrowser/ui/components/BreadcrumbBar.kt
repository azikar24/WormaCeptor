package com.azikar24.wormaceptor.feature.filebrowser.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorIconButton
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import com.azikar24.wormaceptor.feature.filebrowser.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun BreadcrumbBar(
    isAtRoot: Boolean,
    navigationStack: ImmutableList<String>,
    onRootClick: () -> Unit,
    onBreadcrumbClick: (index: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(navigationStack.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(
                horizontal = WormaCeptorTokens.Spacing.lg,
                vertical = WormaCeptorTokens.Spacing.sm,
            ),
        horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WormaCeptorIconButton(onClick = onRootClick) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = stringResource(R.string.filebrowser_root),
                tint = if (isAtRoot) {
                    WormaCeptorTokens.semantic().accent
                } else {
                    WormaCeptorTokens.semantic().textSecondary
                },
            )
        }

        navigationStack.forEachIndexed { index, path ->
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = WormaCeptorTokens.semantic().textSecondary,
                modifier = Modifier.padding(horizontal = WormaCeptorTokens.Spacing.xxs),
            )

            val fileName = path.substringAfterLast('/')
            val isLast = index == navigationStack.lastIndex

            Text(
                text = fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isLast) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isLast) {
                    WormaCeptorTokens.semantic().accent
                } else {
                    WormaCeptorTokens.semantic().textSecondary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .semantics { role = Role.Button }
                    .clickable { onBreadcrumbClick(index) }
                    .padding(WormaCeptorTokens.Spacing.xs),
            )
        }
    }
}

@SuppressLint("SdCardPath")
@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun BreadcrumbBarPreview() {
    WormaCeptorTheme {
        BreadcrumbBar(
            isAtRoot = false,
            navigationStack = persistentListOf(
                "/data/data/com.example/files",
                "/data/data/com.example/files/config",
                "/data/data/com.example/files/config/settings",
            ),
            onRootClick = {},
            onBreadcrumbClick = {},
        )
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun BreadcrumbBarRootPreview() {
    WormaCeptorTheme {
        BreadcrumbBar(
            isAtRoot = true,
            navigationStack = persistentListOf(),
            onRootClick = {},
            onBreadcrumbClick = {},
        )
    }
}
