package com.azikar24.wormaceptor.core.ui.components.tab

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTheme
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

/** Fixed tab row with muted unselected tabs and a hairline divider, matching [WormaCeptorTopBar] colors. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WormaCeptorTabRow(
    selectedTabIndex: Int,
    titles: List<String>,
    onTabSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {
            HorizontalDivider(
                thickness = WormaCeptorTokens.BorderWidth.thin,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = WormaCeptorTokens.Alpha.BOLD),
            )
        },
    ) {
        titles.forEachIndexed { index, title ->
            val selected = selectedTabIndex == index
            Tab(
                selected = selected,
                onClick = { onTabSelect(index) },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = WormaCeptorTokens.semantic().textSecondary,
                text = {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Preview(showBackground = true)
@Composable
private fun WormaCeptorTabRowPreview() {
    WormaCeptorTheme {
        WormaCeptorTabRow(
            selectedTabIndex = 0,
            titles = listOf("Transactions", "Crashes", "Tools"),
            onTabSelect = {},
        )
    }
}
