package com.azikar24.wormaceptorapp.screens.securestorage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorFAB
import com.azikar24.wormaceptor.core.ui.components.button.WormaCeptorSmallFAB
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

@Composable
internal fun SpeedDialFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddTestData: () -> Unit,
    onAddEntry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.md),
    ) {
        SpeedDialItem(
            visible = expanded,
            label = "Add Test Data",
            icon = Icons.Default.Science,
            onClick = onAddTestData,
        )
        SpeedDialItem(
            visible = expanded,
            label = "Add Entry",
            icon = Icons.Default.VpnKey,
            onClick = onAddEntry,
        )
        WormaCeptorFAB(
            onClick = { onExpandedChange(!expanded) },
            icon = if (expanded) Icons.Default.Close else Icons.Default.Add,
            contentDescription = if (expanded) "Close" else "Add",
            iconModifier = Modifier.rotate(rotation),
        )
    }
}

@Composable
private fun SpeedDialItem(
    visible: Boolean,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn() + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            Surface(
                shape = WormaCeptorTokens.Shapes.button,
                color = WormaCeptorTokens.semantic().surfaceVariant,
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(
                        horizontal = WormaCeptorTokens.Spacing.sm,
                        vertical = WormaCeptorTokens.Spacing.xs,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            WormaCeptorSmallFAB(
                onClick = onClick,
                icon = icon,
                contentDescription = label,
            )
        }
    }
}
