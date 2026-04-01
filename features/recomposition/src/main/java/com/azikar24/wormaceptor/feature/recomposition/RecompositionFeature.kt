package com.azikar24.wormaceptor.feature.recomposition

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.azikar24.wormaceptor.feature.recomposition.ui.RecompositionSummaryScreen
import com.azikar24.wormaceptor.feature.recomposition.vm.RecompositionViewModel

/**
 * Entry point composable for the Recomposition Inspector feature.
 */
@Composable
fun RecompositionInspector(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
) {
    val viewModel: RecompositionViewModel = viewModel()

    RecompositionSummaryScreen(
        viewModel = viewModel,
        onBack = { onNavigateBack?.invoke() },
        modifier = modifier,
    )
}
