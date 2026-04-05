package com.azikar24.wormaceptor.feature.viewer.ui.components.filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.azikar24.wormaceptor.core.ui.theme.WormaCeptorTokens
import kotlinx.collections.immutable.ImmutableMap

@Composable
internal fun MethodFilterBars(
    methodCounts: ImmutableMap<String, Int>,
    selectedMethods: Set<String>,
    onMethodToggled: (String) -> Unit,
) {
    val methods = listOf("GET", "POST", "PUT", "DELETE", "PATCH")

    Column(
        verticalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
    ) {
        methods.chunked(2).forEach { rowMethods ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WormaCeptorTokens.Spacing.sm),
            ) {
                rowMethods.forEach { method ->
                    val count = methodCounts[method] ?: 0
                    val color = methodColor(method)
                    val isSelected = method in selectedMethods

                    GridFilterCard(
                        label = method,
                        count = count,
                        color = color,
                        isSelected = isSelected,
                        onClick = { onMethodToggled(method) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowMethods.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun methodColor(method: String) = when (method.uppercase()) {
    "GET" -> WormaCeptorTokens.Colors.Status.green
    "POST" -> WormaCeptorTokens.Colors.Status.blue
    "PUT" -> WormaCeptorTokens.Colors.Status.amber
    "DELETE" -> WormaCeptorTokens.Colors.Status.red
    "PATCH" -> WormaCeptorTokens.Colors.HttpMethod.patch
    else -> WormaCeptorTokens.Colors.Status.grey
}
