package com.azikar24.wormaceptorapp.screens.securestorage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azikar24.wormaceptor.core.ui.components.WormaCeptorCard
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens

@Composable
internal fun EncryptedPrefsSection(
    entries: List<EncryptedPrefEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        SecureStorageEmptyState(
            icon = Icons.Default.Lock,
            title = "No encrypted preferences",
            subtitle = "Tap 'Add Test Data' to create some entries",
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(WormaCeptorTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
        ) {
            items(entries, key = { it.key }) { entry ->
                EncryptedPrefItem(
                    entry = entry,
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }
}

@Composable
private fun EncryptedPrefItem(
    entry: EncryptedPrefEntry,
    modifier: Modifier = Modifier,
) {
    var isValueVisible by remember { mutableStateOf(false) }

    WormaCeptorCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WormaCeptorTokens.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
                ) {
                    Text(
                        text = entry.key,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = getTypeColor(entry.type).copy(alpha = 0.15f),
                    ) {
                        Text(
                            text = entry.type,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = getTypeColor(entry.type),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(WormaCeptorTokens.Spacing.xs))
                Text(
                    text = if (isValueVisible) entry.value else maskValue(entry.value),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(onClick = { isValueVisible = !isValueVisible }) {
                Icon(
                    imageVector = if (isValueVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isValueVisible) "Hide value" else "Show value",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
